package org.vicky.utilities.ContextLogger;

import org.vicky.platform.PlatformLogger;
import org.vicky.platform.defaults.DefaultPlatformLogger;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AsyncContextLogger - non-blocking context logger that offloads actual logging to a single background thread.
 * <p>
 * Usage:
 * AsyncContextLogger al = new AsyncContextLogger(ContextType.FEATURE, "MyFeature", new DefaultPlatformLogger("MyFeature"));
 * al.print("Hello {}", "world"); // returns immediately
 * al.shutdownAndFlush(2, TimeUnit.SECONDS); // at shutdown
 */
public class AsyncContextLogger extends ContextLogger {

    private final BlockingQueue<Runnable> queue;
    private final ExecutorService executor;
    private final AtomicLong dropped = new AtomicLong(0);
    private final int capacity;

    /**
     * Create with default capacity (10_000) and a daemon worker thread.
     */
    public AsyncContextLogger(ContextType context, String contextName, PlatformLogger logger) {
        this(context, contextName, logger, 10_000);
    }

    /**
     * Create with configurable queue capacity.
     *
     * @param capacity max queued entries before dropping
     */
    public AsyncContextLogger(ContextType context, String contextName, PlatformLogger logger, int capacity) {
        super(context, contextName, logger);

        this.capacity = Math.max(128, capacity);
        this.queue = new ArrayBlockingQueue<>(this.capacity);

        // single-threaded daemon executor with a sensible name
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "AsyncContextLogger-" + contextName);
            t.setDaemon(true);
            return t;
        });

        // worker: loop pulling runnables and executing them
        this.executor.submit(this::workerLoop);
    }

    /**
     * Create with all defaults.
     */
    public AsyncContextLogger(ContextType context, String contextName) {
        this(context, contextName, new DefaultPlatformLogger(contextName));
    }

    // Utility to copy varargs into a safe immutable array for background use
    private static Object[] copyArgs(Object[] args) {
        if (args == null || args.length == 0) return new Object[0];
        Object[] copy = new Object[args.length];
        System.arraycopy(args, 0, copy, 0, args.length);
        return copy;
    }

    // Provide a factory that uses default platform logger if needed
    public static AsyncContextLogger createDefault(ContextType context, String contextName) {
        return new AsyncContextLogger(context, contextName, new org.vicky.platform.defaults.DefaultPlatformLogger(contextName));
    }

    // Worker loop: blocking poll but exits on executor shutdown
    private void workerLoop() {
        try {
            while (!Thread.currentThread().isInterrupted() && !executor.isShutdown()) {
                try {
                    Runnable task = queue.poll(250, TimeUnit.MILLISECONDS);
                    if (task != null) {
                        try {
                            task.run(); // task typically calls AsyncContextLogger.super.print(...)
                        } catch (Throwable t) {
                            // swallow exceptions from tasks but log minimal notice directly
                            // Avoid calling our own print (would enqueue), so use platform logger directly:
                            try {
                                logger.error("AsyncContextLogger task exception: " + t.getMessage());
                            } catch (Throwable ignored) {
                                // last resort: nothing to do
                            }
                        }
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }

            // Drain remaining tasks on shutdown
            Runnable r;
            while ((r = queue.poll()) != null) {
                try {
                    r.run();
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable fatal) {
            try {
                logger.error("AsyncContextLogger worker fatal error: " + fatal.getMessage());
            } catch (Throwable ignore) {
            }
        }
    }

    /**
     * Enqueue a runnable non-blocking. If the queue is full the entry is dropped and a counter increments.
     * The runnable should call the superclass printing method (i.e. AsyncContextLogger.super.print(...))
     * so we avoid re-enqueue recursion.
     */
    private void enqueueNonBlocking(Runnable r) {
        Objects.requireNonNull(r);
        boolean offered = queue.offer(r); // non-blocking
        if (!offered) {
            dropped.incrementAndGet();
            // We intentionally do not block or call super synchronously to keep non-blocking guarantee.
        }
    }

    // Expose some management methods
    public long getDroppedCount() {
        return dropped.get();
    }

    public int getQueueCapacity() {
        return capacity;
    }

    public int getQueueSize() {
        return queue.size();
    }

    // ------------------------
    // Override public print APIs to capture calls and enqueue runnables
    // ------------------------

    /**
     * Ask the logger to finish queued items and stop within the timeout.
     * Non-blocking for the caller until this method is invoked.
     *
     * @param timeout how long to wait for shutdown
     * @param unit    time unit
     * @return true if shutdown completed within timeout, false otherwise
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean shutdownAndFlush(long timeout, TimeUnit unit) throws InterruptedException {
        // stop accepting new tasks by shutting down executor after worker drains queue
        // We first stop submitting new tasks by shutting down executor after we submit a "poison" task.
        // But because tasks simply call super.print, we can just shutdown executor after waiting for queue drain.
        executor.shutdown(); // prevents new runnables from being executed (but queue still contains tasks)
        try {
            return executor.awaitTermination(timeout, unit);
        } finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        }
    }

    /**
     * Force immediate shutdown (best-effort).
     */
    public void forceShutdownNow() {
        executor.shutdownNow();
    }

    @Override
    public void print(String message) {
        // capture immutable snapshot minimal work on caller thread
        final String m = message;
        enqueueNonBlocking(() -> AsyncContextLogger.super.print(m));
    }

    @Override
    public void debug(String message) {
        // capture immutable snapshot minimal work on caller thread
        final String m = message;
        enqueueNonBlocking(() -> AsyncContextLogger.super.debug(m));
    }

    @Override
    public void print(String message, boolean isError) {
        final String m = message;
        final boolean e = isError;
        enqueueNonBlocking(() -> AsyncContextLogger.super.print(m, e));
    }

    @Override
    public void print(String message, boolean isError, Object... args) {
        final String m = message;
        final boolean e = isError;
        final Object[] a = copyArgs(args);
        enqueueNonBlocking(() -> AsyncContextLogger.super.print(m, e, a));
    }

    @Override
    public void print(String message, Object... args) {
        final String m = message;
        final Object[] a = copyArgs(args);
        enqueueNonBlocking(() -> AsyncContextLogger.super.print(m, a));
    }

    @Override
    public void debug(String message, Object... args) {
        final String m = message;
        final Object[] a = copyArgs(args);
        enqueueNonBlocking(() -> AsyncContextLogger.super.debug(m, a));
    }

    @Override
    public void print(String message, LogType type) {
        final String m = message;
        final LogType t = type;
        enqueueNonBlocking(() -> AsyncContextLogger.super.print(m, t));
    }

    @Override
    public void print(String message, LogType type, Object... args) {
        final String m = message;
        final LogType t = type;
        final Object[] a = copyArgs(args);
        enqueueNonBlocking(() -> AsyncContextLogger.super.print(m, t, a));
    }

    @Override
    public void print(String message, LogType type, LogPostType effect) {
        final String m = message;
        final LogType t = type;
        final LogPostType e = effect;
        enqueueNonBlocking(() -> AsyncContextLogger.super.print(m, t, e));
    }

    @Override
    public void print(String message, LogType type, LogPostType effect, Object... args) {
        final String m = message;
        final LogType t = type;
        final LogPostType e = effect;
        final Object[] a = copyArgs(args);
        enqueueNonBlocking(() -> AsyncContextLogger.super.print(m, t, e, a));
    }

    @Override
    public void print(String message, LogType type, boolean shouldAffectMessage) {
        final String m = message;
        final LogType t = type;
        final boolean s = shouldAffectMessage;
        enqueueNonBlocking(() -> AsyncContextLogger.super.print(m, t, s));
    }

    @Override
    public void debug(String message, boolean shouldAffectMessage) {
        final String m = message;
        final boolean s = shouldAffectMessage;
        enqueueNonBlocking(() -> AsyncContextLogger.super.print(m, s));
    }

    @Override
    public void print(String message, LogType type, boolean shouldAffectMessage, Object... args) {
        final String m = message;
        final LogType t = type;
        final boolean s = shouldAffectMessage;
        final Object[] a = copyArgs(args);
        enqueueNonBlocking(() -> AsyncContextLogger.super.print(m, t, s, a));
    }

    @Override
    public void debug(String message, boolean shouldAffectMessage, Object... args) {
        final String m = message;
        final boolean s = shouldAffectMessage;
        final Object[] a = copyArgs(args);
        enqueueNonBlocking(() -> AsyncContextLogger.super.debug(m, s, a));
    }

    @Override
    public void print(String message, LogType type, LogPostType effect, boolean shouldAffectMessage) {
        final String m = message;
        final LogType t = type;
        final LogPostType e = effect;
        final boolean s = shouldAffectMessage;
        enqueueNonBlocking(() -> AsyncContextLogger.super.print(m, t, e, s));
    }

    @Override
    public void print(String message, LogType type, LogPostType effect, boolean shouldAffectMessage, Object... args) {
        final String m = message;
        final LogType t = type;
        final LogPostType e = effect;
        final boolean s = shouldAffectMessage;
        final Object[] a = copyArgs(args);
        enqueueNonBlocking(() -> AsyncContextLogger.super.print(m, t, e, s, a));
    }
}
