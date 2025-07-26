/* Licensed under Apache-2.0 2024. */
package org.vicky.betterHUD.bossbar.triggers;

import java.util.UUID;
import java.util.function.BiConsumer;
import kr.toxicity.hud.api.bukkit.trigger.HudBukkitEventTrigger;
import kr.toxicity.hud.api.bukkit.update.BukkitEventUpdateEvent;
import kr.toxicity.hud.api.update.UpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.vicky.vicky_utils;

public class BossEntityTrigger implements HudBukkitEventTrigger<MythicMobBossBarRegistered> {
  @Override
  public @NotNull Class<MythicMobBossBarRegistered> getEventClass() {
    return MythicMobBossBarRegistered.class;
  }

  @Override
  public @NotNull UUID getKey(MythicMobBossBarRegistered event) {
    return event.getMob().getUniqueId();
  }

  @Override
  public void registerEvent(@NotNull BiConsumer<UUID, UpdateEvent> eventConsumer) {
    Bukkit.getPluginManager()
        .registerEvent(
            getEventClass(),
            new Listener() {
              @EventHandler
              public void onMythicBossBarRegistered(MythicMobBossBarRegistered e) {}
            },
            EventPriority.MONITOR,
            (_l, e) -> {
              if (getEventClass().isAssignableFrom(e.getClass())) {
                var cast = getEventClass().cast(e);
                var wrapper = new BukkitEventUpdateEvent(cast, getKey(cast));
                eventConsumer.accept(getKey(cast), wrapper);
              }
            },
            vicky_utils.getPlugin(),
            true);
  }
}
