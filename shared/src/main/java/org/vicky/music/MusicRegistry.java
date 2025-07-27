/* Licensed under Apache-2.0 2024. */
package org.vicky.music;

import java.io.File;
import java.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.vicky.music.utils.MusicPiece;
import org.vicky.musicPlayer.MusicPlayer;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.PlatformPlugin;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.DatabaseManager.dao_s.MusicPieceDAO;
import org.vicky.utilities.DatabaseManager.dao_s.MusicPlayerDAO;
import org.vicky.utilities.Registry;
import org.vicky.utilities.XmlConfigManager;

public class MusicRegistry extends Registry<MusicPiece, MusicRegistry> {
  private final List<MusicPiece> musicPieces = new ArrayList<>();
  private final MusicPlayer player = MusicPlayer.INSTANCE;
  public static final Map<String, TextColor> genreColors = new HashMap<>();

  static {
    genreColors.putAll(
        Map.of(
            "ROCK", NamedTextColor.BLACK,
            "BLUES", TextColor.color(0, 200, 255),
            "CHILL", TextColor.color(88, 113, 255),
            "BATTLE", TextColor.color(255, 44, 44),
            "RETRO", TextColor.color(255, 150, 44),
            "DEFAULT", TextColor.color(180, 180, 180)));
  }

  private final XmlConfigManager manager = new XmlConfigManager(new File(PlatformPlugin.dataFolder(), ""));

  public MusicRegistry() {
    super("MusicRegistry");
    manager.createConfig("configs/music_piece.xml", "musicInformation");
    for (var genreColor : genreColors.entrySet()) {
      if (manager.getConfigValue(genreColor.getKey()) != null) {
        genreColors.put(
            genreColor.getKey(), TextColor.color(manager.getIntegerValue(genreColor.getKey())));
      }
      manager.setConfigValue(genreColor.getKey(), genreColor.getValue().value(), null, null);
    }
  }

  @Override
  public Collection<MusicPiece> getRegisteredEntities() {
    return Collections.unmodifiableList(musicPieces);
  }

  @Override
  public void register(MusicPiece child) {
    getLogger().print("Adding music: " + child.pieceName(), ContextLogger.LogType.PENDING);
    musicPieces.add(child);
    var couldBe = new MusicPieceDAO().findById(child.key());
    if (couldBe == null) {
      var piece = new org.vicky.utilities.DatabaseManager.templates.MusicPiece(child.key());
      new MusicPieceDAO().save(piece);
    }
  }

  public void playPiece(String key, PlatformPlayer toListen) {
    if (musicPieces.stream().noneMatch(p -> p.key().equals(key))) {
      toListen.sendMessage(Component.text("Music Piece '" + key + "' not found.", NamedTextColor.DARK_RED));
      getLogger()
          .print(
              "Music Piece with key '" + key + "' not found on registry",
              ContextLogger.LogType.WARNING);
      return;
    }
    MusicPiece piece = musicPieces.stream().filter(p -> p.key().equals(key)).findFirst().get();
    toListen.sendMessage(
        Component.text("♫ Now Playing: ", NamedTextColor.GOLD, TextDecoration.BOLD)
            .append(
                Component.text(
                    piece.pieceName(), TextColor.fromHexString("#b25bb4"), TextDecoration.ITALIC))
            .append(Component.text(" by ", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(
                Component.text(
                    String.join(NamedTextColor.GOLD + ", " + NamedTextColor.LIGHT_PURPLE, piece.authors()),
                    Style.style(TextDecoration.ITALIC))));
    player.play(toListen, piece);
  }

  public Component renderMusicPage(PlatformPlayer player, int page) {
    final int pageSize = 10;
    final var dbMusic = new MusicPlayerDAO().findById(player.uniqueId()).get();
    int totalPages = (int) Math.ceil(dbMusic.getOwnedPieces().size() / (double) pageSize);
    page = Math.max(1, Math.min(page, totalPages));

    int start = (page - 1) * pageSize;
    int end = Math.min(start + pageSize, dbMusic.getOwnedPieces().size());
    List<MusicPiece> fullOwnedList = new ArrayList<>();
    for (var key : dbMusic.getOwnedPieces()) {
      fullOwnedList.add(
          musicPieces.stream().filter(k -> k.key().equals(key.getId())).findAny().get());
    }

    List<MusicPiece> pageItems = fullOwnedList.subList(start, end);
    Component header =
        Component.text(
            "🎵 Music Library (Page " + page + "/" + totalPages + ")",
            NamedTextColor.GOLD,
            TextDecoration.BOLD);
    Component list = Component.empty();

    int index = 0;
    for (MusicPiece piece : pageItems) {
      index++;
      list =
          list.appendNewline()
              .append(
                  Component.text(index + "> ♫ " + piece.pieceName(), NamedTextColor.LIGHT_PURPLE)
                      .hoverEvent(
                          HoverEvent.showText(
                              Component.text("Click to play!", NamedTextColor.GREEN)))
                      .clickEvent(ClickEvent.runCommand("/play_piece " + piece.key())))
              .append(
                  Component.text(" - More Information", NamedTextColor.GOLD, TextDecoration.BOLD)
                      .hoverEvent(
                          HoverEvent.showText(
                              Component.text(Arrays.toString(piece.authors()))
                                  .appendNewline()
                                  .append(
                                      Component.text("Genre: " + piece.genre())
                                          .color(
                                              genreColors.getOrDefault(
                                                  piece.genre().toUpperCase(),
                                                  NamedTextColor.GREEN))))));
    }

    Component footer = Component.empty();
    if (page > 1) {
      footer =
          footer.append(
              Component.text("« Prev", NamedTextColor.AQUA)
                  .clickEvent(ClickEvent.runCommand("/music_piece page " + (page - 1)))
                  .hoverEvent(HoverEvent.showText(Component.text("Previous page"))));
    }
    footer = footer.append(Component.text(" | ", NamedTextColor.GRAY));
    if (page < totalPages) {
      footer =
          footer.append(
              Component.text("Next »", NamedTextColor.AQUA)
                  .clickEvent(ClickEvent.runCommand("/music_piece page " + (page + 1)))
                  .hoverEvent(HoverEvent.showText(Component.text("Next page"))));
    }

    return header.append(list).appendNewline().append(footer);
  }

  public MusicPlayer getPlayer() {
    return player;
  }
}
