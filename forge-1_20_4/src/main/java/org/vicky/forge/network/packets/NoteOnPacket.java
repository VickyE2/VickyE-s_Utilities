package org.vicky.forge.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.vicky.forge.client.audio.ClientSynthManager;

import java.util.UUID;

/**
 * @param instId       short instrument id, optional use
 * @param wave         0=sine,1=square,2=saw,3=triangle,4=noise
 * @param freqHz       frequency in Hz (or compute from midi)
 * @param velocity     0..1
 * @param release      ADSR seconds (release used on noteOff)
 * @param sustainLoop  if true, client will hold sustain until NoteOff
 * @param vibratoDepth rate in Hz, depth in cents
 * @param uid          unique note id to match noteOff
 */
public record NoteOnPacket(String instId, byte wave, float freqHz, float velocity, float attack, float decay,
                           float sustain, float release, boolean sustainLoop, float vibratoRate, float vibratoDepth,
                           UUID uid) {

    public static void encode(NoteOnPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.instId, 64);
        buf.writeByte(pkt.wave);
        buf.writeFloat(pkt.freqHz);
        buf.writeFloat(pkt.velocity);
        buf.writeFloat(pkt.attack);
        buf.writeFloat(pkt.decay);
        buf.writeFloat(pkt.sustain);
        buf.writeFloat(pkt.release);
        buf.writeBoolean(pkt.sustainLoop);
        buf.writeFloat(pkt.vibratoRate);
        buf.writeFloat(pkt.vibratoDepth);
        buf.writeUUID(pkt.uid);
    }

    public static NoteOnPacket decode(FriendlyByteBuf buf) {
        String inst = buf.readUtf(64);
        byte wave = buf.readByte();
        float f = buf.readFloat();
        float vel = buf.readFloat();
        float a = buf.readFloat();
        float d = buf.readFloat();
        float s = buf.readFloat();
        float r = buf.readFloat();
        boolean sl = buf.readBoolean();
        float vr = buf.readFloat();
        float vd = buf.readFloat();
        var uid = buf.readUUID();
        return new NoteOnPacket(inst, wave, f, vel, a, d, s, r, sl, vr, vd, uid);
    }

    public static void handle(NoteOnPacket pkt, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            // Run on client main thread
            ClientSynthManager.get().noteOn(pkt);
        });
        ctx.setPacketHandled(true);
    }
}
