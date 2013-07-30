package me.riking.bungeemmo.bukkit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import me.riking.bungeemmo.bukkit.fetcher.DataFetcher;
import me.riking.bungeemmo.bukkit.fetcher.DataStore;
import me.riking.bungeemmo.bukkit.tasks.HeartbeatSendPacket;
import me.riking.bungeemmo.common.data.TransitPlayerProfile;
import me.riking.bungeemmo.common.messaging.PluginMessageUtil;
import me.riking.bungeemmo.common.messaging.ProfilePullMessage;
import me.riking.bungeemmo.common.messaging.ServerStopMessage;
import me.riking.bungeemmo.common.messaging.StartupMessage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.nossr50.database.DatabaseManagerFactory;
import com.gmail.nossr50.util.player.UserManager;

public class BukkitPlugin extends JavaPlugin {
    public static Thread serverThread;
    private static BukkitPlugin instance;
    public BungeeDatabaseManager dbMan;
    public ConnectionManager connMan;
    public DataFetcher dataFetcher;
    public DataStore dataStore;
    public PlayerListener playerListener;

    public String shortName;

    public InetAddress backupProxy;
    public int backupPort;

    @Override
    public void onLoad() {
        serverThread = Thread.currentThread();
        // This will throw an exception if the mcMMO version is bad
        DatabaseManagerFactory.setCustomDatabaseManagerClass(BungeeDatabaseManager.class);
    }

    @Override
    public void onEnable() {
        instance = this;
        shortName = getDescription().getPrefix();

        playerListener = new PlayerListener(this);
        Bukkit.getPluginManager().registerEvents(playerListener, this);

        dbMan = (BungeeDatabaseManager) DatabaseManagerFactory.getDatabaseManager();

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, PluginMessageUtil.BUNGEE_CHANNEL_NAME);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, PluginMessageUtil.MCMMO_CHANNEL_NAME);
        //Bukkit.getMessenger().registerIncomingPluginChannel(this, PluginMessageUtil.BUNGEE_CHANNEL_NAME, listener);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, PluginMessageUtil.MCMMO_CHANNEL_NAME, new McmmoMessageListener(this));

        connMan = new ConnectionManager(this);
        dataFetcher = new DataFetcher(this);
        dataStore = new DataStore(this);

        new HeartbeatSendPacket(this); // self-schedules

        // Protocol startup
        connMan.onEnable();
    }

    @Override
    public void onDisable() {
        // Do mcMMO's saving for it, then clear the player list to avoid any double-saving
        UserManager.saveAll();
        UserManager.clearAll();
        connMan.addPacket(new ServerStopMessage());
        connMan.heartbeat();
    }

    /**
     * Uses:
     * <ol>
     * <li>All 3 Futures' get() methods</li>
     * </ol>
     *
     * Please keep the size of this list to a MINIMUM.
     *
     * @return the plugin instance
     */
    public static BukkitPlugin getInstance() {
        return instance;
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
