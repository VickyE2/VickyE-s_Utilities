/* Licensed under Apache-2.0 2024. */
package org.vicky.guiparent;

/**
 * GuiSpaceExceededException is thrown when an ItemConfigClass contains more items than the available slots
 * in the GUI.
 */
public class GuiSpaceExceededException extends RuntimeException {
  public GuiSpaceExceededException(String message) {
    super(message);
  }
}
