/* Licensed under Apache-2.0 2025. */
package org.vicky.platform;

public interface PlatformConfig {
	boolean getBooleanValue(String debug);

	String getStringValue(String debug);

	int getIntegerValue(String debug);

	float getFloatValue(String debug);

	double getDoubleValue(String debug);
}
