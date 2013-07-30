package me.riking.bungeemmo.bukkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import me.riking.bungeemmo.common.data.TransitPlayerProfile;
import me.riking.bungeemmo.common.messaging.Message;
import me.riking.bungeemmo.common.messaging.PluginMessageUtil;
import me.riking.bungeemmo.common.messaging.ProfilePullMessage;
import me.riking.bungeemmo.common.messaging.StartupMessage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.MessageTooLargeException;

public class ConnectionManager {
    private final Plugin plugin;
    private final ConcurrentLinkedQueue<Message> queue; // helper to avoid constant dereferencing
    public final ArrayList<String> otherServers;
    private boolean established = false;
    private boolean couldSend = false;

    public ConnectionManager(Plugin plugin) {
        this.plugin = plugin;
        this.queue = PluginMessageUtil.queue;
        otherServers = new ArrayList<String>(4);
    }

    /**
     * When WelcomeMessage is recieved
     */
    public void established() {
        established = true;
    }

    public void addPacket(Message m) {
        queue.add(m);
    }

    public void burstSend(Player player) {
        while (send(player));
    }

    public void heartbeat() {
        if (!queue.isEmpty()) {
            Player[] players = Bukkit.getOnlinePlayers();
            if (players.length != 0) {
                couldSend = true;
                onRestore();
                while (send(players[0]));
            } else {
                couldSend = false;
                onDrop();
            }
        }
    }

    public boolean isConnected() {
        return couldSend;
    }

    private void onDrop() {
        // TODO Auto-generated method stub

    }

    private void onRestore() {
        // TODO Auto-generated method stub

    }

    private boolean send(Player player) {
        Message m = queue.poll();
        if (m == null) {
            return false;
        }

        try {
            player.sendPluginMessage(plugin, m.getSendingChannelName(), PluginMessageUtil.writeMessage(m));
        } catch (MessageTooLargeException e) {
            System.err.println(m.getClass().getSimpleName() + " message was too large");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void onEnable() {
        // Queue startup announce
        addPacket(new StartupMessage(TransitPlayerProfile.getVersion(), PluginMessageUtil.prettyVersion));

        Player[] players = plugin.getServer().getOnlinePlayers();
        if (players.length != 0) {
            // This only happens on reload, so we need to get all the profiles back
            for (Player p : players) {
                addPacket(new ProfilePullMessage(p.getName(), true));
            }
        }
        heartbeat();
    }

    public void onEmptyServer() {
        if (queue.isEmpty()) {
            plugin.getLogger().info("No messages in queue, shutting down.");
            return;
        }

        plugin.getLogger().warning("Leftover packets to send. Initiating backup connection...");
        // XXX todo
    }
}
