/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.network.registeredpackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.vicky.forge.network.Packetable;

public record PauseAnimationPacket(int entityId) implements Packetable {

	public static void encode(PauseAnimationPacket pkt, FriendlyByteBuf buf) {
		buf.writeInt(pkt.entityId);
	}

	public static PauseAnimationPacket decode(FriendlyByteBuf buf) {
		return new PauseAnimationPacket(buf.readInt());
	}

	public static void handle(PauseAnimationPacket pkt, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			// delegate to client adapter
			org.vicky.forge.client.animation.GeckoLibAdapterManager.pauseAnimationClient(pkt.entityId);
		});
		ctx.setPacketHandled(true);
	}
}
