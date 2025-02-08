/* Licensed under Apache-2.0 2025. */
package org.vicky.utilities.ContextLogger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.utilities.ANSIColor;
import org.vicky.vicky_utils;

/**
 * ContextLogger provides a structured logging utility with ANSI color formatting
 * for both plugin-specific and global Bukkit logging.
 * <p>
 * This logger allows messages to be formatted with a context tag,
 * which includes a context type and a context name, as well as
 * additional formatting such as color and post effects (e.g., bold, italic).
 * </p>
 */
public class ContextLogger {
  private final ContextType context;
  private final String contextName;
  private final JavaPlugin plugin;

  /**
   * Constructs a ContextLogger with the specified context type and context name.
   * The plugin instance is retrieved via {@code vicky_utils.getPlugin()}.
   *
   * @param context     The context type (e.g., SYSTEM, FEATURE, HIBERNATE)
   * @param contextName A name representing the logging context (converted to uppercase)
   */
  public ContextLogger(ContextType context, String contextName) {
    this.context = context;
    this.plugin = vicky_utils.getPlugin();
    this.contextName = contextName.toUpperCase();
  }

  /**
   * Constructs a ContextLogger with the specified context type, context name, and plugin instance.
   *
   * @param context     The context type (e.g., SYSTEM, FEATURE, HIBERNATE)
   * @param contextName A name representing the logging context (converted to uppercase)
   * @param plugin      The JavaPlugin instance associated with this logger
   */
  public ContextLogger(ContextType context, String contextName, JavaPlugin plugin) {
    this.context = context;
    this.plugin = plugin;
    this.contextName = contextName.toUpperCase();
  }

  /**
   * Logs a message to the plugin logger with an optional error flag.
   * The context tag is formatted in red if the message is an error, or in cyan otherwise.
   *
   * @param message The message to log
   * @param isError If true, the message is treated as an error (red formatting); otherwise, cyan is used.
   */
  public void print(String message, boolean isError) {
    String contextTag =
            "[" + ANSIColor.colorize((isError ? "red" : "cyan") + "[" + context + "-" + contextName + "]") + "] ";
    String finalContext =
            contextTag + (isError ? ANSIColor.colorize(message, ANSIColor.RED) : message);
    plugin.getLogger().info(finalContext);
  }

  /**
   * Logs a message to the plugin logger using the default cyan context formatting.
   *
   * @param message The message to log.
   */
  public void print(String message) {
    String contextTag =
            "[" + ANSIColor.colorize("cyan[" + context + "-" + contextName + "]") + "] ";
    String finalContext = contextTag + message;
    plugin.getLogger().info(finalContext);
  }

  /**
   * Logs a message to the plugin logger with a specified log type.
   * The log type determines the color formatting for the context tag and the message.
   *
   * @param message The message to log.
   * @param type    The log type, which determines the color used for formatting.
   */
  public void print(String message, LogType type) {
    String contextTag =
            "[" + ANSIColor.colorize(type.color + "[" + context + "-" + contextName + "]") + "] ";
    String finalContext = contextTag + ANSIColor.colorize(type.color + "[" + message + "]");
    plugin.getLogger().info(finalContext);
  }

  /**
   * Logs a message to the plugin logger with a specified log type and a post-formatting effect.
   * The post effect is applied to the message.
   *
   * @param message The message to log.
   * @param type    The log type which determines the base color for formatting.
   * @param effect  The post-formatting effect to apply (e.g., bold, italic, underline).
   */
  public void print(String message, LogType type, LogPostType effect) {
    String contextTag =
            "[" + ANSIColor.colorize(type.color + "[" + context + "-" + contextName + "]") + "] ";
    String finalContext =
            contextTag + ANSIColor.colorize(effect.effect + "[" + type.color + "[" + message + "]" + "]");
    plugin.getLogger().info(finalContext);
  }

