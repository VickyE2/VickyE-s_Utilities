/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform.player;

import net.kyori.adventure.text.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.vicky.forge.entity.ForgePlatformLivingEntity;
import org.vicky.forge.forgeplatform.adventure.AdventureComponentConverter;
import org.vicky.forge.forgeplatform.item.ForgeItemStack;
import org.vicky.forge.forgeplatform.useables.ForgeVec3;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.entity.PlatformEntity;
import org.vicky.platform.guiscreens.GuiType;
import org.vicky.platform.item.InteractionHand;
import org.vicky.platform.item.PlatformItemStack;
import org.vicky.platform.player.PlatformInventory;
import org.vicky.platform.player.PlatformPlayer;
import org.vicky.platform.player.PlatformRandom;
import org.vicky.platform.world.PlatformLocation;

import java.util.Locale;
import java.util.UUID;

public class ForgePlatformPlayer extends ForgePlatformLivingEntity implements PlatformPlayer {

	private final ServerPlayer player;

	public ForgePlatformPlayer(ServerPlayer player) {
		super(player);
		this.player = player;
	}

	public static ForgePlatformPlayer adapt(ServerPlayer player) {
		return new ForgePlatformPlayer(player);
	}

	@Override
	public UUID uniqueId() {
		return player.getUUID();
	}

	@Override
	public Component name() {
		return Component.translatable(player.getName().getString());
	}

	@Override
	public PlatformRandom random() {
		return null;
	}

	@Override
	public PlatformItemStack itemInHand(InteractionHand interactionHand) {
		return null;
	}

	@Override
	public void setItemInHand(InteractionHand interactionHand, PlatformItemStack platformItemStack) {

	}

	@Override
	public void playItemBreakAnimation(InteractionHand interactionHand) {

	}

	@Override
	public PlatformInventory inventory() {
		return null;
	}

	@Override
	public void sendMessage(Component msg) {
		player.sendSystemMessage(AdventureComponentConverter.toNative(msg));
	}

	@Override
	public void sendMessage(String msg) {
		player.sendSystemMessage(AdventureComponentConverter.toNative(Component.translatable(msg)));
	}

	@Override
	public void sendMessage(PlatformEntity sender, String message) {
		Component formatted = Component.text("<")
				.append(sender.getCustomName().orElseGet(() -> Component.text("unknown")))
				.append(Component.text("> "))
				.append(Component.text(message));

		player.sendSystemMessage(AdventureComponentConverter.toNative(formatted));
	}

	@Override
	public void sendMessage(PlatformEntity sender, Component message) {
		Component formatted = Component.text("<")
				.append(sender.getCustomName().orElseGet(() -> Component.text("unknown")))
				.append(Component.text("> "))
				.append(message);

		player.sendSystemMessage(AdventureComponentConverter.toNative(formatted));
	}

	@Override
	public void showBossBar(PlatformBossBar bar) {
		System.out.println("I was requested to put on a bossbar...");
		bar.addViewer(ForgePlatformPlayer.adapt(player));
	}

	@Override
	public void hideBossBar(PlatformBossBar bar) {
		System.out.println("I was requested to remove a bossbar...");
		bar.removeViewer(ForgePlatformPlayer.adapt(player));
	}

	@Override
	public void giveItem(PlatformItemStack item) {
		if (item instanceof ForgeItemStack i) {
			ItemStack stack = i.delegate();
			ordinal.onItemPickup(i.delegate().getEntityRepresentation().spawnAtLocation(stack));
		}
	}

	@Override
	public boolean hasPermissions(int i) {
		return false;
	}

	@Override
	public void playSound(PlatformLocation location, String soundName, Object category, Float volume, Float pitch) {
		ResourceLocation soundId = ResourceLocation.parse(soundName);
		SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(soundId);
		if (event == null)
			return;
		player.playNotifySound(event, SoundSource.valueOf(category.toString().toUpperCase(Locale.ROOT)), // e.g.
				// "RECORDS"
				volume, pitch);
	}

	@Override
	public void openGui(GuiType guiType) {

	}

	@Override
	public @NotNull PlatformLocation getLocation() {
		Level level = player.level();
		return new ForgeVec3(level, player.getX(), player.getY(), player.getZ(), player.yRotO, player.xRotO);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ForgePlatformPlayer))
			return false;
		return ((ForgePlatformPlayer) obj).getHandle() == this.player;
	}

	public @NotNull ServerPlayer getHandle() {
		return player;
	}
}