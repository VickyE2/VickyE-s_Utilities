/* Licensed under Apache-2.0 2024. */
package org.vicky.ecosystem.exceptions;

/**
 * Thrown when the target plugin specified in a communication request
 * cannot be found in the registered communicator server.
 */
public class TargetPluginNotFoundException extends RuntimeException {
  /**
   * Constructs the exception with the plugin name that was not found.
   *
   * @param pluginName the name of the plugin that could not be found
   */
  public TargetPluginNotFoundException(String pluginName, boolean justForChanging) {
    super("Target plugin not found: '" + pluginName + "'");
  }

  /**
   * Constructs the exception with a custom message.
   *
   * @param message the detailed exception message
   */
  public TargetPluginNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructs the exception with a message and cause.
   *
   * @param message the message describing the exception
   * @param cause   the underlying cause of the exception
   */
  public TargetPluginNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs the exception with a cause only.
   *
   * @param cause the root cause
   */
  public TargetPluginNotFoundException(Throwable cause) {
    super(cause);
  }
}
