package me.riking.bungeemmo.bukkit;

import java.util.List;

import com.gmail.nossr50.datatypes.database.PlayerStat;

public class LeaderData {
    public List<PlayerStat> stats;
    public long lastRefresh;

    public LeaderData() {
    }
}