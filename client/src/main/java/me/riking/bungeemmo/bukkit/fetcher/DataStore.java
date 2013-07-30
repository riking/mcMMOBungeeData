package me.riking.bungeemmo.bukkit.fetcher;

import java.util.HashMap;
import java.util.Map;

import me.riking.bungeemmo.bukkit.BukkitPlugin;
import me.riking.bungeemmo.bukkit.LeaderData;
import me.riking.bungeemmo.bukkit.RankData;
import me.riking.bungeemmo.common.data.LeaderboardRequest;

import com.gmail.nossr50.datatypes.player.PlayerProfile;

public class DataStore {
    private BukkitPlugin plugin;
    public Map<String, PlayerProfile> cachedProfiles;
    /*  */ Map<String, Long> profileCacheAge;
    public Map<String, RankData> cachedRanks;
    public Map<LeaderboardRequest, LeaderData> cachedLeaderboard;

    public DataStore(BukkitPlugin plugin) {
        this.plugin = plugin;
        cachedProfiles = new HashMap<String, PlayerProfile>();
        cachedRanks = new HashMap<String, RankData>();
        cachedLeaderboard = new HashMap<LeaderboardRequest, LeaderData>();
    }
}
