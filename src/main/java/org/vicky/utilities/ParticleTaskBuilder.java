/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;

public class ParticleTaskBuilder {
  private long startTime = System.currentTimeMillis();
  private Arrow arrow;
  private double radiusH = 1.4;
  private double radiusM = 1.1;
  private double heightStep = 1;
  private Color headColor = Color.ORANGE;
  private Color transitionColorStart = Color.PURPLE;
  private Color transitionColorEnd = Color.BLUE;
  private int headCount = 20;
  private int middleCount = 20;
  private double spreadXH = 2;
  private double spreadYH = 2;
  private double spreadZH = 2;
  private double spreadXM = 1;
  private double spreadYM = 1;
  private double spreadZM = 1;
  private float speedH = 2;
  private float speedM = 1;
  private long lagBehind = 0;
  private double backwardVelocity = 2;
  private float sizeH = 5;
  private float sizeM = 2.7f;
  private Particle particleH = Particle.REDSTONE;
  private Particle particleM = Particle.REDSTONE;
  private ParticleTypeEffect.ParticleTypeEffects effectTypeH =
      ParticleTypeEffect.ParticleTypeEffects.HELIX;
  private ParticleTypeEffect.ParticleTypeEffects effectTypeM =
      ParticleTypeEffect.ParticleTypeEffects.PULSE_WAVES;
  private double rFreq = 0.5;
  private double pFreq = 0.2;
  private double angleStep = 5.0;
  private ParticleTypeEffect.SpacingMode spacingMode = ParticleTypeEffect.SpacingMode.LINEAR;
  private int circleNumber = 20;
  private float yaw = arrow.getYaw();
  private float pitch = arrow.getPitch();

  public ParticleTaskBuilder setStartTime(long startTime) {
    this.startTime = startTime;
    return this;
  }

  public ParticleTaskBuilder setArrow(Arrow arrow) {
    this.arrow = arrow;
    return this;
  }

  public ParticleTaskBuilder setRadiusH(double radiusH) {
    this.radiusH = radiusH;
    return this;
  }

  public ParticleTaskBuilder setRadiusM(double radiusM) {
    this.radiusM = radiusM;
    return this;
  }

  public ParticleTaskBuilder setHeightStep(double heightStep) {
    this.heightStep = heightStep;
    return this;
  }

  public ParticleTaskBuilder setHeadColor(Color headColor) {
    this.headColor = headColor;
    return this;
  }

  public ParticleTaskBuilder setTransitionColorStart(Color transitionColorStart) {
    this.transitionColorStart = transitionColorStart;
    return this;
  }

  public ParticleTaskBuilder setTransitionColorEnd(Color transitionColorEnd) {
    this.transitionColorEnd = transitionColorEnd;
    return this;
  }

  public ParticleTaskBuilder setHeadCount(int headCount) {
    this.headCount = headCount;
    return this;
  }

  public ParticleTaskBuilder setMiddleCount(int middleCount) {
    this.middleCount = middleCount;
    return this;
  }

  public ParticleTaskBuilder setSpreadXH(double spreadXH) {
    this.spreadXH = spreadXH;
    return this;
  }

  public ParticleTaskBuilder setSpreadYH(double spreadYH) {
    this.spreadYH = spreadYH;
    return this;
  }

  public ParticleTaskBuilder setSpreadZH(double spreadZH) {
    this.spreadZH = spreadZH;
    return this;
  }

  public ParticleTaskBuilder setSpreadXM(double spreadXM) {
    this.spreadXM = spreadXM;
    return this;
  }

  public ParticleTaskBuilder setSpreadYM(double spreadYM) {
    this.spreadYM = spreadYM;
    return this;
  }

  public ParticleTaskBuilder setSpreadZM(double spreadZM) {
    this.spreadZM = spreadZM;
    return this;
  }

  public ParticleTaskBuilder setSpeedH(float speedH) {
    this.speedH = speedH;
    return this;
  }

  public ParticleTaskBuilder setSpeedM(float speedM) {
    this.speedM = speedM;
    return this;
  }

  public ParticleTaskBuilder setLagBehind(long lagBehind) {
    this.lagBehind = lagBehind;
    return this;
  }

  public ParticleTaskBuilder setBackwardVelocity(double backwardVelocity) {
    this.backwardVelocity = backwardVelocity;
    return this;
  }

  public ParticleTaskBuilder setSizeH(float sizeH) {
    this.sizeH = sizeH;
    return this;
  }

  public ParticleTaskBuilder setSizeM(float sizeM) {
    this.sizeM = sizeM;
    return this;
  }

  public ParticleTaskBuilder setParticleH(Particle particleH) {
    this.particleH = particleH;
    return this;
  }

  public ParticleTaskBuilder setParticleM(Particle particleM) {
    this.particleM = particleM;
    return this;
  }

  public ParticleTaskBuilder setEffectTypeH(ParticleTypeEffect.ParticleTypeEffects effectTypeH) {
    this.effectTypeH = effectTypeH;
    return this;
  }

  public ParticleTaskBuilder setEffectTypeM(ParticleTypeEffect.ParticleTypeEffects effectTypeM) {
    this.effectTypeM = effectTypeM;
    return this;
  }

  public ParticleTaskBuilder setrFreq(double rFreq) {
    this.rFreq = rFreq;
    return this;
  }

  public ParticleTaskBuilder setpFreq(double pFreq) {
    this.pFreq = pFreq;
    return this;
  }

  public ParticleTaskBuilder setAngleStep(double angleStep) {
    this.angleStep = angleStep;
    return this;
  }

  public ParticleTaskBuilder setSpacingMode(ParticleTypeEffect.SpacingMode spacingMode) {
    this.spacingMode = spacingMode;
    return this;
  }

  public ParticleTaskBuilder setCircleNumber(int circleNumber) {
    this.circleNumber = circleNumber;
    return this;
  }

  public ParticleTaskBuilder setYaw(float yaw) {
    this.yaw = yaw;
    return this;
  }

  public ParticleTaskBuilder setPitch(float pitch) {
    this.pitch = pitch;
    return this;
  }

  public ParticleTask build() {
    return new ParticleTask(
        startTime,
        arrow,
        radiusH,
        radiusM,
        heightStep,
        headColor,
        transitionColorStart,
        transitionColorEnd,
        headCount,
        middleCount,
        spreadXH,
        spreadYH,
        spreadZH,
        spreadXM,
        spreadYM,
        spreadZM,
        speedH,
        speedM,
        lagBehind,
        backwardVelocity,
        sizeH,
        sizeM,
        particleH,
        particleM,
        effectTypeH,
        effectTypeM,
        rFreq,
        pFreq,
        angleStep,
        spacingMode,
        circleNumber,
        yaw,
        pitch);
  }
}