  /**
   * Logs a message to the plugin logger with a specified log type.
   * Optionally, the message itself can be affected by the log type's color formatting.
   *
   * @param message             The message to log.
   * @param type                The log type which determines the color formatting of the context tag.
   * @param shouldAffectMessage If true, the message is formatted with the log type's color; otherwise, it is not.
   */
  public void print(String message, LogType type, boolean shouldAffectMessage) {
    String contextTag =
            "[" + ANSIColor.colorize(type.color + "[" + context + "-" + contextName + "]") + "] ";
    String finalContext;
    if (shouldAffectMessage) {
      finalContext = contextTag + ANSIColor.colorize(type.color + "[" + message + "]");
    } else {
      finalContext = contextTag + message;
    }
    plugin.getLogger().info(finalContext);
  }

  /**
   * Logs a message to the plugin logger with a specified log type and post-formatting effect,
   * with an option to affect the message formatting.
   *
   * @param message             The message to log.
   * @param type                The log type which determines the base color for formatting the context tag.
   * @param effect              The post-formatting effect to apply.
   * @param shouldAffectMessage If true, the message is additionally formatted with the log type's color; otherwise, only the effect is applied.
   */
  public void print(String message, LogType type, LogPostType effect, boolean shouldAffectMessage) {
    String contextTag =
            "[" + ANSIColor.colorize(type.color + "[" + context + "-" + contextName + "]") + "] ";
    String finalContext;
    if (shouldAffectMessage) {
      finalContext =
              contextTag + ANSIColor.colorize(effect.effect + "[" + type.color + "[" + message + "]" + "]");
    } else {
      finalContext = contextTag + ANSIColor.colorize(effect.effect + "[" + message + "]");
    }
    plugin.getLogger().info(finalContext);
  }

  /**
   * Logs a message to the global Bukkit logger with an optional error flag.
   * The context tag is formatted in red if the message is an error, or in cyan otherwise.
   *
   * @param message The message to log.
   * @param isError If true, the message is treated as an error (red formatting); otherwise, cyan is used.
   */
  public void printBukkit(String message, boolean isError) {
    String contextTag =
            "[" + ANSIColor.colorize((isError ? "red" : "cyan") + "[" + context + "-" + contextName + "]") + "] ";
    String finalContext =
            contextTag + (isError ? ANSIColor.colorize(message, ANSIColor.RED) : message);
    Bukkit.getLogger().info(finalContext);
  }

  /**
   * Logs a message to the global Bukkit logger with default cyan context formatting.
   *
   * @param message The message to log.
   */
  public void printBukkit(String message) {
    String contextTag =
            "[" + ANSIColor.colorize("cyan[" + context + "-" + contextName + "]") + "] ";
    String finalContext = contextTag + message;
    Bukkit.getLogger().info(finalContext);
  }

  /**
   * Logs a message to the global Bukkit logger using a specified log type.
   * The log type determines the color formatting for the context tag and message.
   *
   * @param message The message to log.
   * @param type    The log type which determines the color used for formatting.
   */
  public void printBukkit(String message, LogType type) {
    String contextTag =
            "[" + ANSIColor.colorize(type.color + "[" + context + "-" + contextName + "]") + "] ";
    String finalContext = contextTag + ANSIColor.colorize(type.color + "[" + message + "]");
    Bukkit.getLogger().info(finalContext);
  }

  /**
   * Logs a message to the global Bukkit logger using a specified log type and post-formatting effect.
   * The post effect is applied to the message.
   *
   * @param message The message to log.
   * @param type    The log type which determines the base color for formatting.
   * @param effect  The post-formatting effect to apply (e.g., bold, italic, underline).
   */
  public void printBukkit(String message, LogType type, LogPostType effect) {
    String contextTag =
            "[" + ANSIColor.colorize(type.color + "[" + context + "-" + contextName + "]") + "] ";
    String finalContext =
            contextTag + ANSIColor.colorize(effect.effect + "[" + type.color + "[" + message + "]" + "]");
    Bukkit.getLogger().info(finalContext);
  }

