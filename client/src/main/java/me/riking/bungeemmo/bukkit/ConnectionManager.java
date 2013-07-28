package me.riking.bungeemmo.bukkit;

import java.io.ByteArrayOutputStream;
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

    public ConnectionManager(Plugin plugin) {
        this.plugin = plugin;
        this.queue = PluginMessageUtil.queue;
        otherServers = new ArrayList<String>(4);
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
                while (send(players[0]));
            }
        }
    }

    private boolean send(Player player) {
        Message m = queue.poll();
        if (m == null) {
            return false;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            m.write(out);
            player.sendPluginMessage(plugin, m.getSendingChannelName(), out.toByteArray());
        } catch (MessageTooLargeException e) {
            System.err.println(m.getClass().getSimpleName() + " message was too large");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void onEmptyServer() {
        plugin.getLogger().info("Server is empty");
        if (queue.isEmpty()) {
            plugin.getLogger().info("No messages in queue, we're fine.");
            return;
        }

        plugin.getLogger().warning("Leftover packets to send. Attempting backup protocol...");
    }
}
