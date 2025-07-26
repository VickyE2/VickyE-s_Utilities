/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions;

import static org.vicky.global.Global.bossbarManager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.vicky.betterHUD.bossbar.BossbarManager;
import org.vicky.vicky_utils;

public class BossbarPlaceholderExpansion extends PlaceholderExpansion {

  public static final String IDENTIFIER = "bossbar";
  private final String author;
  private final String version;

  public BossbarPlaceholderExpansion(vicky_utils plugin) {
    this.author = plugin.getDescription().getAuthors().toString();
    this.version = plugin.getDescription().getVersion();
  }

  @Override
  public @NotNull String getIdentifier() {
    return IDENTIFIER;
  }

  @Override
  public @NotNull String getAuthor() {
    return author;
  }

  @Override
  public @NotNull String getVersion() {
    return version;
  }

  /**
   * Expected format:
   * <ul>
   *     <li><pre>bossbar_{BOSS_NAME}_{property}</pre></li>
   * </ul>
   * e.g., <pre>{IDENTIFIER}_{BOSS_NAME}_name</pre>
   */
  @Override
  public String onPlaceholderRequest(Player player, @NotNull String identifier) {
    if (identifier.isEmpty()) return "";

    String[] parts = identifier.split("_", 2);
    if (parts.length < 2) return "";

    String bossID = parts[0];
    String property = parts[1].toLowerCase();

    if (!bossbarManager.isBossRegistered(bossID)) return property.equals("visible") ? "false" : "";

    BossbarManager.CachedBossInfo bossMob = bossbarManager.getCachedBossInfo(bossID);

    if (bossMob == null) return "";
    return switch (property) {
      case "type" -> bossMob.type();
      case "name" -> bossMob.name();
      case "phase", "state", "stage" -> String.valueOf(bossMob.phase());
      case "health", "current_health" -> String.valueOf(bossMob.currentHealth());
      case "maxhealth", "max_health" -> String.valueOf(bossMob.maxHealth());
      case "percent", "health_percent" -> {
        double percent = (bossMob.currentHealth() / bossMob.maxHealth()) * 100;
        yield String.format("%.0f", percent);
      }
      case "visible" -> bossbarManager.bossIsVisibleTo(bossID, player) ? "true" : "false";
      default -> "";
    };
  }
}
/*

 POPUP YAML - GPT

 popups:
 boss_near_popup_1:
   visible_condition:
     first: papi:boss_near_player_1_visible
     second: '1'
     operation: '=='
   layout:
     image_layouts:
       1:
         layout: bossbar_lava
         conditions:
           1:
             first: papi:boss_near_player_1_type
             second: "'LavaTitan'"
             operation: '=='
           2:
             first: papi:boss_near_player_1_phase
             second: 1
             operation: '=='
       2:
         layout: bossbar_lava_p2
         conditions:
           1:
             first: papi:boss_near_player_1_type
             second: "'LavaTitan'"
             operation: '=='
           2:
             first: papi:boss_near_player_1_phase
             second: 2
             operation: '=='
       3:
         layout: bossbar_ice
         conditions:
           1:
             first: papi:boss_near_player_1_type
             second: "'IceQueen'"
             operation: '=='
       4:
         layout: bossbar_void
         conditions:
           1:
             first: papi:boss_near_player_1_type
             second: "'VoidSpecter'"
             operation: '=='
       5:
         layout: bossbar_default
         conditions:
           1:
             first: papi:boss_near_player_1_type
             second: "'Unknown'"
             operation: '=='


*/
