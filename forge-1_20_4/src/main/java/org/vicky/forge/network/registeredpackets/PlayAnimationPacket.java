/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.network.registeredpackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.vicky.forge.network.Packetable;
import org.vicky.platform.items.Animation;

public record PlayAnimationPacket(int entityId, Animation animation) implements Packetable {

	public static void encode(PlayAnimationPacket pkt, FriendlyByteBuf buf) {
		buf.writeInt(pkt.entityId);
		buf.writeUtf(pkt.animation.getKey(), 32767);
		buf.writeBoolean(pkt.animation.getLoop());
		buf.writeInt(pkt.animation.getBlendTime());
		buf.writeBoolean(pkt.animation.getInterruptable());
		buf.writeInt(pkt.animation.getPriority());
	}

	public static PlayAnimationPacket decode(FriendlyByteBuf buf) {
		return new PlayAnimationPacket(buf.readInt(),
				new Animation(buf.readUtf(), buf.readBoolean(), buf.readInt(), buf.readBoolean(), buf.readInt()));
	}

	public static void handle(PlayAnimationPacket pkt, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			// delegate to client adapter
			org.vicky.forge.client.animation.GeckoLibAdapterManager.playAnimationClient(pkt.entityId, pkt.animation);
		});
		ctx.setPacketHandled(true);
	}
}
