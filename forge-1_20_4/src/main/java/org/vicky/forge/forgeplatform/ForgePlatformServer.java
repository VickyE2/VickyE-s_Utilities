package org.vicky.forge.forgeplatform;

import net.minecraftforge.server.ServerLifecycleHooks;
import org.vicky.forge.forgeplatform.player.ForgePlatformPlayer;
import org.vicky.platform.player.PlatformPlayer;
import org.vicky.platform.server.PlatformScheduler;
import org.vicky.platform.server.PlatformServer;

import java.util.List;

public class ForgePlatformServer implements PlatformServer {

    private static ForgePlatformServer INSTANCE;

    private ForgePlatformServer() {
    }

    public static ForgePlatformServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgePlatformServer();
        }
        return INSTANCE;
    }

    @Override
    public List<PlatformPlayer> getPlayers() {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()
                    .stream().map(ForgePlatformPlayer::new).map(it -> (PlatformPlayer) it)
                    .toList();
        }
        return List.of();
    }

    @Override
    public PlatformScheduler getScheduler() {
        return ForgePlatformScheduler.getInstance();
    }
}
