/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.PlatformScheduler;
import org.vicky.utilities.ContextLogger.ContextLogger;

/**
 * A yaml based config manager
 */
public class ConfigManager {

  public ConfigurationLoader<CommentedConfigurationNode> loader;
  public CommentedConfigurationNode rootNode;
  public ContextLogger logger = new ContextLogger(ContextLogger.ContextType.SYSTEM, "CONFIG-YML");
  public ConfigurationOptions options;
  public boolean shouldLog = true;
  final ObjectMapper.Factory factory = ObjectMapper.factoryBuilder().build();
  private final PlatformScheduler scheduler = PlatformPlugin.scheduler();

  /**
   * Creates the config in the path specified with the parent dir being the {@link PlatformPlugin} datafolder {@link PlatformPlugin#dataFolder()}
   *
   * @param path The path of the config file (.yml)
   */
  public ConfigManager(String path) {
    createConfig(path);
  }

  /**
   * Creates the config in the path specified with the parent dir being the {@link PlatformPlugin} datafolder {@link PlatformPlugin#dataFolder()}
   *
   * @param configFile The path of the config file (.yml)
   */
  public ConfigManager(File configFile) {
    createPathedConfig(configFile.getPath());
  }

  /**
   * Creates the config in the path specified.
   * @param configFile The path of the config file (.yml)
   * @param shouldLog Specifies weather the context logger should give out logs on steps taken
   */
  public ConfigManager(File configFile, boolean shouldLog) {
    createPathedConfig(configFile.getPath());
    this.shouldLog = shouldLog;
  }

  /**
   * Creates the config in the path specified with the parent dir being the {@link PlatformPlugin} datafolder {@link PlatformPlugin#dataFolder()}
   *
   * @param path The path of the config file (.yml)
   * @param shouldLog Specifies weather the context logger should give out logs on steps taken
   */
  public ConfigManager(String path, boolean shouldLog) {
    this.shouldLog = shouldLog;
    createConfig(path);
  }

  /**
   * Creates the config manager classwith no associated config
   *
   * @param shouldLog Specifies weather the context logger should give out logs on steps taken
   */
  public ConfigManager(boolean shouldLog) {
    this.shouldLog = shouldLog;
  }

  // Create or load the config file
  public void createConfig(String path) {
    File configFile = new File(PlatformPlugin.dataFolder(), path);

    options =
        YamlConfigurationLoader.builder().indent(2).nodeStyle(NodeStyle.BLOCK).defaultOptions().serializers(builder -> builder.registerAnnotatedObjects(factory));

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
          logger.print("Created new config file: " + path);
        }
        loadConfigValues();
      } catch (IOException e) {
        logger.print(
            "Failed to create new config file: " + e.getMessage(), ContextLogger.LogType.ERROR);
      }
    } else {
      loadConfigValues();
    }
    new Thread(
            () -> {
              try {
                WatchService watcher = FileSystems.getDefault().newWatchService();
                Path configDir = PlatformPlugin.dataFolder().toPath();
                configDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                  WatchKey key = watcher.take();
                  for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.context().toString().equalsIgnoreCase(path)) {
                      scheduler.runMain(
                              () -> {
                                reloadAndUpdateStatics();
                                logger.print(
                                    "Config auto-reloaded due to file change.",
                                    ContextLogger.LogType.BASIC);
                              });
                    }
                  }
                  key.reset();
                }
              } catch (IOException | InterruptedException e) {
                logger.print("Error setting up config file watcher: " + e.getMessage(), true);
                e.printStackTrace();
              }
            })
        .start();
  }

  public void createPathedConfig(String path) {
    File configFile = new File(path);

    options =
        YamlConfigurationLoader.builder().indent(2).nodeStyle(NodeStyle.BLOCK).defaultOptions().serializers(builder -> builder.registerAnnotatedObjects(factory));

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
          logger.print("Created new config file: " + path);
        }
        loadConfigValues();
      } catch (IOException e) {
        logger.print(
            "Failed to create new config file: " + e.getMessage(), ContextLogger.LogType.ERROR);
      }
    } else {
      loadConfigValues();
    }
  }

  public void loadConfig(String path, String file) {
    File configFile = new File(path, file);

    options = YamlConfigurationLoader.builder().defaultOptions().serializers(builder -> builder.registerAnnotatedObjects(factory));

    loader = YamlConfigurationLoader.builder().path(configFile.toPath()).build();

    if (!configFile.exists()) {
      if (shouldLog) {
        logger.print("File " + file + "does not exist.", ContextLogger.LogType.ERROR);
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
        logger.print("Config loaded successfully.", ContextLogger.LogType.AMBIENCE);
      }
    } catch (Exception e) {
      logger.print(
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
            logger.print(
                configFileName
                    + " Has been successfully loaded from zip-file: "
                    + zipFilePath.getFileName());
        }
      } else {
        if (shouldLog)
          logger.print(
              "Could not find the required '"
                  + configFileName
                  + "' config file inside zip-file: "
                  + zipFilePath.getFileName());
      }
    } catch (Exception e) {
      logger.print(
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
      logger.print("Could not save config.yml!", ContextLogger.LogType.ERROR);
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
      logger.print(
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
        logger.print("Config value set for " + key + ": " + values);
      }
    } catch (Exception e) {
      logger.print(
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
      logger.print(
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
      logger.print(
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
      logger.print("Failed to add config: " + key, ContextLogger.LogType.WARNING);
    }
    saveConfig(); // Save changes to the config file
  }

  public void reloadAndUpdateStatics() {
    loadConfigValues();
  }
}
