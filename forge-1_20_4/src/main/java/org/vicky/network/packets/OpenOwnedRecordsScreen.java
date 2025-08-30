package org.vicky.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import org.vicky.client.ClientIncomingPacketHandler;

import java.util.ArrayList;
import java.util.List;

public record OpenOwnedRecordsScreen(List<String> songs) {
    public static OpenOwnedRecordsScreen decode(FriendlyByteBuf friendlyByteBuf) {
        var songs = new ArrayList<String>();
        var size = friendlyByteBuf.readInt();
        for (var i = 0; i < size; i++) {
            songs.add(friendlyByteBuf.readUtf());
        }
        return new OpenOwnedRecordsScreen(songs);
    }

    public static void handle(OpenOwnedRecordsScreen msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientIncomingPacketHandler.proceedWithOpeningScoreScreen(msg, ctx)));
        ctx.setPacketHandled(true);
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(songs.size());
        for (var song : songs) {
            friendlyByteBuf.writeUtf(song);
        }
    }
}
