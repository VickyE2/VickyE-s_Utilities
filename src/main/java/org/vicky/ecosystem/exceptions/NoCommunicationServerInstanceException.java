/* Licensed under Apache-2.0 2024. */
package org.vicky.ecosystem.exceptions;

/**
 * Thrown when a communication server instance is expected but not found.
 * This may occur if an attempt is made to register or communicate with a server
 * before it has been properly initialized.
 */
public class NoCommunicationServerInstanceException extends RuntimeException {

  /**
   * Constructs a new exception with a default message.
   */
  public NoCommunicationServerInstanceException() {
    super("No communication server instance found. Has it been initialized?");
  }

  /**
   * Constructs a new exception with a custom message.
   *
   * @param message the detailed error message
   */
  public NoCommunicationServerInstanceException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with a custom message and a cause.
   *
   * @param message the message describing the exception
   * @param cause   the root cause
   */
  public NoCommunicationServerInstanceException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with a cause only.
   *
   * @param cause the underlying reason for the exception
   */
  public NoCommunicationServerInstanceException(Throwable cause) {
    super(cause);
  }
}
