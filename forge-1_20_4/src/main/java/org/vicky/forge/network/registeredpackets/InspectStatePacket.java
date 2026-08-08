/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.network.registeredpackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.vicky.forge.forgeplatform.item.ExtendedDescriptorItem;
import org.vicky.forge.forgeplatform.item.InspectManager;
import org.vicky.forge.network.Packetable;

public record InspectStatePacket(boolean isInspecting) implements Packetable {

	public static void encode(InspectStatePacket pkt, FriendlyByteBuf buf) {
		buf.writeBoolean(pkt.isInspecting);
	}

	public static InspectStatePacket decode(FriendlyByteBuf buf) {
		return new InspectStatePacket(buf.readBoolean());
	}

	public static void handle(InspectStatePacket pkt, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer player = ctx.getSender();
			if (player != null) {
				if (pkt.isInspecting) {
					ItemStack stack = player.getMainHandItem();
					if (!(stack.getItem() instanceof ExtendedDescriptorItem)) return; // Abort if they are cheating/desynced
				}
				InspectManager.setInspecting(player, pkt.isInspecting);

				// (Optional) If other players need to see this player inspecting (like an animation),
				// you would broadcast an S2C packet to all players tracking this player here.
			}
		});
		ctx.setPacketHandled(true);
	}
}
