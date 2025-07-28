/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities;

import org.vicky.platform.IColor;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.defaults.CommonParticle;
import org.vicky.platform.entity.PlatformArrow;
import org.vicky.platform.entity.PlatformParticle;
import org.vicky.platform.utils.Vec3;
import org.vicky.platform.world.PlatformLocation;

/**
 * ParticleTask is a {@link Runnable} that continuously spawns particles around
 * an arrow entity, creating various visual effects such as helices, waves,
 * bursts, and more.
 *
 * <p>
 * The task calculates positions for two groups of particles (head and middle)
 * based on several parameters, including radii, particle counts, speed, and
 * offsets. The particles are rotated around the arrow's position using a custom
 * rotation method.
 *
 * <p>
 * Future enhancements may include replacing the current rotation (based on yaw
 * and pitch) with a full quaternion-based rotation system for more accurate and
 * flexible orientation handling.
 *
 * @author
 */
public class ParticleTask implements Runnable {
	private volatile boolean running = true;
	private final PlatformArrow arrow;
	private final double radiusH;
	private final double radiusM;
	private final double heightStep;
	private final long startTime;
	private final IColor headColor;
	private final IColor transitionColorStart;
	private final IColor transitionColorEnd;
	private final int headCount;
	private final int middleCount;
	private final double spreadXH;
	private final double spreadYH;
	private final double spreadZH;
	private final double spreadXM;
	private final double spreadYM;
	private final double spreadZM;
	private final float speedH;
	private final float speedM;
	private final long lagBehind;
	private final double backwardVelocity;
	private final double rFreq;
	private final double pFreq;
	private final double angleStep;
	private final float sizeH;
	private final float sizeM;
	private final ParticleTypeEffect.SpacingMode spacingMode;
	private final int circleNumber;
	private final PlatformParticle particleH;
	private final PlatformParticle particleM;
	private final ParticleTypeEffect.ParticleTypeEffects effectTypeH;
	private final ParticleTypeEffect.ParticleTypeEffects effectTypeM;
	private final float yaw;
	private final float pitch;

