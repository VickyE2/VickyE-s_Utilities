/* Licensed under Apache-2.0 2025. */
package org.vicky.utilities.Theme;

import static org.vicky.global.Global.databaseManager;

import org.vicky.utilities.ANSIColor;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.DatabaseManager.templates.Theme;
import org.vicky.utilities.UUIDGenerator;

public class ThemeStorer {
  private ContextLogger logger =
      new ContextLogger(ContextLogger.ContextType.FEATURE, "THEMES-STORER");

  public ThemeStorer() {}

  public void addTheme(String themeId, String themeName) {
    if (!isRegisteredTheme(themeId)) {
      logger.print("Added theme: " + ANSIColor.colorize("yellow[" + themeName + "]"));
      Theme context = new Theme();
      context.setId(UUIDGenerator.generateUUIDFromString(themeId));
      context.setName(themeName);
      databaseManager.saveEntity(context);
    } else {
      logger.print("Theme " + ANSIColor.colorize("yellow[" + themeName + "] already exists"));
    }
  }

  public boolean isRegisteredTheme(String themeId) {
    return databaseManager.entityExists(Theme.class, themeId);
  }

  public String getThemeID(String themeName) {
    Theme context = databaseManager.getEntityByNaturalId(Theme.class, themeName);
    if (context != null) {
      return context.getId().toString();
    }
    return null;
  }
}
