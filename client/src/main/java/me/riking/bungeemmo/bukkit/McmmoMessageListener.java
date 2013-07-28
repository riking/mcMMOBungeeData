package me.riking.bungeemmo.bukkit;

import java.io.IOException;

import me.riking.bungeemmo.common.messaging.AnnounceMessage;
import me.riking.bungeemmo.common.messaging.Message;
import me.riking.bungeemmo.common.messaging.PluginMessageUtil;
import me.riking.bungeemmo.common.messaging.ProfilePushMessage;
import me.riking.bungeemmo.common.messaging.WelcomeMessage;

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
        } else if (m instanceof AnnounceMessage) {
            AnnounceMessage me = (AnnounceMessage) m;
            plugin.connMan.otherServers.add(me.newServerName);
        } else if (m instanceof ProfilePushMessage) {
            ProfilePushMessage me = (ProfilePushMessage) m;
            if (me.success) {
                plugin.dataFetcher.fulfill(TransitDataConverter.fromTransit(me.profile));
            } else {
                // XXX wat do
                plugin.dataFetcher.cancelFetch(null); // er what was the name
            }
        }
    }
}
