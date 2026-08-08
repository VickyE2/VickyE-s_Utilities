/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.network.registeredpackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.network.Packetable;

public record StopAnimationPacket(int entityId, @Nullable String animation) implements Packetable {

	public static void encode(StopAnimationPacket pkt, FriendlyByteBuf buf) {
		buf.writeInt(pkt.entityId);
		if (pkt.animation != null && !pkt.animation.isEmpty()) {
			buf.writeBoolean(true);
			buf.writeUtf(pkt.animation, 32767);
		}
		else {
			buf.writeBoolean(false);
		}
	}

	public static StopAnimationPacket decode(FriendlyByteBuf buf) {
		return new StopAnimationPacket(buf.readInt(),
				buf.readBoolean() ? buf.readUtf(32767) : null);
	}

	public static void handle(StopAnimationPacket pkt, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			// delegate to client adapter
			org.vicky.forge.client.animation.GeckoLibAdapterManager.stopAnimationClient(pkt.entityId, pkt.animation);
		});
		ctx.setPacketHandled(true);
	}
}
