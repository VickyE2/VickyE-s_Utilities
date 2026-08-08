/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.item;

import org.vicky.platform.player.PlatformPlayer;

public interface ItemAction {
	void execute(PlatformPlayer player, InteractionContext context);

}