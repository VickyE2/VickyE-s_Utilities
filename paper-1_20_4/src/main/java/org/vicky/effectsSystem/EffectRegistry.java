/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.effectsSystem;

import static io.lumine.mythic.lib.api.event.armorequip.ArmorListener.isAirOrNull;
import static org.vicky.betterHUD.vickyUtilsCompat.StatusEffectYamlBuilderKt.buildStatusEffectYaml;
import static org.vicky.kotlinUtils.UtilsKt.writeImage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.vicky.betterHUD.derived.Quad;
import org.vicky.mythic.MythicRegistrar;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.Registry;
import org.vicky.utilities.XmlConfigManager;
import org.vicky.vicky_utils;

public class EffectRegistry extends Registry<StatusEffect, EffectRegistry> {
	private final Map<String, StatusEffect> effectMap = new HashMap<>();
	private final List<Brewable> brewables = new ArrayList<>();
	private final Map<StatusEffect, Quad<InputStream, String, String, String>> toWrites = new HashMap<>();
	private final MythicRegistrar registrar;
	private final JavaPlugin plugin;
	private final XmlConfigManager config;

	public EffectRegistry(MythicRegistrar registrar, JavaPlugin plugin) {
		super("EffectRegistry");
		this.registrar = registrar;
		this.plugin = plugin;
		this.config = new XmlConfigManager();
		config.createConfig("configs/registered_effects.xml", "effects");
		getLogger().print("EffectRegistry initialized.", ContextLogger.LogType.BASIC, false);
	}

	public void writeAllToDisk() {
		StringBuilder images = new StringBuilder();
		StringBuilder layouts = new StringBuilder();
		StringBuilder popups = new StringBuilder();
		final Path path = plugin.getDataFolder().toPath().getParent().resolve("BetterHud");
		toWrites.forEach((effect, quad) -> {
			String key = effect.getKey();
			if (config.getConfigValue(key) == null) {
				getLogger().print("Adding effect to xml manager", ContextLogger.LogType.PENDING, false);
			}
			config.setConfigValue(key, "", Instant.now().toString(), Map.of("plugin", effect.plugin.getName()));
			try {
				getLogger().print("Writing " + key + " to disk", ContextLogger.LogType.PENDING);
				writeImage(quad.getW(),
						new File(plugin.getDataFolder().getParent(), "BetterHud/assets/vcompat_effects/icons"), key);
				images.append(quad.getX());
				layouts.append(quad.getY());
				popups.append(quad.getZ());
				if (effect instanceof Listener listener) {
					Bukkit.getPluginManager().registerEvents(listener, plugin);
				}
			} catch (Exception e) {
				getLogger().print("Error while writing '" + key + "' " + e.getMessage(), true);
			}
		});
		try {
			Files.writeString(path.resolve("images/status-effect-images.yml"), images.toString());
			Files.writeString(path.resolve("layouts/status-effect-layouts.yml"), layouts.toString());
			Files.writeString(path.resolve("popups/status-effect-popups.yml"), popups.toString());
		} catch (IOException e) {
			getLogger().print("Error while majors", true);
			throw new RuntimeException(e);
		}
		getLogger().print("Complete. Clearing maps.", ContextLogger.LogType.SUCCESS, true);
		for (String effectKey : effectMap.keySet()) {
			boolean written = toWrites.keySet().stream().anyMatch(e -> e.getKey().equals(effectKey));
			if (!written) {
				getLogger().print("Warning: Effect '" + effectKey + "' was registered but not written to disk.",
						ContextLogger.LogType.WARNING, true);
			}
		}
		toWrites.clear();
	}