	/**
	 * Constructs a new ParticleTask.
	 *
	 * @param startTime
	 *            The start time (in milliseconds) for the particle effect (used to
	 *            calculate elapsed time and animation angle).
	 * @param arrow
	 *            The Arrow entity around which the particles are spawned.
	 * @param radiusH
	 *            The horizontal radius for the head particles.
	 * @param radiusM
	 *            The horizontal radius for the middle particles.
	 * @param heightStep
	 *            The vertical increment between particle layers.
	 * @param headColor
	 *            The color used for head particles (for redstone or dust options).
	 * @param transitionColorStart
	 *            The starting color for transition particles.
	 * @param transitionColorEnd
	 *            The ending color for transition particles.
	 * @param headCount
	 *            The number of head particles to spawn.
	 * @param middleCount
	 *            The number of middle particles to spawn.
	 * @param spreadXH
	 *            The horizontal spread for head particles along X.
	 * @param spreadYH
	 *            The vertical spread for head particles along Y.
	 * @param spreadZH
	 *            The horizontal spread for head particles along Z.
	 * @param spreadXM
	 *            The horizontal spread for middle particles along X.
	 * @param spreadYM
	 *            The vertical spread for middle particles along Y.
	 * @param spreadZM
	 *            The horizontal spread for middle particles along Z.
	 * @param speedH
	 *            The speed parameter for head particles.
	 * @param speedM
	 *            The speed parameter for middle particles.
	 * @param lagBehind
	 *            A delay factor for positioning middle particles relative to the
	 *            arrow.
	 * @param backwardVelocity
	 *            A velocity component moving backward from the arrow.
	 * @param sizeH
	 *            The size of head particles.
	 * @param sizeM
	 *            The size of middle particles.
	 * @param particleH
	 *            The Particle type used for head particles.
	 * @param particleM
	 *            The Particle type used for middle particles.
	 * @param effectTypeH
	 *            The effect type for head particles (determines shape, e.g., LINE,
	 *            HELIX, etc.).
	 * @param effectTypeM
	 *            The effect type for middle particles.
	 * @param rFreq
	 *            The frequency modifier for oscillating effects (defaulted to 0.5
	 *            if 0).
	 * @param pFreq
	 *            The frequency modifier for pulse effects (defaulted to 0.2 if 0).
	 * @param angleStep
	 *            The step size for angle increments (defaulted to 1.0 if 0).
	 * @param spacingMode
	 *            The spacing mode for effects that require specific spacing.
	 * @param circleNumber
	 *            The number of circles used in certain effect types.
	 * @param yaw
	 *            The initial yaw angle for particle rotation.
	 * @param pitch
	 *            The initial pitch angle for particle rotation.
	 */
	public ParticleTask(long startTime, PlatformArrow arrow, double radiusH, double radiusM, double heightStep,
			IColor headColor, IColor transitionColorStart, IColor transitionColorEnd, int headCount, int middleCount,
			double spreadXH, double spreadYH, double spreadZH, double spreadXM, double spreadYM, double spreadZM,
			float speedH, float speedM, long lagBehind, double backwardVelocity, float sizeH, float sizeM,
			PlatformParticle particleH, PlatformParticle particleM, ParticleTypeEffect.ParticleTypeEffects effectTypeH,
			ParticleTypeEffect.ParticleTypeEffects effectTypeM, double rFreq, double pFreq, double angleStep,
			ParticleTypeEffect.SpacingMode spacingMode, int circleNumber, float yaw, float pitch) {

		this.arrow = arrow;
		this.radiusH = radiusH;
		this.radiusM = radiusM;
		this.heightStep = heightStep;
		this.headColor = headColor;
		this.transitionColorStart = transitionColorStart;
		this.transitionColorEnd = transitionColorEnd;
		this.headCount = headCount;
		this.middleCount = middleCount;
		this.spreadXH = spreadXH;
		this.spreadYH = spreadYH;
		this.spreadZH = spreadZH;
		this.spreadXM = spreadXM;
		this.spreadYM = spreadYM;
		this.spreadZM = spreadZM;
		this.speedH = speedH;
		this.speedM = speedM;
		this.sizeH = sizeH;
		this.sizeM = sizeM;
		this.spacingMode = spacingMode;
		this.circleNumber = circleNumber;
		this.particleH = particleH;
		this.particleM = particleM;
		this.lagBehind = lagBehind;
		this.rFreq = (rFreq == 0) ? 0.5 : rFreq; // Default to 0.5 if rFreq is 0
		this.pFreq = (pFreq == 0) ? 0.2 : pFreq; // Default to 0.2 if pFreq is 0
		this.angleStep = (angleStep == 0) ? 1.0 : angleStep; // Default to 1.0 if angleStep is 0
		this.backwardVelocity = backwardVelocity;
		this.startTime = startTime;
		this.effectTypeH = effectTypeH;
		this.effectTypeM = effectTypeM;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	// The following quaternion rotation methods have been commented out.
	// They represent an alternative approach using quaternion rotation, which can
	// be implemented in
	// the future.
	/*
	 * public void alignParticlesToArrow(PlatformLocation[] positions, Arrow arrow)
	 * { Vec3 direction = arrow.getLocation().getDirection().normalize(); Quaternion
	 * rotation = getRotationTo(new Vec3(0, 1, 0), direction); for (int i = 0; i <
	 * positions.length; i++) { Vec3 particleVector =
	 * positions[i].toVector().subtract(arrow.getLocation().toVector()); Vec3
	 * rotatedVector = rotateVectorByQuaternion(particleVector, rotation);
	 * positions[i].setX(arrow.getLocation().getX() + rotatedVector.getX());
	 * positions[i].setY(arrow.getLocation().getY() + rotatedVector.getY());
	 * positions[i].setZ(arrow.getLocation().getZ() + rotatedVector.getZ()); } }
	 * 
	 * public Quaternion getRotationTo(Vec3 from, Vec3 to) { Vec3 axis =
	 * from.clone().crossProduct(to).normalize(); double angle =
	 * Math.acos(from.clone().dot(to)); double sinHalfAngle = Math.sin(angle / 2);
	 * double cosHalfAngle = Math.cos(angle / 2); return new Quaternion(axis.getX()
	 * * sinHalfAngle, axis.getY() * sinHalfAngle, axis.getZ() * sinHalfAngle,
	 * cosHalfAngle); }
	 * 
	 * public Vec3 rotateVectorByQuaternion(Vec3 v, Quaternion q) { Quaternion p =
	 * new Quaternion(v.getX(), v.getY(), v.getZ(), 0); Quaternion qConjugate =
	 * q.conjugate(); Quaternion result = q.multiply(p).multiply(qConjugate); return
	 * new Vec3(result.getX(), result.getY(), result.getZ()); }
	 * 
	 * 
	 * Rotates a particle location around the arrow's location based on the arrow's
	 * velocity and rotation. <p> This method calculates a new location by first
	 * computing a relative position vector between the particle and the arrow, then
	 * applying yaw and pitch rotations based on the arrow's orientation. The method
	 * currently uses basic trigonometric rotation rather than a full
	 * quaternion-based approach. </p>
	 *
	 * @param particleLoc The original location of the particle.
	 * 
	 * @param arrowLoc The location of the arrow.
	 * 
	 * @param direction The direction vector (typically the arrow's velocity) used
	 * for rotation.
	 * 
	 * @return A new PlatformLocation that is the rotated position of the particle
	 * relative to the arrow.
	 * 
	 * private PlatformLocation rotateAroundArrow(PlatformLocation particleLoc,
	 * PlatformLocation arrowLoc, Vec3 direction) { Vec3 relativePos =
	 * particleLoc.toVector().subtract(arrowLoc.toVector()); direction =
	 * direction.normalize(); // Multiply relative position by the normalized
	 * direction to influence rotation (adjust as needed)
	 * relativePos.multiply(direction);
	 * 
	 * double yaw = Math.toRadians(arrowLoc.getYaw()); double pitch =
	 * Math.toRadians(arrowLoc.getPitch());
	 * 
	 * double xRot = relativePos.getX(); double yRot = relativePos.getY(); double
	 * zRot = relativePos.getZ();
	 * 
	 * // Rotate around Yaw (Horizontal rotation) double xPrime = xRot *
	 * Math.cos(-yaw) - zRot * Math.sin(-yaw); double zPrime = xRot * Math.sin(-yaw)
	 * + zRot * Math.cos(-yaw);
	 * 
	 * // Rotate around Pitch (Vertical rotation) double yPrime = yRot *
	 * Math.cos(pitch) - zPrime * Math.sin(pitch); zPrime = yRot * Math.sin(pitch) +
	 * zPrime * Math.cos(pitch);
	 * 
	 * Vec3 rotatedPos = new Vec3(xPrime, yPrime, zPrime); return
	 * arrowLoc.clone().add(rotatedPos); }
	 */

	/**
	 * The main task method that is executed repeatedly.
	 *
	 * <p>
	 * This method checks if the arrow is dead, calculates the elapsed time since
	 * the task started, determines the current animation angle, and spawns
	 * particles based on the configured effect types for both head and middle
	 * particles. The particles are rotated using the
	 * {@link #rotateAroundArrow(PlatformLocation, PlatformLocation, Vec3)} method.
	 */
	@Override
	public void run() {
		while (running) {
			if (arrow.isDead()) {
				running = false;
				return;
			}

			PlatformLocation loc = arrow.getLocation();
			long currentTime = System.currentTimeMillis();
			long elapsedTime = currentTime - startTime; // Calculate elapsed time
			double angle = (elapsedTime / 10.0);

			// Generate particle positions based on effect type for head and middle groups
			PlatformLocation[] headPositions;
			PlatformLocation[] middlePositions;

			// Determine head particle positions based on the head effect type
			switch (effectTypeH) {
				case LINE :
					headPositions = ParticleTypeEffect.LINE(loc.clone(), radiusH, headCount);
					break;
				case HELIX :
					headPositions = ParticleTypeEffect.HELIX(loc.clone(), radiusH, heightStep, angle, headCount);
					break;
				case WAVY_LINE :
					headPositions = ParticleTypeEffect.WAVY_LINE(loc.clone(), radiusH, heightStep, angle, headCount);
					break;
				case BURST_SPIRAL :
					headPositions = ParticleTypeEffect.BURST_SPIRAL(loc.clone(), radiusH, heightStep, angle, headCount);
					break;
				case CONVERGING_LINES :
					headPositions = ParticleTypeEffect.CONVERGING_LINES(loc.clone(), radiusH, heightStep, angle,
							headCount);
					break;
				case RIPPLES :
					headPositions = ParticleTypeEffect.RIPPLES(loc.clone(), radiusH, angleStep, angle, headCount,
							spacingMode, circleNumber);
					break;
				case FALLING_LEAVES :
					headPositions = ParticleTypeEffect.FALLING_LEAVES(loc.clone(), radiusH, heightStep, angle,
							headCount);
					break;
				case EXPLODING_STARS :
					headPositions = ParticleTypeEffect.EXPLODING_STARS(loc.clone(), radiusH, heightStep, angle,
							headCount);
					break;
				case PULSE_WAVES :
					headPositions = ParticleTypeEffect.PULSE_WAVES(loc.clone(), radiusH, heightStep, angle, headCount,
							pFreq);
					break;
				case OSCILLATING_RINGS :
					headPositions = ParticleTypeEffect.OSCILLATING_RINGS(loc.clone(), radiusH, heightStep, angle,
							headCount, rFreq);
					break;
				default :
					throw new IllegalStateException("Unexpected effect type: " + effectTypeH);
			}

			// Determine middle particle positions based on the middle effect type
			switch (effectTypeM) {
				case LINE :
					middlePositions = ParticleTypeEffect.LINE(
							(PlatformLocation) loc.clone()
									.subtract(arrow.getVelocity().normalize().multiply(lagBehind)),
							radiusM, middleCount);
					break;
				case HELIX :
					middlePositions = ParticleTypeEffect.HELIX(
							(PlatformLocation) loc.clone()
									.subtract(arrow.getVelocity().normalize().multiply(lagBehind)),
							radiusM, heightStep, angle, middleCount);
					break;
				case WAVY_LINE :
					middlePositions = ParticleTypeEffect.WAVY_LINE(
							(PlatformLocation) loc.clone()
									.subtract(arrow.getVelocity().normalize().multiply(lagBehind)),
							radiusM, heightStep, angle, middleCount);
					break;
				case BURST_SPIRAL :
					middlePositions = ParticleTypeEffect.BURST_SPIRAL(
							(PlatformLocation) loc.clone()
									.subtract(arrow.getVelocity().normalize().multiply(lagBehind)),
							radiusM, heightStep, angle, middleCount);
					break;
				case CONVERGING_LINES :
					middlePositions = ParticleTypeEffect.CONVERGING_LINES(
							(PlatformLocation) loc.clone()
									.subtract(arrow.getVelocity().normalize().multiply(lagBehind)),
							radiusM, heightStep, angle, middleCount);
					break;
				case RIPPLES :
					middlePositions = ParticleTypeEffect.RIPPLES(
							(PlatformLocation) loc.clone()
									.subtract(arrow.getVelocity().normalize().multiply(lagBehind)),
							radiusM, angleStep, angle, middleCount, spacingMode, circleNumber);
					break;
				case FALLING_LEAVES :
					middlePositions = ParticleTypeEffect.FALLING_LEAVES(
							(PlatformLocation) loc.clone()
									.subtract(arrow.getVelocity().normalize().multiply(lagBehind)),
							radiusM, heightStep, angle, middleCount);
					break;
				case EXPLODING_STARS :
					middlePositions = ParticleTypeEffect.EXPLODING_STARS(
							(PlatformLocation) loc.clone()
									.subtract(arrow.getVelocity().normalize().multiply(lagBehind)),
							radiusM, heightStep, angle, middleCount);
					break;
				case PULSE_WAVES :
					middlePositions = ParticleTypeEffect.PULSE_WAVES(
							loc.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind)), radiusM,
							heightStep, angle, middleCount, pFreq);
					break;
				case OSCILLATING_RINGS :
					middlePositions = ParticleTypeEffect.OSCILLATING_RINGS(
							loc.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind)), radiusM,
							heightStep, angle, middleCount, rFreq);
					break;
				default :
					throw new IllegalStateException("Unexpected effect type: " + effectTypeM);
			}

			PlatformLocation[] rotheadPositions = headPositions;
			PlatformLocation[] rotmidPositions = middlePositions;

			// Adjust locations for particles for head and middle effects
			PlatformLocation redstoneLocation = loc.clone();
			var norm = arrow.getVelocity().normalize().multiply(lagBehind);
			PlatformLocation dustLocation = loc.clone().subtract(norm.x, norm.y, norm.z);

			// Spawn head particles based on the particle type
			if (this.particleH == CommonParticle.REDSTONE) {
				for (PlatformLocation pos : rotheadPositions) {
					PlatformLocation rotatedPos = rotateAroundArrow(pos, loc, arrow.getVelocity());
					PlatformPlugin.particleProvider().spawnColored(particleH, rotatedPos, 1, spreadXH, spreadYH,
							spreadZH, speedH, headColor, sizeH);
				}
			} else if (particleH == CommonParticle.DUST_COLOR_TRANSITION) {
				for (PlatformLocation pos : rotheadPositions) {
					PlatformLocation rotatedPos = rotateAroundArrow(pos, loc, arrow.getVelocity());
					PlatformPlugin.particleProvider().spawnTransition(particleH, rotatedPos, 1, spreadXH, spreadYH,
							spreadZH, speedH, transitionColorStart, transitionColorEnd, sizeH);
				}
			} else {
				for (PlatformLocation pos : rotheadPositions) {
					PlatformLocation rotatedPos = rotateAroundArrow(pos, loc, arrow.getVelocity());
					PlatformPlugin.particleProvider().spawnBasic(particleH, rotatedPos, 1, spreadXH, spreadYH, spreadZH,
							speedH, sizeH);
				}
			}

			// Spawn middle particles based on the particle type
			if (particleM == CommonParticle.DUST_COLOR_TRANSITION) {
				for (PlatformLocation pos : rotmidPositions) {
					PlatformLocation rotatedPos = rotateAroundArrow(pos, loc, arrow.getVelocity());
					PlatformPlugin.particleProvider().spawnTransition(particleM, rotatedPos, 1, spreadXM, spreadYM,
							spreadZM, speedM, transitionColorStart, transitionColorEnd, sizeM);
				}
			} else if (particleM == CommonParticle.REDSTONE) {
				for (PlatformLocation pos : rotmidPositions) {
					PlatformLocation rotatedPos = rotateAroundArrow(pos, loc, arrow.getVelocity());
					PlatformPlugin.particleProvider().spawnColored(particleM, rotatedPos, 1, spreadXM, spreadYM,
							spreadZM, speedM, headColor, sizeM);
				}
			} else {
				for (org.vicky.platform.world.PlatformLocation pos : rotmidPositions) {
					PlatformLocation BParticleLocation = (PlatformLocation) loc.clone()
							.subtract(arrow.getVelocity().normalize().multiply(lagBehind));
					PlatformLocation rotatedPos = rotateAroundArrow(pos, loc, arrow.getVelocity());
					PlatformPlugin.particleProvider().spawnBasic(particleM, rotatedPos, 1, spreadXM, spreadYM, spreadZM,
							speedM, sizeM);
				}
			}
		}
	}

	/**
	 * Rotates a particle location around the arrow's location.
	 *
	 * <p>
	 * This method computes a new location by taking the particle's current location
	 * in Quaternion, calculating its vector relative to the arrow's location,
	 * applying rotations based on the arrow's yaw and pitch, and returning the new
	 * location relative to the arrow.
	 *
	 * @param particleLoc
	 *            the original location of the particle
	 * @param arrowLoc
	 *            the location of the arrow
	 * @param direction
	 *            the arrow's velocity vector (used to derive rotation)
	 * @return a new PlatformLocation that is the rotated position of the particle
	 *         relative to the arrow
	 */
	private PlatformLocation rotateAroundArrow(PlatformLocation particleLoc, PlatformLocation arrowLoc,
			Vec3 direction) {
		return new QuaternionRotation().rotateAroundArrow(particleLoc, arrowLoc, direction);
	}

	public void stop() {
		running = false;
	}
}
