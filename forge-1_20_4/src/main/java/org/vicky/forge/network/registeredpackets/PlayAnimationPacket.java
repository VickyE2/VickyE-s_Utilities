package org.vicky.forge.network.registeredpackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.vicky.forge.network.Packetable;

public record PlayAnimationPacket(int entityId, String animationKey, boolean loop) implements Packetable {

    public static void encode(PlayAnimationPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.entityId);
        buf.writeUtf(pkt.animationKey == null ? "" : pkt.animationKey, 32767);
        buf.writeBoolean(pkt.loop);
    }

    public static PlayAnimationPacket decode(FriendlyByteBuf buf) {
        int id = buf.readInt();
        String key = buf.readUtf(32767);
        if (key.isEmpty()) key = null;
        boolean loop = buf.readBoolean();
        return new PlayAnimationPacket(id, key, loop);
    }

    public static void handle(PlayAnimationPacket pkt, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            // delegate to client adapter
            org.vicky.forge.client.animation.GeckoLibAdapterManager.playAnimationClient(pkt.entityId, pkt.animationKey, pkt.loop);
        });
        ctx.setPacketHandled(true);
    }
}

