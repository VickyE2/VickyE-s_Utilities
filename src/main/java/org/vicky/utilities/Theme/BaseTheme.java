/* Licensed under Apache-2.0 2025. */
package org.vicky.utilities.Theme;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.vicky.utilities.Identifiable;

public abstract class BaseTheme implements Identifiable {
  private final String id;
  private final String packName;
  private final String name;
  private final String description;

  public BaseTheme(String id, String name, String packName) {
    this.id = id;
    this.name = name;
    this.packName = packName;
    this.description = null;
  }

  public BaseTheme(String id, String name, String packName, String description) {
    this.id = id;
    this.name = name;
    this.packName = packName;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Copies the ZIP pack of the theme from <strong>resources/themes</strong> to the specified destination path.
   *
   * @param destinationPath the file path to which the ZIP should be copied
   * @throws IOException if an I/O error occurs
   */
  public void relocatePack(String destinationPath) throws IOException {
    try (InputStream zipStream =
        getClass().getResourceAsStream("/themes/" + this.packName + ".zip")) {
      if (zipStream == null) {
        throw new FileNotFoundException(
            "Theme pack '" + packName + ".zip' not found in themes resources folder");
      }
      Path destination = Paths.get(destinationPath);
      Files.createDirectories(destination.getParent());
      Files.copy(zipStream, destination, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  @Override
  public String getIdentifier() {
    return id;
  }
}
