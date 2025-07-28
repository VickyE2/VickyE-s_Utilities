/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import org.bukkit.entity.Arrow;
import org.bukkit.util.Vector;
import org.vicky.platform.entity.PlatformArrow;
import org.vicky.platform.utils.Vec3;
import org.vicky.platform.world.PlatformLocation;

public class BukkitArrowAdapter implements PlatformArrow {

	private final Arrow arrow;

	public BukkitArrowAdapter(Arrow arrow) {
		this.arrow = arrow;
	}

	@Override
	public void setGravity(boolean gravity) {
		arrow.setGravity(gravity);
	}

	@Override
	public void remove() {
		arrow.remove();
	}

	@Override
	public void setCustomName(String name) {
		arrow.setCustomName(name);
	}

	@Override
	public void teleport(double x, double y, double z) {
		arrow.teleport(arrow.getLocation().set(x, y, z));
	}

	@Override
	public boolean isValid() {
		return arrow.isValid();
	}

	@Override
	public Object getHandle() {
		return arrow;
	}

	@Override
	public float getYaw() {
		return arrow.getLocation().getYaw();
	}

	@Override
	public float getPitch() {
		return arrow.getLocation().getPitch();
	}

	@Override
	public boolean isDead() {
		return arrow.isDead();
	}

	@Override
	public PlatformLocation getLocation() {
		return BukkitLocationAdapter.from(arrow.getLocation());
	}

	@Override
	public Vec3 getVelocity() {
		Vector vel = arrow.getVelocity();
		return new Vec3(vel.getX(), vel.getY(), vel.getZ());
	}
}
