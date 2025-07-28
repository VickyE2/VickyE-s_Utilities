/* Licensed under Apache-2.0 2025. */
package org.vicky.platform.world;

public interface PlatformWorld {
	String getName(); // optional identifier

	Object getNative(); // underlying platform world object
}
