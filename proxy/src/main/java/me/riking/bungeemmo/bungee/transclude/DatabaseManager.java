package me.riking.bungeemmo.bungee.transclude;

import java.util.List;

import me.riking.bungeemmo.common.data.LeaderboardRequest;
import me.riking.bungeemmo.common.data.TransitLeaderboardValue;
import me.riking.bungeemmo.common.data.TransitPlayerProfile;
import me.riking.bungeemmo.common.data.TransitPlayerRank;

public interface DatabaseManager {
    // One month in milliseconds
    public final long PURGE_TIME = 2630000000L * 3; // TODO Config.getInstance().getOldUsersCutoff();

    // Milliseconds in a second
    public final long MILLIS_CONVERSION_FACTOR = 1000;

    /**
     * Purge users with 0 power level from the database.
     */
    public void purgePowerlessUsers();

    /**
     * Purge users who haven't logged on in over a certain time frame from the database.
     */
    public void purgeOldUsers();

    /**
     * Remove a user from the database.
     *
     * @param playerName The name of the user to remove
     * @return true if the user was successfully removed, false otherwise
     */
    public boolean removeUser(String playerName);

    /**
     * Save a user to the database.
     *
     * @param profile The profile of the player to save
     */
    public void saveUser(TransitPlayerProfile profile);

    /**
    * Retrieve leaderboard info.
    *
    * @param skillName The skill to retrieve info on
    * @param pageNumber Which page in the leaderboards to retrieve
    * @param statsPerPage The number of stats per page
    * @return the requested leaderboard information
    */
    public List<TransitLeaderboardValue> readLeaderboard(LeaderboardRequest request);

    /**
     * Retrieve rank info.
     *
     * @param playerName The name of the user to retrieve the rankings for
     * @return the requested rank information
     */
    public TransitPlayerRank readRank(String playerName);

    /**
     * Add a new user to the database.
     *
     * @param playerName The name of the player to be added to the database
     */
    public void newUser(String playerName);

    /**
     * Load a player from the database.
     *
     * @param playerName The name of the player to load from the database
     * @param createNew Whether to create a new record if the player is not
     *          found
     * @return The player's data, or an unloaded PlayerProfile if not found
     *          and createNew is false
     */
    public TransitPlayerProfile loadPlayerProfile(String playerName, boolean createNew);

    /**
     * Get all users currently stored in the database.
     *
     * @return list of playernames
     */
    public List<String> getStoredUsers();
}
