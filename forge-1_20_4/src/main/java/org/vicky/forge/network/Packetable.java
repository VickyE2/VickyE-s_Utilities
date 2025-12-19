/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.network;

public interface Packetable {
    default net.minecraftforge.network.SimpleChannel channel() {
        return PacketHandler.MAIN_CHNNEL;
    }
}
