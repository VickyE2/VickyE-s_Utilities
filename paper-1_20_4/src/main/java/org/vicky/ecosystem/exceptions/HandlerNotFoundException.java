/* Licensed under Apache-2.0 2024. */
package org.vicky.ecosystem.exceptions;

/**
 * Thrown when a requested communication handler could not be found
 * for a given plugin or key.
 */
public class HandlerNotFoundException extends RuntimeException {
  /**
   * Constructs a new exception with a detailed message.
   *
   * @param pluginName the name of the target plugin
   * @param key        the communication key that was not found
   */
  public HandlerNotFoundException(String pluginName, String key) {
    super("Handler not found for plugin '" + pluginName + "' with key '" + key + "'");
  }

  /**
   * Constructs a new exception with a custom message.
   *
   * @param message the custom message
   */
  public HandlerNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with a message and cause.
   *
   * @param message the message
   * @param cause   the cause
   */
  public HandlerNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with a cause.
   *
   * @param cause the cause
   */
  public HandlerNotFoundException(Throwable cause) {
    super(cause);
  }
}
