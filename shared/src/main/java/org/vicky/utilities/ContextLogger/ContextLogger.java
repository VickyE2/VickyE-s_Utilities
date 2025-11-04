/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.ContextLogger;

import org.vicky.platform.PlatformLogger;
import org.vicky.platform.defaults.DefaultPlatformLogger;
import org.vicky.utilities.ANSIColor;

import java.util.ArrayList;
import java.util.List;

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
  protected final ContextType context;
  protected final String contextName;
  protected final PlatformLogger logger;

  /**
   * Constructs a ContextLogger with the specified context type and context name.
   * The plugin instance is retrieved via {@code vicky_utils.getPlugin()}.
   *
   * @param context     The context type (e.g., SYSTEM, FEATURE, HIBERNATE)
   * @param contextName A name representing the logging context (converted to uppercase)
   */
  public ContextLogger(ContextType context, String contextName, PlatformLogger logger) {
    this.context = context;
    this.contextName = contextName.toUpperCase();
    this.logger = logger;
  }

  public ContextLogger(ContextType context, String contextName) {
    this.context = context;
    this.contextName = contextName.toUpperCase();
    this.logger = new DefaultPlatformLogger(contextName);
  }

  public static String replacePlaceholders(String input, List<Integer> indices, List<String> replacements) {
    StringBuilder sb = new StringBuilder();
    int placeholderIndex = 0; // counts {} occurrences
    int replacementIndex = 0; // where we are in replacements list

    for (int i = 0; i < input.length(); i++) {
      if (i + 1 < input.length() && input.charAt(i) == '{' && input.charAt(i + 1) == '}') {
        // check if this {} is one we should replace
        if (indices.contains(placeholderIndex)) {
          sb.append(replacements.get(replacementIndex++));
        } else {
          sb.append("{}"); // leave as-is
        }
        placeholderIndex++;
        i++; // skip over '}'
      } else {
        sb.append(input.charAt(i));
      }
    }
    return sb.toString();
  }

  public static String replaceAllOrdered(String input, List<String> replacements) {
    StringBuilder sb = new StringBuilder();
    int replacementIndex = 0;

    for (int i = 0; i < input.length(); i++) {
      if (i + 1 < input.length() && input.charAt(i) == '{' && input.charAt(i + 1) == '}') {
        if (replacementIndex < replacements.size()) {
          sb.append(replacements.get(replacementIndex++));
        } else {
          sb.append("{}"); // no replacement left
        }
        i++; // skip '}'
      } else {
        sb.append(input.charAt(i));
      }
    }

    return sb.toString();
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
    logger.info(finalContext);
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
        "["
            + ANSIColor.colorize(
                (isError ? "red" : "cyan") + "[" + context + "-" + contextName + "]")
            + "] ";
    String finalContext =
        contextTag + (isError ? ANSIColor.colorize(message, ANSIColor.RED) : message);
    if (isError) logger.error(finalContext);
    else logger.info(finalContext);
  }

  /**
   * Logs a message to the plugin logger using the default cyan context formatting.
   * This takes an array of object arguments
   * They will be replaced in the message by the {} placeholder
   *
   * @param message The message to log.
   */
  public void print(String message, boolean isError, Object... args) {
    List<String> finalised = new ArrayList<>();
    for (var arg : args) {
      finalised.add(arg.toString());
    }
    String contextTag =
            "["
                    + ANSIColor.colorize(
                    (isError ? "red" : "cyan") + "[" + context + "-" + contextName + "]")
                    + "] ";
    message = replaceAllOrdered(message, finalised);
    String finalContext =
            contextTag + (isError ? ANSIColor.colorize(message, ANSIColor.RED) : message);
    if (isError) logger.error(finalContext);
    else logger.info(finalContext);
  }

  /**
   * Logs a message to the plugin logger using the default cyan context formatting.
   * This takes an array of object arguments
   * They will be replaced in the message by the {} placeholder
   *
   * @param message The message to log.
   */
  public void print(String message, Object... args) {
    List<String> finalised = new ArrayList<>();
    for (var arg : args) {
      finalised.add(arg.toString());
    }

    String contextTag =
            "[" + ANSIColor.colorize("cyan[" + context + "-" + contextName + "]") + "] ";
    String finalContext = contextTag + replaceAllOrdered(message, finalised);
    logger.info(finalContext);
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
    if (type.equals(LogType.WARNING)) logger.warn(finalContext);
    else if (type.equals(LogType.ERROR)) logger.error(finalContext);
    else if (type.equals(LogType.AMBIENCE)) logger.debug(finalContext);
    else logger.info(finalContext);
  }

  /**
   * Logs a message to the plugin logger using the default cyan context formatting.
   * This takes an array of object arguments
   * They will be replaced in the message by the {} placeholder
   *
   * @param message The message to log.
   */
  public void print(String message, LogType type, Object... args) {
    List<String> finalised = new ArrayList<>();
    for (var arg : args) {
      finalised.add(arg.toString());
    }
    message = replaceAllOrdered(message, finalised);
    print(message, type);
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
        contextTag
            + ANSIColor.colorize(effect.effect + "[" + type.color + "[" + message + "]" + "]");
    if (type.equals(LogType.WARNING)) logger.warn(finalContext);
    else if (type.equals(LogType.ERROR)) logger.error(finalContext);
    else if (type.equals(LogType.AMBIENCE)) logger.debug(finalContext);
    else logger.info(finalContext);
  }

  /**
   * Logs a message to the plugin logger using the default cyan context formatting.
   * This takes an array of object arguments
   * They will be replaced in the message by the {} placeholder
   *
   * @param message The message to log.
   */
  public void print(String message, LogType type, LogPostType effect, Object... args) {
    List<String> finalised = new ArrayList<>();
    for (var arg : args) {
      finalised.add(arg.toString());
    }
    message = replaceAllOrdered(message, finalised);
    print(message, type, effect);
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
    if (type.equals(LogType.WARNING)) logger.warn(finalContext);
    else if (type.equals(LogType.ERROR)) logger.error(finalContext);
    else if (type.equals(LogType.AMBIENCE)) logger.debug(finalContext);
    else logger.info(finalContext);
  }

  /**
   * Logs a message to the plugin logger using the default cyan context formatting.
   * This takes an array of object arguments
   * They will be replaced in the message by the {} placeholder
   *
   * @param message The message to log.
   */
  public void print(String message, LogType type, boolean shouldAffectMessage, Object... args) {
    List<String> finalised = new ArrayList<>();
    for (var arg : args) {
      finalised.add(arg.toString());
    }
    message = replaceAllOrdered(message, finalised);
    print(message, type, shouldAffectMessage);
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
          contextTag
              + ANSIColor.colorize(effect.effect + "[" + type.color + "[" + message + "]" + "]");
    } else {
      finalContext = contextTag + ANSIColor.colorize(effect.effect + "[" + message + "]");
    }
    if (type.equals(LogType.WARNING)) logger.warn(finalContext);
    else if (type.equals(LogType.ERROR)) logger.error(finalContext);
    else if (type.equals(LogType.AMBIENCE)) logger.debug(finalContext);
    else logger.info(finalContext);
  }

  /**
   * Logs a message to the plugin logger using the default cyan context formatting.
   * This takes an array of object arguments
   * They will be replaced in the message by the {} placeholder
   *
   * @param message The message to log.
   */
  public void print(String message, LogType type, LogPostType effect, boolean shouldAffectMessage, Object... args) {
    List<String> finalised = new ArrayList<>();
    for (var arg : args) {
      finalised.add(arg.toString());
    }
    message = replaceAllOrdered(message, finalised);
    print(message, type, effect, shouldAffectMessage);
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
    HIBERNATE,
    /**
     * Ecosystem-related logging context.
     */
    COMMUNICATION,

    /**
     * Storage-related logging context.
     */
    REGISTRY
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
