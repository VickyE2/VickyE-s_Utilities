/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.Theme;

import static org.vicky.global.Global.themeSelectionListener;

import jakarta.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.guiparent.BaseGui;
import org.vicky.guiparent.ButtonAction;
import org.vicky.guiparent.GuiCreator;
import org.vicky.utilities.DatabaseManager.dao_s.DatabasePlayerDAO;
import org.vicky.utilities.DatabaseManager.dao_s.ThemeDAO;
import org.vicky.utilities.DatabaseManager.templates.DatabasePlayer;
import org.vicky.utilities.DatabaseManager.templates.Theme;
import org.vicky.utilities.SmallCapsConverter;

public class ThemeSelectionGui extends BaseGui {
  /**
   * Constructs a ThemeSelectionGui with a specified plugin and a custom GUI listener.
   *
   * @param plugin the JavaPlugin instance for the plugin
   */
  public ThemeSelectionGui(JavaPlugin plugin) {
    super(plugin, themeSelectionListener);
  }

  /**
   * Displays the GUI to the specified player.
   * <p>
   * Each subclass must implement this method to open its particular GUI.
   * </p>
   *
   * @param player the player to whom the GUI is shown
   */
  @Override
  public void showGui(Player player) {
    List<Theme> themes = new ArrayList<>(new ThemeDAO().getAll());
    List<GuiCreator.ItemConfig> themeConfigs = new ArrayList<>();
    themes.forEach(
        theme -> {
          GuiCreator.ItemConfig config =
              GuiCreator.ItemConfigFactory.fromComponents(
                  null,
                  SmallCapsConverter.toSmallCaps(theme.getName()),
                  "",
                  true,
                  null,
                  "vicky_themes:icon_" + theme.getId(),
                  List.of(
                      Component.text(
                          theme.getDescription(),
                          TextColor.color(255, 215, 0),
                          TextDecoration.ITALIC)),
                  ButtonAction.ofRunCode(
                      event -> {
                        DatabasePlayerDAO dao = new DatabasePlayerDAO();
                        Optional<DatabasePlayer> playerOptional =
                            dao.findById(player.getUniqueId());
                        if (playerOptional.isEmpty()) {
                          try {
                            throw new NoResultException("Failed to locate database player...");
                          } catch (Exception e) {
                            throw new RuntimeException(e);
                          }
                        }
                        DatabasePlayer databasePlayer = playerOptional.get();
                        if (databasePlayer.getUserTheme() != theme.getId()) {
                          databasePlayer.setUserTheme(theme.getId());
                          dao.update(databasePlayer);
                        }
                      },
                      true));
          themeConfigs.add(config);
          guiManager.openPaginatedGUI(
              player,
              6,
              GuiCreator.ArrowGap.MEDIUM,
              new ArrayList<>(),
              themeConfigs,
              "",
              0,
              0,
              "",
              true,
              true,
              "",
              -8);
        });
  }
}
