/* Licensed under Apache-2.0 2024. */
package org.vicky.mythic.conditions;

import static org.vicky.vicky_utils.plugin;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;
import org.vicky.mythic.BaseCondition;

public class ExperiencingStatusEffect implements BaseCondition {
  private final String statusEffectName;

  public ExperiencingStatusEffect(MythicLineConfig config) {
    statusEffectName =
        config.getString(
            new String[] {
              "status_effect",
              "se",
              "effect",
              "e",
              "effect_name",
              "en",
              "potion",
              "p",
              "custom_effect",
              "ce"
            },
            "UNKNOWN");
  }

  @Override
  public boolean check(AbstractEntity abstractEntity) {
    return getStatusEffects(abstractEntity.getBukkitEntity()).contains(statusEffectName);
  }

  @SuppressWarnings("unchecked")
  private List<String> getStatusEffects(Entity entity) {
    for (MetadataValue value : entity.getMetadata("statusEffects")) {
      if (value.getOwningPlugin() == plugin && value.value() instanceof List<?> list) {
        return (List<String>) list;
      }
    }
    return Collections.emptyList();
  }
}
