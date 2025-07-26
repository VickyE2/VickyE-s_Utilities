/* Licensed under Apache-2.0 2024. */
package org.vicky.music.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A music track is a collection of MusicEvents.
 */
public class MusicTrack {
  private final List<MusicEvent> events = new ArrayList<>();

  /**
   * Adds a music event to the track.
   *
   * @param event the MusicEvent to add.
   */
  public void addEvent(MusicEvent event) {
    events.add(event);
    events.sort(Comparator.comparingLong(MusicEvent::timeOffset));
  }

  /**
   * Returns an unmodifiable list of MusicEvents in this track.
   *
   * @return the list of MusicEvents.
   */
  public List<MusicEvent> getEvents() {
    return Collections.unmodifiableList(events);
  }
}
