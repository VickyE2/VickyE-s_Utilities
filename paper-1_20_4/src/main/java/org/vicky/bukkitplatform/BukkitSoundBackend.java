/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.music.utils.MusicEvent;
import org.vicky.musicPlayer.MusicPlayer;
import org.vicky.musicPlayer.PlatformSoundBackend;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.utils.SoundCategory;
import org.vicky.vicky_utils;

public class BukkitSoundBackend implements PlatformSoundBackend {
    public static final double NOTE_DURATION = 0.33;

    @Nullable
    @Override
    public Integer playNote(@NotNull PlatformPlayer player, @NotNull MusicEvent event) {
        // Use the existing system: resolve the sound name and call
        // PlatformPlayer.playSound
        var name = MusicPlayer.INSTANCE.resolveCustomSound(event);
        player.playSound(player.getLocation(), name, event.category(), event.volume(), 0.5f);
        return null; // Bukkit playSound has no uid
    }

    @Override
    public void playNoteFor(@NotNull PlatformPlayer player, @NotNull MusicEvent event, double timeSeconds) {
        // Convert seconds -> ticks (Minecraft: 20 ticks = 1 second)
        long totalTicks = Math.max(1L, Math.round(timeSeconds * 20.0));
        double noteDurationTicks = NOTE_DURATION * 20.0; // keep using your existing NOTE_DURATION (ticks)
        double sustainTicks = totalTicks - (noteDurationTicks * 2.0);
        int sustainCount = (sustainTicks > 0) ? (int) ((sustainTicks + noteDurationTicks - 1) / noteDurationTicks) : 0;

        // Resolve base name and strip trailing _1/_2/_3 if present
        String name = MusicPlayer.INSTANCE.resolveCustomSound(event);
        String baseName = name.replaceAll("_[123]$", ""); // remove only trailing part suffix

        // Compute pitch multiplier from MIDI note if possible. Default to 1.0f
        float pitchMul = 0.5f;

        // Immediately play the attack part (_1)
        player.playSound(player.getLocation(), baseName + "_1", event.category(), event.volume(), pitchMul);

        // Schedule sustain parts (_2)
        for (int i = 1; i <= sustainCount; i++) {
            final long delayTicks = (long) (i * noteDurationTicks); // safe final capture for lambda
            Bukkit.getScheduler().scheduleSyncDelayedTask(vicky_utils.plugin, () -> player
                            .playSound(player.getLocation(), baseName + "_2", event.category(), event.volume(), pitchMul),
                    delayTicks);
        }

        // Schedule final release part (_3). If sustainCount == 0 this runs at
        // NOTE_DURATION ticks.
        final long releaseDelay = (long) ((sustainCount + 1L) * noteDurationTicks);
        Bukkit.getScheduler().scheduleSyncDelayedTask(vicky_utils.plugin, () -> player.playSound(player.getLocation(),
                baseName + "_3", event.category(), event.volume(), pitchMul), releaseDelay);
    }

    @Override
    public void stopNote(@NotNull PlatformPlayer platformPlayer, @Nullable Integer integer) {

    }

    @Override
    public void playNamed(@NotNull PlatformPlayer player, @NotNull String soundName, @Nullable SoundCategory category,
                          float volume, int pitch) {
        player.playSound(player.getLocation(), soundName, category != null ? category : "master", volume, 0.5f);
    }
}
