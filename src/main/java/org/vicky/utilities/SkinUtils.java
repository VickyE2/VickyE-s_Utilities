/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class SkinUtils {

  /**
   * Retrieves the skin texture URL for a given player UUID.
   * @param uuid The UUID of the player.
   * @return The URL of the player's skin texture.
   */
  public static String getPlayerSkinURL(UUID uuid) {
    try {
      // Call Mojang API to get the player's UUID profile
      URL url =
          new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString());
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");

      // Read the response
      InputStreamReader reader = new InputStreamReader(connection.getInputStream());
      JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();

      // Get the 'properties' field (which contains the skin data)
      JsonObject properties =
          jsonResponse.get("properties").getAsJsonArray().get(0).getAsJsonObject();
      String textureBase64 = properties.get("value").getAsString();

      // Decode the Base64 encoded texture data
      String decodedTexture = new String(Base64.getDecoder().decode(textureBase64));
      JsonObject textureJson = JsonParser.parseString(decodedTexture).getAsJsonObject();

      // Get the actual URL of the texture from the JSON response
      return textureJson
          .get("textures")
          .getAsJsonObject()
          .get("SKIN")
          .getAsJsonObject()
          .get("url")
          .getAsString();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
