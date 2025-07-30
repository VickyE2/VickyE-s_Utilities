package org.vicky.forgeplatform.useables;

import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.PlatformPlayer;
import org.vicky.forgeplatform.adventure.AdventureComponentConverter;
import org.vicky.platform.defaults.BossBarColor;
import org.vicky.platform.defaults.BossBarOverlay;

import java.util.UUID;

public class ForgeDefaultPlatformBossBar implements PlatformBossBar {
    private final UUID uuid = UUID.randomUUID();
    private final BossEvent bossEvent;
    private final ClientboundBossEventPacket addPacket;
    private final ClientboundBossEventPacket removePacket;

    public ForgeDefaultPlatformBossBar(Component title, float progress, BossBarColor color, BossBarOverlay overlay) {
        this.bossEvent = new BossEvent(
                uuid,
                AdventureComponentConverter.toNative(title),
                toColor(color),
                toOverlay(overlay)
        ){

        };
        bossEvent.setProgress(progress);
        bossEvent.setDarkenScreen(false);
        bossEvent.setPlayBossMusic(false);
        bossEvent.setCreateWorldFog(false);

        addPacket = ClientboundBossEventPacket.createAddPacket(bossEvent);
        removePacket = ClientboundBossEventPacket.createRemovePacket(uuid);
    }

    @Override
    public void setTitle(Component title) {
        bossEvent.setName(AdventureComponentConverter.toNative(title));
    }

    @Override
    public void setProgress(Float progress) {
        bossEvent.setProgress(progress);
    }

    @Override
    public void setVisible(Boolean visible, PlatformPlayer player) {
        if (!(player instanceof ForgePlatformPlayer forgePlayer)) return;
        if (visible)
            forgePlayer.getHandle().connection.send(addPacket);
        else
            forgePlayer.getHandle().connection.send(removePacket);
    }

    @Override
    public void setColor(BossBarColor color) {
        bossEvent.setColor(toColor(color));
    }

    @Override
    public void setOverlay(BossBarOverlay overlay) {
        bossEvent.setOverlay(toOverlay(overlay));
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

    }

    // Helpers to convert enums
    private BossEvent.BossBarColor toColor(BossBarColor color) {
        return switch (color) {
            case RED -> BossEvent.BossBarColor.RED;
            case GREEN -> BossEvent.BossBarColor.GREEN;
            case BLUE -> BossEvent.BossBarColor.BLUE;
            case YELLOW -> BossEvent.BossBarColor.YELLOW;
            case PURPLE -> BossEvent.BossBarColor.PURPLE;
            case WHITE -> BossEvent.BossBarColor.WHITE;
            case PINK -> BossEvent.BossBarColor.PINK;
        };
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
