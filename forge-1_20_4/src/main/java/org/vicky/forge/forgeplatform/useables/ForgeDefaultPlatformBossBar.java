/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.forgeplatform.useables;

import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import org.vicky.forge.forgeplatform.adventure.AdventureComponentConverter;
import org.vicky.platform.IColor;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.defaults.BossBarOverlay;
import org.vicky.platform.utils.BossBarDescriptor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.vicky.VickyUtilitiesForge.server;

public class ForgeDefaultPlatformBossBar implements PlatformBossBar {
    private final UUID uuid = UUID.randomUUID();
    private final BossEvent bossEvent;
    private final ClientboundBossEventPacket removePacket;
    private final Set<UUID> viewers = new HashSet<>();
    private BossBarDescriptor descriptor;

    public ForgeDefaultPlatformBossBar(BossBarDescriptor descriptor) {
        this.descriptor = descriptor;
        this.bossEvent = new BossEvent(uuid, AdventureComponentConverter.toNative(descriptor.title),
                toColor(descriptor.color), toOverlay(descriptor.overlay)) {
        };
        bossEvent.setProgress(descriptor.progress);
        bossEvent.setDarkenScreen(false);
        bossEvent.setPlayBossMusic(false);
        bossEvent.setCreateWorldFog(false);
        removePacket = ClientboundBossEventPacket.createRemovePacket(uuid);
    }

    @Override
    public void setTitle(Component title) {
        bossEvent.setName(AdventureComponentConverter.toNative(title));
        sendUpdate(ClientboundBossEventPacket.createUpdateNamePacket(bossEvent));
    }

    @Override
    public void setProgress(Float progress) {
        bossEvent.setProgress(progress);
        sendUpdate(ClientboundBossEventPacket.createUpdateProgressPacket(bossEvent));
    }

    @Override
    public void setVisible(Boolean visible, PlatformPlayer player) {
        if (!(player instanceof ForgePlatformPlayer forgePlayer))
            return;
        if (visible) {
            viewers.add(player.uniqueId());
            forgePlayer.getHandle().connection.send(ClientboundBossEventPacket.createAddPacket(bossEvent));
        } else {
            viewers.remove(player.uniqueId());
            forgePlayer.getHandle().connection.send(removePacket);
        }
    }

    @Override
    public boolean isVisible(PlatformPlayer player) {
        return viewers.contains(player.uniqueId());
    }

    @Override
    public void setColor(IColor color) {
        bossEvent.setColor(toColor(color));
        sendUpdate(ClientboundBossEventPacket.createUpdateStylePacket(bossEvent));
    }

    @Override
    public void setOverlay(BossBarOverlay overlay) {
        bossEvent.setOverlay(toOverlay(overlay));
        sendUpdate(ClientboundBossEventPacket.createUpdateStylePacket(bossEvent));
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
        viewers.forEach(v -> setVisible(false, PlatformPlugin.getPlayer(v).get()));
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
        bossEvent.setName(AdventureComponentConverter.toNative(descriptor.title));
        bossEvent.setColor(toColor(descriptor.color));
        bossEvent.setOverlay(toOverlay(descriptor.overlay));
        bossEvent.setProgress(descriptor.getProgress());
        hideAll();
        sendUpdate(ClientboundBossEventPacket.createAddPacket(bossEvent));
    }

    // Send an update packet to all viewers
    private void sendUpdate(ClientboundBossEventPacket packet) {
        for (UUID viewerId : viewers) {
            ServerPlayer sp = server.getPlayerList().getPlayer(viewerId);
            if (sp != null)
                sp.connection.send(packet);
        }
    }

    // Helpers to convert enums
    private BossEvent.BossBarColor toColor(IColor color) {
        // Define the fixed RGB values of the BossBar colors
        record NamedColor(BossEvent.BossBarColor barColor, float r, float g, float b) {
        }

        NamedColor[] bossColors = new NamedColor[]{new NamedColor(BossEvent.BossBarColor.RED, 1.0f, 0.0f, 0.0f),
                new NamedColor(BossEvent.BossBarColor.GREEN, 0.0f, 1.0f, 0.0f),
                new NamedColor(BossEvent.BossBarColor.BLUE, 0.0f, 0.0f, 1.0f),
                new NamedColor(BossEvent.BossBarColor.YELLOW, 1.0f, 1.0f, 0.0f),
                new NamedColor(BossEvent.BossBarColor.PURPLE, 0.5f, 0.0f, 0.5f),
                new NamedColor(BossEvent.BossBarColor.WHITE, 1.0f, 1.0f, 1.0f),
                new NamedColor(BossEvent.BossBarColor.PINK, 1.0f, 0.0f, 1.0f)};

        BossEvent.BossBarColor closest = BossEvent.BossBarColor.WHITE;
        double bestDistance = Double.MAX_VALUE;

        for (NamedColor nc : bossColors) {
            double dr = color.getRed() - nc.r;
            double dg = color.getGreen() - nc.g;
            double db = color.getBlue() - nc.b;
            double dist = dr * dr + dg * dg + db * db; // Euclidean distance in RGB space

            if (dist < bestDistance) {
                bestDistance = dist;
                closest = nc.barColor;
            }
        }

        return closest;
    }

    private BossEvent.BossBarOverlay toOverlay(BossBarOverlay overlay) {
        return switch (overlay) {
            case PROGRESS -> BossEvent.BossBarOverlay.PROGRESS;
            case NOTCHED_6 -> BossEvent.BossBarOverlay.NOTCHED_6;
            case NOTCHED_10 -> BossEvent.BossBarOverlay.NOTCHED_10;
            case NOTCHED_12 -> BossEvent.BossBarOverlay.NOTCHED_12;
            case NOTCHED_20 -> BossEvent.BossBarOverlay.NOTCHED_20;
        };
    }
}
