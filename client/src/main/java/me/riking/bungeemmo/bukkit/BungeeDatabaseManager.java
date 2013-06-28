package me.riking.bungeemmo.bukkit;

import java.util.List;
import java.util.Map;

import com.gmail.nossr50.database.DatabaseManager;
import com.gmail.nossr50.datatypes.database.PlayerStat;
import com.gmail.nossr50.datatypes.player.PlayerProfile;

public class BungeeDatabaseManager implements DatabaseManager {

    public BungeeDatabaseManager() {

    }

    @Override
    public void purgePowerlessUsers() {
        // Ignore
    }

    @Override
    public void purgeOldUsers() {
        // Ignore
    }

    @Override
    public boolean removeUser(String playerName) {
        // Return failure - Operation not supported
        return false;
    }

    @Override
    public void saveUser(PlayerProfile profile) {
        // TODO Implement

    }

    @Override
    public List<PlayerStat> readLeaderboard(String skillName, int pageNumber, int statsPerPage) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Integer> readRank(String playerName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void newUser(String playerName) {
        // So far, only used by existing DBMans.
        // Safe to ignore atm.
        return;
    }

    @Override
    public PlayerProfile loadPlayerData(String playerName, boolean createNew) {
        // TODO Implement
        return null;
    }

    @Override
    public List<String> getStoredUsers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void convertUsers(DatabaseManager destination) {
        // TODO Auto-generated method stub

    }
}
