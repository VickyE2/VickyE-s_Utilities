/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.AttributedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AtomicFiles;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.xml.XmlConfigurationLoader;
import org.vicky.utilities.ContextLogger.ContextLogger;

public class XmlConfigManager {
  public XmlConfigurationLoader loader;
  public AttributedConfigurationNode rootNode;
  private final JavaPlugin plugin;
  public ContextLogger logger = new ContextLogger(ContextLogger.ContextType.SYSTEM, "CONFIG-XML");
  public ConfigurationOptions options;

  public XmlConfigManager(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  // Create or load the config file
  public void createConfig(String path) {
    File configFile = new File(plugin.getDataFolder(), path);

    // Ensure the parent directories for the file exist
    File parentDir = configFile.getParentFile();
    if (!parentDir.exists()) {
      parentDir.mkdirs(); // Create all necessary parent directories
    }

    options =
        XmlConfigurationLoader.builder().headerMode(HeaderMode.PRESET).indent(4).defaultOptions();

    loader =
        XmlConfigurationLoader.builder()
            .writesExplicitType(false)
            .path(configFile.toPath())
            .indent(4)
            .sink(AtomicFiles.atomicWriterFactory(configFile.toPath(), UTF_8))
            .build();

    if (!configFile.exists()) {
      try {
        // Create the file if it does not exist
        configFile.createNewFile();
        Files.write(
            configFile.toPath(),
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><config></config>"
                .getBytes(StandardCharsets.UTF_8));
        logger.printBukkit("Created new xml file: " + path);
        loadConfigValues();
      } catch (IOException e) {
        logger.printBukkit(
            "Failed to create new xml file: " + e.getMessage(), ContextLogger.LogType.ERROR);
      }
    } else {
      loadConfigValues();
    }
  }

  public void createConfig(String path, String defualtKey) {
    File configFile = new File(plugin.getDataFolder(), path);

    // Ensure the parent directories for the file exist
    File parentDir = configFile.getParentFile();
    if (!parentDir.exists()) {
      parentDir.mkdirs(); // Create all necessary parent directories
    }

    options =
        XmlConfigurationLoader.builder()
            .headerMode(HeaderMode.PRESET)
            .indent(4)
            .defaultTagName(defualtKey)
            .defaultOptions();

    loader =
        XmlConfigurationLoader.builder()
            .defaultTagName(defualtKey)
            .writesExplicitType(false)
            .path(configFile.toPath())
            .indent(4)
            .sink(AtomicFiles.atomicWriterFactory(configFile.toPath(), UTF_8))
            .build();

    if (!configFile.exists()) {
      try {
        // Create the file if it does not exist
        configFile.createNewFile();
        Files.write(
            configFile.toPath(),
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes(StandardCharsets.UTF_8));
        logger.printBukkit("Created new xml file: " + path);
        loadConfigValues();
      } catch (IOException e) {
        logger.printBukkit(
            "Failed to create new xml file: " + e.getMessage(), ContextLogger.LogType.ERROR);
      }
    } else {
      loadConfigValues();
    }
  }

  public void loadConfig(String path, String file) {
    File configFile = new File(path, file);

    options = XmlConfigurationLoader.builder().defaultOptions();

    loader = XmlConfigurationLoader.builder().path(configFile.toPath()).build();

    if (!configFile.exists()) {
      logger.printBukkit("File " + file + "does not exist.", ContextLogger.LogType.ERROR);
    } else {
      loadConfigValues();
    }
  }

  // Load configuration values
  public void loadConfigValues() {
    try {
      rootNode = loader.load(options); // Load the config directly
      logger.printBukkit("Config loaded successfully.", ContextLogger.LogType.AMBIENCE);
    } catch (Exception e) {
      logger.printBukkit(
          "Failed to load configurations from config.yml " + e, ContextLogger.LogType.WARNING);
      e.getCause();
    }
  }

  // Save the configuration to disk
  public void saveConfig() {
    try {
      loader.save(rootNode);
    } catch (IOException e) {
      logger.printBukkit("Could not save config.yml!", ContextLogger.LogType.ERROR);
      e.printStackTrace();
    }
  }

  // Check if a path exists in the configuration
  public boolean doesPathExist(String path) {
    return !rootNode.node((Object[]) path.split("\\.")).virtual();
  }

  // Get a generic config value
  public Object getConfigValue(String path) {
    try {
      return rootNode.node((Object[]) path.split("\\.")).get(Object.class);
    } catch (Exception e) {
      logger.printBukkit(
          "Failed to get config value at path: " + path, ContextLogger.LogType.ERROR);
      e.printStackTrace();
      return null;
    }
  }

  public List<AttributedConfigurationNode> getChildNodes(String path) {
    try {
      AttributedConfigurationNode node = rootNode.node((Object[]) path.split("\\."));
      return node.childrenList();
    } catch (Exception e) {
      logger.printBukkit("Failed to get child nodes at path: " + path, ContextLogger.LogType.ERROR);
      e.printStackTrace();
      return null;
    }
  }

  public boolean isListNode(String path) {
    try {
      AttributedConfigurationNode node = rootNode.node((Object[]) path.split("\\."));
      return node.isList();
    } catch (Exception e) {
      logger.printBukkit(
          "Failed to check if node is list at path: " + path, ContextLogger.LogType.ERROR);
      e.printStackTrace();
      return false;
    }
  }

  public boolean isMapNode(String path) {
    try {
      AttributedConfigurationNode node = rootNode.node((Object[]) path.split("\\."));
      return node.isMap();
    } catch (Exception e) {
      logger.printBukkit(
          "Failed to check if node is map at path: " + path, ContextLogger.LogType.ERROR);
      e.printStackTrace();
      return false;
    }
  }

  public String getTagName(String path) {
    try {
      AttributedConfigurationNode node = rootNode.node((Object[]) path.split("\\."));
      return node.tagName();
    } catch (Exception e) {
      logger.printBukkit("Failed to get tag name at path: " + path, ContextLogger.LogType.ERROR);
      e.printStackTrace();
      return null;
    }
  }

  public Map<String, String> getAttributes(String path) {
    try {
      AttributedConfigurationNode node = rootNode.node((Object[]) path.split("\\."));
      if (node != null) {
        return node.attributes();
      } else {
        return null;
      }
    } catch (Exception e) {
      logger.printBukkit("Failed to get attributes at path: " + path, ContextLogger.LogType.ERROR);
      e.printStackTrace();
      return null;
    }
  }

  // Get boolean config value
  public boolean getBooleanValue(String path) {
    return rootNode.node((Object[]) path.split("\\.")).getBoolean();
  }

  // Get double config value
  public double getDoubleValue(String path) {
    return rootNode.node((Object[]) path.split("\\.")).getDouble();
  }

  public int getIntegerValue(String path) {
    return rootNode.node((Object[]) path.split("\\.")).getInt();
  }

  // Get string config value
  public String getStringValue(String path) {
    return rootNode.node((Object[]) path.split("\\.")).getString();
  }

  // Get uuid config value
  public UUID getUUIDValue(String path) {
    try {
      return rootNode.node((Object[]) path.split("\\.")).get(UUID.class);
    } catch (SerializationException e) {
      throw new RuntimeException(e);
    }
  }

  // Get enum config value
  public <T extends Enum<T>> T getEnumValue(String path, Class<T> enumClass) {
    try {
      return rootNode.node((Object[]) path.split("\\.")).get(enumClass);
    } catch (SerializationException e) {
      throw new RuntimeException("Failed to deserialize enum from path: " + path, e);
    }
  }

  // Set a config value, ensuring parent nodes exist
  public void setConfigValue(
      String Key, Object value, String comment, Map<String, String> attributes) {
    try {
      // Split the parentKey and childKey to handle nested structures
      AttributedConfigurationNode node = rootNode.node((Object[]) Key.split("\\."));
      if (attributes != null) {
        node.attributes(attributes);
      }
      if (comment != null) {
        node.comment(comment);
      }
      node.set(value);
    } catch (Exception e) {
      logger.printBukkit(
          "Failed to add config: " + Key + " with value: " + value, ContextLogger.LogType.WARNING);
    }
    saveConfig(); // Save changes to the config file
  }
}
