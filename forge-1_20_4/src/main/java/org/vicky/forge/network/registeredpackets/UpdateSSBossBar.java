/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.network.registeredpackets;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.VickyUtilitiesForge;
import org.vicky.forge.client.ClientIncomingPacketHandler;
import org.vicky.forge.network.Packetable;

import java.util.UUID;

public final class UpdateSSBossBar extends BossBarMessage implements Packetable {

	public UpdateSSBossBar(UUID id, Component title, Component subTitle, float progress, String hex, @Nullable ResourceLocation image) {
		super(id, title, subTitle, progress, hex, image);
	}

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

	public static void handle(UpdateSSBossBar msg, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> ClientIncomingPacketHandler.updateSSBossBar(msg, ctx)));
		ctx.setPacketHandled(true);
	}
}
