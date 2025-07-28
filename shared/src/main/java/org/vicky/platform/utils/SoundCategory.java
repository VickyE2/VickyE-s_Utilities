/* Licensed under Apache-2.0 2025. */
package org.vicky.platform.utils;

public enum SoundCategory {
	MASTER, MUSIC, RECORDS, WEATHER, BLOCKS, HOSTILE, NEUTRAL, PLAYERS, AMBIENT, VOICE;

	// Optional mapping method if needed per platform
	public static SoundCategory fromString(String name) {
		for (SoundCategory value : values()) {
			if (value.name().equalsIgnoreCase(name))
				return value;
		}
		return MASTER;
	}
}
