package org.vicky.forge.forgeplatform.forgeplatform;

import org.vicky.forge.utilities.ForgeModConfig;
import org.vicky.platform.PlatformConfig;
import org.vicky.utilities.JsonConfigManager;
import org.vicky.utilities.PermittedObject;

public class ForgePlatformConfig implements PlatformConfig {

    private static ForgePlatformConfig INSTANCE;
    private JsonConfigManager manager;

    private ForgePlatformConfig() {
        manager = new JsonConfigManager();
        manager.createConfig("configs/general.json");
    }

    public static ForgePlatformConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgePlatformConfig();
        }
        return INSTANCE;
    }

    @Override
    public boolean getBooleanValue(String debug) {
        return manager.getBooleanValue(debug);
    }

    @Override
    public String getStringValue(String debug) {
        return manager.getStringValue(debug);
    }

    @Override
    public Integer getIntegerValue(String debug) {
        return manager.getIntegerValue(debug);
    }

    @Override
    public Float getFloatValue(String debug) {
        return (float) manager.getIntegerValue(debug);
    }

    @Override
    public Double getDoubleValue(String debug) {
        return manager.getDoubleValue(debug);
    }

    @Override
    public void setConfigValue(String key, PermittedObject<?> value) {
        manager.setConfigValue(key, value.getValue());
    }

    @Override
    public boolean doesKeyExist(String key) {
        return manager.doesPathExist(key);
    }

    @Override
    public void saveConfig() {
        manager.saveConfig();
    }

    public void setConfigValue(String key, Object value) {
        this.manager.setConfigValue(key, value);
    }

    public void syncFromForgeConfig() {
        setConfigValue("debug_mode", ForgeModConfig.DEBUG_MODE.get());
    }
}
