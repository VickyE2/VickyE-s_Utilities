package org.vicky.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import org.vicky.client.ClientIncomingPacketHandler;

import java.util.UUID;

public record RemoveSSBossBar(UUID id) {
    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeUUID(id);
    }

    public static RemoveSSBossBar decode(FriendlyByteBuf friendlyByteBuf) {
        return new RemoveSSBossBar(
            friendlyByteBuf.readUUID()
        );
    }

    public static void handle(RemoveSSBossBar msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientIncomingPacketHandler.removeSSBossBar(msg, ctx)));
        ctx.setPacketHandled(true);
    }
}
