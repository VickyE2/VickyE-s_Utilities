package org.vicky.platform;

public interface PlatformConfig {
    boolean getBooleanValue(String debug);
    String getStringValue(String debug);
    Integer getIntegerValue(String debug);
    Float getFloatValue(String debug);
    Double getDoubleValue(String debug);
}
