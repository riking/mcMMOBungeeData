package me.riking.bungeemmo.bungee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.riking.bungeemmo.bungee.transclude.DatabaseManager;
import me.riking.bungeemmo.bungee.transclude.FlatfileDatabaseManager;
import me.riking.bungeemmo.bungee.transclude.SQLDatabaseManager;
import me.riking.bungeemmo.common.messaging.Message;
import me.riking.bungeemmo.common.messaging.PluginMessageUtil;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeePlugin extends Plugin {
    private static BungeePlugin instance;
    public Set<String> mcmmoServers = new HashSet<>();
    public DatabaseManager dbman;
    public File configFile;
    public File flatfile;
    public Config config;

    @Override
    public void onEnable() {
        instance = this;
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener(this));
        getProxy().registerChannel(PluginMessageUtil.MCMMO_CHANNEL_NAME);

        configFile = new File(this.getDataFolder(), "config.yml");
        flatfile = new File(this.getDataFolder(), "mcmmo.users");
        config = new Config(configFile);
        if (config.getUseMySQL()) {
            dbman = new SQLDatabaseManager(this, config);
        } else {
            dbman = new FlatfileDatabaseManager(this, flatfile);
        }
    }

    public void sendMessage(ServerInfo server, Message m) {
        try {
            server.sendData(PluginMessageUtil.MCMMO_CHANNEL_NAME, PluginMessageUtil.writeMessage(m));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
