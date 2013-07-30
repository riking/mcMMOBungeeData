package me.riking.bungeemmo.bukkit;

import java.io.IOException;
import java.util.logging.Level;

import me.riking.bungeemmo.common.data.TransitPlayerProfile;
import me.riking.bungeemmo.common.messaging.AnnounceMessage;
import me.riking.bungeemmo.common.messaging.BadVersionMessage;
import me.riking.bungeemmo.common.messaging.LeaderboardPushMessage;
import me.riking.bungeemmo.common.messaging.Message;
import me.riking.bungeemmo.common.messaging.PluginMessageUtil;
import me.riking.bungeemmo.common.messaging.ProfilePushMessage;
import me.riking.bungeemmo.common.messaging.RankPushMessage;
import me.riking.bungeemmo.common.messaging.WelcomeMessage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.gmail.nossr50.datatypes.player.PlayerProfile;

public class McmmoMessageListener implements PluginMessageListener {
    private final BukkitPlugin plugin;

    public McmmoMessageListener(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(PluginMessageUtil.MCMMO_CHANNEL_NAME)) {
            return;
        }
        Message m;
        try {
            m = PluginMessageUtil.readIncomingMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (m instanceof WelcomeMessage) {
            WelcomeMessage me = (WelcomeMessage) m;
            PluginMessageUtil.serverName = me.serverName;
            plugin.connMan.otherServers.addAll(me.otherServers);
            // We're up.
            plugin.connMan.established();
        } else if (m instanceof BadVersionMessage) {
            BadVersionMessage me = (BadVersionMessage) m;
            // Version mismatch, so we can't do shit.
            plugin.getLogger().severe("FATAL: Version mismatch with proxy.");
            String newVersion = String.format("%s (%d)", me.prettyVersion, me.versionId);
            plugin.getLogger().severe(String.format("Current version: %s (%d)", PluginMessageUtil.prettyVersion, TransitPlayerProfile.getVersion()));
            plugin.getLogger().severe("Expected version: " + newVersion);
            plugin.getLogger().severe("Shutting down server to avoid data loss.");
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                p.kickPlayer("[" + plugin.shortName + "] Please update BungeeMMO to " + newVersion);
            }

            plugin.getServer().shutdown();
        } else if (m instanceof AnnounceMessage) {
            AnnounceMessage me = (AnnounceMessage) m;
            plugin.connMan.otherServers.add(me.newServerName);
        } else if (m instanceof ProfilePushMessage) {
            ProfilePushMessage me = (ProfilePushMessage) m;
            plugin.dataFetcher.fulfill(me);
        } else if (m instanceof LeaderboardPushMessage) {
            LeaderboardPushMessage me = (LeaderboardPushMessage) m;
            plugin.dataFetcher.fulfill(me);
        } else if (m instanceof RankPushMessage) {
            RankPushMessage me = (RankPushMessage) m;
            plugin.dataFetcher.fulfill(me);
        }
    }
}
