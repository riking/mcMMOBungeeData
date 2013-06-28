package me.riking.bungeemmo.bungee;

import net.md_5.bungee.api.plugin.Plugin;

/*
 * Protocol
 * Proxy -> Node: Push a loaded PlayerProfile
 * Proxy -> Node: Request PlayerProfile save
 * Node -> Proxy: Save a modified PlayerProfile
 * Node -> Proxy: Query Leaderboard, Ranks
 * Node -> Proxy: Ask for conversion
 */

public class BungeePlugin extends Plugin {
    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener());
        getProxy().registerChannel("mcmmodata");
    }
}
