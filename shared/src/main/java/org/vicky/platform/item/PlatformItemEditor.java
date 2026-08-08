/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.item;

import net.kyori.adventure.text.Component;
import org.vicky.platform.tags.CompoundData;

import java.util.List;
import java.util.function.Consumer;

public interface PlatformItemEditor {

	PlatformItemEditor customName(Component name);

	PlatformItemEditor customData(Consumer<CompoundData> consume);

	PlatformItemEditor clearCustomName();

	PlatformItemEditor addLore(List<Component> lines);

	PlatformItemEditor addLore(Component line);

	PlatformItemEditor clearLore();

	PlatformItemEditor damage(int damage);

	PlatformItemEditor unbreakable(boolean unbreakable);

	PlatformItemEditor customModelData(int value);

	PlatformItemEditor enchantment(String enchantmentId, int level);

	PlatformItemEditor removeEnchantment(String enchantmentId);

	PlatformItemEditor customString(String key, String value);

	PlatformItemEditor customInt(String key, int value);

	PlatformItemStack apply();
}