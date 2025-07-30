package org.vicky.forgeplatform.useables;

import net.kyori.adventure.text.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.vicky.network.PacketHandler;
import org.vicky.network.packets.CreateSSBossBar;
import org.vicky.network.packets.RemoveSSBossBar;
import org.vicky.network.packets.UpdateSSBossBar;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.defaults.BossBarColor;
import org.vicky.platform.defaults.BossBarOverlay;

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
    private final Set<PlatformPlayer> viewers = new HashSet<>();

    public MusicScreenSlidingBossBar(Component title, Component subTitle, float progress, String color, @Nullable ResourceLocation image) {
        this.title = title;
        this.subTitle = subTitle;
        this.hex = color;
        this.progress = progress;
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

    public void setHex(String hex) {
        this.hex = hex;
        updateAll();
    }

    public void setImage(@Nullable ResourceLocation image) {
        this.image = image;
        updateAll();
    }

    public void setSubTitle(Component subTitle) {
        this.subTitle = subTitle;
        updateAll();
    }

    @Override
    public void setVisible(Boolean visible, PlatformPlayer viewer) {
        if (!(viewer instanceof ForgePlatformPlayer player)) return;
        if (visible) {
            viewers.add(player);
            PacketHandler.sendToClient(new CreateSSBossBar(id, title, subTitle, progress, hex, image), player.getHandle());
        }
        else {
            var output = viewers.remove(player);
            if (output) {
                PacketHandler.sendToClient(new RemoveSSBossBar(id), player.getHandle());

            }
        }
    }

    @Override
    public void setColor(BossBarColor color) {
        /**/
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

    }

    private void updateAll() {
        for (PlatformPlayer viewer : viewers) {
            if (!(viewer instanceof ForgePlatformPlayer player)) return;
            PacketHandler.sendToClient(new UpdateSSBossBar(id, title, subTitle, progress, hex, image), player.getHandle());
        }
    }

    public Component getTitle() {
        return title;
    }

    public float getProgress() {
        return progress;
    }

    public UUID getId() {
        return id;
    }

    public Set<PlatformPlayer> getViewers() {
        return new HashSet<>(viewers);
    }

    public Component getSubTitle() {
        return subTitle;
    }

    public @Nullable ResourceLocation getImage() {
        return image;
    }

    /**
     * Has no #
     * @return
     */
    public String getHexColor() {
        return hex.replace("#", "");
    }
}
