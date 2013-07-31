package me.riking.bungeemmo.bukkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import me.riking.bungeemmo.common.data.TransitLeaderboardValue;
import me.riking.bungeemmo.common.data.TransitPlayerProfile;
import me.riking.bungeemmo.common.messaging.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.gmail.nossr50.datatypes.database.PlayerStat;
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
            plugin.dataFetcher.fulfill(me.playerName, me.profile);

        } else if (m instanceof LeaderboardPushMessage) {
            LeaderboardPushMessage me = (LeaderboardPushMessage) m;
            ArrayList<TransitLeaderboardValue> transit = me.values;
            ArrayList<PlayerStat> mcmmo = new ArrayList<PlayerStat>();
            for (TransitLeaderboardValue tr : transit) {
                mcmmo.add(new PlayerStat(tr.name, tr.val));
            }
            plugin.dataFetcher.fulfill(me.getRequest(), mcmmo);

        } else if (m instanceof RankPushMessage) {
            RankPushMessage me = (RankPushMessage) m;
            plugin.dataFetcher.fulfill(me.playerName, me.rank);

        } else if (m instanceof ProfilePullMessage) {
            ProfilePullMessage me = (ProfilePullMessage) m;
            PlayerProfile prof = plugin.dataStore.cachedProfiles.get(me.playerName);
            // Send even if null
            plugin.connMan.addPacket(new ProfilePushMessage(me.playerName, TransitDataConverter.toTransit(prof)));

        } else if (m instanceof LeaderboardPullMessage) {
            wrongDirection("LeaderboardPullMessage");
        } else if (m instanceof RankPullMessage) {
            wrongDirection("RankPullMessage");
        } else if (m instanceof StartupMessage) {
            wrongDirection("StartupMessage");
        } else if (m instanceof ServerStopMessage) {
            wrongDirection("ServerStopMessage");
        }
    }

    private void wrongDirection(String s) {
        plugin.getLogger().warning("Ignoring wrong-direction message " + s);
    }
}
