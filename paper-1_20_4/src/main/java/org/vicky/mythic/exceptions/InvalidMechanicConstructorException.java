/* Licensed under Apache-2.0 2024. */
package org.vicky.mythic.exceptions;

/**
 * Thrown when a mechanic class does not have a valid constructor,
 * or if instantiation via reflection fails.
 */
public class InvalidMechanicConstructorException extends RuntimeException {

  public InvalidMechanicConstructorException() {
    super();
  }

  public InvalidMechanicConstructorException(String message) {
    super(message);
  }

  public InvalidMechanicConstructorException(String message, String mechanicName) {
    super(message + " Caused by mechanic with name " + mechanicName);
  }

  public InvalidMechanicConstructorException(String message, String mechanicName, Throwable cause) {
    super(message + " Caused by mechanic with name " + mechanicName, cause);
  }

  public InvalidMechanicConstructorException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidMechanicConstructorException(Throwable cause) {
    super(cause);
  }
}
