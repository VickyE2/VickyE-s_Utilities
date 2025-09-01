package org.vicky.forge.forgeplatform.useables;

import net.kyori.adventure.text.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.network.PacketHandler;
import org.vicky.forge.network.packets.CreateSSBossBar;
import org.vicky.forge.network.packets.RemoveSSBossBar;
import org.vicky.forge.network.packets.UpdateSSBossBar;
import org.vicky.platform.IColor;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.defaults.BossBarOverlay;
import org.vicky.platform.utils.BossBarDescriptor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MusicScreenSlidingBossBar implements PlatformBossBar {
    private final UUID id = UUID.randomUUID();
    private @Nullable ResourceLocation image;
    private Component subTitle;
    private Component title;
    private float progress;
    private String hex;
    private final Set<ForgePlatformPlayer> viewers = new HashSet<>();
    private BossBarDescriptor descriptor;

    public MusicScreenSlidingBossBar(BossBarDescriptor descriptor, @Nullable ResourceLocation image) {
        this.descriptor = descriptor;
        this.title = descriptor.title;
        this.subTitle = descriptor.subTitle;
        this.hex = descriptor.color.toHex();
        this.progress = descriptor.progress;
        this.image = image;
    }

    @Override
    public void setTitle(Component title) {
        this.title = title;
        updateAll(); // Send packet
    }

    @Override
    public void setProgress(Float progress) {
        this.progress = progress;
        updateAll(); // Send packet
    }

    @Override
    public void setVisible(Boolean visible, PlatformPlayer viewer) {
        if (!(viewer instanceof ForgePlatformPlayer player)) return;
        if (visible) {
            viewers.add(player);
            PacketHandler.sendToClient(player.getHandle(), new CreateSSBossBar(id, title, subTitle, progress, hex, image));
        }
        else {
            var output = viewers.remove(player);
            if (output) {
                PacketHandler.sendToClient(player.getHandle(), new RemoveSSBossBar(id));

            }
        }
    }

    @Override
    public boolean isVisible(PlatformPlayer player) {
        if (!(player instanceof ForgePlatformPlayer platformPlayer)) return false;
        return viewers.contains(platformPlayer);
    }

    @Override
    public void setColor(IColor color) {
        this.hex = color.toHex();
    }

    @Override
    public void setOverlay(BossBarOverlay overlay) {
        /**/
    }

    @Override
    public void addViewer(PlatformPlayer viewer) {
        setVisible(true, viewer);
    }

    @Override
    public void removeViewer(PlatformPlayer viewer) {
        setVisible(false, viewer);
    }

    @Override
    public void hideAll() {
        viewers.forEach(v -> {
            PacketHandler.sendToClient(v.getHandle(), new RemoveSSBossBar(id));
        });
    }

    @Override
    public BossBarDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void setDescriptor(BossBarDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public void updateFromDescriptor() {
        // this.title = descriptor.title;
        this.hex = descriptor.color.toHex();
        // this.subTitle = descriptor.subTitle;
        this.progress = descriptor.progress;
        // this.image = new ResourceLocation((String) descriptor.getInformation().getOrDefault("icon", "minecraft:dirt"));
        updateAll();
    }

    private void updateAll() {
        for (PlatformPlayer viewer : viewers) {
            if (!(viewer instanceof ForgePlatformPlayer player)) return;
            PacketHandler.sendToClient(player.getHandle(), new UpdateSSBossBar(id, title, subTitle, progress, hex, image));
        }
    }
}
