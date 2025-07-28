/* Licensed under Apache-2.0 2025. */
package org.vicky.platform;

public interface PlatformChatFormatter {
	String hex(String hex);

	String gradient(String text, String fromHex, String toHex);
}
