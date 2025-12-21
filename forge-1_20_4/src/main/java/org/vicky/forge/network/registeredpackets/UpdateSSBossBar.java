/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.network.registeredpackets;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;
import org.vicky.VickyUtilitiesForge;
import org.vicky.forge.client.ClientIncomingPacketHandler;
import org.vicky.forge.network.Packetable;

import java.util.UUID;

public record UpdateSSBossBar(UUID id, Component title, Component subTitle, float progress, String hex,
                              @Nullable ResourceLocation image) implements Packetable {
    public static UpdateSSBossBar decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();

        Component title = safeDeserializeComponent(buf.readUtf());

        Component subTitle = null;
        if (buf.readBoolean()) {
            subTitle = safeDeserializeComponent(buf.readUtf());
        }

        float progress = buf.readFloat();
        String hex = buf.readUtf();

        ResourceLocation image = ResourceLocation.parse("minecraft:dirt");
        if (buf.readBoolean()) {
            try {
                image = ResourceLocation.parse(buf.readUtf());
            } catch (Exception ex) {
                VickyUtilitiesForge.LOGGER.warn("Bad image resource in packet", ex);
            }
        }

        return new UpdateSSBossBar(id, title, subTitle, progress, hex, image);
    }

    private static Component safeDeserializeComponent(String json) {
        try {
            if (json == null || json.isBlank())
                return Component.empty();
            return GsonComponentSerializer.gson().deserialize(json);
        } catch (Exception ex) {
            // Decoding failed — fallback to an empty component and log
            VickyUtilitiesForge.LOGGER.warn("Failed to deserialize component from packet, using empty component", ex);
            return Component.empty();
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(id);

        // title - assume not null, but still safe
        buf.writeUtf(GsonComponentSerializer.gson().serialize(title));

        // subTitle - nullable -> write a boolean flag then the string
        if (subTitle != null) {
            buf.writeBoolean(true);
            buf.writeUtf(GsonComponentSerializer.gson().serialize(subTitle));
        } else {
            buf.writeBoolean(false);
        }

        buf.writeFloat(progress);
        buf.writeUtf(hex == null ? "" : hex);

        // image - nullable -> boolean flag then the string
        if (image != null) {
            buf.writeBoolean(true);
            buf.writeUtf(image.toString());
        } else {
            buf.writeBoolean(false);
        }
    }

    public static void handle(UpdateSSBossBar msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientIncomingPacketHandler.updateSSBossBar(msg, ctx)));
        ctx.setPacketHandled(true);
    }
}
