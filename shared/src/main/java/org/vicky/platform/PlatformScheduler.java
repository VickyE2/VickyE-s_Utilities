/* Licensed under Apache-2.0 2026. */
package org.vicky.platform;

public interface PlatformScheduler {
	void runMain(Runnable task);
	void runAsync(Runnable task);
	void runScheduled(Runnable task, Long tickOffset);
}
