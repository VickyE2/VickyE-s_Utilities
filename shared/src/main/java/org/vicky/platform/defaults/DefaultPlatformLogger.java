/* Licensed under Apache-2.0 2025. */
package org.vicky.platform.defaults;

import org.vicky.platform.PlatformLogger;
import org.vicky.platform.PlatformPlugin;

public class DefaultPlatformLogger implements PlatformLogger {
	private final String prefix;

	public DefaultPlatformLogger(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public void info(String msg) {
		PlatformPlugin.logger().info("[INFO][" + prefix + "] " + msg);
	}

	@Override
	public void warn(String msg) {
		PlatformPlugin.logger().warn("[WARN][" + prefix + "] " + msg);
	}

	@Override
	public void error(String msg) {
		PlatformPlugin.logger().error("[ERROR][" + prefix + "] " + msg);
	}

	@Override
	public void error(String msg, Throwable throwable) {
		PlatformPlugin.logger().error("[ERROR][" + prefix + "] " + msg, throwable);
		throwable.printStackTrace(System.err);
	}

	@Override
	public void debug(String msg) {
		PlatformPlugin.logger().debug("[DEBUG][" + prefix + "] " + msg);
	}
}
