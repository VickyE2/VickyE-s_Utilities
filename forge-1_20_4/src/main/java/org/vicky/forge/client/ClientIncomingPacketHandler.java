package org.vicky.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.vicky.forge.client.screen.SimpleMusicSliderBossBar;
import org.vicky.forge.client.screen.SongLibraryScreen;
import org.vicky.forge.forgeplatform.forgeplatform.adventure.AdventureComponentConverter;
import org.vicky.forge.network.packets.CreateSSBossBar;
import org.vicky.forge.network.packets.OpenOwnedRecordsScreen;
import org.vicky.forge.network.packets.RemoveSSBossBar;
import org.vicky.forge.network.packets.UpdateSSBossBar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientIncomingPacketHandler {
    static final Map<UUID, SimpleMusicSliderBossBar> activeBars = new HashMap<>();


    public static void proceedWithSSBossBar(CreateSSBossBar msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            SimpleMusicSliderBossBar bar = new SimpleMusicSliderBossBar();
            bar.setProgress(msg.progress());
            bar.setTitle(AdventureComponentConverter.toNative(msg.title()));
            bar.setSubTitle(AdventureComponentConverter.toNative(msg.subTitle()));
            bar.setColor(msg.hex());
            bar.setImage(msg.image());
            activeBars.put(msg.id(), bar);
        });
    }

    public static void proceedWithOpeningScoreScreen(OpenOwnedRecordsScreen msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            var screen = new SongLibraryScreen(msg.songs());
            Minecraft.getInstance().setScreen(screen);
        });
    }

    public static void updateSSBossBar(UpdateSSBossBar msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            SimpleMusicSliderBossBar bar = activeBars.get(msg.id());
            if (bar != null) {
                if (msg.title() != null) bar.setTitle(AdventureComponentConverter.toNative(msg.title()));
                bar.setProgress(msg.progress());
                bar.setTitle(AdventureComponentConverter.toNative(msg.title()));
                bar.setSubTitle(AdventureComponentConverter.toNative(msg.subTitle()));
                bar.setColor(msg.hex());
                bar.setImage(msg.image());
            }
        });
    }

    public static void removeSSBossBar(RemoveSSBossBar msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            SimpleMusicSliderBossBar bar = activeBars.get(msg.id());
            if (bar != null) {
                bar.setVisible(false); // Let it slide out
            }
            activeBars.remove(msg.id());
        });
    }
}
