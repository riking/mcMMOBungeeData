package me.riking.bungeemmo.bukkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import me.riking.bungeemmo.bukkit.fetcher.LeaderboardFuture;
import me.riking.bungeemmo.bukkit.fetcher.ProfileFuture;
import me.riking.bungeemmo.common.data.LeaderboardRequest;
import me.riking.bungeemmo.common.messaging.ProfilePushMessage;

import com.gmail.nossr50.database.DatabaseManager;
import com.gmail.nossr50.datatypes.database.PlayerStat;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.SkillType;

public class BungeeDatabaseManager implements DatabaseManager {
    private final BukkitPlugin plugin;
    public static final long EXPIRY_TIME = 600000;

    public BungeeDatabaseManager(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void saveUser(PlayerProfile arg0) {
        plugin.connMan.addPacket(new ProfilePushMessage(arg0.getPlayerName(), TransitDataConverter.toTransit(arg0)));
    }

    @Override
    public void convertUsers(DatabaseManager destination) {
        // We can't pull a user list from the proxy, it would be too damn big...
        // Figure this out later
        throw new UnsupportedOperationException("BungeeDatabaseManager does not yet support from-conversion. Point SQLDatabaseManager at the proxy's database.");
    }

    @Override
    public List<String> getStoredUsers() {
        // We can't pull a user list from the proxy, it would be too damn big...
        // Figure this out later
        throw new UnsupportedOperationException("BungeeDatabaseManager does not yet support from-conversion. Point SQLDatabaseManager at the proxy's database.");
    }

    @Override
    public PlayerProfile loadPlayerProfile(String playerName, boolean createIfEmpty) {
        PlayerProfile cached = plugin.dataStore.cachedProfiles.get(playerName);
        if (cached != null) {
            // Skip requesting
            return cached;
        }
        ProfileFuture future = plugin.dataFetcher.getProfile(playerName, createIfEmpty);
        // TODO now what, we can't block this.
        return null;
    }

    // This doesn't have any external usage, and we don't need it anyways.
    @Override
    public void newUser(String playerName) {
    }

    @Override
    public List<PlayerStat> readLeaderboard(String skillName, int pageNumber, int statsPerPage) {
        LeaderboardRequest request;
        if (skillName.equalsIgnoreCase("all")) {
            request = new LeaderboardRequest(null, pageNumber, statsPerPage);
        } else {
            SkillType type = SkillType.getSkill(skillName);
            request = new LeaderboardRequest(TransitDataConverter.toTransit(type), pageNumber, statsPerPage);
        }

        LeaderData data = plugin.dataStore.cachedLeaderboard.get(request);
        if (data != null) {
            long expires = System.currentTimeMillis() + EXPIRY_TIME;
            if (data.lastRefresh < expires) {
                return data.stats;
            } else {
                if (Thread.currentThread().equals(BukkitPlugin.serverThread)) {
                    // Cannot hang server thread, so ask for refesh and just give what we have
                    plugin.dataFetcher.getLeaderboard(request);
                    return data.stats;
                }
            }
        } else {
            if (Thread.currentThread().equals(BukkitPlugin.serverThread)) {
                // Cannot hang server thread, so ask for the data and return null
                plugin.dataFetcher.getLeaderboard(request);
                return null;
            }
        }
        LeaderboardFuture future = plugin.dataFetcher.getLeaderboard(request);

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
}
