package me.riking.bungeemmo.bukkit;

import java.util.List;
import java.util.Map;

import me.riking.bungeemmo.common.messaging.ProfilePushMessage;

import com.gmail.nossr50.database.DatabaseManager;
import com.gmail.nossr50.datatypes.database.PlayerStat;
import com.gmail.nossr50.datatypes.player.PlayerProfile;

public class BungeeDatabaseManager implements DatabaseManager {
    private final BukkitPlugin plugin;
    private Map<String, PlayerProfile> activeProfiles;

    public BungeeDatabaseManager(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void saveUser(PlayerProfile arg0) {
        plugin.connMan.addPacket(new ProfilePushMessage(TransitDataConverter.toTransit(arg0), true));
    }

    @Override
    public void convertUsers(DatabaseManager destination) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getStoredUsers() {
        return null;
    }

    @Override
    public PlayerProfile loadPlayerProfile(String playerName, boolean createIfEmpty) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void newUser(String playerName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void purgeOldUsers() {
        // Do nothing, the proxy schedules these on its own
        return;
    }

    @Override
    public void purgePowerlessUsers() {
        // Do nothing, the proxy schedules these on its own
        return;
    }

    @Override
    public List<PlayerStat> readLeaderboard(String arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Integer> readRank(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean removeUser(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }
}
