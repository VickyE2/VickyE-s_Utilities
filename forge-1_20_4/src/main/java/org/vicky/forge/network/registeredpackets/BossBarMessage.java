package org.vicky.forge.network.registeredpackets;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BossBarMessage {
    public final UUID id;
    public final @Nullable Component title, subTitle;
    public final float progress;
    public final String hex;
    public final @Nullable ResourceLocation image;

    public BossBarMessage(UUID id, @Nullable Component title, @Nullable Component subTitle, float progress, String hex, @Nullable ResourceLocation image) {
        this.id = id;
        this.title = title;
        this.subTitle = subTitle;
        this.progress = progress;
        this.hex = hex;
        this.image = image;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(id);

        // title - assume not null, but still safe
        buf.writeUtf(GsonComponentSerializer.gson().serialize(title));

        // subTitle - nullable -> write a boolean flag then the string
        if (subTitle != null) {
            buf.writeBoolean(true);
            buf.writeUtf(GsonComponentSerializer.gson().serialize(subTitle));
        } else {
            buf.writeBoolean(false);
        }

        buf.writeFloat(progress);
        buf.writeUtf(hex == null ? "" : hex);

        // image - nullable -> boolean flag then the string
        if (image != null) {
            buf.writeBoolean(true);
            buf.writeUtf(image.toString());
        } else {
            buf.writeBoolean(false);
        }
    }
}
