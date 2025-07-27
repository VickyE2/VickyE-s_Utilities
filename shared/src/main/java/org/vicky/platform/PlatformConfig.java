package org.vicky.platform;

public interface PlatformConfig {
    boolean getBooleanValue(String debug);
    boolean getStringValue(String debug);
    boolean getIntegerValue(String debug);
    boolean getFloatValue(String debug);
    boolean getDoubleValue(String debug);
}
