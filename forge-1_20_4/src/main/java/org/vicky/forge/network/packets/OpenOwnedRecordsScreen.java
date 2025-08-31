package org.vicky.forge.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import org.vicky.forge.client.ClientIncomingPacketHandler;

import java.util.ArrayList;
import java.util.List;

public record OpenOwnedRecordsScreen(List<String> songs) {
    public static OpenOwnedRecordsScreen decode(FriendlyByteBuf buf) {
        int size = Math.min(buf.readVarInt(), 1000); // prevent abuse
        var songs = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            songs.add(buf.readUtf(256));
        }
        return new OpenOwnedRecordsScreen(songs);
    }

    public static void handle(OpenOwnedRecordsScreen msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            // System.out.println("Yeah i received the packet... so what?");
            ClientIncomingPacketHandler.proceedWithOpeningScoreScreen(msg);
        }));
        ctx.setPacketHandled(true);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(songs.size());
        for (var song : songs) {
            buf.writeUtf(song, 256); // reasonable cap
        }
    }
}
