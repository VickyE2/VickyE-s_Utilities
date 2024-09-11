package org.v_utls.utilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandRunner {

    public static void runCommandAsConsole(String command) {
        // Execute a command as the server console
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public static void runCommandAsPlayer(Player player, String command) {
        // Execute a command as the given player
        Bukkit.dispatchCommand(player, command);
    }
}

