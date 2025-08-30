package org.vicky.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.vicky.forgeplatform.useables.ForgePlatformPlayer;
import org.vicky.music.MusicRegistry;
import org.vicky.music.utils.MusicPiece;
import org.vicky.musicPlayer.MusicPlayer;

import java.util.Optional;

public record PlaySpecifyableSong(String id) {
    public static PlaySpecifyableSong decode(FriendlyByteBuf friendlyByteBuf) {
        return new PlaySpecifyableSong(
                friendlyByteBuf.readUtf()
        );
    }

    public static void handle(PlaySpecifyableSong msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            Optional<MusicPiece> piece = MusicRegistry.getInstance(MusicRegistry.class).getRegisteredEntities()
                    .stream().filter(it -> it.key().equals(msg.id)).findFirst();
            piece.ifPresentOrElse(
                    musicPiece -> MusicPlayer.INSTANCE.play(
                            new ForgePlatformPlayer(ctx.getSender()),
                            musicPiece,
                            "vicky_music:icons/" + musicPiece.key() + ".png"),
                    () -> {
                        ctx.getSender().sendSystemMessage(Component.literal("Sadly we could not find that music piece on the server..."));
                    }
            );
        });
        ctx.setPacketHandled(true);
    }

    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeUtf(id);
    }
}
