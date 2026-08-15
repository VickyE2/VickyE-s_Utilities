/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.network.registeredpackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.vicky.forge.client.ClientInspectManager;
import org.vicky.forge.network.Packetable;

import java.util.UUID;

public record UpdateInspectStatePacket(UUID player, boolean isInspecting) implements Packetable {

	public static void encode(UpdateInspectStatePacket pkt, FriendlyByteBuf buf) {
		buf.writeUUID(pkt.player);
		buf.writeBoolean(pkt.isInspecting);
	}

	public static UpdateInspectStatePacket decode(FriendlyByteBuf buf) {
		return new UpdateInspectStatePacket(buf.readUUID(), buf.readBoolean());
	}

	public static void handle(UpdateInspectStatePacket pkt, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			ClientInspectManager.setInspecting(pkt.player, pkt.isInspecting);
		});
		ctx.setPacketHandled(true);
	}
}
