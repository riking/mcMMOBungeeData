package me.riking.bungeemmo.bukkit;

import java.util.HashMap;
import java.util.Map;

import me.riking.bungeemmo.common.messaging.ProfilePullMessage;

import com.gmail.nossr50.datatypes.player.PlayerProfile;

/**
 * Manager class for getting PlayerProfiles from Bungee and delivering them to
 * players. All methods must be non-blocking because they are called from the
 * main thread.
 */
public class DataFetcher {
    private final BukkitPlugin plugin;
    private final Map<String, PlayerProfile> pendingProfiles;

    public DataFetcher(BukkitPlugin plugin) {
        this.plugin = plugin;
        this.pendingProfiles = new HashMap<String, PlayerProfile>();
    }

    public void startFetch(String playerName, boolean create) {
        pendingProfiles.put(playerName, null);
        plugin.connMan.addPacket(new ProfilePullMessage(playerName, create));
    }

    public void cancelFetch(String playerName) {
        pendingProfiles.remove(playerName);
    }

    /**
     * Check if the PlayerProfile is being fetched.
     *
     * @param playerName
     * @return
     */
    public boolean isPending(String playerName) {
        return pendingProfiles.containsKey(playerName);
    }

    public boolean isReady(String playerName) {
        return pendingProfiles.get(playerName) != null;
    }

    /**
     * Check if the PlayerProfile is ready, and if so, get it.
     * <p>
     * This must be non-blocking because it is called from the main thread.
     *
     * @param playerName
     * @return the PlayerProfile if ready, null if not
     * @throws IllegalArgumentException if there is no pending fetch for the
     *             player
     */
    public PlayerProfile tryGet(String playerName) {
        //Validate.isTrue(pendingProfiles.containsKey(playerName), "A profile for that player is not pending");
        return pendingProfiles.get(playerName);
    }

    public void fulfill(PlayerProfile profile) {
        String playerName = profile.getPlayerName();
        if (pendingProfiles.containsKey(playerName)) {
            if (pendingProfiles.get(playerName) == null) {
                pendingProfiles.put(playerName, profile);
            }
        }
    }
}
