/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions;

import java.util.Optional;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.vicky.effectsSystem.EffectRegistry;
import org.vicky.effectsSystem.StatusEffect;
import org.vicky.vicky_utils;

public class EffectsPlaceholderExpansion extends PlaceholderExpansion {
  public static final String IDENTIFIER = "veffects";
  private final String author;
  private final String version;

  public EffectsPlaceholderExpansion(vicky_utils plugin) {
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
   * <pre>veffects_{property}:{effect_name}</pre>
   */
  @Override
  public String onPlaceholderRequest(Player player, @NotNull String identifier) {
    String[] parts = identifier.split(":");
    if (parts.length != 2) return "";
    String property = parts[0];
    String effectName = parts[1];

    Optional<StatusEffect> optional =
        EffectRegistry.getInstance(EffectRegistry.class).getEffect(effectName);
    if (optional.isEmpty()) return "";
    StatusEffect effect = optional.get();

    return switch (property.toLowerCase()) {
      case "duration" -> String.valueOf(effect.getDuration(player));
      case "level" -> String.valueOf(effect.getLevel(player));
      case "active", "has", "haseffect" -> String.valueOf(effect.isEntityAffected(player));
      case "key" -> effect.getKey();
      case "type" -> effect.getEffectType().name().toLowerCase();
      default -> "";
    };
  }
}
