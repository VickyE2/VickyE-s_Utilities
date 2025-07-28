/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform;

import org.bukkit.Bukkit;
import org.vicky.platform.PlatformScheduler;
import org.vicky.vicky_utils;

public class BukkitPlatformScheduler implements PlatformScheduler {
	@Override
	public void runMain(Runnable task) {
		Bukkit.getScheduler().runTask(vicky_utils.plugin, task);
	}

	@Override
	public void runScheduled(Runnable task, Long tickOffset) {
		Bukkit.getScheduler().runTaskLater(vicky_utils.plugin, task, tickOffset);
	}
}
