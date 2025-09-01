package org.vicky.forge.network.registeredpackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.vicky.forge.client.audio.ClientSynthManager;
import org.vicky.forge.network.SynthPacket;

import java.util.UUID;

/**
 * @param instId       short instrument id, optional use
 * @param midiId       The key in the midi
 * @param midiNote     THe Pitch
 * @param velocity     0..1
 * @param release      ADSR seconds (release used on noteOff)
 * @param sustainLoop  if true, client will hold sustain until NoteOff
 * @param vibratoDepth rate in Hz, depth in cents
 * @param uid          unique note id to match noteOff
 */
public record NoteOnPacket(String instId, int[] midiId, Integer midiNote, float velocity, float attack, float decay,
                           float sustain, float release, boolean sustainLoop, float vibratoRate, float vibratoDepth,
                           UUID uid) implements SynthPacket {

    public static void encode(NoteOnPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.instId, 64);
        buf.writeVarIntArray(pkt.midiId);
        buf.writeVarInt(pkt.midiNote);
        buf.writeFloat(pkt.velocity);
        buf.writeFloat(pkt.attack);
        buf.writeFloat(pkt.decay);
        buf.writeFloat(pkt.sustain);
        buf.writeFloat(pkt.release);
        buf.writeBoolean(pkt.sustainLoop);
        buf.writeFloat(pkt.vibratoRate);
        buf.writeFloat(pkt.vibratoDepth);
        if (pkt.uid != null) {
            buf.writeBoolean(true);
            buf.writeUUID(pkt.uid);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static NoteOnPacket decode(FriendlyByteBuf buf) {
        String inst = buf.readUtf(64);
        int[] midiId = buf.readVarIntArray();
        Integer midiNote = buf.readVarInt();
        float vel = buf.readFloat();
        float a = buf.readFloat();
        float d = buf.readFloat();
        float s = buf.readFloat();
        float r = buf.readFloat();
        boolean sl = buf.readBoolean();
        float vr = buf.readFloat();
        float vd = buf.readFloat();
        UUID uid = null;
        if (buf.readBoolean()) {
            uid = buf.readUUID();
        }
        return new NoteOnPacket(inst, midiId, midiNote, vel, a, d, s, r, sl, vr, vd, uid);
    }

    public static void handle(NoteOnPacket pkt, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            // Run on client main thread
            // System.out.println("Yeah i got that note cuh");
            ClientSynthManager.get().noteOn(pkt);
        });
        ctx.setPacketHandled(true);
    }
}
