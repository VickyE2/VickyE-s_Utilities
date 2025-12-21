/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.client;

/**
 * Small theme holder that you can later load from a config or resource JSON.
 *
 * @param panelBackground
 *            ARGB
 */
public record Theme(int panelBackground, int contentBackground, int titleColor, int titleSmallColor, int subliminalText,
		int slotBackground, int slotBorder, int hoverOverlay, int scrollTrack, int scrollThumb, int footerColor) {

	public static final Theme DEFAULT = new Theme(0xFF2B2B2B, // panel bg
			0xFF1B1B1B, // content bg
			0xFFFFFFFF, // title color
			0xFFFFFFFF, // small title
			0xFFAAAAAA, // authors
			0xFF2A2A2A, // slot bg
			0xFF101010, // slot border
			0x20FFFFFF, // hover overlay (alpha)
			0xFF121212, // scroll track
			0xFF555555, // scroll thumb
			0xFFCCCCCC // footer
	);

	public static Theme SELECTED = Theme.DEFAULT;
}
