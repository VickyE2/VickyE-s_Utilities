/* Licensed under Apache-2.0 2024. */
package org.vicky.afk;

import static org.vicky.global.Global.globalConfigManager;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import dev.lone.itemsadder.api.Events.ResourcePackSendEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.CombatHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.vicky.afk.event.PlayerAFKEvent;
import org.vicky.afk.event.PlayerAFKKickEvent;
import org.vicky.afk.event.PlayerBTKEvent;
import org.vicky.utilities.BukkitHex;

/**
 * AFKManager handles detecting and managing AFK players.
 * <p>
 * It tracks the last activity time for each player and uses an asynchronous timer to
 * check for inactivity. If a player is inactive for longer than the configured AFK threshold,
 * the manager marks the player as AFK and fires a {@link PlayerAFKEvent}. When the player performs
 * any activity (e.g. moves, interacts, opens an inventory, throws an item, uses a command, or chats),
 * their activity is updated; if they were marked AFK, a {@link PlayerBTKEvent} is fired.
 * </p>
 * <p>
 * If the AFK time exceeds the kick threshold (if not set to -1), the player is kicked.
 * All heavy tasks (such as iterating over all online players) are run asynchronously to reduce server load.
 * However, any Bukkit API calls that must run on the main thread (e.g. kicking a player, firing events)
 * are scheduled accordingly.
 * </p>
 *
 * <p><b>Potential Issues and Limitations:</b></p>
 * <ul>
 *   <li>
 *     <b>Event Coverage:</b> It is crucial to update a player's activity from all relevant events
 *     (movement, interactions, inventory open/close, item drop, commands, chat, etc.). Missing an event
 *     may cause false AFK detection.
 *   </li>
 *   <li>
 *     <b>Async vs. Synchronous:</b> The AFK checker runs asynchronously for performance,
 *     but any operations that require the main thread (kicking, firing events) are scheduled via
 *     {@code Bukkit.getScheduler().runTask()}. Ensure proper synchronization when sharing data.
 *   </li>
 *   <li>
 *     <b>Configuration:</b> The time thresholds are in milliseconds. For example, a threshold of 5 minutes
 *     should be set as 300000. A kick threshold of -1 disables kicking.
 *   </li>
 *   <li>
 *     <b>Performance:</b> The periodic check runs every second. With very high player counts, consider
 *     tuning the interval or optimizing the check further.
 *   </li>
 * </ul>
 */
@SuppressWarnings("deprecation")
public class AFKManager implements Listener {

  private final Plugin plugin;
  private final long afkThresholdMillis;
  private final long kickThresholdMillis;
  private final ConcurrentMap<String, Long> lastActivity;
  private final ConcurrentMap<String, Long> afkCommandCooldown;
  private final Set<String> afkPlayers;

