package org.vicky.forgeplatform;

import org.vicky.platform.PlatformConfig;
import org.vicky.utilities.ForgeModConfig;
import org.vicky.utilities.JsonConfigManager;

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
    public int getIntegerValue(String debug) {
        return manager.getIntegerValue(debug);
    }

    @Override
    public float getFloatValue(String debug) {
        return manager.getIntegerValue(debug);
    }

    @Override
    public double getDoubleValue(String debug) {
        return manager.getDoubleValue(debug);
    }

    public void setConfigValue(String key, Object value) {
        this.manager.setConfigValue(key, value);
    }

    public void syncFromForgeConfig() {
        setConfigValue("debug_mode", ForgeModConfig.DEBUG_MODE.get());
    }
}
