package me.riking.bungeemmo.bukkit.tasks;

import me.riking.bungeemmo.bukkit.BukkitPlugin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class TrashCachedProfileTask implements Runnable {
    private BukkitPlugin plugin;
    private String playerName;

    public TrashCachedProfileTask(BukkitPlugin plugin, String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void run() {
        OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
        if (p.isOnline()) {
            // Halt the shredder, they're online!
            return;
        } else {
            plugin.dataStore.cachedProfiles.remove(playerName);
        }
    }
}
