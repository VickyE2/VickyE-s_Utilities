/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.server;

import org.vicky.platform.player.PlatformPlayer;

import java.util.List;

public interface PlatformServer {
	List<PlatformPlayer> getPlayers();
	PlatformScheduler getScheduler();
}