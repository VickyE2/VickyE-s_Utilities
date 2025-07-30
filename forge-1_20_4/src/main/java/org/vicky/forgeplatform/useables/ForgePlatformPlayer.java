package org.vicky.forgeplatform.useables;

import net.kyori.adventure.text.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.PlatformPlayer;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import org.vicky.forgeplatform.adventure.AdventureComponentConverter;
import org.vicky.platform.world.PlatformLocation;

public class ForgePlatformPlayer implements PlatformPlayer {

    private final ServerPlayer player;

    public ForgePlatformPlayer(ServerPlayer player) {
        this.player = player;
    }

    public static ForgePlatformPlayer adapt(ServerPlayer player) {
        return new ForgePlatformPlayer(player);
    }

    @Override
    public UUID uniqueId() {
        return player.getUUID();
    }

    @Override
    public Component name() {
        return Component.translatable(player.getName().getString());
    }

    @Override
    public void sendMessage(Component msg) {
        player.sendSystemMessage(AdventureComponentConverter.toNative(msg));
    }

    @Override
    public void sendMessage(String msg) {
        player.sendSystemMessage(AdventureComponentConverter.toNative(Component.translatable(msg)));
    }

    @Override
    public void sendComponent(Component component) {
        player.sendSystemMessage(AdventureComponentConverter.toNative(component));
    }

    @Override
    public void showBossBar(PlatformBossBar bar) {
        if (bar instanceof ForgeDefaultPlatformBossBar forgeBar) {
            forgeBar.addViewer(ForgePlatformPlayer.adapt(player));
        }
    }

    @Override
    public void hideBossBar(PlatformBossBar bar) {
        if (bar instanceof ForgeDefaultPlatformBossBar forgeBar) {
            forgeBar.removeViewer(ForgePlatformPlayer.adapt(player));
        }
    }

    @Override
    public void playSound(PlatformLocation location, String soundName, Object soundCategory, Float volume, Float pitch) {
        if (location instanceof ForgeVec3 loc) {
            ResourceLocation soundId = new ResourceLocation(soundName);
            SoundSource category = soundCategory instanceof SoundSource source ? source : SoundSource.PLAYERS;
            player.level().playSound(null, loc.getX(), loc.getY(), loc.getZ(), net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(soundId), category, volume, pitch);
        }
    }

    @Override
    public PlatformLocation getLocation() {
        Level level = player.level();
        return new ForgeVec3(
                level,
                player.getX(),
                player.getY(),
                player.getZ()
        );
    }

    public ServerPlayer getHandle() {
        return player;
    }
}