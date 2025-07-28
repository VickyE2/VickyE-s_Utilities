/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.betterHUD.bossbar;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.vicky.betterHUD.bossbar.triggers.MythicMobBossBarRegistered;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.vicky_utils;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import kr.toxicity.hud.api.BetterHudAPI;
import kr.toxicity.hud.api.bukkit.update.BukkitEventUpdateEvent;

public class BossbarManager {
	public final ContextLogger logger = new ContextLogger(ContextLogger.ContextType.FEATURE, "BOSSBARS");
	private final Map<String, BukkitRunnable> registeredListeners = new ConcurrentHashMap<>();
	private final Map<String, CachedBossInfo> cachedInfo = new ConcurrentHashMap<>();

	public BossbarManager() {
	}

	public static String getBossId(LivingEntity entity) {
		if (!entity.hasMetadata("bossBarAttainableName"))
			return null;
		var meta = entity.getMetadata("bossBarAttainableName");
		if (meta.isEmpty())
			return null;
		return meta.get(0).asString();
	}

	public static String getBossType(LivingEntity entity) {
		if (!entity.hasMetadata("bossBarAttainableType"))
			return null;
		var meta = entity.getMetadata("bossBarAttainableType");
		if (meta.isEmpty())
			return null;
		return meta.get(0).asString();
	}

	public static String getBossPhase(LivingEntity entity) {
		if (!entity.hasMetadata("bossBarAttainablePhase"))
			return null;
		var meta = entity.getMetadata("bossBarAttainablePhase");
		if (meta.isEmpty())
			return null;
		return meta.get(0).asString();
	}

	public CachedBossInfo getCachedBossInfo(String bossId) {
		return cachedInfo.get(bossId);
	}

	/**
	 * Registers a boss type that supports having a bossbar. Example bossName:
	 * "LavaTitan", "IceQueen"
	 */
	public boolean registerBoss(String bossName, String bossType, int range, String popupName,
			LivingEntity bossEntity) {
		var betterHud = BetterHudAPI.inst();
		var popup = betterHud.getPopupManager().getPopup(popupName);
		if (popup == null) {
			logger.print("Popup '" + popupName + "' dosent exist for boss " + bossName + ".",
					ContextLogger.LogType.ERROR);
			return false;
		}

		if (cachedInfo.containsKey(bossName)) {
			logger.print("Boss '" + bossName + "' is already registered.", ContextLogger.LogType.WARNING);
			return false;
		}
		cachedInfo.put(bossName,
				new CachedBossInfo(bossType, bossName, 1,
						bossEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), bossEntity.getHealth(),
						bossEntity.getUniqueId(), ConcurrentHashMap.newKeySet()));
		logger.print("Registered boss for bossbar: " + bossName);