	public void registerMechanicEffect(StatusEffect effect, Class<StatusEffectMechanic> optionalMechanic) {
		toWrites.put(effect, new Quad<>(effect.getIcon(),
				buildStatusEffectYaml(effect.getKey(), effect.getKey(), effect.getEffectType())));
		String key = effect.getKey();
		effectMap.put(key, effect);
		getLogger().print("Registered effect: " + key, ContextLogger.LogType.BASIC, false);

		if (optionalMechanic == null) {
			registrar.addMechanic(key,
					(config, plugin) -> new StatusEffectMechanic(config, (JavaPlugin) plugin, effect));
			getLogger().print("Registered default mechanic for effect: " + key, ContextLogger.LogType.AMBIENCE, false);
		} else {
			registrar.addMechanic(key, optionalMechanic);
			getLogger().print("Registered custom mechanic class for effect: " + key, ContextLogger.LogType.AMBIENCE,
					false);
		}

		if (effect instanceof Brewable brewable) {
			brewables.add(brewable);
		}
	}

	@Override
	public void register(StatusEffect effect) {
		toWrites.put(effect, new Quad<>(effect.getIcon(),
				buildStatusEffectYaml(effect.getKey(), effect.getKey(), effect.getEffectType())));
		String key = effect.getKey();
		effectMap.put(key, effect);
		getLogger().print("Registered non-mechanic effect: " + key, ContextLogger.LogType.BASIC, false);

		if (effect instanceof Brewable brewable) {
			brewables.add(brewable);
		}
	}

	public Optional<StatusEffect> getEffect(String key) {
		boolean exists = effectMap.containsKey(key);
		/*
		 * getLogger().print( "Lookup for effect \"" + key + "\": " + (exists ? "FOUND"
		 * : "NOT FOUND"), ContextLogger.LogType.WARNING, false);
		 */
		return Optional.ofNullable(effectMap.get(key));
	}

	public void apply(String key, LivingEntity entity, int duration, int level) {
		getEffect(key).ifPresentOrElse(effect -> {
			getLogger().print("Applying effect \"" + key + "\" with duration=" + duration + ", level=" + level
					+ " to entity=" + entity.getName(), ContextLogger.LogType.BASIC, false);
			effect.apply(entity, duration, level);
		}, () -> getLogger().print("Attempted to apply unknown effect \"" + key + "\" to " + entity.getName(),
				ContextLogger.LogType.ERROR, true));
	}

	public void stop(String key, LivingEntity entity) {
		getEffect(key).ifPresent(effect -> {
			getLogger().print("Stopping effect \"" + key + "\" on entity=" + entity.getName(),
					ContextLogger.LogType.BASIC, false);
			effect.stopEffect(entity);
		});
	}

	public Optional<Brewable> match(ItemStack ingredient, ItemStack basePotion) {
		for (Brewable brewable : brewables) {
			if (isSimilar(brewable.mainBrewItem(), ingredient) && isSimilar(brewable.bottomPotion(), basePotion)) {
				return Optional.of(brewable);
			}
		}
		return Optional.empty();
	}

	private static boolean isSimilar(ItemStack a, ItemStack b) {
		return a != null && b != null && a.getType() == b.getType() && Objects.equals(a.getItemMeta(), b.getItemMeta());
	}

	public List<Brewable> getBrewables() {
		return Collections.unmodifiableList(brewables);
	}

	@Override
	public Collection<StatusEffect> getRegisteredEntities() {
		getLogger().print("Requested all registered effects. Total: " + effectMap.size(),
				ContextLogger.LogType.AMBIENCE, false);
		return effectMap.values();
	}

	public static class EffectRegistryListener implements Listener {
		private final Map<String, BrewingSession> activeBrews = new HashMap<>();
		private final EffectRegistry registry = EffectRegistry.getInstance(EffectRegistry.class);
		private static final ContextLogger logger = new ContextLogger(ContextLogger.ContextType.SUB_SYSTEM,
				"EFFECT-BREW");

