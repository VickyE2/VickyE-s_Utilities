/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.Theme;

import org.vicky.utilities.ANSIColor;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.DatabaseManager.apis.ThemeAPI;
import org.vicky.utilities.DatabaseManager.templates.Theme;

public class ThemeStorer {
  private ContextLogger logger =
      new ContextLogger(ContextLogger.ContextType.FEATURE, "THEMES-STORER");

  public ThemeStorer() {}

  public void addTheme(String themeId, String themeName) {
    if (!isRegisteredTheme(themeId)) {
      logger.print("Added theme: " + ANSIColor.colorize("yellow[" + themeName + "]"));
      Theme context = new Theme();
      context.setId(themeId);
      context.setName(themeName);
      new ThemeAPI().createTheme(context);
    } else {
      logger.print("Theme " + ANSIColor.colorize("yellow[" + themeName + "] already exists"));
    }
  }

  public boolean isRegisteredTheme(String themeId) {
    return new ThemeAPI().doesExistById(themeId);
  }

  public String getThemeID(String themeName) {
    ThemeAPI service = new ThemeAPI();
    if (service.doesExistByName(themeName)) {
      return service.getThemeByName(themeName).getId().toString();
    }
    return null;
  }
}
