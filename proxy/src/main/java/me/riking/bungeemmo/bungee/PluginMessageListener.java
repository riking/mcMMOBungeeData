package me.riking.bungeemmo.bungee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import me.riking.bungeemmo.common.data.TransitPlayerProfile;
import me.riking.bungeemmo.common.messaging.AnnounceMessage;
import me.riking.bungeemmo.common.messaging.BadVersionMessage;
import me.riking.bungeemmo.common.messaging.LeaderboardPullMessage;
import me.riking.bungeemmo.common.messaging.LeaderboardPushMessage;
import me.riking.bungeemmo.common.messaging.Message;
import me.riking.bungeemmo.common.messaging.PluginMessageUtil;
import me.riking.bungeemmo.common.messaging.ProfilePullMessage;
import me.riking.bungeemmo.common.messaging.ProfilePushMessage;
import me.riking.bungeemmo.common.messaging.RankPullMessage;
import me.riking.bungeemmo.common.messaging.RankPushMessage;
import me.riking.bungeemmo.common.messaging.ServerStopMessage;
import me.riking.bungeemmo.common.messaging.StartupMessage;
import me.riking.bungeemmo.common.messaging.WelcomeMessage;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PluginMessageListener implements Listener {
    private BungeePlugin plugin;

    public PluginMessageListener(BungeePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleConnected(ServerConnectedEvent event) {
        // TODO check this
        // are we sending in the right direction?
        // is that the right data to be sending?
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream ostr = new DataOutputStream(out);
        try {
            ostr.writeUTF(PluginMessageUtil.MCMMO_CHANNEL_NAME);
            ostr.close();
        } catch (IOException e) {
        }
        event.getServer().sendData("REGISTER", out.toByteArray());
    }

    @EventHandler
    public void handlePluginMessage(PluginMessageEvent event) {
        if (!PluginMessageUtil.MCMMO_CHANNEL_NAME.equals(event.getTag())) {
            return;
        }
        // Don't pass through to other side, it's handled here
        event.setCancelled(true);

        if (!(event.getSender() instanceof Server)) {
            // Kick clients that try to give us mcmmo data
            event.getSender().disconnect("Attempt to send data over a retricted channel");
            return;
        }
        Server serverConnection = (Server) event.getSender();
        ServerInfo server = serverConnection.getInfo();
        try {
            Message m = PluginMessageUtil.readIncomingMessage(event.getData());
            if (m instanceof StartupMessage) {
                StartupMessage me = (StartupMessage) m;
                if (me.versionId != TransitPlayerProfile.getVersion()) {
                    BadVersionMessage bm = new BadVersionMessage(TransitPlayerProfile.getVersion(), PluginMessageUtil.prettyVersion);
                    plugin.sendMessage(server, bm);
                } else {
                    WelcomeMessage wm = new WelcomeMessage(server.getName(), new ArrayList<>(plugin.mcmmoServers));
                    plugin.sendMessage(server, wm);

                    AnnounceMessage an = new AnnounceMessage(server.getName(), true);
                    for (String otherServer : plugin.mcmmoServers) {
                        ServerInfo other = plugin.getProxy().getServerInfo(otherServer);
                        plugin.sendMessage(other, an);
                    }
                    plugin.mcmmoServers.add(server.getName());
                }
            } else if (m instanceof ServerStopMessage) {
                ServerStopMessage me = (ServerStopMessage) m;

                plugin.mcmmoServers.remove(server.getName());
                AnnounceMessage an = new AnnounceMessage(server.getName(), false);
                for (String otherServer : plugin.mcmmoServers) {
                    ServerInfo other = plugin.getProxy().getServerInfo(otherServer);
                    plugin.sendMessage(other, an);
                }
            } else if (m instanceof ProfilePushMessage) {

            } else if (m instanceof ProfilePullMessage) {

            } else if (m instanceof LeaderboardPullMessage) {

            } else if (m instanceof RankPullMessage) {

            } else if (m instanceof AnnounceMessage) {
                wrongDirection("AnnounceMessage");
            } else if (m instanceof BadVersionMessage) {
                wrongDirection("BadVersionMessage");
            } else if (m instanceof LeaderboardPushMessage) {
                wrongDirection("LeaderboardPushMessage");
            } else if (m instanceof RankPushMessage) {
                wrongDirection("RankPushMessage");
            } else if (m instanceof WelcomeMessage) {
                wrongDirection("WelcomeMessage");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void wrongDirection(String s) {
        plugin.getLogger().warning("Ignoring wrong-direction message " + s);
    }
}
