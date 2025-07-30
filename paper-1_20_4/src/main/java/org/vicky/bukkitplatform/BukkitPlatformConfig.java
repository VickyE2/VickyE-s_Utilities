/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform;

import org.vicky.global.Global;
import org.vicky.platform.PlatformConfig;

public class BukkitPlatformConfig implements PlatformConfig {
	@Override
	public boolean getBooleanValue(String key) {
		return Global.globalConfigManager.getBooleanValue(key);
	}

	@Override
	public String getStringValue(String key) {
		return Global.globalConfigManager.getStringValue(key);
	}

	@Override
	public Integer getIntegerValue(String key) {
		return Global.globalConfigManager.getIntegerValue(key);
	}

	@Override
	public Float getFloatValue(String key) {
		return (float) Global.globalConfigManager.getIntegerValue(key);
	}

	@Override
	public Double getDoubleValue(String key) {
		return Global.globalConfigManager.getDoubleValue(key);
	}
}
