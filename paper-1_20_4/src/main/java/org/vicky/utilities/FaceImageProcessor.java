/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.vicky.vicky_utils;

public class FaceImageProcessor {

  private final File facesFolder = vicky_utils.plugin.getDataFolder();

  /**
   * Retrieves the player's face from the skin, crops it, resizes it if needed,
   * and saves it as a .puuid file.
   *
   * @param skinURL The URL of the player's skin texture.
   * @param uuid    The player's UUID, used for saving the image file.
   * @param width   The width to resize the image to (optional).
   * @param height  The height to resize the image to (optional).
   * @throws IOException if any error occurs during processing.
   */
  public void processAndSavePlayerFace(String skinURL, UUID uuid, int width, int height)
      throws IOException {
    // Check if the .puuid image already exists
    File faceFile = new File(facesFolder, uuid.toString() + ".puuid");
    if (faceFile.exists()) {
      vicky_utils
          .plugin
          .getLogger()
          .info(
              ANSIColor.colorize(
                  "Face image already exists as .puuid for UUID: " + uuid, ANSIColor.YELLOW));
      return;
    }

    // Download the player's skin from the URL
    BufferedImage skinImage = ImageIO.read(new URL(skinURL));

    // Crop the face part of the skin (x: 8, y: 8, width: 8, height: 8)
    BufferedImage faceImage = skinImage.getSubimage(8, 8, 8, 8);

    // If resizing is needed, resize the face image
    if (width > 0 && height > 0) {
      faceImage = resizeImage(faceImage, width, height);
    }

    // Save the cropped (and resized) face as .puuid (renamed png)
    ImageIO.write(faceImage, "png", faceFile);
    vicky_utils
        .plugin
        .getLogger()
        .info(ANSIColor.colorize("Face image saved for UUID: " + uuid, ANSIColor.GREEN));
  }

  /**
   * Resizes the given image to the specified width and height.
   *
   * @param originalImage The original image to resize.
   * @param width         The target width.
   * @param height        The target height.
   * @return The resized BufferedImage.
   */
  private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
    BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = resizedImage.createGraphics();
    g.drawImage(originalImage, 0, 0, width, height, null);
    g.dispose(); // Free resources
    return resizedImage;
  }

  /**
   * Converts the .puuid file back to a .png file when needed.
   *
   * @param uuid The player's UUID.
   * @throws IOException if any error occurs during the conversion.
   */
  public void convertPuuidToPng(UUID uuid) throws IOException {
    // Check if the .puuid file exists
    File puuidFile = new File(facesFolder, uuid.toString() + ".puuid");
    if (!puuidFile.exists()) {
      vicky_utils
          .plugin
          .getLogger()
          .warning(
              ANSIColor.colorize("No .puuid file found for UUID: " + uuid, ANSIColor.RED_BOLD));
      vicky_utils
          .plugin
          .getLogger()
          .info(ANSIColor.colorize("creating puuid file for UUID: " + uuid, ANSIColor.GREEN));
      processAndSavePlayerFace(SkinUtils.getPlayerSkinURL(uuid), uuid, -1, -1);
      try {
        // Wait for 5 seconds (5000 milliseconds) before converting the file
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        Bukkit.getLogger().warning("Error generated during wait: " + e);
        e.printStackTrace();
      }
      convertPuuidToPng(uuid);
      return;
    }

    // Convert .puuid back to .png
    BufferedImage faceImage = ImageIO.read(puuidFile);

    // Save it as a .png file
    File pngFile = new File(facesFolder, uuid + ".png");
    ImageIO.write(faceImage, "png", pngFile);
    System.out.println("Converted .puuid to .png for UUID: " + uuid);
  }
}
