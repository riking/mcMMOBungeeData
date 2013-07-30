package me.riking.bungeemmo.bukkit.tasks;

import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.nossr50.datatypes.player.PlayerProfile;

import me.riking.bungeemmo.bukkit.fetcher.DataFuture;

/**
 * Auto-schedules as an async task.
 */
public class TaskWaitForPlayerProfile extends BukkitRunnable {
    private final Plugin plugin;
    private final String player;
    private final DataFuture<PlayerProfile> future;

    public TaskWaitForPlayerProfile(Plugin plugin, String player, DataFuture<PlayerProfile> future) {
        this.player = player;
        this.future = future;
        this.plugin = plugin;

        runTaskAsynchronously(plugin);
    }

    // Reminder: ASYNC TASK - NO BUKKIT API EXCEPT SCHEDULER
    @Override
    public void run() {
        PlayerProfile newProfile;
        try {
             newProfile = future.get();
             Bukkit.getScheduler().runTask(plugin, new TaskFillPlayerProfile(player, newProfile));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
