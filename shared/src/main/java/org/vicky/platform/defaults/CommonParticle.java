/* Licensed under Apache-2.0 2025. */
package org.vicky.platform.defaults;

import org.vicky.platform.entity.PlatformParticle;

public enum CommonParticle implements PlatformParticle {
	REDSTONE("minecraft:dust", true, false), DUST_COLOR_TRANSITION("minecraft:dust_color_transition", true,
			true), SPELL("minecraft:spell", false, false), FLAME("minecraft:flame", false, false);

	private final String id;
	private final boolean color;
	private final boolean transition;

	CommonParticle(String id, boolean color, boolean transition) {
		this.id = id;
		this.color = color;
		this.transition = transition;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean supportsColor() {
		return color;
	}

	@Override
	public boolean supportsTransition() {
		return transition;
	}
}
