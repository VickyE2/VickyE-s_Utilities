/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.server;

/** Represents a handle to a scheduled or repeating task. */
public interface PlatformTask {
	void cancel();
	boolean isCancelled();
}
