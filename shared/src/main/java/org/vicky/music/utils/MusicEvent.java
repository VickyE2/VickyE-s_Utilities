/* Licensed under Apache-2.0 2024. */
package org.vicky.music.utils;

import org.vicky.platform.utils.SoundCategory;

/**
 * Represents a single sound event in a music track.
 *
 * @param timeOffset in ticks (1 tick = 50ms)
 */
public record MusicEvent(
    long timeOffset,
    Sound sound,
    Integer pitch,
    float volume,
    SoundCategory category,
    MusicBuilder.NotePart part,
    Integer noteId) {
  /**
   * Constructs a MusicEvent.
   *
   * @param timeOffset the delay (in ticks) after the track start to play this sound.
   * @param sound      the sound to play.
   * @param pitch      the pitch of the sound.
   * @param volume     the volume of the sound.
   * @param category   the sound category.
   */
  public MusicEvent(
          long timeOffset, Sound sound, Integer pitch, float volume, SoundCategory category) {
    this(timeOffset, sound, pitch, volume, category, null, null);
  }

  /**
   * Constructs a MusicEvent.
   *
   * @param timeOffset the delay (in ticks) after the track start to play this sound.
   * @param sound      the sound to play.
   * @param pitch      the pitch of the sound.
   * @param volume     the volume of the sound.
   * @param category   the sound category.
   */
  public MusicEvent(
          long timeOffset, Sound sound, Integer pitch, float volume, SoundCategory category, Integer uid) {
    this(timeOffset, sound, pitch, volume, category, null, uid);
  }
}
