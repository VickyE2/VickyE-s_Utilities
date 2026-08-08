/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.network.registeredpackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.vicky.forge.network.Packetable;

public record ResumeAnimationPacket(int entityId) implements Packetable {

	public static void encode(ResumeAnimationPacket pkt, FriendlyByteBuf buf) {
		buf.writeInt(pkt.entityId);
	}

	public static ResumeAnimationPacket decode(FriendlyByteBuf buf) {
		return new ResumeAnimationPacket(buf.readInt());
	}

	public static void handle(ResumeAnimationPacket pkt, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			// delegate to client adapter
			org.vicky.forge.client.animation.GeckoLibAdapterManager.resumeAnimationClient(pkt.entityId);
		});
		ctx.setPacketHandled(true);
	}
}