		@EventHandler
		public void onBrewingClick(InventoryClickEvent event) {
			if (!(event.getClickedInventory() instanceof BrewerInventory inventory))
				return;
			if (event.getSlot() != 3 && event.getInventory().getType() != InventoryType.BREWING)
				return;

			var stand = (BrewingStand) inventory.getHolder();
			if (stand == null)
				return;

			Bukkit.getScheduler().runTaskLater(vicky_utils.plugin,
					() -> handleBrewCheck((Player) event.getWhoClicked(), stand, inventory), 2L);
		}

		private void handleBrewCheck(Player player, BrewingStand stand, BrewerInventory inventory) {
			ItemStack ingredient = inventory.getItem(3);
			if (isAirOrNull(ingredient)) {
				stopBrewing(stand);
				return;
			}

			List<Brewable> possibleBrews = new ArrayList<>();
			for (Brewable brew : registry.getBrewables()) {
				if (brew.mainBrewItem().isSimilar(ingredient)) {
					possibleBrews.add(brew);
				}
			}

			if (possibleBrews.isEmpty()) {
				stopBrewing(stand);
				return;
			}

			String locKey = stand.getLocation().toString();
			if (!activeBrews.containsKey(locKey)) {
				startBrewTask(player, stand, inventory, ingredient, possibleBrews);
			} else {
				activeBrews.get(locKey).updatePossibleBrews(possibleBrews);
			}
		}

		private void startBrewTask(Player player, BrewingStand stand, BrewerInventory inventory, ItemStack ingredient,
				List<Brewable> possibleBrews) {
			AtomicInteger brewTicks = new AtomicInteger(400);
			String locKey = stand.getLocation().toString();

			BukkitTask task = new BukkitRunnable() {
				@Override
				public void run() {
					if (!stand.getBlock().getType().equals(Material.BREWING_STAND)) {
						stopBrewing(stand);
						cancel();
						return;
					}

					if (brewTicks.get() <= 0) {
						completeBrew(player, inventory, ingredient, possibleBrews);
						stopBrewing(stand);
						cancel();
						return;
					}

					// Simulate brewing progress
					stand.setBrewingTime(brewTicks.getAndAdd(-1));
				}
			}.runTaskTimer(vicky_utils.plugin, 2L, 1L);

			activeBrews.put(locKey, new BrewingSession(task, possibleBrews));
			logger.print("Started brewing at " + locKey, ContextLogger.LogType.BASIC, false);
		}

		private void completeBrew(Player player, BrewerInventory inventory, ItemStack ingredient,
				List<Brewable> possibleBrews) {
			for (int i = 0; i < 3; i++) {
				ItemStack bottle = inventory.getItem(i);
				if (isAirOrNull(bottle) || !(bottle.getItemMeta() instanceof PotionMeta))
					continue;
				for (Brewable brew : possibleBrews) {
					if (!brew.mainBrewItem().isSimilar(ingredient))
						continue;
					if (brew.bottomPotion().isSimilar(bottle)) {
						ItemStack result = brew.brewResult(player);
						inventory.setItem(i, result);
						logger.print("Brewed effect at slot " + i, ContextLogger.LogType.AMBIENCE, false);
						break;
					}
				}
			}

			// Shrink ingredient
			ItemStack ingr = inventory.getItem(3);
			if (ingr != null) {
				ingr.setAmount(ingr.getAmount() - 1);
				if (ingr.getAmount() <= 0)
					inventory.setItem(3, null);
			}
		}

		private void stopBrewing(BrewingStand stand) {
			String locKey = stand.getLocation().toString();
			BrewingSession session = activeBrews.remove(locKey);
			if (session != null)
				session.task.cancel();
			logger.print("Stopped brewing at " + locKey, ContextLogger.LogType.AMBIENCE, false);
		}

		private static class BrewingSession {
			BukkitTask task;
			List<Brewable> brewables;

			BrewingSession(BukkitTask task, List<Brewable> brewables) {
				this.task = task;
				this.brewables = brewables;
			}

			void updatePossibleBrews(List<Brewable> newBrews) {
				this.brewables = newBrews;
			}
		}
	}
}
