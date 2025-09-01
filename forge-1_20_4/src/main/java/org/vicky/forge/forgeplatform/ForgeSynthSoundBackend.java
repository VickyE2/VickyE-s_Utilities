package org.vicky.forge.forgeplatform;

import org.jetbrains.annotations.NotNull;
import org.vicky.forge.client.audio.InstrumentMapper;
import org.vicky.forge.forgeplatform.useables.ForgePlatformPlayer;
import org.vicky.forge.network.PacketHandler;
import org.vicky.forge.network.registeredpackets.NoteOffPacket;
import org.vicky.forge.network.registeredpackets.NoteOnPacket;
import org.vicky.music.utils.MusicEvent;
import org.vicky.musicPlayer.DefaultAdsrMapper;
import org.vicky.musicPlayer.PlatformSoundBackend;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.utils.SoundCategory;

import java.util.Optional;
import java.util.UUID;

public class ForgeSynthSoundBackend implements PlatformSoundBackend {

    @Override
    public UUID playNote(@NotNull PlatformPlayer player, @NotNull MusicEvent event) {
        System.out.println("Trying to play note");

        ForgePlatformPlayer serverPlayer = asServerPlayer(player);
        if (serverPlayer == null) {
            return null;
        }

        var adsr = DefaultAdsrMapper.INSTANCE.map(event);
        UUID uid = event.noteId();

        // map wave choice
        int[] midiId = event.sound() != null ? InstrumentMapper.getBankAndProgram(event.sound()) : new int[]{0, 0};

        // convert pitch -> frequency
        float freqHz = 440.0f * event.pitch();

        float vibratoRate = 5.0f;
        float vibratoDepth = 5.0f;

        NoteOnPacket noteOn = new NoteOnPacket(
                event.sound() != null ? event.sound().name() : "inst",
                midiId,
                freqHz,
                event.volume(),
                adsr.getA(),
                adsr.getD(),
                adsr.getS(),
                adsr.getR(),
                adsr.getSustainLoop(),
                vibratoRate,
                vibratoDepth,
                uid
        );

        PacketHandler.sendToClient(serverPlayer.getHandle(), noteOn);
        return uid;
    }

    @Override
    public void stopNote(PlatformPlayer player, UUID uid) {
        ForgePlatformPlayer serverPlayer = asServerPlayer(player);
        if (serverPlayer == null) {
            return;
        }

        NoteOffPacket off = new NoteOffPacket(uid);
        PacketHandler.sendToClient(serverPlayer.getHandle(), off);
    }

    @Override
    public void playNamed(PlatformPlayer player, String soundName, SoundCategory category, float volume, float pitch) {
        player.playSound(
                player.getLocation(),
                soundName,
                category != null ? category : "master",
                volume,
                pitch
        );
    }

    private ForgePlatformPlayer asServerPlayer(PlatformPlayer player) {
        Optional<PlatformPlayer> opt = PlatformPlugin.getPlayer(player.uniqueId());
        if (opt.isPresent() && opt.get() instanceof ForgePlatformPlayer) {
            return (ForgePlatformPlayer) opt.get();
        }
        return null;
    }
}