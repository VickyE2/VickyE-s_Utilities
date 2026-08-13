/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.server;

public interface PlatformScheduler {

	// --- Basic Execution ---
	void runMain(Runnable task);

	void runAsync(Runnable task);

	// --- Delayed & Repeating Execution ---

	/** Runs a task after a specific tick delay. */
	PlatformTask runScheduled(Runnable task, long delayTicks);

	/**
	 * Runs a task repeatedly.
	 * 
	 * @param task
	 *            The logic to execute.
	 * @param delayTicks
	 *            Ticks to wait before the first execution.
	 * @param intervalTicks
	 *            Ticks to wait between subsequent executions (1 = every tick).
	 */
	PlatformTask runRepeating(Runnable task, long delayTicks, long intervalTicks);
}
