/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.jackson.FieldValueSeparatorStyle;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.vicky.platform.PlatformPlugin;
import org.vicky.utilities.ContextLogger.ContextLogger;

public class JsonConfigManager {
  public ConfigurationLoader<? extends ConfigurationNode> loader;
  public ConfigurationNode rootNode;
  public ContextLogger logger = new ContextLogger(ContextLogger.ContextType.SYSTEM, "CONFIG-JSON");
  public ConfigurationOptions options;

  // Create or load the config file
  public void createConfig(String path) {
    File configFile = new File(PlatformPlugin.dataFolder(), path);

    // Ensure the parent directories for the file exist
    File parentDir = configFile.getParentFile();
    if (!parentDir.exists()) {
      parentDir.mkdirs(); // Create all necessary parent directories
    }

    options = JacksonConfigurationLoader.builder().headerMode(HeaderMode.PRESET).defaultOptions();

    loader =
        JacksonConfigurationLoader.builder()
            .path(configFile.toPath())
            .indent(2)
            .fieldValueSeparatorStyle(FieldValueSeparatorStyle.SPACE_BOTH_SIDES)
            .build();

    if (!configFile.exists()) {
      try {
        // Create the file if it does not exist
        configFile.createNewFile();
        try (FileWriter writer = new FileWriter(configFile)) {
          writer.write("{}"); // Start with an empty JSON object
        }
        logger.print("Created new json file: " + path);
      } catch (IOException e) {
        logger.print(
            "Failed to create new json file: " + e.getMessage(), ContextLogger.LogType.ERROR);
      }
    } else {
      loadConfigValues();
    }
  }

  // Load configuration values
  public void loadConfigValues() {
    try {
      rootNode = loader.load(options); // Load the config directly
      logger.print("Config loaded successfully.", ContextLogger.LogType.AMBIENCE);
    } catch (Exception e) {
      logger.print(
          "Failed to load configurations from config.yml " + e, ContextLogger.LogType.WARNING);
      e.getCause();
    }
  }

  // Save the configuration to disk
  public synchronized void saveConfig() {
    try {
      // Save your configuration
      loader.save(rootNode);
    } catch (ConfigurateException e) {
      logger.print(
          "Could not save config asynchronously: " + e.getMessage(), ContextLogger.LogType.ERROR);
      if (e.getCause() instanceof AccessDeniedException) {
        logger.print(
            "Access denied while saving config. Check file permissions.",
            ContextLogger.LogType.ERROR);
      }
    }
  }

  // Check if a path exists in the configuration
  public synchronized boolean doesPathExist(String path) {
    return !rootNode.node((Object[]) path.split("\\.")).virtual();
  }

  // Get a generic config value
  public synchronized Object getConfigValue(String path) {
    try {
      return rootNode.node((Object[]) path.split("\\.")).get(Object.class);
    } catch (Exception e) {
      logger.print(
          "Failed to get config value at path: " + path, ContextLogger.LogType.ERROR);
      e.printStackTrace();
      return null;
    }
  }

  // Get boolean config value
  public synchronized boolean getBooleanValue(String path) {
    return rootNode.node((Object[]) path.split("\\.")).getBoolean();
  }

  // Get double config value
  public synchronized double getDoubleValue(String path) {
    return rootNode.node((Object[]) path.split("\\.")).getDouble();
  }

  public synchronized int getIntegerValue(String path) {
    return rootNode.node((Object[]) path.split("\\.")).getInt();
  }

