package org.vicky.forge.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.vicky.forge.client.audio.ClientSynthManager;

import java.util.UUID;


public record NoteOffPacket(UUID uid) {

    public static void encode(NoteOffPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.uid);
    }

    public static NoteOffPacket decode(FriendlyByteBuf buf) {
        return new NoteOffPacket(buf.readUUID());
    }

    public static void handle(NoteOffPacket pkt, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> ClientSynthManager.get().noteOff(pkt.uid));
        ctx.setPacketHandled(true);
    }
}
