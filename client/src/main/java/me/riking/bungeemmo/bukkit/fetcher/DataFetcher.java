package me.riking.bungeemmo.bukkit.fetcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gmail.nossr50.datatypes.database.PlayerStat;

import me.riking.bungeemmo.bukkit.BukkitPlugin;
import me.riking.bungeemmo.bukkit.LeaderData;
import me.riking.bungeemmo.bukkit.TransitDataConverter;
import me.riking.bungeemmo.bukkit.RankData;
import me.riking.bungeemmo.common.data.LeaderboardRequest;
import me.riking.bungeemmo.common.data.TransitPlayerProfile;
import me.riking.bungeemmo.common.data.TransitPlayerRank;
import me.riking.bungeemmo.common.messaging.LeaderboardPullMessage;
import me.riking.bungeemmo.common.messaging.ProfilePullMessage;
import me.riking.bungeemmo.common.messaging.RankPullMessage;

/**
 * Manager class for getting PlayerProfiles from Bungee and delivering them to
 * players. All methods must be non-blocking because they are called from the
 * main thread.
 */
public class DataFetcher {
    final BukkitPlugin plugin;
    final Map<String, DataFuture<TransitPlayerProfile>> pendingProfiles;
    final Map<String, DataFuture<TransitPlayerRank>> pendingRanks;
    final Map<LeaderboardRequest, DataFuture<List<PlayerStat>>> pendingLeaderboard;

    public DataFetcher(BukkitPlugin plugin) {
        this.plugin = plugin;
        this.pendingProfiles = new HashMap<String, DataFuture<TransitPlayerProfile>>();
        this.pendingRanks = new HashMap<String, DataFuture<TransitPlayerRank>>();
        this.pendingLeaderboard = new HashMap<LeaderboardRequest, DataFuture<List<PlayerStat>>>();
    }

    /**
     * Give the received PlayerProfile to all waiting threads.
     */
    public void fulfill(String player, TransitPlayerProfile profile) {
        plugin.dataStore.cachedProfiles.put(player, TransitDataConverter.fromTransit(profile));

        synchronized (pendingProfiles) {
            DataFuture<TransitPlayerProfile> future = pendingProfiles.remove(player);
            if (future != null) {
                future.fulfill(profile);
            }
        }
    }

    /**
     * Give the received rank data to all waiting threads.
     */
    public void fulfill(String player, TransitPlayerRank rank) {
        RankData data = new RankData();
        data.rank = rank;
        data.lastRefresh = System.currentTimeMillis();
        plugin.dataStore.cachedRanks.put(player, data);

        synchronized (pendingRanks) {
            DataFuture<TransitPlayerRank> future = pendingRanks.remove(player);
            if (future != null) {
                future.fulfill(rank);
            }
        }
    }

    /**
     * Give the received leaderboard data to all waiting threads.
     */
    public void fulfill(LeaderboardRequest request, List<PlayerStat> board) {
        LeaderData data = new LeaderData();
        data.stats = board;
        data.lastRefresh = System.currentTimeMillis();
        plugin.dataStore.cachedLeaderboard.put(request, data);

        synchronized (pendingLeaderboard) {
            DataFuture<List<PlayerStat>> future = pendingLeaderboard.remove(request);
            if (future != null) {
                future.fulfill(board);
            }
        }
    }

    public DataFuture<TransitPlayerProfile> getProfile(String playerName) {
        return getProfile(playerName, true);
    }

    /**
     * Start getting the PlayerProfile from the proxy for this player.
     *
     * @param playerName
     * @return the ProfileFuture that will recieve the data
     */
    public DataFuture<TransitPlayerProfile> getProfile(String playerName, boolean createInDb) {
        DataFuture<TransitPlayerProfile> future;
        synchronized (pendingProfiles) {
            future = pendingProfiles.get(playerName);
            if (future != null) {
                return future;
            }
            future = new DataFuture<TransitPlayerProfile>();
            pendingProfiles.put(playerName, future);
        }

        startProfileFetch(playerName, createInDb);
        return future;
    }

    public DataFuture<TransitPlayerRank> getRank(String playerName) {
        DataFuture<TransitPlayerRank> future;
        synchronized (pendingRanks) {
            future = pendingRanks.get(playerName);
            if (future != null) {
                return future;
            }
            future = new DataFuture<TransitPlayerRank>();
            pendingRanks.put(playerName, future);
        }

        startRankFetch(playerName);
        return future;
    }

    public DataFuture<List<PlayerStat>> getLeaderboard(LeaderboardRequest request) {
        DataFuture<List<PlayerStat>> future;
        synchronized (pendingLeaderboard) {
            future = pendingLeaderboard.get(request);
            if (future != null) {
                return future;
            }
            future = new DataFuture<List<PlayerStat>>();
            pendingLeaderboard.put(request, future);
        }

        startLeaderFetch(request);
        return future;
    }

    public DataFuture<TransitPlayerProfile> getPendingProfile(String player) {
        return pendingProfiles.get(player);
    }

    public DataFuture<TransitPlayerRank> getPendingRank(String player) {
        return pendingRanks.get(player);
    }

    public DataFuture<List<PlayerStat>> getPendingLeaderboard(LeaderboardRequest request) {
        return pendingLeaderboard.get(request);
    }

    void startProfileFetch(String playerName, boolean createInDb) {
        ProfilePullMessage m = new ProfilePullMessage(playerName, createInDb);
        plugin.connMan.addPacket(m);
    }

    void startRankFetch(String playerName) {
        RankPullMessage m = new RankPullMessage(playerName);
        plugin.connMan.addPacket(m);
    }

    void startLeaderFetch(LeaderboardRequest request) {
        LeaderboardPullMessage m = new LeaderboardPullMessage(request);
        plugin.connMan.addPacket(m);
    }
}
