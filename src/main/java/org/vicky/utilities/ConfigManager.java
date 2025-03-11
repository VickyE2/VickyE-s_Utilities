/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.vicky.utilities.ContextLogger.ContextLogger;

public class ConfigManager {

  public ConfigurationLoader<CommentedConfigurationNode> loader;
  public CommentedConfigurationNode rootNode;
  public JavaPlugin plugin;
  public ContextLogger logger = new ContextLogger(ContextLogger.ContextType.SYSTEM, "CONFIG-YML");
  public ConfigurationOptions options;
  public boolean shouldLog = true;

  // Constructor 1: With a specified path
  public ConfigManager(JavaPlugin plugin, String path) {
    this.plugin = plugin;
    createConfig(path);
  }

  public ConfigManager(JavaPlugin plugin, String path, boolean shouldLog) {
    this.plugin = plugin;
    this.shouldLog = shouldLog;
    createConfig(path);
  }

  // Constructor 2: Without a path
  public ConfigManager(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public ConfigManager(JavaPlugin plugin, boolean shouldLog) {
    this.plugin = plugin;
    this.shouldLog = shouldLog;
  }

  // Create or load the config file
  public void createConfig(String path) {
    File configFile = new File(plugin.getDataFolder(), path);

    options =
        YamlConfigurationLoader.builder().indent(2).nodeStyle(NodeStyle.BLOCK).defaultOptions();

    loader =
        YamlConfigurationLoader.builder()
            .path(configFile.toPath())
            .indent(2)
            .nodeStyle(NodeStyle.BLOCK)
            .build();

    if (!configFile.exists()) {
      try {
        // Create the file if it does not exist
        configFile.createNewFile();
        if (shouldLog) {
          logger.printBukkit("Created new config file: " + path);
        }
        loadConfigValues();
      } catch (IOException e) {
        logger.printBukkit(
            "Failed to create new config file: " + e.getMessage(), ContextLogger.LogType.ERROR);
      }
    } else {
      loadConfigValues();
    }
  }

  public void createPathedConfig(String pat) {
    File configFile = new File(pat);

    options =
        YamlConfigurationLoader.builder().indent(2).nodeStyle(NodeStyle.BLOCK).defaultOptions();

    loader =
        YamlConfigurationLoader.builder()
            .path(configFile.toPath())
            .indent(2)
            .nodeStyle(NodeStyle.BLOCK)
            .build();

    if (!configFile.exists()) {
      try {
        // Create the file if it does not exist
        configFile.createNewFile();
        if (shouldLog) {
          logger.printBukkit("Created new config file: " + pat);
        }
        loadConfigValues();
      } catch (IOException e) {
        logger.printBukkit(
            "Failed to create new config file: " + e.getMessage(), ContextLogger.LogType.ERROR);
      }
    } else {
      loadConfigValues();
    }
  }

  public void loadConfig(String path, String file) {
    File configFile = new File(path, file);

    options = YamlConfigurationLoader.builder().defaultOptions();

    loader = YamlConfigurationLoader.builder().path(configFile.toPath()).build();

    if (!configFile.exists()) {
      if (shouldLog) {
        logger.printBukkit("File " + file + "does not exist.", ContextLogger.LogType.ERROR);
      }
    } else {
      loadConfigValues();
    }
  }

  // Load configuration values
  public void loadConfigValues() {
    try {
      rootNode = loader.load(options); // Load the config directly
      if (shouldLog) {
        logger.printBukkit("Config loaded successfully.", ContextLogger.LogType.AMBIENCE);
      }
    } catch (Exception e) {
      logger.printBukkit(
          "Failed to load configurations from config.yml " + e, ContextLogger.LogType.WARNING);
      e.getCause();
    }
  }

  public void loadConfigFromZip(Path zipFilePath, String configFileName) throws IOException {
    try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
      ZipEntry entry = zipFile.getEntry(configFileName);

      if (entry != null && !entry.isDirectory()) {
        try (InputStream inputStream = zipFile.getInputStream(entry);
            InputStreamReader reader = new InputStreamReader(inputStream)) {

          // Create a YamlConfigurationLoader using the InputStream
          YamlConfigurationLoader loader =
              YamlConfigurationLoader.builder()
                  .source(() -> new BufferedReader(reader))
                  .nodeStyle(NodeStyle.BLOCK)
                  .build();

          // Load the YAML config directly from the input stream
          rootNode = loader.load(ConfigurationOptions.defaults());

          // Log success
          if (shouldLog)
            logger.printBukkit(
                configFileName
                    + " Has been successfully loaded from zip-file: "
                    + zipFilePath.getFileName());
        }
      } else {
        if (shouldLog)
          logger.printBukkit(
              "Could not find the required '"
                  + configFileName
                  + "' config file inside zip-file: "
                  + zipFilePath.getFileName());
      }
    } catch (Exception e) {
      logger.printBukkit(
          "Failed to load configurations from zip file: "
              + zipFilePath.getFileName()
              + " reason: "
              + e.getMessage());
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

  public void setListConfigValue(String key, List<String> values) {
    try {
      CommentedConfigurationNode node = rootNode.node((Object[]) key.split("\\."));
      node.set(values); // Set the list of values
      saveConfig(); // Save changes to the config file
      if (shouldLog) {
        logger.printBukkit("Config value set for " + key + ": " + values);
      }
    } catch (Exception e) {
      logger.printBukkit(
          "Failed to set config value for " + key + ": " + e.getMessage(),
          ContextLogger.LogType.WARNING);
    }
  }

  // Method to retrieve a list of strings from a specified key
  public List<String> getListConfigValue(String key) {
    List<String> values = new ArrayList<>();
    try {
      ConfigurationNode node = rootNode.node((Object[]) key.split("\\."));
      for (ConfigurationNode childNode : node.childrenList()) {
        String value = childNode.getString();
        if (value != null) {
          values.add(value);
        }
      }
    } catch (Exception e) {
      logger.printBukkit(
          "Failed to get list config value for " + key + ": " + e.getMessage(),
          ContextLogger.LogType.ERROR);
    }
    return values;
  }

  // Get boolean config value
  public boolean getBooleanValue(String path) {
    return rootNode.node((Object[]) path.split("\\.")).getBoolean();
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

  // Set a config value, ensuring parent nodes exist
  public void setConfigValue(String parentKey, String childKey, Object value, String comment) {
    try {
      // Split the parentKey and childKey to handle nested structures
      CommentedConfigurationNode parentNode = rootNode.node((Object[]) parentKey.split("\\."));
      CommentedConfigurationNode childNode = parentNode.node((Object[]) childKey.split("\\."));

      // Set the value and comment
      childNode.set(value).comment(comment);
    } catch (Exception e) {
      logger.printBukkit(
          "Failed to add config: " + parentKey + "." + childKey + " with value: " + value,
          ContextLogger.LogType.WARNING);
    }
    saveConfig(); // Save changes to the config file
  }

  // Set a braced config value (for parent nodes)
  public void setBracedConfigValue(String key, Object value, String comment) {
    try {
      rootNode.node(key).set(value).comment(comment);
    } catch (Exception e) {
      logger.printBukkit("Failed to add config: " + key, ContextLogger.LogType.WARNING);
    }
    saveConfig(); // Save changes to the config file
  }
}
