package org.vicky.listeners;

import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.vicky.betterHUD.depr.BossbarOverlay;

import java.util.Optional;

public class MythicMobSpawnListener implements Listener {

    @EventHandler
    public void onMobSpawn(MythicMechanicLoadEvent event){
        Optional<ISkillMechanic> possibility = event.getMechanic();
    }

    @EventHandler
    public void onMobSpawn(MythicMobSpawnEvent event) {
        Entity mob = event.getEntity();

        BossbarOverlay.ListenerSetting listenerSetting = new BossbarOverlay.ListenerSetting(
                "placeholder",
                "(number)papi:mmocore_health",
                "(number)papi:mmocore_max_health"
        );
    }
}
