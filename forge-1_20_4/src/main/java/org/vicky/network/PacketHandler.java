package org.vicky.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import org.vicky.VickyUtilitiesForge;
import org.vicky.network.packets.CreateSSBossBar;
import org.vicky.network.packets.RemoveSSBossBar;
import org.vicky.network.packets.UpdateSSBossBar;

public class PacketHandler {
    private static final int PROTOCOL_VERSION = 1;
    public static final SimpleChannel INSTANCE =
            ChannelBuilder.named(new ResourceLocation(VickyUtilitiesForge.MODID, "main"))
                    .serverAcceptedVersions(((status, version) -> version == PROTOCOL_VERSION))
                    .clientAcceptedVersions(((status, version) -> version == PROTOCOL_VERSION))
                    .networkProtocolVersion(PROTOCOL_VERSION)
                    .simpleChannel();

    private static int packetId = 0;

    public static void register() {
        INSTANCE.messageBuilder(CreateSSBossBar.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CreateSSBossBar::encode)
                .decoder(CreateSSBossBar::decode)
                .consumerMainThread(CreateSSBossBar::handle)
                .add();
        INSTANCE.messageBuilder(UpdateSSBossBar.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(UpdateSSBossBar::encode)
                .decoder(UpdateSSBossBar::decode)
                .consumerMainThread(UpdateSSBossBar::handle)
                .add();
        INSTANCE.messageBuilder(RemoveSSBossBar.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RemoveSSBossBar::encode)
                .decoder(RemoveSSBossBar::decode)
                .consumerMainThread(RemoveSSBossBar::handle)
                .add();
    }

    public static void sendToServer(Object packet) {
        INSTANCE.send(packet, PacketDistributor.SERVER.noArg());
    }

    public static void sendToClient(Object packet, ServerPlayer player) {
        INSTANCE.send(packet, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToAllClient(Object packet) {
        INSTANCE.send(packet, PacketDistributor.ALL.noArg());
    }
}
