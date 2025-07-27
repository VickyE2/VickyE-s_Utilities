/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import org.vicky.platform.utils.Vec3;
import org.vicky.platform.world.PlatformLocation;

public class ParticleTypeEffect {

  public static PlatformLocation[] LINE(PlatformLocation origin, double radius, int particleCount) {
    PlatformLocation[] positions = new PlatformLocation[particleCount];
    for (int i = 0; i < particleCount; i++) {
      double x = origin.getX() + radius;
      double z = origin.getZ() + radius;
      double y = origin.getY();
      positions[i] = new PlatformLocation(origin.getWorld(), x, y, z);
    }
    return positions;
  }

  public static PlatformLocation[] HELIX(
      PlatformLocation origin, double radius, double heightStep, double angle, int particleCount) {
    PlatformLocation[] positions = new PlatformLocation[particleCount];
    double step = 2 * Math.PI / particleCount; // Adjust step based on the number of points

    for (int i = 0; i < particleCount; i++) {
      double theta = angle + i * step;

      // Calculate the helix in local coordinates
      double x = radius * Math.cos(theta);
      double z = radius * Math.sin(theta);
      double y = i * heightStep / particleCount;

      // Create a vector for the point in local space
      Vec3 point = new Vec3(x, y, z);

      // Set the position
      positions[i] = (PlatformLocation) origin.clone().add(point);
    }

    return positions;
  }

  public static PlatformLocation[] RIPPLES(
      PlatformLocation origin,
      double radius,
      double angleStep,
      double angle,
      int particleCount,
      SpacingMode spacingMode,
      int numCircles) {
    if (numCircles <= 0) {
      return new PlatformLocation[0]; // Return an empty array
    }

    PlatformLocation[] positions = new PlatformLocation[particleCount];
    int minPointsPerCircle = 6; // Minimum number of points per circle
    int pointsPerCircle = particleCount / numCircles;

    // Generate the ripple effect
    for (int circleIndex = 0; circleIndex < numCircles; circleIndex++) {
      int currentCirclePoints = Math.max(minPointsPerCircle, pointsPerCircle);

      // Calculate the size of the circle based on the angle parameter
      double sizeFactor =
          Math.max(
              0.4 * radius, Math.min(1.2 * radius, angle / 360.0)); // Normalize angle to 0-1 range
      double circleRadius;
      if (spacingMode == SpacingMode.LINEAR) {
        circleRadius = radius * (circleIndex + 1) / numCircles;
      } else { // EXPONENTIAL
        circleRadius = radius * (1.0 + Math.pow(circleIndex, 1.5) / Math.pow(numCircles, 1.5));
      }
      circleRadius *= sizeFactor; // Adjust circle radius based on size factor

      // Calculate angular step
      double thetaStep = 2 * Math.PI / currentCirclePoints;

      for (int i = 0; i < currentCirclePoints; i++) {
        double theta = i * thetaStep;

        // Create the ripple in local coordinates
        double x = circleRadius * Math.cos(theta);
        double z = circleRadius * Math.sin(theta);
        double y = 0; // Keep the ripple effect flat

        // Create a vector for the point in local space
        Vec3 point = new Vec3(x, y, z);

        // Set the position
        int index = circleIndex * currentCirclePoints + i;
        if (index < positions.length) {
          positions[index] = (PlatformLocation) origin.clone().add(point);
        }
      }
    }

    return positions;
  }

  public static PlatformLocation[] WAVY_LINE(
      PlatformLocation origin, double radius, double heightStep, double angle, int particleCount) {
    PlatformLocation[] positions = new PlatformLocation[particleCount];

    double step = 2 * Math.PI / particleCount; // Adjust step based on the number of points

    for (int i = 0; i < particleCount; i++) {
      double theta = angle + i * step;

      double x = origin.getX() + (radius * Math.cos(theta) * Math.sin(angle));
      double z = origin.getZ() + (radius * Math.sin(theta) * Math.cos(angle));
      double y = origin.getY() + (theta * heightStep);

      // Create a vector for the point in local space
      Vec3 point = new Vec3(x, y, z);

      // Set the position
      positions[i] = (PlatformLocation) origin.clone().add(point);
    }
    return positions;
  }

  public static PlatformLocation[] BURST_SPIRAL(
      PlatformLocation origin, double radius, double heightStep, double angle, int particleCount) {
    PlatformLocation[] positions = new PlatformLocation[particleCount];

    double step = 2 * Math.PI / particleCount; // Adjust step based on the number of points

    for (int i = 0; i < particleCount; i++) {
      double theta = angle + i * step;

      double spiralRadius =
          radius * (i / (double) particleCount); // Increasing radius for burst effect
      double x = origin.getX() + spiralRadius * Math.cos(theta);
      double z = origin.getZ() + spiralRadius * Math.sin(theta);
      double y = origin.getY() + theta * heightStep;

      // Create a vector for the point in local space
      Vec3 point = new Vec3(x, y, z);

      // Set the position
      positions[i] = (PlatformLocation) origin.clone().add(point);
    }
    return positions;
  }

  // Reuse the same rotateVectorToDirection function from the helix effect

  // Function to rotate a vector to align with a given direction

  public static PlatformLocation[] CONVERGING_LINES(
      PlatformLocation origin, double radius, double heightStep, double angle, int particleCount) {
    PlatformLocation[] positions = new PlatformLocation[particleCount];

    double step = 2 * Math.PI / particleCount; // Adjust step based on the number of points

    for (int i = 0; i < particleCount; i++) {
      double theta = angle + (i / (double) particleCount) * 2 * Math.PI;
      double t = (i / (double) particleCount); // A value between 0 and 1
      double x = origin.getX() + (radius - radius * t) * Math.cos(theta);
      double z = origin.getZ() + (radius - radius * t) * Math.sin(theta);
      double y = origin.getY() + theta * heightStep;

      // Create a vector for the point in local space
      Vec3 point = new Vec3(x, y, z);

      // Set the position
      positions[i] = (PlatformLocation) origin.clone().add(point);
    }
    return positions;
  }

