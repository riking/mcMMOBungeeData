package me.riking.bungeemmo.bukkit;

import java.net.InetAddress;
import java.util.ArrayList;

import me.riking.bungeemmo.bukkit.fetcher.DataFuture;
import me.riking.bungeemmo.bukkit.tasks.TaskAddToBlockedPlayers;
import me.riking.bungeemmo.bukkit.tasks.TrashCachedProfileTask;
import me.riking.bungeemmo.common.data.TransitPlayerProfile;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.util.player.UserManager;

public class PlayerListener implements Listener {
    public static InetAddress anyProxy;
    private final BukkitPlugin plugin;
    private ArrayList<String> blockedPlayers = new ArrayList<String>(3);
    private boolean anyBlocked = false;

    public PlayerListener(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    public void addToBlocked(String playerName) {
        anyBlocked = true;
        blockedPlayers.add(playerName);
    }

    public void removeFromBlocked(String playerName) {
        blockedPlayers.remove(playerName);
        anyBlocked = !blockedPlayers.isEmpty();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        if (anyBlocked) { // fast-escape, because this is costly
            if (blockedPlayers.contains(event.getPlayer().getName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLoginFirst(PlayerLoginEvent event) {
        if (!plugin.checkIp(event.getAddress())) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "[MmoBcDb] You must connect via a BungeeCord server, not direct");
        }
        // Burst pending packets if they're the only player
        // (This must happen after IP validation, because otherwise the client gets the packets)
        // Remember, we don't care if they're banned - they're still connected through BungeeCord, which is what matters
        Player[] players = Bukkit.getOnlinePlayers();
        if (players.length == 0) {
            plugin.connMan.burstSend(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoginLast(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }

        plugin.dataFetcher.getProfile(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoinLater(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        String playerName = p.getName();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        DataFuture<TransitPlayerProfile> future = plugin.dataFetcher.getPendingProfile(playerName);
        if (future != null) {
            future.cancel(true);
        }

        if (plugin.isEnabled()) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new TrashCachedProfileTask(plugin, playerName));
        }

        McMMOPlayer mp = UserManager.getPlayer(event.getPlayer());
        mp.getProfile().save();

        Player[] players = plugin.getServer().getOnlinePlayers();
        if (players.length <= 1) {
            // We no longer have anyone to send packets through
            plugin.connMan.onEmptyServer();
        }
    }
}
