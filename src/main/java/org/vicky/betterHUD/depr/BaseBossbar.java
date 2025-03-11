/* Licensed under Apache-2.0 2024. */
package org.vicky.betterHUD.depr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * BaseBossbar is an abstract class that defines a customizable bossbar.
 * It supports:
 *  - A bossbar type and display name.
 *  - A primary texture and overlay.
 *  - Phase textures/overlays which change depending on the boss' health percentage.
 *  - Optional animation for the texture (frame durations in ticks).
 *
 * Extend this class and implement the render() method for your mod's rendering system.
 */
public abstract class BaseBossbar {
  protected BossbarType type;

  protected String name;
  protected String baseTexture;
  protected String baseOverlay;
  protected Map<Float, String> phaseTextures;
  protected Map<Float, String> phaseOverlays;
  protected List<Integer> textureFrameDurations;
  protected int currentTextureFrame;
  protected int tickCounter;

  /**
   * Constructs a BaseBossbar with the given parameters.
   *
   * @param type        The bossbar type.
   * @param name        The display name.
   * @param baseTexture The primary texture name (without ".png").
   * @param baseOverlay The primary overlay name (without ".png").
   */
  public BaseBossbar(BossbarType type, String name, String baseTexture, String baseOverlay) {
    this.type = type;
    this.name = name;
    this.baseTexture = baseTexture;
    this.baseOverlay = baseOverlay;

    // Use reverse order so that higher thresholds (closer to full health) are checked first.
    this.phaseTextures = new TreeMap<>(Collections.reverseOrder());
    this.phaseOverlays = new TreeMap<>(Collections.reverseOrder());

    this.textureFrameDurations = new ArrayList<>();
    this.currentTextureFrame = 0;
    this.tickCounter = 0;
  }

  /**
   * Adds a phase change for both texture and overlay.
   * When the boss' HP percentage is <= threshold, these will override the base assets.
   *
   * @param threshold The HP percentage threshold.
   * @param texture   The texture name (without ".png") for this phase.
   * @param overlay   The overlay name (without ".png") for this phase.
   */
  public void addPhase(float threshold, String texture, String overlay) {
    phaseTextures.put(threshold, texture);
    phaseOverlays.put(threshold, overlay);
  }

  /**
   * Adds a phase change for only the overlay.
   * When the boss' HP percentage is less than or equal to the threshold, these will override the base assets.
   *
   * @param threshold The HP percentage threshold.
   * @param overlay   The overlay name (without ".png") for this phase.
   */
  public void addOverlayPhase(float threshold, String overlay) {
    phaseOverlays.put(threshold, overlay);
  }

  /**
   * Adds a phase change for only the texture.
   * When the boss' HP percentage is less than or equal to the threshold, these will override the base assets.
   *
   * @param threshold The HP percentage threshold.
   * @param texture   The texture name (without ".png") for this phase.
   */
  public void addTexturePhase(float threshold, String texture) {
    phaseTextures.put(threshold, texture);
  }

  /**
   * Sets the animation frame durations for the bossbar texture.
   *
   * @param frameDurations A list of durations (in ticks) for each frame.
   */
  public void setTextureAnimationFrames(List<Integer> frameDurations) {
    this.textureFrameDurations = frameDurations;
  }

  /**
   * Returns the appropriate texture based on the current HP percentage.
   * It checks the phase textures first; if none match, it returns the base texture.
   *
   * @param hpPercentage The current HP percentage (0.0 to 100.0).
   * @return The texture name to use.
   */
  public String getTextureForHP(float hpPercentage) {
    for (Float threshold : phaseTextures.keySet()) {
      if (hpPercentage <= threshold) {
        return phaseTextures.get(threshold);
      }
    }
    return baseTexture;
  }

  /**
   * Returns the appropriate overlay based on the current HP percentage.
   *
   * @param hpPercentage The current HP percentage (0.0 to 100.0).
   * @return The overlay name to use.
   */
  public String getOverlayForHP(float hpPercentage) {
    for (Float threshold : phaseOverlays.keySet()) {
      if (hpPercentage <= threshold) {
        return phaseOverlays.get(threshold);
      }
    }
    return baseOverlay;
  }

  /**
   * This update method should be called every tick.
   * It updates the animation frame if the texture is animated.
   */
  public void update() {
    if (!textureFrameDurations.isEmpty()) {
      tickCounter++;
      int currentFrameDuration = textureFrameDurations.get(currentTextureFrame);
      if (tickCounter >= currentFrameDuration) {
        tickCounter = 0;
        currentTextureFrame = (currentTextureFrame + 1) % textureFrameDurations.size();
      }
    }
  }

  /**
   * Returns the current texture frame index.
   * This can be used by the rendering system to display the correct animation frame.
   *
   * @return The current animation frame index.
   */
  public int getCurrentTextureFrame() {
    return currentTextureFrame;
  }

  /**
   * Render the bossbar. The actual rendering code is context-specific and should be implemented
   * in a subclass, which would draw the bossbar at the given coordinates using the current textures.
   *
   * @param hpPercentage The current HP percentage (0.0 to 100.0).
   * @param x            The x-coordinate where the bossbar should be drawn.
   * @param y            The y-coordinate where the bossbar should be drawn.
   */
  public abstract void render(float hpPercentage, int x, int y);
}
