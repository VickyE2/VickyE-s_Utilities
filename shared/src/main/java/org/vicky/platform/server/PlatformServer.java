/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.server;

import java.util.List;

import org.vicky.platform.player.PlatformPlayer;

public interface PlatformServer {
	List<PlatformPlayer> getPlayers();
	PlatformScheduler getScheduler();
}