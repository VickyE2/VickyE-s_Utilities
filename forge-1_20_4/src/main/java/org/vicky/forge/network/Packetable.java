package org.vicky.forge.network;

public interface Packetable {
    default net.minecraftforge.network.SimpleChannel channel() {
        return PacketHandler.INSTANCE;
    }
}
