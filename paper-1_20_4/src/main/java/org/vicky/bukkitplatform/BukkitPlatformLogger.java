/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform;

import org.bukkit.Bukkit;
import org.vicky.platform.PlatformLogger;

public class BukkitPlatformLogger implements PlatformLogger {
	@Override
	public void info(String message) {
		Bukkit.getLogger().info(message);
	}

	@Override
	public void warn(String msg) {
		Bukkit.getLogger().warning(msg);
	}

	@Override
	public void error(String msg) {
		Bukkit.getLogger().severe(msg);
	}

	@Override
	public void debug(String msg) {
		Bukkit.getLogger().info("[DEBUG]" + msg);
	}

	@Override
	public void error(String msg, Throwable throwable) {
		Bukkit.getLogger().severe(msg);
		throwable.printStackTrace();
	}
}