  public static PlatformLocation[] FALLING_LEAVES(
      PlatformLocation origin, double radius, double heightStep, double angle, int particleCount) {
    PlatformLocation[] positions = new PlatformLocation[particleCount];

    double step = 2 * Math.PI / particleCount; // Adjust step based on the number of points

    for (int i = 0; i < particleCount; i++) {
      double theta = angle + (i / (double) particleCount) * 2 * Math.PI;
      double x = origin.getX() + radius * Math.cos(theta);
      double z = origin.getZ() + radius * Math.sin(theta);
      double y = origin.getY() - (i / (double) particleCount) * heightStep; // Falling effect
      // Create a vector for the point in local space
      Vec3 point = new Vec3(x, y, z);

      // Set the position
      positions[i] = (PlatformLocation) origin.clone().add(point);
    }
    return positions;
  }

  public static PlatformLocation[] EXPLODING_STARS(
      PlatformLocation origin, double radius, double heightStep, double angle, int particleCount) {
    PlatformLocation[] positions = new PlatformLocation[particleCount];

    double step = 2 * Math.PI / particleCount; // Adjust step based on the number of points

    for (int i = 0; i < particleCount; i++) {
      double theta = angle + (i / (double) particleCount) * 2 * Math.PI;
      double explosionRadius = radius * Math.random(); // Randomized explosion radius
      double x = origin.getX() + explosionRadius * Math.cos(theta);
      double z = origin.getZ() + explosionRadius * Math.sin(theta);
      double y = origin.getY() + heightStep; // Constant height for explosion effect

      // Create a vector for the point in local space
      Vec3 point = new Vec3(x, y, z);

      // Set the position
      positions[i] = (PlatformLocation) origin.clone().add(point);
    }
    return positions;
  }

  public static PlatformLocation[] PULSE_WAVES(
      PlatformLocation origin,
      double radius,
      double heightStep,
      double angle,
      int particleCount,
      double pFreq) {
    PlatformLocation[] positions = new PlatformLocation[particleCount];
    // Adjust step based on the number of points
    for (int i = 0; i < particleCount; i++) {
      double theta = angle + (i / (double) particleCount) * 2 * Math.PI;
      double waveRadius = radius * Math.sin(angle + i * pFreq); // Pulse effect
      double x = origin.getX() + waveRadius * Math.cos(theta);
      double z = origin.getZ() + waveRadius * Math.sin(theta);
      double y = origin.getY() + heightStep;

      // Create a vector for the point in local space
      Vec3 point = new Vec3(x, y, z);

      // Set the position
      positions[i] = (PlatformLocation) origin.clone().add(point);
    }
    return positions;
  }

  public static PlatformLocation[] OSCILLATING_RINGS(
      PlatformLocation origin,
      double radius,
      double heightStep,
      double angle,
      int particleCount,
      double rFreq) {
    PlatformLocation[] positions = new PlatformLocation[particleCount];

    double step = 2 * Math.PI / particleCount; // Adjust step based on the number of points

    for (int i = 0; i < particleCount; i++) {
      double theta = angle + (i / (double) particleCount) * 2 * Math.PI;
      double oscillationRadius = radius * Math.sin(angle + i * rFreq); // Oscillating effect
      double x = origin.getX() + oscillationRadius * Math.cos(theta);
      double z = origin.getZ() + oscillationRadius * Math.sin(theta);
      double y = origin.getY() + heightStep;

      // Create a vector for the point in local space
      Vec3 point = new Vec3(x, y, z);

      // Set the position
      positions[i] = (PlatformLocation) origin.clone().add(point);
    }
    return positions;
  }

  // Utility method to rotate a vector to align with the given direction
  public static Vec3 rotateVector(Vec3 offset, Vec3 direction) {
    // Calculate the angle of rotation around the Y-axis
    double yaw = Math.atan2(direction.getZ(), direction.getX()) - Math.PI / 2;

    // Create a rotation matrix for the Yaw rotation
    double cosYaw = Math.cos(yaw);
    double sinYaw = Math.sin(yaw);

    double rotatedX = offset.getX() * cosYaw - offset.getZ() * sinYaw;
    double rotatedZ = offset.getX() * sinYaw + offset.getZ() * cosYaw;

    // Return the rotated vector
    return new Vec3(rotatedX, offset.getY(), rotatedZ);
  }

  // Rotate a vector around a given axis by a specific angle
  private static Vec3 rotateAroundAxis(Vec3 v, Vec3 axis, double angle) {
    double cosTheta = Math.cos(angle);
    double sinTheta = Math.sin(angle);

    // Rodrigues' rotation formula
    return v.multiply(cosTheta)
        .add(axis.crossProduct(v).multiply(sinTheta))
        .add(axis.multiply(axis.dot(v) * (1 - cosTheta)));
  }

  public enum ParticleTypeEffects {
    LINE,
    HELIX,
    WAVY_LINE,
    BURST_SPIRAL,
    CONVERGING_LINES,
    RIPPLES,
    FALLING_LEAVES,
    EXPLODING_STARS,
    PULSE_WAVES,
    OSCILLATING_RINGS
  }

  public enum SpacingMode {
    LINEAR,
    EXPONENTIAL
  }
}
