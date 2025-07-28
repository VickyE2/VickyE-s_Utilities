/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities;

/**
 * Utility class for converting standard text into small caps.
 *
 * <p>
 * For example, the input "RING OF ACCELERATION" is converted into: "ʀɪɴɢ ᴏғ
 * ᴀᴄᴄᴇʟᴇʀᴀᴛɪᴏɴ".
 */
public class SmallCapsConverter {

	// A mapping string representing small-caps for A-Z in order.
	// Note: Some letters (e.g. Q, X) do not have dedicated small-caps in Unicode;
	// in these cases, a similar looking character or a fallback is provided.
	private static final String SMALL_CAPS = "ᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘQʀsᴛᴜᴠᴡxʏᴢ";

	/**
	 * Converts the given string into small caps.
	 *
	 * <p>
	 * The method first converts the input to uppercase. Then, for each character in
	 * the range A–Z, it replaces it with the corresponding small-cap letter from
	 * the mapping. Non-alphabetic characters are appended unchanged.
	 *
	 * @param input
	 *            the input string to convert; may be null
	 * @return the converted string in small caps, or null if the input was null
	 */
	public static String toSmallCaps(String input) {
		if (input == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(input.length());
		// Convert input to uppercase so that the mapping is consistent.
		for (char c : input.toUpperCase().toCharArray()) {
			if (c >= 'A' && c <= 'Z') {
				int index = c - 'A';
				sb.append(SMALL_CAPS.charAt(index));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
