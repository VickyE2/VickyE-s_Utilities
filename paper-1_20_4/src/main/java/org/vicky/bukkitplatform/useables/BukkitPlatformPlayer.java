/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import java.util.UUID;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.vicky.platform.*;
import org.vicky.platform.world.PlatformLocation;

import net.kyori.adventure.text.Component;

public class BukkitPlatformPlayer implements PlatformPlayer {

	private final Player player;

	private BukkitPlatformPlayer(Player player) {
		this.player = player;
	}

	public static BukkitPlatformPlayer of(Player player) {
		if (player == null) {
			return null;
		}
		return new BukkitPlatformPlayer(player);
	}

	@Override
	public @NotNull UUID uniqueId() {
		return player.getUniqueId();
	}

	@Override
	public @NotNull Component name() {
		return player.name(); // Adventure's name() returns Component
	}

	@Override
	public void sendMessage(@NotNull Component msg) {
		player.sendMessage(msg);
	}

	@Override
	public void sendMessage(@NotNull String msg) {
		player.sendMessage(msg);
	}

	@Override
	public void sendComponent(@NotNull Component component) {
		player.sendMessage(component); // same as sendMessage(Component)
	}

	@Override
	public void showBossBar(@NotNull PlatformBossBar bar) {
		if (bar instanceof BukkitPlatformBossBar bukkitBar) {
			bukkitBar.getBossBar().addViewer(player);
		}
	}

	@Override
	public void hideBossBar(@NotNull PlatformBossBar bar) {
		if (bar instanceof BukkitPlatformBossBar bukkitBar) {
			bukkitBar.getBossBar().removeViewer(player);
		}
	}

	@Override
	public void playSound(@NotNull PlatformLocation location, @NotNull String soundName, Object soundCategory,
			Float volume, Float pitch) {
		SoundCategory bukkitCategory = soundCategory instanceof SoundCategory sc ? sc : SoundCategory.MASTER;
		player.playSound(BukkitLocationAdapter.to((PlatformLocation) location), soundName, bukkitCategory, volume,
				pitch);
	}

	@Override
	public @NotNull PlatformLocation getLocation() {
		return BukkitLocationAdapter.from(player.getLocation());
	}

	public Player getBukkitPlayer() {
		return player;
	}
}
