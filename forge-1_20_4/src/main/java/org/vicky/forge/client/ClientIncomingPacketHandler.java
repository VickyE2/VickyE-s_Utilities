/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.vicky.forge.client.screen.SimpleMusicSliderBossBar;
import org.vicky.forge.client.screen.SongLibraryScreen;
import org.vicky.forge.forgeplatform.adventure.AdventureComponentConverter;
import org.vicky.forge.network.registeredpackets.CreateSSBossBar;
import org.vicky.forge.network.registeredpackets.OpenOwnedRecordsScreen;
import org.vicky.forge.network.registeredpackets.RemoveSSBossBar;
import org.vicky.forge.network.registeredpackets.UpdateSSBossBar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientIncomingPacketHandler {
    static final Map<UUID, SimpleMusicSliderBossBar> activeBars = new HashMap<>();

    public static void proceedWithSSBossBar(CreateSSBossBar msg) {
        try {
            SimpleMusicSliderBossBar bar = new SimpleMusicSliderBossBar();

            // null-safe conversions — fallback to empty component / default image
            bar.setProgress(msg.progress());
            bar.setTitle(msg.title() == null ? Component.empty() : AdventureComponentConverter.toNative(msg.title()));
            bar.setSubTitle(
                    msg.subTitle() == null ? Component.empty() : AdventureComponentConverter.toNative(msg.subTitle()));
            bar.setColor(msg.hex() == null ? "#FFFFFF" : msg.hex());
            bar.setImage(msg.image()); // image may be null — let bar handle null gracefully

            activeBars.put(msg.id(), bar);
            System.out.println("[client] Created bossbar: " + msg.id() + " activeBars=" + activeBars.size());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void proceedWithOpeningScoreScreen(OpenOwnedRecordsScreen msg) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.setScreen(new SongLibraryScreen(msg.songs())));
    }

    public static void updateSSBossBar(UpdateSSBossBar msg, CustomPayloadEvent.Context ctx) {
        SimpleMusicSliderBossBar bar = activeBars.get(msg.id());
        if (bar != null) {
            if (msg.title() != null)
                bar.setTitle(AdventureComponentConverter.toNative(msg.title()));
            bar.setProgress(msg.progress());
            bar.setTitle(AdventureComponentConverter.toNative(msg.title()));
            bar.setSubTitle(AdventureComponentConverter.toNative(msg.subTitle()));
            bar.setColor(msg.hex());
            bar.setImage(msg.image());
        }
    }

    public static void removeSSBossBar(RemoveSSBossBar msg, CustomPayloadEvent.Context ctx) {
        SimpleMusicSliderBossBar bar = activeBars.get(msg.id());
        if (bar != null) {
            bar.setVisible(false); // Let it slide out
        }
        activeBars.remove(msg.id());
    }
}
