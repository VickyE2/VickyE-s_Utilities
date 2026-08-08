/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.player;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.entity.PlatformEntity;
import org.vicky.platform.entity.PlatformLivingEntity;
import org.vicky.platform.guiscreens.GuiType;
import org.vicky.platform.item.InteractionHand;
import org.vicky.platform.item.PlatformItemStack;
import org.vicky.platform.utils.ComponentUtil;
import org.vicky.platform.world.PlatformLocation;

import java.util.Optional;
import java.util.UUID;

public interface PlatformPlayer extends PlatformLivingEntity {
	UUID uniqueId();

	@NotNull
	Component name();
	@Override
	default void setCustomName(@NotNull Component name) {
		throw new UnsupportedOperationException("Entity type of PlatformPlayer does not support custom name.");
	}
	@Override
	default @NotNull Optional<@NotNull Component> getCustomName() {
		return Optional.of(name());
	}

	PlatformRandom random();

	PlatformItemStack itemInHand(InteractionHand hand);

	void setItemInHand(InteractionHand hand, PlatformItemStack stack);

	void playItemBreakAnimation(InteractionHand hand);

	PlatformInventory inventory();

	void giveItem(PlatformItemStack stack);

	boolean hasPermissions(int level);

	static PlatformPlayer cast(Object item) {
		if (item instanceof PlatformPlayer platformPlayer) {
			return platformPlayer;
		}
		// If it's a Minecraft native ItemStack, wrap or convert it here:
		// return new MyPlatformItemStackWrapper((net.minecraft.world.item.ItemStack)
		// item);
		throw new IllegalArgumentException("Cannot cast " + item + " to PlatformItemStack");
	}

	void sendMessage(Component msg);
	default void sendMessage(String msg) {
		sendMessage(ComponentUtil.createStr(msg));
	}
	default void sendMessage(PlatformEntity sender, String message) {
		sendMessage(sender, ComponentUtil.createStr(message));
	}
	void sendMessage(PlatformEntity sender, Component message);

	void showBossBar(PlatformBossBar bar);
	void hideBossBar(PlatformBossBar bar);

	void playSound(PlatformLocation location, String soundName, Object soundCategory, Float volume, Float pitch);
	void openGui(GuiType spec);

	@Override
	default boolean isPlayer() {
		return true;
	}
}