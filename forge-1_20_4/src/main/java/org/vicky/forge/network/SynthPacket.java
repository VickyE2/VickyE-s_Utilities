/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.network;

/**
 * To know if it's a synth
 */
public interface SynthPacket extends Packetable {
	@Override
	default net.minecraftforge.network.SimpleChannel channel() {
		return PacketHandler.SYNTH_CHANNEL;
	}
}
