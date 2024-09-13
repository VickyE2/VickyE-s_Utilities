package org.v_utls.utilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


public class ParticleTask extends BukkitRunnable {

    private final Arrow arrow;
    private final double radiusH;
    private final double radiusM;
    private final double heightStep;
    private final long startTime;
    private final Color headColor;
    private final Color transitionColorStart;
    private final Color transitionColorEnd;
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
    private final Particle particleH;
    private final Particle particleM;
    private final ParticleTypeEffect.ParticleTypeEffects effectTypeH;
    private final ParticleTypeEffect.ParticleTypeEffects effectTypeM;
    private final float yaw;
    private final float pitch;

    public ParticleTask(long startTime, Arrow arrow, double radiusH, double radiusM, double heightStep, Color headColor, Color transitionColorStart, Color transitionColorEnd,
                        int headCount, int middleCount,
                        double spreadXH, double spreadYH, double spreadZH,
                        double spreadXM, double spreadYM, double spreadZM,
                        float speedH, float speedM,
                        long lagBehind, double backwardVelocity,
                        float sizeH, float sizeM,
                        Particle particleH, Particle particleM,
                        ParticleTypeEffect.ParticleTypeEffects effectTypeH, ParticleTypeEffect.ParticleTypeEffects effectTypeM,
                        double rFreq, double pFreq, double angleStep, ParticleTypeEffect.SpacingMode spacingMode, int circleNumber,
                        float yaw, float pitch) {

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
        this.rFreq = (rFreq == 0) ? 0.5 : rFreq; // Default to 1.0 if rFreq is 0
        this.pFreq = (pFreq == 0) ? 0.2 : pFreq; // Default to 1.0 if pFreq is 0
        this.angleStep = (angleStep == 0) ? 1.0 : angleStep; // Default to 1.0 if angleStep is 0
        this.backwardVelocity = backwardVelocity;
        this.startTime = startTime;
        this.effectTypeH = effectTypeH;
        this.effectTypeM = effectTypeM;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /*
    public void alignParticlesToArrow(Location[] positions, Arrow arrow) {
        // Get the arrow's direction vector and normalize it
        Vector direction = arrow.getLocation().getDirection().normalize();

        // Calculate the rotation quaternion from the default direction (e.g., Y-axis) to the arrow's direction
        Quaternion rotation = getRotationTo(new Vector(0, 1, 0), direction);

        // Apply the rotation to each particle position
        for (int i = 0; i < positions.length; i++) {
            Vector particleVector = positions[i].toVector().subtract(arrow.getLocation().toVector());

            // Rotate the particle position using the quaternion
            Vector rotatedVector = rotateVectorByQuaternion(particleVector, rotation);

            // Update the particle position
            positions[i].setX(arrow.getLocation().getX() + rotatedVector.getX());
            positions[i].setY(arrow.getLocation().getY() + rotatedVector.getY());
            positions[i].setZ(arrow.getLocation().getZ() + rotatedVector.getZ());
        }
    }

    public Quaternion getRotationTo(Vector from, Vector to) {
        Vector axis = from.clone().crossProduct(to).normalize();
        double angle = Math.acos(from.clone().dot(to));
        double sinHalfAngle = Math.sin(angle / 2);
        double cosHalfAngle = Math.cos(angle / 2);

        return new Quaternion(axis.getX() * sinHalfAngle, axis.getY() * sinHalfAngle, axis.getZ() * sinHalfAngle, cosHalfAngle);
    }

    public Vector rotateVectorByQuaternion(Vector v, Quaternion q) {
        Quaternion p = new Quaternion(v.getX(), v.getY(), v.getZ(), 0);
        Quaternion qConjugate = q.conjugate();
        Quaternion result = q.multiply(p).multiply(qConjugate);

        return new Vector(result.getX(), result.getY(), result.getZ());
    }
*/

    private Location rotateAroundArrow(Location particleLoc, Location arrowLoc, Vector direction) {
        Vector relativePos = particleLoc.toVector().subtract(arrowLoc.toVector());
        direction = direction.normalize();
        // Adjust relativePos by direction if needed, e.g., influence the rotation
        relativePos.multiply(direction); // This is an example, adjust as necessary

        double yaw = Math.toRadians(arrowLoc.getYaw());
        double pitch = Math.toRadians(arrowLoc.getPitch());

        double xRot = relativePos.getX();
        double yRot = relativePos.getY();
        double zRot = relativePos.getZ();

        // Rotate around Yaw (Horizontal axis)
        double xPrime = xRot * Math.cos(-yaw) - zRot * Math.sin(-yaw);
        double zPrime = xRot * Math.sin(-yaw) + zRot * Math.cos(-yaw);

        // Rotate around Pitch (Vertical axis)
        double yPrime = yRot * Math.cos(pitch) - zPrime * Math.sin(pitch);
        zPrime = yRot * Math.sin(pitch) + zPrime * Math.cos(pitch);

        Vector rotatedPos = new Vector(xPrime, yPrime, zPrime);
        return arrowLoc.clone().add(rotatedPos);
    }



    @Override
    public void run() {
        if (arrow.isDead()) {
            cancel();
            return;
        }


        Location loc = arrow.getLocation();
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime; // Calculate the elapsed time
        double angle = (elapsedTime / 10.0);

        // Generate particle positions based on effect type
        Location[] headPositions;
        Location[] middlePositions;

        switch (effectTypeH) {
            case LINE:
                headPositions = ParticleTypeEffect.LINE(loc.clone(), radiusH, headCount);
                break;
            case HELIX:
                headPositions = ParticleTypeEffect.HELIX(loc.clone(), radiusH, heightStep, angle, headCount);
                break;
            case WAVY_LINE:
                headPositions = ParticleTypeEffect.WAVY_LINE(loc.clone(), radiusH, heightStep, angle, headCount);
                break;
            case BURST_SPIRAL:
                headPositions = ParticleTypeEffect.BURST_SPIRAL(loc.clone(), radiusH, heightStep, angle, headCount);
                break;
            case CONVERGING_LINES:
                headPositions = ParticleTypeEffect.CONVERGING_LINES(loc.clone(), radiusH, heightStep, angle, headCount);
                break;
            case RIPPLES:
                headPositions = ParticleTypeEffect.RIPPLES(loc.clone(), radiusH, angleStep, angle, headCount, spacingMode, circleNumber);
                break;
            case FALLING_LEAVES:
                headPositions = ParticleTypeEffect.FALLING_LEAVES(loc.clone(), radiusH, heightStep, angle, headCount);
                break;
            case EXPLODING_STARS:
                headPositions = ParticleTypeEffect.EXPLODING_STARS(loc.clone(), radiusH, heightStep, angle, headCount);
                break;
            case PULSE_WAVES:
                headPositions = ParticleTypeEffect.PULSE_WAVES(loc.clone(), radiusH, heightStep, angle, headCount, pFreq);
                break;
            case OSCILLATING_RINGS:
                headPositions = ParticleTypeEffect.OSCILLATING_RINGS(loc.clone(), radiusH, heightStep, angle, headCount, rFreq);
                break;
            default:
                throw new IllegalStateException("Unexpected effect type: " + effectTypeH);

        }
        switch (effectTypeM) {
            case LINE:
                middlePositions = ParticleTypeEffect.LINE(loc.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind)), radiusM, middleCount);
                break;
            case HELIX:
                middlePositions = ParticleTypeEffect.HELIX(loc.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind)), radiusM, heightStep, angle, middleCount);
                break;
            case WAVY_LINE:
                middlePositions = ParticleTypeEffect.WAVY_LINE(loc.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind)), radiusM, heightStep, angle, middleCount);
                break;
            case BURST_SPIRAL:
                middlePositions = ParticleTypeEffect.BURST_SPIRAL(loc.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind)), radiusM, heightStep, angle, middleCount);
                break;
            case CONVERGING_LINES:
                middlePositions = ParticleTypeEffect.CONVERGING_LINES(loc.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind)), radiusM, heightStep, angle, middleCount);
                break;
            case RIPPLES:
                middlePositions = ParticleTypeEffect.RIPPLES(loc.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind)), radiusM, angleStep, angle, middleCount, spacingMode, circleNumber);
                break;
            case FALLING_LEAVES:
                middlePositions = ParticleTypeEffect.FALLING_LEAVES(loc.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind)), radiusM, heightStep, angle, middleCount);
                break;
            case EXPLODING_STARS:
                middlePositions = ParticleTypeEffect.EXPLODING_STARS(loc.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind)), radiusM, heightStep, angle, middleCount);
                break;
            case PULSE_WAVES:
                middlePositions = ParticleTypeEffect.PULSE_WAVES(loc.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind)), radiusM, heightStep, angle, middleCount, pFreq);
                break;
            case OSCILLATING_RINGS:
                middlePositions = ParticleTypeEffect.OSCILLATING_RINGS(loc.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind)), radiusM, heightStep, angle, middleCount, rFreq);
                break;
            default:
                throw new IllegalStateException("Unexpected effect type: " + effectTypeM);
        }

        Location[] rotheadPositions = headPositions;
        Location[] rotmidPositions = middlePositions;

        // Adjust locations for particles for head
        Location redstoneLocation = loc.clone();
        Location dustLocation = loc.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind));

        /*This is the  way the particle type and its variant are chosen. Also note that the second two color parameters
         * Are always for the dust that's transitioning asides that the first color is always for the redstone*/
        if (this.particleH == Particle.REDSTONE) {
            for (Location pos : rotheadPositions) {
                Location rotatedPos = rotateAroundArrow(pos, loc, arrow.getVelocity());
                redstoneLocation.getWorld().spawnParticle(particleH, rotatedPos, 1, spreadXH, spreadYH, spreadZH, speedH, new Particle.DustOptions(headColor, sizeH));
            }
        } else if (particleH == Particle.DUST_COLOR_TRANSITION) {
            // Generate dust transition particles for head
            for (Location pos : rotheadPositions) {
                Location rotatedPos = rotateAroundArrow(pos, loc, arrow.getVelocity());
                redstoneLocation.getWorld().spawnParticle(particleH, rotatedPos, 1, spreadXH, spreadYH, spreadZH, speedH, new Particle.DustTransition(transitionColorStart, transitionColorEnd, sizeH));
            }
        } else {
            // Default particle type handling for head
            for (Location pos : rotheadPositions) {
                Location rotatedPos = rotateAroundArrow(pos, loc, arrow.getVelocity());
                loc.getWorld().spawnParticle(particleH, rotatedPos, 1, spreadXH, spreadYH, spreadZH, speedH, sizeH);
            }
        }

        if (particleM == Particle.DUST_COLOR_TRANSITION) {
            // Lag behind by adjusting the location of dust particles
            dustLocation = dustLocation.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind));
            for (Location pos : rotmidPositions) {
                // Generate dust transition particles for middle
                Location rotatedPos = rotateAroundArrow(pos, loc, arrow.getVelocity());
                dustLocation.getWorld().spawnParticle(particleM, rotatedPos, 1, spreadXM, spreadYM, spreadZM, speedM, new Particle.DustTransition(transitionColorStart, transitionColorEnd, sizeM));
            }
        } else if (particleM == Particle.REDSTONE) {
            //redstone particle handling for middle
            for (Location pos : rotmidPositions) {
                // Generate dust transition particles for middle
                Location rotatedPos = rotateAroundArrow(pos, loc, arrow.getVelocity());
                dustLocation.getWorld().spawnParticle(particleM, rotatedPos, 1, spreadXM, spreadYM, spreadZM, speedM, new Particle.DustOptions(headColor, sizeM));
            }
        } else {
            //default particle handling for middle
            for (Location pos : rotmidPositions) {
                Location BParticleLocation = loc.clone().subtract(arrow.getVelocity().normalize().multiply(lagBehind));
                // Generate dust transition particles for middle
                Location rotatedPos = rotateAroundArrow(pos, loc, arrow.getVelocity());
                BParticleLocation.getWorld().spawnParticle(particleM, rotatedPos, 1, spreadXM, spreadYM, spreadZM, speedM, sizeM);
            }
        }

    }
}
