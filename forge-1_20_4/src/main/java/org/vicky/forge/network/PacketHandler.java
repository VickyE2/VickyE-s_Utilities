package org.vicky.forge.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import org.vicky.VickyUtilitiesForge;
import org.vicky.forge.network.registeredpackets.*;

public class PacketHandler {
    private static final int PROTOCOL_VERSION = 1;
    public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(VickyUtilitiesForge.MODID, "synth");
    public static final SimpleChannel INSTANCE =
            ChannelBuilder.named(new ResourceLocation(VickyUtilitiesForge.MODID, "main"))
                    .serverAcceptedVersions(((status, version) -> version == PROTOCOL_VERSION))
                    .clientAcceptedVersions(((status, version) -> version == PROTOCOL_VERSION))
                    .networkProtocolVersion(PROTOCOL_VERSION)
                    .simpleChannel();
    private static final int SYNTH_PROTOCOL_VERSION = 1;
    public static final SimpleChannel SYNTH_CHANNEL = ChannelBuilder.named(CHANNEL_NAME)
            .serverAcceptedVersions(((status, version) -> version == SYNTH_PROTOCOL_VERSION))
            .clientAcceptedVersions(((status, version) -> version == SYNTH_PROTOCOL_VERSION))
            .networkProtocolVersion(SYNTH_PROTOCOL_VERSION)
            .simpleChannel();

    private static int packetId = 0;
    private static int synthPacketId = 0;

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
        INSTANCE.messageBuilder(OpenOwnedRecordsScreen.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenOwnedRecordsScreen::encode)
                .decoder(OpenOwnedRecordsScreen::decode)
                .consumerMainThread(OpenOwnedRecordsScreen::handle)
                .add();
        INSTANCE.messageBuilder(PlaySpecifyableSong.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PlaySpecifyableSong::encode)
                .decoder(PlaySpecifyableSong::decode)
                .consumerMainThread(PlaySpecifyableSong::handle)
                .add();
        SYNTH_CHANNEL.messageBuilder(NoteOnPacket.class, synthPacketId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(NoteOnPacket::encode)
                .decoder(NoteOnPacket::decode)
                .consumerMainThread(NoteOnPacket::handle)
                .add();
        SYNTH_CHANNEL.messageBuilder(NoteOffPacket.class, synthPacketId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(NoteOffPacket::encode)
                .decoder(NoteOffPacket::decode)
                .consumerMainThread(NoteOffPacket::handle)
                .add();
    }

    public static void sendToServer(Packetable packet) {
        INSTANCE.send(packet, PacketDistributor.SERVER.noArg());
    }

    public static void sendToClient(ServerPlayer player, Packetable packet) {
        // System.out.println("SynthPacket.class loader = " + org.vicky.forge.network.SynthPacket.class.getClassLoader());
        packet.channel().send(packet, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToAllClient(Packetable packet) {
        INSTANCE.send(packet, PacketDistributor.ALL.noArg());
    }
}
