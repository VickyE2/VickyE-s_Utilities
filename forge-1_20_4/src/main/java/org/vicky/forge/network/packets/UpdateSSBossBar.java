package org.vicky.forge.network.packets;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.client.ClientIncomingPacketHandler;

import java.util.UUID;

public record UpdateSSBossBar(UUID id, Component title, Component subTitle, float progress, String hex, @Nullable ResourceLocation image) {
    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(id);
        friendlyByteBuf.writeUtf(GsonComponentSerializer.gson().serialize(title));
        friendlyByteBuf.writeUtf(GsonComponentSerializer.gson().serialize(subTitle));
        friendlyByteBuf.writeFloat(progress);
        friendlyByteBuf.writeUtf(hex);
        friendlyByteBuf.writeUtf(image.toString());
    }

    public static UpdateSSBossBar decode(FriendlyByteBuf friendlyByteBuf) {
        return new UpdateSSBossBar(
                friendlyByteBuf.readUUID(),
                GsonComponentSerializer.gson().deserialize(friendlyByteBuf.readUtf()),
                GsonComponentSerializer.gson().deserialize(friendlyByteBuf.readUtf()),
                friendlyByteBuf.readFloat(),
                friendlyByteBuf.readUtf(),
                new ResourceLocation(friendlyByteBuf.readUtf())
        );
    }

    public static void handle(UpdateSSBossBar msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientIncomingPacketHandler.updateSSBossBar(msg, ctx)));
        ctx.setPacketHandled(true);
    }
}
