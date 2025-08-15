package org.vicky.platform;

import org.vicky.utilities.PermittedObject;

public interface PlatformConfig {
    boolean getBooleanValue(String debug);
    String getStringValue(String debug);
    Integer getIntegerValue(String debug);
    Float getFloatValue(String debug);
    Double getDoubleValue(String debug);

    void setConfigValue(String key, PermittedObject<?> value);

    boolean doesKeyExist(String key);

    void saveConfig();
}