  // Get string config value
  public synchronized String getStringValue(String path) {
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
  public synchronized void setConfigValue(String Key, Object value) {
    try {
      ConfigurationNode parentNode = rootNode.node((Object[]) Key.split("\\."));

      // Set the value and comment
      parentNode.set(value);
    } catch (Exception e) {
      logger.print(
          "Failed to add config: " + Key + " with value: " + value, ContextLogger.LogType.WARNING);
    }
    saveConfig(); // Save changes to the config file
  }

  // Asynchronous methods

  // Create or load the config file asynchronously
  public CompletableFuture<Void> createConfigAsync(String path) {
    return CompletableFuture.runAsync(
            () -> {
              File configFile = new File(PlatformPlugin.dataFolder(), path);

              // Ensure the parent directories for the file exist
              File parentDir = configFile.getParentFile();
              if (!parentDir.exists()) {
                parentDir.mkdirs(); // Create all necessary parent directories
              }

              options =
                  JacksonConfigurationLoader.builder()
                      .headerMode(HeaderMode.PRESET)
                      .defaultOptions();

              loader =
                  JacksonConfigurationLoader.builder()
                      .path(configFile.toPath())
                      .indent(2)
                      .fieldValueSeparatorStyle(FieldValueSeparatorStyle.SPACE_BOTH_SIDES)
                      .build();

              if (!configFile.exists()) {
                try {
                  configFile.createNewFile();
                  try (FileWriter writer = new FileWriter(configFile)) {
                    writer.write("{}"); // Start with an empty JSON object
                  }
                  logger.print("Created new json file: " + path);
                } catch (IOException e) {
                  logger.print(
                      "Failed to create new json file: " + e.getMessage(),
                      ContextLogger.LogType.ERROR);
                }
              }
            })
        .thenCompose(
            v -> loadConfigValuesAsync()); // Chain loading of the config after file creation
  }

  // Load configuration values asynchronously
  public CompletableFuture<Void> loadConfigValuesAsync() {
    return CompletableFuture.runAsync(
        () -> {
          try {
            rootNode = loader.load(options); // Load the config directly
            logger.print("Config loaded successfully.", ContextLogger.LogType.AMBIENCE);
          } catch (Exception e) {
            logger.print(
                "Failed to load configurations: " + e, ContextLogger.LogType.WARNING);
          }
        });
  }

  // Save the configuration asynchronously
  public CompletableFuture<Void> saveConfigAsync() {
    return CompletableFuture.runAsync(
        () -> {
          try {
            // Save your configuration
            loader.save(rootNode);
          } catch (ConfigurateException e) {
            logger.print(
                "Could not save config asynchronously: " + e.getMessage(),
                ContextLogger.LogType.ERROR);
            if (e.getCause() instanceof AccessDeniedException) {
              logger.print(
                  "Access denied while saving config. Check file permissions.",
                  ContextLogger.LogType.ERROR);
            }
          }
        });
  }

  // Set a config value asynchronously, ensuring parent nodes exist
  public CompletableFuture<Void> setConfigValueAsync(String key, Object value) {
    return CompletableFuture.runAsync(
            () -> {
              try {
                ConfigurationNode parentNode = rootNode.node((Object[]) key.split("\\."));
                parentNode.set(value); // Set the value
              } catch (Exception e) {
                logger.print(
                    "Failed to add config: " + key + " with value: " + value,
                    ContextLogger.LogType.WARNING);
                e.printStackTrace();
              }
            })
        .thenCompose(v -> saveConfigAsync()); // Chain saving of the config after setting the value
  }

  // Get a generic config value asynchronously
  public CompletableFuture<Object> getConfigValueAsync(String path) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return rootNode.node((Object[]) path.split("\\.")).get(Object.class);
          } catch (Exception e) {
            logger.print(
                "Failed to get config value at path: " + path, ContextLogger.LogType.ERROR);
            e.printStackTrace();
            return null;
          }
        });
  }

  // Get boolean config value asynchronously
  public CompletableFuture<Boolean> getBooleanValueAsync(String path) {
    return CompletableFuture.supplyAsync(
        () -> rootNode.node((Object[]) path.split("\\.")).getBoolean());
  }

  // Get double config value asynchronously
  public CompletableFuture<Double> getDoubleValueAsync(String path) {
    return CompletableFuture.supplyAsync(
        () -> rootNode.node((Object[]) path.split("\\.")).getDouble());
  }

  // Get integer config value asynchronously
  public CompletableFuture<Integer> getIntegerValueAsync(String path) {
    return CompletableFuture.supplyAsync(
        () -> rootNode.node((Object[]) path.split("\\.")).getInt());
  }

  // Get string config value asynchronously
  public CompletableFuture<String> getStringValueAsync(String path) {
    return CompletableFuture.supplyAsync(
        () -> rootNode.node((Object[]) path.split("\\.")).getString());
  }
}
