package me.riking.bungeemmo.bukkit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import me.riking.bungeemmo.bukkit.tasks.HeartbeatSendPacket;
import me.riking.bungeemmo.common.data.TransitPlayerProfile;
import me.riking.bungeemmo.common.messaging.PluginMessageUtil;
import me.riking.bungeemmo.common.messaging.ProfilePullMessage;
import me.riking.bungeemmo.common.messaging.StartupMessage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.nossr50.database.DatabaseManagerFactory;
import com.gmail.nossr50.util.player.UserManager;

public class BukkitPlugin extends JavaPlugin {
    public ConnectionManager connMan;
    public DataFetcher dataFetcher;
    public PlayerListener playerListener;

    public InetAddress backupProxy;
    public int backupPort;

    @Override
    public void onLoad() {
        // This will throw an exception if the mcMMO version is bad
        DatabaseManagerFactory.setCustomDatabaseManagerClass(BungeeDatabaseManager.class);
    }

    @Override
    public void onEnable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, PluginMessageUtil.BUNGEE_CHANNEL_NAME);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, PluginMessageUtil.MCMMO_CHANNEL_NAME);
        //Bukkit.getMessenger().registerIncomingPluginChannel(this, PluginMessageUtil.BUNGEE_CHANNEL_NAME, listener);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, PluginMessageUtil.MCMMO_CHANNEL_NAME, new McmmoMessageListener(this));

        new HeartbeatSendPacket(this); // self-schedules
        connMan = new ConnectionManager(this);
        dataFetcher = new DataFetcher(this);
        playerListener = new PlayerListener(this);
        Bukkit.getPluginManager().registerEvents(playerListener, this);

        // Protocol startup
        connMan.addPacket(new StartupMessage(TransitPlayerProfile.getVersion(), PluginMessageUtil.prettyVersion));
        Player[] players = getServer().getOnlinePlayers();
        if (players.length != 0) {
            // This only happens on reload, so we need to get all the profiles back
            for (Player p : players) {
                connMan.addPacket(new ProfilePullMessage(p.getName(), true));
            }
        }
        connMan.heartbeat();
    }

    @Override
    public void onDisable() {
        // Do mcMMO's saving for it, then clear the player list to avoid any double-saving
        UserManager.saveAll();
        UserManager.clearAll();
        connMan.heartbeat();
        connMan.onEmptyServer();
        connMan.close();
    }

    /**
     * Check if player is connecting through BungeeCord (this is mandatory).
     * The check is done through a list in the config file.
     *
     * @param address player address
     * @return if address is a BungeeCord proxy according to server admin
     */
    public boolean checkIp(InetAddress address) {
        List<String> allowedIps = getConfig().getStringList("proxy-ips");
        for (String s : allowedIps) {
            if (s.equals("ANY_LOCAL")) {
                if (address.isLoopbackAddress() || address.isAnyLocalAddress()) {
                    return true;
                }
            } else if (s.equals("LOOPBACK")) {
                if (address.isLoopbackAddress()) {
                    return true;
                }
            }
            try {
                InetAddress white = InetAddress.getByName(s);
                if (white.equals(address)) {
                    return true;
                }
            } catch (UnknownHostException e) {
                continue;
            }
        }
        return false;
    }
}
