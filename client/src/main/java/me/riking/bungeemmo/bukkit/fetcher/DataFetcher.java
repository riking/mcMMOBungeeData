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
    private final BukkitPlugin plugin;
    private final Map<String, ProfileFuture> pendingProfiles;
    private final Map<String, RankFuture> pendingRanks;
    private final Map<LeaderboardRequest, LeaderboardFuture> pendingLeaderboard;

    public DataFetcher(BukkitPlugin plugin) {
        this.plugin = plugin;
        this.pendingProfiles = new HashMap<String, ProfileFuture>();
        this.pendingRanks = new HashMap<String, RankFuture>();
        this.pendingLeaderboard = new HashMap<LeaderboardRequest, LeaderboardFuture>();
    }

    /**
     * Give the received PlayerProfile to all waiting threads.
     */
    public void fulfill(String player, TransitPlayerProfile profile) {
        plugin.dataStore.cachedProfiles.put(player, TransitDataConverter.fromTransit(profile));

        synchronized (pendingProfiles) {
            ProfileFuture future = pendingProfiles.remove(player);
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
            RankFuture future = pendingRanks.get(player);
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
            LeaderboardFuture future = pendingLeaderboard.get(request);
            if (future != null) {
                future.fulfill(board);
            }
        }
    }

    public ProfileFuture getProfile(String playerName) {
        return getProfile(playerName, true);
    }

    /**
     * Start getting the PlayerProfile from the proxy for this player.
     *
     * @param playerName
     * @return the ProfileFuture that will recieve the data
     */
    public ProfileFuture getProfile(String playerName, boolean createInDb) {
        ProfileFuture future;
        synchronized (pendingProfiles) {
            future = pendingProfiles.get(playerName);
            if (future != null) {
                return future;
            }
            future = new ProfileFuture();
            pendingProfiles.put(playerName, future);
        }

        startProfileFetch(playerName, createInDb);
        return future;
    }

    public RankFuture getRank(String playerName) {
        RankFuture future;
        synchronized (pendingRanks) {
            future = pendingRanks.get(playerName);
            if (future != null) {
                return future;
            }
            future = new RankFuture();
            pendingRanks.put(playerName, future);
        }

        startRankFetch(playerName);
        return future;
    }

    public LeaderboardFuture getLeaderboard(LeaderboardRequest request) {
        LeaderboardFuture future;
        synchronized (pendingLeaderboard) {
            future = pendingLeaderboard.get(request);
            if (future != null) {
                return future;
            }
            future = new LeaderboardFuture();
            pendingLeaderboard.put(request, future);
        }

        startLeaderFetch(request);
        return future;
    }

    private void startProfileFetch(String playerName, boolean createInDb) {
        ProfilePullMessage m = new ProfilePullMessage(playerName, createInDb);
        plugin.connMan.addPacket(m);
    }

    private void startRankFetch(String playerName) {
        RankPullMessage m = new RankPullMessage(playerName);
        plugin.connMan.addPacket(m);
    }

    private void startLeaderFetch(LeaderboardRequest request) {
        LeaderboardPullMessage m = new LeaderboardPullMessage(request);
        plugin.connMan.addPacket(m);
    }
}