  /**
   * Constructs an AFKManager with the specified thresholds.
   *
   * @param plugin The plugin instance.
   */
  public AFKManager(Plugin plugin) {
    this.plugin = plugin;
    this.afkThresholdMillis = globalConfigManager.getIntegerValue("defaults.AFKThreshold");
    this.kickThresholdMillis = globalConfigManager.getIntegerValue("defaults.AFKKickThreshold");
    this.lastActivity = new ConcurrentHashMap<>();
    this.afkCommandCooldown = new ConcurrentHashMap<>();
    this.afkPlayers = ConcurrentHashMap.newKeySet();
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  /**
   * Updates the last activity timestamp for the given player.
   * <p>
   * This method should be called whenever the player performs an action that resets their AFK timer
   * (e.g., moves, interacts, opens an inventory, uses a command, chats, or throws an item).
   * If the player was previously marked as AFK, this method fires a {@link PlayerBTKEvent}.
   * </p>
   *
   * @param player The player whose activity is being updated.
   */
  public boolean updateActivity(@NotNull Player player) {
    if (player.isInsideVehicle()
        || (player.getLocation().getBlock().getType().equals(Material.WATER)
            && !player.isSwimming())) {
      return false;
    }
    String uuid = player.getUniqueId().toString();
    long now = System.currentTimeMillis();
    lastActivity.put(uuid, now);
    if (afkPlayers.contains(uuid)) {
      afkPlayers.remove(uuid);
      Bukkit.getScheduler()
          .runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerBTKEvent(player)));
    }
    return true;
  }

  /**
   * Checks if the player is in a WorldGuard region that bypasses AFK detection.
   *
   * @param player The player to check.
   * @return True if the player's location is in a region with id "afk-bypass" (or similar flag), false otherwise.
   */
  private boolean isInAfkBypassRegion(Player player) {
    LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    com.sk89q.worldguard.protection.regions.ProtectedRegion region =
        container
            .createQuery()
            .getApplicableRegions(localPlayer.getLocation())
            .getRegions()
            .stream()
            .filter(r -> r.getId().equalsIgnoreCase("afk-bypass"))
            .findFirst()
            .orElse(null);
    return region != null;
  }

  /**
   * Handles the /afk command.
   * <p>
   * This command can only be executed once every 10 minutes per player. Additionally, it will only work if
   * the player is not in combat or taking damage (determined by an external MMOCore CombatManager).
   * If permitted, the player is marked as AFK and a PlayerAFKEvent is fired.
   * </p>
   *
   * @param player The player executing the command.
   */
  public void handleAfkCommand(@NotNull Player player) {
    String uuid = player.getUniqueId().toString();
    long now = System.currentTimeMillis();
    long cooldownMillis = 10 * 60 * 1000;
    if (afkCommandCooldown.containsKey(uuid)) {
      long lastUsed = afkCommandCooldown.get(uuid);
      if (now - lastUsed < cooldownMillis) {
        player.sendMessage(ChatColor.RED + "You can only use /afk once every 10 minutes.");
        return;
      }
    }
    if (isInCombat(player)) {
      player.sendMessage(ChatColor.RED + "You cannot use /afk while in combat or taking damage.");
      return;
    }
    if (updateActivity(player)) {
      afkPlayers.add(uuid);
      Bukkit.getScheduler()
          .runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerAFKEvent(player)));
      player.sendMessage(ChatColor.YELLOW + "You are now marked as AFK.");
      afkCommandCooldown.put(uuid, now);
    } else {
      player.sendMessage(ChatColor.RED + "You cannot use /afk while in a vehicle or in water...");
    }
  }

  /**
   * Checks if the player is in combat with mmocore..
   *
   * @param player The player to check.
   * @return True if the player is in combat or taking damage; false otherwise.
   */
  private boolean isInCombat(Player player) {
    PlayerData data = PlayerData.get(player);
    return data.isInCombat();
  }

  /**
   * Starts an asynchronous repeating task to check player activity.
   * <p>
   * Every second, this task iterates over all online players to determine if their inactivity
   * exceeds the AFK threshold. If so, the player is marked as AFK and a {@link PlayerAFKEvent} is fired.
   * If the kick threshold is set (i.e., not -1) and exceeded, the player is kicked.
   * </p>
   */
  public void startActivityChecker() {
    new BukkitRunnable() {
      @Override
      public void run() {
        long now = System.currentTimeMillis();
        for (Player player : Bukkit.getOnlinePlayers()) {
          String uuid = player.getUniqueId().toString();
          long last = lastActivity.getOrDefault(uuid, now);
          long inactiveTime = now - last;

          if (new CombatHandler(PlayerData.get(player)).isInCombat()) {
            Bukkit.getScheduler().runTask(plugin, () -> lastActivity.put(uuid, now));
          }

          if (!afkPlayers.contains(uuid) && inactiveTime >= afkThresholdMillis) {
            afkPlayers.add(uuid);

            Bukkit.getScheduler()
                .runTask(
                    plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerAFKEvent(player)));
          }

          if (kickThresholdMillis != -1 && inactiveTime >= kickThresholdMillis) {
            if (!isInAfkBypassRegion(player))
              Bukkit.getScheduler()
                  .runTask(
                      plugin,
                      () -> {
                        PlayerAFKKickEvent event = new PlayerAFKKickEvent(player);
                        Bukkit.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                          player.kick(
                              Component.text(
                                  globalConfigManager.getStringValue("defaults.AFKKickMessage")
                                          != null
                                      ? globalConfigManager.getStringValue(
                                          "defaults.AFKKickMessage")
                                      : "You were kicked for being afk",
                                  TextColor.color(225, 130, 0),
                                  TextDecoration.ITALIC));
                          afkPlayers.remove(uuid);
                          lastActivity.remove(uuid);
                        } else {
                          String newUuid = player.getUniqueId().toString();
                          long newNow = System.currentTimeMillis();
                          lastActivity.put(newUuid, newNow);
                        }
                      });
          }
        }
      }
    }.runTaskTimerAsynchronously(plugin, 20L, 20L);
  }

  /**
   * Checks if a player is currently marked as AFK.
   *
   * @param player The player to check.
   * @return True if the player is marked as AFK; false otherwise.
   */
  public boolean isAfk(@NotNull Player player) {
    return afkPlayers.contains(player.getUniqueId().toString());
  }

  /**
   * Gets an unmodifiable set of the UUIDs of players currently marked as AFK.
   *
   * @return A set of UUID strings representing AFK players.
   */
  public Set<String> getAfkPlayers() {
    return Collections.unmodifiableSet(afkPlayers);
  }

  @EventHandler
  public void onPlayerBTK(PlayerBTKEvent e) {
    List<String> afkMessages = globalConfigManager.getListConfigValue("defaults.BTKMessages");
    if (afkMessages.isEmpty()) {
      e.getPlayer().sendMessage(Component.text("You are no longer AFK."));
    } else {
      int randomIndex = new Random().nextInt(afkMessages.size()); // No need to subtract 1
      e.getPlayer()
          .sendMessage(
              Component.text(
                  "⨀  " + afkMessages.get(randomIndex),
                  TextColor.color(0, 200, 0),
                  TextDecoration.ITALIC));
    }
    if (globalConfigManager.getBooleanValue("defaults.AllowBFKInvulnerable")) {
      e.getPlayer().setInvulnerable(true);
      Bukkit.getScheduler()
          .scheduleSyncDelayedTask(
              plugin,
              () -> {
                e.getPlayer().setInvulnerable(false);
              },
              globalConfigManager.getIntegerValue("defaults.BFKInvulnerableTime"));
    }
  }

  @EventHandler
  public void onPlayerAFK(PlayerAFKEvent e) {
    List<String> afkMessages = globalConfigManager.getListConfigValue("defaults.AFKMessages");
    if (afkMessages.isEmpty()) {
      e.getPlayer().sendMessage(Component.text("You are now AFK."));
    } else {
      int randomIndex = new Random().nextInt(afkMessages.size()); // No need to subtract 1
      e.getPlayer()
          .sendMessage(
              Component.text(
                  "⁜  " + afkMessages.get(randomIndex),
                  TextColor.color(0, 200, 0),
                  TextDecoration.ITALIC));
    }
  }

  @EventHandler
  public void playerMoveEvent(PlayerMoveEvent e) {
    Player player = e.getPlayer();
    if (e.getFrom().distanceSquared(e.getTo()) > 0.05) {
      updateActivity(player);
    }
  }

  @EventHandler
  public void playerJumpEvent(PlayerJumpEvent e) {
    updateActivity(e.getPlayer());
  }

  @EventHandler
  public void inventoryOpenEvent(InventoryOpenEvent e) {
    if (e.getPlayer() instanceof Player player) updateActivity(player);
  }

  @EventHandler
  public void itemDroppedEvent(PlayerDropItemEvent e) {
    updateActivity(e.getPlayer());
  }

  @EventHandler
  public void playerChatEvent(AsyncChatEvent e) {
    updateActivity(e.getPlayer());
  }

  @EventHandler
  public void playerRunCommandEvent(PlayerCommandSendEvent e) {
    updateActivity(e.getPlayer());
  }

  @EventHandler
  public void playerResourcePackDownload(ResourcePackSendEvent e) {
    updateActivity(e.getPlayer());
  }

  @EventHandler
  public void playerDamageEvent(EntityDamageEvent e) {
    if (globalConfigManager.getBooleanValue("defaults.NoAFKDamage")) {
      if (e.getEntity() instanceof Player player) if (isAfk(player)) e.setCancelled(true);
      if (e.getDamageSource() instanceof Player attacker) {
        Bukkit.getScheduler()
            .scheduleSyncDelayedTask(
                plugin,
                () ->
                    attacker.sendMessage(
                        Component.text(
                            BukkitHex.colorize(
                                "#BB0000[Sorry but you cannot attack that player.]"
                                    + " #AAAA00[AFK]"))));
      }
    }
  }
}
