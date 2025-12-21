/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.forgeplatform;

import org.jetbrains.annotations.NotNull;
import org.vicky.forge.client.audio.InstrumentMapper;
import org.vicky.forge.forgeplatform.useables.ForgePlatformPlayer;
import org.vicky.forge.network.PacketHandler;
import org.vicky.forge.network.registeredpackets.NoteOffPacket;
import org.vicky.forge.network.registeredpackets.NoteOnPacket;
import org.vicky.music.utils.MusicEvent;
import org.vicky.musicPlayer.ADSR;
import org.vicky.musicPlayer.DefaultAdsrMapper;
import org.vicky.musicPlayer.PlatformSoundBackend;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.utils.SoundCategory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ForgeSynthSoundBackend implements PlatformSoundBackend {
    private static final Set<Integer> keys = new HashSet<>();
    public static final ForgeSynthSoundBackend INSTANCE =
            new ForgeSynthSoundBackend();

    private ForgeSynthSoundBackend() {}

    private static @NotNull NoteOnPacket getNoteOnPacket(@NotNull MusicEvent event, ADSR adsr, Integer uid) {
        int[] midiId = event.sound() != null ? InstrumentMapper.getBankAndProgram(event.sound()) : new int[]{0, 0};

        // convert pitch -> frequency
        Integer midiNote = event.pitch();

        float vibratoRate = 5.0f;
        float vibratoDepth = 5.0f;

        NoteOnPacket noteOn = new NoteOnPacket(event.sound() != null ? event.sound().name() : "inst", midiId, midiNote,
                event.volume(), adsr.getAttack(), adsr.getDecay(), adsr.getSustain(), adsr.getRelease(),
                adsr.getSustainLoop(), vibratoRate, vibratoDepth, uid);
        return noteOn;
    }

    @Override
    public Integer playNote(@NotNull PlatformPlayer player, @NotNull MusicEvent event) {
        // System.out.println("Trying to play note");
        ForgePlatformPlayer serverPlayer = asServerPlayer(player);
        if (serverPlayer != null) {
            var adsr = DefaultAdsrMapper.INSTANCE.map(event);
            Integer uid = event.noteId();
            if (keys.contains(uid)) {
                return uid;
            } else {
                keys.add(uid);
            }

            NoteOnPacket noteOn = getNoteOnPacket(event, adsr, uid);

            PacketHandler.sendToClient(serverPlayer.getHandle(), noteOn);
            return uid;
        }

        return null;
    }

    @Override
    public void stopNote(@NotNull PlatformPlayer player, Integer uid) {
        ForgePlatformPlayer serverPlayer = asServerPlayer(player);
        if (serverPlayer != null) {
            keys.remove(uid);
            NoteOffPacket off = new NoteOffPacket(uid);
            PacketHandler.sendToClient(serverPlayer.getHandle(), off);
        }
    }

    @Override
    public void playNamed(PlatformPlayer player, @NotNull String soundName, SoundCategory category, float volume,
                          int pitch) {
        player.playSound(player.getLocation(), soundName, category != null ? category : "master", volume,
                (float) (440.0 * Math.pow(2.0, (pitch - 69) / 12.0)));
    }

    @Override
    public void playNoteFor(@NotNull PlatformPlayer player, @NotNull MusicEvent event, double durationSeconds) {
        // Step 1: Play the note immediately
        Integer uid = playNote(player, event);
        if (uid == null) {
            return;
        }

        // Step 2: Schedule a stop after the given duration
        long delayMillis = (long) (durationSeconds * 1000);

        // ⚠️ Replace this with Forge scheduler if available
        new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                stopNote(player, uid);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private ForgePlatformPlayer asServerPlayer(PlatformPlayer player) {
        Optional<PlatformPlayer> opt = PlatformPlugin.getPlayer(player.uniqueId());
        if (opt.isPresent() && opt.get() instanceof ForgePlatformPlayer) {
            return (ForgePlatformPlayer) opt.get();
        }
        return null;
    }
}