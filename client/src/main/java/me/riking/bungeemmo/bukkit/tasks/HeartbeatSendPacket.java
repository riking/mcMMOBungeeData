package me.riking.bungeemmo.bukkit.tasks;

import me.riking.bungeemmo.bukkit.BukkitPlugin;

import org.bukkit.scheduler.BukkitRunnable;

public class HeartbeatSendPacket extends BukkitRunnable {
    private final BukkitPlugin plugin;

    public HeartbeatSendPacket(BukkitPlugin plugin) {
        this.plugin = plugin;
        this.runTaskTimer(plugin, 0, 2);
    }

    @Override
    public void run() {
        plugin.connMan.heartbeat();
    }

}