  /**
   * Logs a message to the global Bukkit logger using a specified log type.
   * Optionally, the message itself can be affected by the log type's color formatting.
   *
   * @param message             The message to log.
   * @param type                The log type which determines the color formatting of the context tag.
   * @param shouldAffectMessage If true, the message is formatted with the log type's color; otherwise, it is not.
   */
  public void printBukkit(String message, LogType type, boolean shouldAffectMessage) {
    String contextTag =
            "[" + ANSIColor.colorize(type.color + "[" + context + "-" + contextName + "]") + "] ";
    String finalContext;
    if (shouldAffectMessage) {
      finalContext = contextTag + ANSIColor.colorize(type.color + "[" + message + "]");
    } else {
      finalContext = contextTag + message;
    }
    Bukkit.getLogger().info(finalContext);
  }

  /**
   * Logs a message to the global Bukkit logger using a specified log type, post-formatting effect,
   * and an option to affect the message formatting.
   *
   * @param message             The message to log.
   * @param type                The log type which determines the base color for the context tag.
   * @param effect              The post-formatting effect to apply (e.g., bold, italic, underline).
   * @param shouldAffectMessage If true, the message is additionally formatted with the log type's color; otherwise, only the effect is applied.
   */
  public void printBukkit(String message, LogType type, LogPostType effect, boolean shouldAffectMessage) {
    String contextTag =
            "[" + ANSIColor.colorize(type.color + "[" + context + "-" + contextName + "]") + "] ";
    String finalContext;
    if (shouldAffectMessage) {
      finalContext =
              contextTag + ANSIColor.colorize(effect.effect + "[" + type.color + "[" + message + "]" + "]");
    } else {
      finalContext = contextTag + ANSIColor.colorize(effect.effect + "[" + message + "]");
    }
    Bukkit.getLogger().info(finalContext);
  }

  /**
   * Enumeration defining different logging contexts.
   */
  public enum ContextType {
    /**
     * System-level logging context.
     */
    SYSTEM,
    /**
     * Sub-system-level logging context.
     * Usually used in cases of logs for a class of a system like:
     * <pre>
     *     class SystemA extends SystemB {
     *
     *     }
     * </pre>
     */
    SUB_SYSTEM,
    /**
     * Feature-level logging context.
     */
    FEATURE,
    /**
     * A sub feature level logging context.
     * Usually used in cases of logs for a class of a feature like:
     * <pre>
     *     class FeatureA extends FeatureB {
     *
     *     }
     * </pre>
     */
    MINI_FEATURE,
    /**
     * Hibernate-related logging context.
     */
    HIBERNATE
  }

  /**
   * Enumeration defining different log message types with associated ANSI color codes.
   */
  public enum LogType {
    /**
     * Represents error messages (red).
     */
    ERROR("red"),
    /**
     * Represents warning messages (yellow).
     */
    WARNING("yellow"),
    /**
     * Represents success messages (green).
     */
    SUCCESS("green"),
    /**
     * Represents pending messages (orange).
     */
    PENDING("orange"),
    /**
     * Represents basic messages (cyan).
     */
    BASIC("cyan"),
    /**
     * Represents plain messages (white).
     */
    PLAIN("white"),
    /**
     * Represents ambient messages (purple).
     */
    AMBIENCE("purple");

    /**
     * The ANSI color code for the log type.
     */
    public final String color;

    LogType(String color) {
      this.color = color;
    }
  }

  /**
   * Enumeration defining post-formatting effects for log messages.
   */
  public enum LogPostType {
    /**
     * Underlines the log message.
     */
    UNDERLINE("underline"),
    /**
     * Strikes through the log message.
     */
    STRIKETHROUGH("strikethrough"),
    /**
     * Applies both bold and italic formatting to the log message.
     */
    BOLD_ITALIC("bold_italic"),
    /**
     * Applies italic formatting to the log message.
     */
    ITALIC("italic"),
    /**
     * Applies bold formatting to the log message.
     */
    BOLD("bold");

    /**
     * The effect code used for formatting the log message.
     */
    public final String effect;

    LogPostType(String effect) {
      this.effect = effect;
    }
  }
}
