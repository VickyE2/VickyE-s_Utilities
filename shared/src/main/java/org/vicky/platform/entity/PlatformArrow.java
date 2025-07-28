/* Licensed under Apache-2.0 2025. */
package org.vicky.platform.entity;

import org.vicky.platform.utils.Vec3;
import org.vicky.platform.world.PlatformLocation;

public interface PlatformArrow {
	void setGravity(boolean gravity);

	void remove();

	void setCustomName(String name);

	void teleport(double x, double y, double z);

	boolean isValid();

	Object getHandle(); // Optional: for deeper reflection/NMS if really needed

	float getYaw();

	float getPitch();

	boolean isDead();

	PlatformLocation getLocation();

	Vec3 getVelocity();
}
