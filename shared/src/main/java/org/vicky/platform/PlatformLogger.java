/* Licensed under Apache-2.0 2025. */
package org.vicky.platform;

public interface PlatformLogger {
	void info(String message);

	void warn(String msg);

	void error(String msg);

	void debug(String msg);

	void error(String msg, Throwable throwable);
}