		BukkitRunnable bossPerTickListener = new BukkitRunnable() {
			@Override
			public void run() {
				if (bossEntity.isDead()) {
					var viewers = cachedInfo.get(bossName).viewers();
					for (UUID uuid : viewers) {
						var hudPlayer = betterHud.getPlayerManager().getHudPlayer(uuid);
						if (hudPlayer != null)
							popup.hide(hudPlayer);
					}
					cancel();
					return;
				}
				CachedBossInfo info = cachedInfo.get(bossName);
				if (bossEntity.getHealth() != cachedInfo.get(bossName).currentHealth()) {
					cachedInfo.put(bossName, new CachedBossInfo(info.type(), info.name(), info.phase(),
							bossEntity.getHealth(), info.maxHealth(), info.uuid(), info.viewers()));
					info = cachedInfo.get(bossName);
				}
				if (Integer.parseInt(getBossPhase(bossEntity)) != cachedInfo.get(bossName).phase()) {
					cachedInfo.put(bossName,
							new CachedBossInfo(info.type(), info.name(), Integer.parseInt(getBossPhase(bossEntity)),
									info.currentHealth(), info.maxHealth(), info.uuid(), info.viewers()));
					info = cachedInfo.get(bossName);
				}
				Collection<Player> nearby = bossEntity.getLocation().getWorld()
						.getNearbyPlayers(bossEntity.getLocation(), range);
				for (Player player : nearby) {
					var listedPlayers = info.viewers();
					if (!listedPlayers.contains(player.getUniqueId())) {
						listedPlayers.add(player.getUniqueId());
						var hudPlayer = betterHud.getPlayerManager().getHudPlayer(player.getUniqueId());
						if (hudPlayer != null)
							popup.show(new BukkitEventUpdateEvent(
									new MythicMobBossBarRegistered(bossEntity, bossName, bossType,
											bossEntity.getMetadata("bossBarAttainablePhase").get(0) != null
													? bossEntity.getMetadata("bossBarAttainablePhase").get(0).asInt()
													: 1),
									UUID.randomUUID()), hudPlayer);
					}
				}
				var viewers = cachedInfo.get(bossName).viewers();
				Iterator<UUID> iterator = viewers.iterator();
				while (iterator.hasNext()) {
					UUID uuid = iterator.next();
					if (nearby.stream().noneMatch(p -> p.getUniqueId().equals(uuid))) {
						iterator.remove(); // ✅ safe way to remove while iterating
						var hudPlayer = betterHud.getPlayerManager().getHudPlayer(uuid);
						if (hudPlayer != null)
							popup.hide(hudPlayer);
					}
				}
			}
		};
		registeredListeners.put(bossName, bossPerTickListener);
		bossPerTickListener.runTaskTimer(vicky_utils.getPlugin(), 0L, 40L);
		logger.print("Successfully registered bossbar for boss: " + bossName);
		return true;
	}

	/**
	 * Unregisters a boss type, so it no longer has a bossbar.
	 */
	public void unregisterBoss(String bossId) {
		if (cachedInfo.remove(bossId) != null) {
			logger.print("Unregistered boss for bossbar: " + bossId);
			if (!registeredListeners.get(bossId).isCancelled())
				registeredListeners.get(bossId).cancel();
		} else {
			logger.print("Boss '" + bossId + "' was not registered.", ContextLogger.LogType.WARNING);
		}
	}

	/**
	 * Checks if the boss type has a registered bossbar.
	 */
	public boolean isBossRegistered(String bossId) {
		if (bossId == null)
			return false;
		return cachedInfo.containsKey(bossId);
	}

	public void registerListener(Plugin plugin) {
		Bukkit.getPluginManager().registerEvents(new BossBarManagerListener(), plugin);
	}

	public LivingEntity getBossEntity(String bossID) {
		if (isBossRegistered(bossID)) {
			UUID bossUUID = cachedInfo.get(bossID).uuid();
			return (LivingEntity) Bukkit.getEntity(bossUUID);
		} else {
			return null;
		}
	}

	public boolean bossIsVisibleTo(String bossID, Player player) {
		if (isBossRegistered(bossID)) {
			return cachedInfo.get(bossID).viewers().contains(player.getUniqueId());
		} else {
			return false;
		}
	}

	public record CachedBossInfo(String type, String name, int phase, double currentHealth, double maxHealth, UUID uuid,
			Set<UUID> viewers) {
	}

	private class BossBarManagerListener implements Listener {
		/**
		 * Called when a MythicMobDeathEvent is fired.
		 * <p>
		 * </p>
		 * <p>
		 * <em> Used to unregister the mo's bossbar if it is. </em>
		 * </p>
		 *
		 * @param event
		 *            the MythicMechanicLoadEvent containing the mechanic name and
		 *            configuration
		 */
		@EventHandler
		public void onMythicMobKilledEvent(MythicMobDeathEvent event) {
			if (isBossRegistered(getBossId((LivingEntity) event.getEntity()))) {
				unregisterBoss(getBossId((LivingEntity) event.getEntity()));
			}
		}

		@EventHandler
		public void onMythicMobRemoveEvent(MythicMobDespawnEvent event) {
			if (isBossRegistered(getBossId((LivingEntity) event.getEntity()))) {
				unregisterBoss(getBossId((LivingEntity) event.getEntity()));
			}
		}
	}
}
