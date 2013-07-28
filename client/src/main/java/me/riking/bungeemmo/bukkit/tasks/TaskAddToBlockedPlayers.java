package me.riking.bungeemmo.bukkit.tasks;

import me.riking.bungeemmo.bukkit.BukkitPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TaskAddToBlockedPlayers implements Runnable {
    private BukkitPlugin plugin;
    private String playerName;

    public TaskAddToBlockedPlayers(BukkitPlugin plugin, String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void run() {
        Player p = Bukkit.getPlayerExact(playerName);
        if (p != null) {
            plugin.playerListener.addToBlocked(playerName);
        }
    }
}
