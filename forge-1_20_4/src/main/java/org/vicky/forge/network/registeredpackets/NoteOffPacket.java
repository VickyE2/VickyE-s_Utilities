package org.vicky.forge.network.registeredpackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.vicky.forge.client.audio.ClientSynthManager;
import org.vicky.forge.network.SynthPacket;

import java.util.UUID;


public record NoteOffPacket(UUID uid) implements SynthPacket {

    public static void encode(NoteOffPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.uid);
    }

    public static NoteOffPacket decode(FriendlyByteBuf buf) {
        return new NoteOffPacket(buf.readUUID());
    }

    public static void handle(NoteOffPacket pkt, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            // System.out.println("Yeah i got that OFFFF note cuh");
            ClientSynthManager.get().noteOff(pkt.uid);
        });
        ctx.setPacketHandled(true);
    }
}
