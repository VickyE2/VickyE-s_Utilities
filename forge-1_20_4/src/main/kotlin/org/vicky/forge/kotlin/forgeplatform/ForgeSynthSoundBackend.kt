package org.vicky.forge.kotlin.forgeplatform

// package org.vicky.musicPlayer.audio

import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.PacketDistributor
import org.vicky.forge.forgeplatform.useables.ForgePlatformPlayer
import org.vicky.forge.network.packets.NoteOffPacket
import org.vicky.forge.network.packets.NoteOnPacket
import org.vicky.music.utils.MusicEvent
import org.vicky.musicPlayer.DefaultAdsrMapper
import org.vicky.musicPlayer.PlatformSoundBackend
import org.vicky.platform.PlatformPlayer
import org.vicky.platform.utils.SoundCategory
import java.util.*

fun interface Sender {
    fun send(player: ServerPlayer, packet: Any)
}

class ForgeSynthSoundBackend(
    private val sendToPlayer: Sender // small adapter so code is testable; pass ModNetwork.CHANNEL::send wrapper
) : PlatformSoundBackend {

    override fun playNote(player: PlatformPlayer, event: MusicEvent): UUID? {
        println("Trying to play note")
        // Only valid for actual server players; PlatformPlayer must expose a way to get server player.
        val serverPlayer = player.asServerPlayer().orElse(null) ?: return null

        val adsr = DefaultAdsrMapper.map(event)
        val uid = event.noteId

        // map wave choice; choose a default if your MusicEvent doesn't include waveform
        val waveId: Byte = when (event.sound?.name?.lowercase()) {
            "piano" -> 0 // e.g., sine
            "harp" -> 0
            "synth" -> 2
            else -> 0
        }

        // convert pitch (your system uses floats as multiplicative pitch) -> frequencyHz
        val freqHz = (440.0f * event.pitch).toFloat() // if event.pitch already maps to multiplier; adjust if it's MIDI

        // vibrato defaults; you can extend MusicEvent with real fields later
        val vibratoRate = 5.0f
        val vibratoDepth = 5.0f

        // Build NoteOnPacket (use your Java class - constructor signature from earlier)
        val noteOn = NoteOnPacket(
            event.sound?.name ?: "inst",
            waveId,
            freqHz,
            event.volume,
            adsr.a,
            adsr.d,
            adsr.s,
            adsr.r,
            adsr.sustainLoop,
            vibratoRate,
            vibratoDepth,
            uid
        )

        // send packet to the specific player
        sendToPlayer.send(
            serverPlayer.handle,
            PacketDistributor.PLAYER.with(serverPlayer.handle) to noteOn
        ) // adapter expects (ServerPlayer, Any)
        return uid
    }

    override fun stopNote(player: PlatformPlayer, uid: UUID?) {
        val serverPlayer = player.asServerPlayer().orElse(null) ?: return
        val off = NoteOffPacket(uid)
        sendToPlayer.send(serverPlayer.handle, PacketDistributor.PLAYER.with(serverPlayer.handle) to off)
    }

    override fun playNamed(
        player: PlatformPlayer,
        soundName: String,
        category: SoundCategory?,
        volume: Float,
        pitch: Float
    ) {
        // If you want synth backend also to support playing a named sound via playSound:
        val serverPlayer = player.asServerPlayer().orElse(null) ?: return
        player.playSound(player.location, soundName, category ?: "master", volume, pitch)
    }
}

private fun PlatformPlayer.asServerPlayer(): Optional<ForgePlatformPlayer> {
    return if (this is ForgePlatformPlayer) {
        Optional.of(this)
    } else {
        Optional.empty()
    }
}
