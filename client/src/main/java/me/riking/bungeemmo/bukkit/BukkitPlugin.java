package me.riking.bungeemmo.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.nossr50.database.DatabaseManagerFactory;

public class BukkitPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        // This will throw an exception if the versions are bad
        DatabaseManagerFactory.setCustomDatabaseManagerClass(BungeeDatabaseManager.class);
    }

    @Override
    public void onEnable() {
        // No dependency checks needed :3
    }
}
