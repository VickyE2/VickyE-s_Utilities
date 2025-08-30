package org.vicky.forge.music;

import org.vicky.music.MusicRegistry;
import org.vicky.platform.utils.ResourceLocation;

public final class SongEntry {
    public final String id;            // unique song id (used for play packet)
    public final String title;         // displayed title
    public final String authors;       // displayed subtitle
    public final ResourceLocation icon; // texture location (e.g., new ResourceLocation("vickyutils", "textures/gui/icons/song1.png"))

    public SongEntry(String id) {
        var piece = MusicRegistry.getInstance(MusicRegistry.class).getRegisteredEntities().stream().filter(it -> it.key().equals(id))
                .findFirst().get();
        this.id = id;
        this.title = piece.pieceName();
        this.authors = String.join(", ", piece.authors());
        this.icon = ResourceLocation.from("vicky_music:icons/" + id + ".png");
    }
}