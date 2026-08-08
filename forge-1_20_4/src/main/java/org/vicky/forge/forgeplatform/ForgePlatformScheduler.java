/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vicky.platform.server.PlatformScheduler;
import org.vicky.platform.server.PlatformTask;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.vicky.forge.VickyUtilitiesForge.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgePlatformScheduler implements PlatformScheduler {

	private final Queue<ScheduledTask> taskQueue = new ConcurrentLinkedQueue<>();

	private final ExecutorService asyncExecutor =
			Executors.newCachedThreadPool(r -> {
				Thread thread = new Thread(r, "VickyUtilities-Async");
				thread.setDaemon(true);
				return thread;
			});

	private static ForgePlatformScheduler INSTANCE;

	private ForgePlatformScheduler() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static ForgePlatformScheduler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ForgePlatformScheduler();
		}
		return INSTANCE;
	}

	@Override
	public void runMain(Runnable task) {
		taskQueue.add(new ScheduledTask(task, 0, -1));
	}

	@Override
	public void runAsync(Runnable task) {
		asyncExecutor.execute(() -> {
			try {
				task.run();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		});
	}

	@Override
	public PlatformTask runScheduled(Runnable task, long delayTicks) {
		var scheduled = new ScheduledTask(task, delayTicks, -1);
		taskQueue.add(scheduled);
		return scheduled;
	}

	@Override
	public PlatformTask runRepeating(Runnable task, long delayTicks, long intervalTicks) {
		var scheduled = new ScheduledTask(task, delayTicks, intervalTicks);
		taskQueue.add(scheduled);
		return scheduled;
	}

	public void runScheduled(Runnable task, Long tickOffset) {
		taskQueue.add(new ScheduledTask(task, tickOffset != null ? tickOffset : 0, -1));
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;

		Iterator<ScheduledTask> iterator = taskQueue.iterator();
		while (iterator.hasNext()) {
			ScheduledTask task = iterator.next();

			// Clean up early if already cancelled before hitting target tick
			if (task.isCancelled()) {
				iterator.remove();
				continue;
			}

			task.tick--;
			if (task.tick <= 0) {
				task.run();

				// Check if cancelled inside execution or if it's a one-shot task
				if (task.isCancelled()) {
					iterator.remove();
				} else if (task.isRepeating()) {
					// Reset tick counter for the next interval cycle
					task.tick = task.intervalTicks;
				} else {
					iterator.remove();
				}
			}
		}
	}

	private static class ScheduledTask implements PlatformTask {
		private final Runnable task;
		private long tick;
		private final long intervalTicks;
		private volatile boolean cancelled = false;

		ScheduledTask(Runnable task, long delayTicks, long intervalTicks) {
			this.task = task;
			this.tick = delayTicks;
			this.intervalTicks = intervalTicks;
		}

		boolean isRepeating() {
			return intervalTicks > 0;
		}

		void run() {
			if (cancelled) return;
			try {
				task.run();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		@Override
		public void cancel() {
			this.cancelled = true;
		}

		@Override
		public boolean isCancelled() {
			return this.cancelled;
		}
	}
}
