/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.Theme;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.vicky.utilities.Identifiable;

/**
 * BaseTheme is an abstract class representing a theme pack.
 * <p>
 * It provides basic metadata (ID, name, pack name, description) and functionality to relocate
 * a theme pack ZIP file from the plugin's resources to a specified destination.
 * </p>
 */
public abstract class BaseTheme implements Identifiable {
  private final String id;
  private final String packName;
  private final String name;
  private final String description;

  /**
   * Constructs a BaseTheme with the specified ID, name, and pack name.
   * <p>
   * The description is set to null.
   * </p>
   *
   * @param id       the unique identifier for the theme
   * @param name     the display name of the theme
   * @param packName the name of the pack file (without extension)
   */
  public BaseTheme(String id, String name, String packName) {
    this.id = id;
    this.name = name;
    this.packName = packName;
    this.description = null;
  }

  /**
   * Constructs a BaseTheme with the specified ID, name, pack name, and description.
   *
   * @param id          the unique identifier for the theme
   * @param name        the display name of the theme
   * @param packName    the name of the pack file (without extension)
   * @param description a description of the theme
   */
  public BaseTheme(String id, String name, String packName, String description) {
    this.id = id;
    this.name = name;
    this.packName = packName;
    this.description = description;
  }

  /**
   * Retrieves the display name of the theme.
   *
   * @return the theme's name
   */
  public String getName() {
    return name;
  }

  /**
   * Retrieves the description of the theme.
   *
   * @return the theme's description, or null if not set
   */
  public String getDescription() {
    return description;
  }

  /**
   * Copies the ZIP pack of the theme from the <strong>resources/themes</strong> folder
   * to the specified destination path.
   * <p>
   * This method creates any necessary parent directories and overwrites an existing file.
   * </p>
   *
   * @param destinationPath the file path to which the ZIP should be copied
   * @throws IOException if an I/O error occurs, including if the resource is not found
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

  /**
   * Retrieves the unique identifier of this theme.
   *
   * @return the theme's unique ID
   */
  @Override
  public String getIdentifier() {
    return id;
  }
}
