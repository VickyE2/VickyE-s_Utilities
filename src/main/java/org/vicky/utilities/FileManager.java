/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.utilities.ContextLogger.ContextLogger;

public class FileManager {

  private final JavaPlugin plugin;
  private ContextLogger logger = new ContextLogger(ContextLogger.ContextType.SYSTEM, "EXTRACTOR");

  public FileManager(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  /**
   * Extracts assets from the plugin JAR to the ItemsAdder directory.
   * It will extract everything in the specified folder from the JAR.
   *
   * @param folders List of folder paths inside the JAR to extract.
   */
  public void extractDefaultIAAssets(List<String> folders) {
    try {
      // Get the URL of the JAR file
      URL jarUrl = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();

      try (ZipInputStream zip = new ZipInputStream(jarUrl.openStream())) {
        logger.printBukkit("Extracting assets...", ContextLogger.LogType.PENDING);

        // Root folder where the extracted assets will go (ItemsAdder root)
        File itemsAdderRoot = new File(plugin.getDataFolder().getParent(), "ItemsAdder");

        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
          String entryName = entry.getName();

          for (String folder : folders) {
            if (entryName.startsWith(folder) && !entry.isDirectory()) {
              // Extract this file into the destination folder
              extractFile(itemsAdderRoot, entryName);
            }
          }
        }

        logger.printBukkit("Extraction complete!", ContextLogger.LogType.SUCCESS);
      }
    } catch (IOException e) {
      logger.printBukkit("ERROR EXTRACTING assets! StackTrace:", true);
      e.printStackTrace();
    }
  }

  /**
   * Extracts a file from the JAR into the destination directory.
   * It ensures the entire directory structure is created.
   *
   * @param destRoot The destination root folder (e.g., ItemsAdder directory).
   * @param entryName The name of the file in the JAR to extract.
   * @throws IOException If an I/O error occurs.
   */
  private void extractFile(File destRoot, String entryName) throws IOException {
    // Destination file on the system
    File destFile = new File(destRoot, entryName);

    // Ensure parent directories exist
    File parentDir = destFile.getParentFile();
    if (!parentDir.exists()) {
      parentDir.mkdirs(); // Create directories if they don't exist
    }

    // Only extract if the file doesn't already exist
    if (!destFile.exists()) {
      try (InputStream in = plugin.getResource(entryName);
          FileOutputStream out = new FileOutputStream(destFile)) {

        if (in != null) {
          byte[] buffer = new byte[1024];
          int bytesRead;
          while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
          }
          logger.printBukkit("Extracted: " + entryName, ContextLogger.LogType.BASIC);
        } else {
          logger.printBukkit("Resource not found: " + entryName, ContextLogger.LogType.WARNING);
        }
      }
    }
  }
}
