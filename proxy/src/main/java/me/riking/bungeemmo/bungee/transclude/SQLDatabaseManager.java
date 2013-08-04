package me.riking.bungeemmo.bungee.transclude;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import me.riking.bungeemmo.bungee.BungeePlugin;
import me.riking.bungeemmo.bungee.Config;
import me.riking.bungeemmo.common.data.LeaderboardRequest;
import me.riking.bungeemmo.common.data.TransitAbilityType;
import me.riking.bungeemmo.common.data.TransitHudType;
import me.riking.bungeemmo.common.data.TransitLeaderboardValue;
import me.riking.bungeemmo.common.data.TransitMobHealthbarType;
import me.riking.bungeemmo.common.data.TransitPlayerProfile;
import me.riking.bungeemmo.common.data.TransitPlayerRank;
import me.riking.bungeemmo.common.data.TransitSkillType;

public final class SQLDatabaseManager implements DatabaseManager {
    private BungeePlugin plugin;
    private String connectionString;
    private String tablePrefix = Config.getInstance().getMySQLTablePrefix();
    private Connection connection = null;

    // Scale waiting time by this much per failed attempt
    private final double SCALING_FACTOR = 40.0;

    // Minimum wait in nanoseconds (default 500ms)
    private final long MIN_WAIT = 500L * 1000000L;

    // Maximum time to wait between reconnects (default 5 minutes)
    private final long MAX_WAIT = 5L * 60L * 1000L * 1000000L;

    // How long to wait when checking if connection is valid (default 3 seconds)
    private final int VALID_TIMEOUT = 3;

    // When next to try connecting to Database in nanoseconds
    private long nextReconnectTimestamp = 0L;

    // How many connection attempts have failed
    private int reconnectAttempt = 0;

    // PreparedStatement holder
    private SQLStatements sqlStatements;

    public SQLDatabaseManager(BungeePlugin plugin, Config config) {
        SQLStatements.setupRaw(tablePrefix);

        checkConnected();
        sqlStatements = new SQLStatements(plugin, connection);
        checkStructure();
    }

    public void purgePowerlessUsers(Set<String> online) {
        checkConnected();
        plugin.getLogger().info("Purging powerless users...");

        Collection<ArrayList<String>> usernames = read("SELECT u.user FROM " + tablePrefix + "skills AS s, " + tablePrefix + "users AS u WHERE s.user_id = u.id AND (s.taming+s.mining+s.woodcutting+s.repair+s.unarmed+s.herbalism+s.excavation+s.archery+s.swords+s.axes+s.acrobatics+s.fishing) = 0").values();

        write();

        processPurge(usernames, future);
        plugin.getLogger().info("Purged " + usernames.size() + " users from the database.");
    }

    public void purgeOldUsers(Set<String> online) {
        checkConnected();
        long currentTime = System.currentTimeMillis();

        plugin.getLogger().info("Purging old users...");

        write();
        setInt(1, currentTime);
        setInt(2, PURGE_TIME);

        plugin.getLogger().info("Purged " + usernames.size() + " users from the database.");;
    }

    public boolean removeUser(String playerName) {
        checkConnected();
        boolean success = update() != 0;

        Misc.profileCleanup(playerName);

        return success;
    }

    public void saveUser(TransitPlayerProfile profile) {
        checkConnected();
        int userId = readId(profile.playerName);
        if (userId == -1) {
            newUser(profile.playerName);
            userId = readId(profile.playerName);
            if (userId == -1) {
                plugin.getLogger().log(Level.WARNING, "Failed to save user " + profile.playerName);
                return;
            }
        }
        TransitMobHealthbarType mobHealthbarType = profile.mobHealthbarType;
        TransitHudType hudType = profile.hudType;

        saveLogin(userId, ((int) (System.currentTimeMillis() / Misc.TIME_CONVERSION_FACTOR)));
        saveHuds(userId, (hudType == null ? "STANDARD" : hudType.toString()), (mobHealthbarType == null ? Config.getInstance().getMobHealthbarDefault().toString() : mobHealthbarType.toString()));
        saveLongs(
                sqlStatements.saveCooldowns,
                userId,
                profile.getSkillDATS(TransitAbilityType.SUPER_BREAKER),
                profile.getSkillDATS(TransitAbilityType.TREE_FELLER),
                profile.getSkillDATS(TransitAbilityType.BERSERK),
                profile.getSkillDATS(TransitAbilityType.GREEN_TERRA),
                profile.getSkillDATS(TransitAbilityType.GIGA_DRILL_BREAKER),
                profile.getSkillDATS(TransitAbilityType.SERRATED_STRIKES),
                profile.getSkillDATS(TransitAbilityType.SKULL_SPLITTER),
                profile.getSkillDATS(TransitAbilityType.BLAST_MINING));
        saveIntegers(
                sqlStatements.saveSkillLevels,
                profile.getSkillLevel(TransitSkillType.TAMING),
                profile.getSkillLevel(TransitSkillType.MINING),
                profile.getSkillLevel(TransitSkillType.REPAIR),
                profile.getSkillLevel(TransitSkillType.WOODCUTTING),
                profile.getSkillLevel(TransitSkillType.UNARMED),
                profile.getSkillLevel(TransitSkillType.HERBALISM),
                profile.getSkillLevel(TransitSkillType.EXCAVATION),
                profile.getSkillLevel(TransitSkillType.ARCHERY),
                profile.getSkillLevel(TransitSkillType.SWORDS),
                profile.getSkillLevel(TransitSkillType.AXES),
                profile.getSkillLevel(TransitSkillType.ACROBATICS),
                profile.getSkillLevel(TransitSkillType.FISHING),
                userId);
        saveIntegers(
                sqlStatements.saveSkillXps,
                profile.getSkillXpLevel(TransitSkillType.TAMING),
                profile.getSkillXpLevel(TransitSkillType.MINING),
                profile.getSkillXpLevel(TransitSkillType.REPAIR),
                profile.getSkillXpLevel(TransitSkillType.WOODCUTTING),
                profile.getSkillXpLevel(TransitSkillType.UNARMED),
                profile.getSkillXpLevel(TransitSkillType.HERBALISM),
                profile.getSkillXpLevel(TransitSkillType.EXCAVATION),
                profile.getSkillXpLevel(TransitSkillType.ARCHERY),
                profile.getSkillXpLevel(TransitSkillType.SWORDS),
                profile.getSkillXpLevel(TransitSkillType.AXES),
                profile.getSkillXpLevel(TransitSkillType.ACROBATICS),
                profile.getSkillXpLevel(TransitSkillType.FISHING),
                userId);
    }

    public List<TransitLeaderboardValue> readLeaderboard(LeaderboardRequest request) {
        List<TransitLeaderboardValue> stats = new ArrayList<TransitLeaderboardValue>();

        if (checkConnected()) {
            String query = skillName.equalsIgnoreCase("ALL") ?  : skillName;
            ResultSet resultSet = null;
            PreparedStatement statement = null;

            try {
                statement = connection.prepareStatement("SELECT " + query + ", user, NOW() FROM " + tablePrefix + "users JOIN " + tablePrefix + "skills ON (user_id = id) WHERE " + query + " > 0 ORDER BY " + query + " DESC, user LIMIT ?, ?");
                statement.setInt(1, (pageNumber * statsPerPage) - statsPerPage);
                statement.setInt(2, statsPerPage);
                resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    ArrayList<String> column = new ArrayList<String>();

                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        column.add(resultSet.getString(i));
                    }

                    stats.add(new TransitLeaderboardValue(column.get(1), Integer.valueOf(column.get(0))));
                }
            }
            catch (SQLException ex) {
                printErrors(ex);
            }
            finally {
                if (statement != null) {
                    try {
                        statement.close();
                    }
                    catch (SQLException e) {
                        // Ignore
                    }
                }
            }
        }

        return stats;
    }

    public TransitPlayerRank readRank(String playerName) {
        Map<TransitSkillType, Integer> skills = new HashMap<TransitSkillType, Integer>();

        if (checkConnected()) {
            ResultSet resultSet;

            try {
                for (TransitSkillType skillType : TransitSkillType.nonChildSkills()) {
                    String skillName = skillType.name().toLowerCase();
                    String sql = "SELECT COUNT(*) AS rank FROM " + tablePrefix + "users JOIN " + tablePrefix + "skills ON user_id = id WHERE " + skillName + " > 0 " +
                                 "AND " + skillName + " > (SELECT " + skillName + " FROM " + tablePrefix + "users JOIN " + tablePrefix + "skills ON user_id = id " +
                                 "WHERE user = ?)";

                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, playerName);
                    resultSet = statement.executeQuery();

                    resultSet.next();

                    int rank = resultSet.getInt("rank");

                    sql = "SELECT user, " + skillName + " FROM " + tablePrefix + "users JOIN " + tablePrefix + "skills ON user_id = id WHERE " + skillName + " > 0 " +
                          "AND " + skillName + " = (SELECT " + skillName + " FROM " + tablePrefix + "users JOIN " + tablePrefix + "skills ON user_id = id " +
                          "WHERE user = '" + playerName + "') ORDER BY user";

                    statement.close();

                    statement = connection.prepareStatement(sql);
                    resultSet = statement.executeQuery();

                    while (resultSet.next()) {
                        if (resultSet.getString("user").equalsIgnoreCase(playerName)) {
                            skills.put(skillType.name(), rank + resultSet.getRow());
                            break;
                        }
                    }

                    statement.close();
                }

                String sql = "SELECT COUNT(*) AS rank FROM " + tablePrefix + "users JOIN " + tablePrefix + "skills ON user_id = id " +
                        "WHERE taming+mining+woodcutting+repair+unarmed+herbalism+excavation+archery+swords+axes+acrobatics+fishing > 0 " +
                        "AND taming+mining+woodcutting+repair+unarmed+herbalism+excavation+archery+swords+axes+acrobatics+fishing > " +
                        "(SELECT taming+mining+woodcutting+repair+unarmed+herbalism+excavation+archery+swords+axes+acrobatics+fishing " +
                        "FROM " + tablePrefix + "users JOIN " + tablePrefix + "skills ON user_id = id WHERE user = ?)";

                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, playerName);
                resultSet = statement.executeQuery();

                resultSet.next();

                int rank = resultSet.getInt("rank");

                statement.close();

                sql = "SELECT user, taming+mining+woodcutting+repair+unarmed+herbalism+excavation+archery+swords+axes+acrobatics+fishing " +
                        "FROM " + tablePrefix + "users JOIN " + tablePrefix + "skills ON user_id = id " +
                        "WHERE taming+mining+woodcutting+repair+unarmed+herbalism+excavation+archery+swords+axes+acrobatics+fishing > 0 " +
                        "AND taming+mining+woodcutting+repair+unarmed+herbalism+excavation+archery+swords+axes+acrobatics+fishing = " +
                        "(SELECT taming+mining+woodcutting+repair+unarmed+herbalism+excavation+archery+swords+axes+acrobatics+fishing " +
                        "FROM " + tablePrefix + "users JOIN " + tablePrefix + "skills ON user_id = id WHERE user = ?) ORDER BY user";

                statement = connection.prepareStatement(sql);
                statement.setString(1, playerName);
                resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    if (resultSet.getString("user").equalsIgnoreCase(playerName)) {
                        skills.put("ALL", rank + resultSet.getRow());
                        break;
                    }
                }

                statement.close();
            }
            catch (SQLException ex) {
                printErrors(ex);
            }
        }

        return skills;
    }

    public void newUser(String playerName) {
        checkConnected();
        PreparedStatement statement = null;

        try {
            statement = sqlStatements.newUser;
            statement.setString(1, playerName);
            statement.setLong(2, System.currentTimeMillis() / Misc.TIME_CONVERSION_FACTOR);
            statement.execute();

            int id = readId(playerName);
            writeMissingRows(id);
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }
    }

    public TransitPlayerProfile loadPlayerProfile(String playerName, boolean create) {
        checkConnected();
        PreparedStatement statement = null;

        try {
            statement = sqlStatements.loadUser;
            statement.setString(1, playerName);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                try {
                    TransitPlayerProfile ret = loadFromResult(playerName, result);
                    result.close();
                    return ret;
                }
                catch (SQLException e) {}
            }
            result.close();
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }

        // Problem, nothing was returned

        // First, read User Id - this is to check for orphans

        int id = readId(playerName);

        if (id == -1) {
            // There is no such user
            if (create) {
                newUser(playerName);
            }

            return new TransitPlayerProfile(playerName, create);
        }
        // There is such a user
        writeMissingRows(id);
        // Retry, and abort on re-failure
        return loadPlayerProfile(playerName, false);
    }

    public void convertUsers(DatabaseManager destination) {
        checkConnected();
        PreparedStatement statement = null;

        try {
            List<String> usernames = getStoredUsers();
            synchronized (sqlStatements.loadUser) {

            }
            statement = sqlStatements.loadUser;
            ResultSet result = null;
            for (String playerName : usernames) {
                statement.setString(1, playerName);
                try {
                    result = statement.executeQuery();
                    result.next();
                    destination.saveUser(loadFromResult(playerName, result));
                    result.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }
        catch (SQLException e) {
            printErrors(e);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
        }
    }

    public List<String> getStoredUsers() {
        checkConnected();
        ArrayList<String> users = new ArrayList<String>();
        try (ResultSet result = sqlStatements.getAllUsers.executeQuery()) {
            while (result.next()) {
                users.add(result.getString("user"));
            }
        } catch (SQLException e) {
            printErrors(e);
        }
        return users;
    }

    /**
    * Check connection status and re-establish if dead or stale.
    *
    * If the very first immediate attempt fails, further attempts
    * will be made in progressively larger intervals up to MAX_WAIT
    * intervals.
    *
    * This allows for MySQL to time out idle connections as needed by
    * server operator, without affecting McMMO, while still providing
    * protection against a database outage taking down Bukkit's tick
    * processing loop due to attempting a database connection each
    * time McMMO needs the database.
    *
    * @return the boolean value for whether or not we are connected
    */
    public boolean checkConnected() {
        boolean isClosed = true;
        boolean isValid = false;
        boolean exists = (connection != null);

        // If we're waiting for server to recover then leave early
        if (nextReconnectTimestamp > 0 && nextReconnectTimestamp > System.nanoTime()) {
            return false;
        }

        if (exists) {
            try {
                isClosed = connection.isClosed();
            }
            catch (SQLException e) {
                isClosed = true;
                e.printStackTrace();
                printErrors(e);
            }

            if (!isClosed) {
                try {
                    isValid = connection.isValid(VALID_TIMEOUT);
                }
                catch (SQLException e) {
                    // Don't print stack trace because it's valid to lose idle connections to the server and have to restart them.
                    isValid = false;
                }
            }
        }

        // Leave if all ok
        if (exists && !isClosed && isValid) {
            if (sqlStatements.isClosed()) {
                sqlStatements = new SQLStatements(plugin, connection);
            }
            // Housekeeping
            nextReconnectTimestamp = 0;
            reconnectAttempt = 0;
            return true;
        }

        // Cleanup after ourselves for GC and MySQL's sake
        if (exists && !isClosed) {
            try {
                connection.close();
            }
            catch (SQLException ex) {
                // This is a housekeeping exercise, ignore errors
            }
            try {
                sqlStatements.close();
            }
            catch (SQLException ex) {
                // Ignore
            }
        }

        // Try to connect again
        connect();

        // Leave if connection is good
        try {
            if (connection != null && !connection.isClosed()) {
                // Schedule a database save if we really had an outage
                // TODO change to bungee
                if (reconnectAttempt > 1) {
                    new SQLReconnectTask().runTaskLater(plugin, 5);
                }
                nextReconnectTimestamp = 0;
                reconnectAttempt = 0;
                sqlStatements = new SQLStatements(connection);
                return true;
            }
        }
        catch (SQLException e) {
            // Failed to check isClosed, so presume connection is bad and attempt later
            e.printStackTrace();
            printErrors(e);
        }

        reconnectAttempt++;
        nextReconnectTimestamp = (long) (System.nanoTime() + Math.min(MAX_WAIT, (reconnectAttempt * SCALING_FACTOR * MIN_WAIT)));
        return false;
    }

    /**
     * Attempt to connect to the mySQL database.
     */
    private void connect() {
        connectionString = "jdbc:mysql://" + Config.getInstance().getMySQLServerName() + ":" + Config.getInstance().getMySQLServerPort() + "/" + Config.getInstance().getMySQLDatabaseName();

        try {
            plugin.getLogger().info("Attempting connection to MySQL...");

            // Force driver to load if not yet loaded
            Class.forName("com.mysql.jdbc.Driver");
            Properties connectionProperties = new Properties();
            connectionProperties.put("user", Config.getInstance().getMySQLUserName());
            connectionProperties.put("password", Config.getInstance().getMySQLUserPassword());
            connectionProperties.put("autoReconnect", "false");
            connectionProperties.put("maxReconnects", "0");
            connection = DriverManager.getConnection(connectionString, connectionProperties);

            plugin.getLogger().info("Connection to MySQL was a success!");
        }
        catch (SQLException ex) {
            connection = null;

            if (reconnectAttempt == 0 || reconnectAttempt >= 11) {
                plugin.getLogger().info("Connection to MySQL failed!");
            }
        }
        catch (ClassNotFoundException ex) {
            connection = null;

            if (reconnectAttempt == 0 || reconnectAttempt >= 11) {
                plugin.getLogger().info("MySQL database driver not found!");
            }
        }
    }

    /**
     * Run all checks and fixes on the database structure.
     */
    private void checkStructure() {
        write(SQLStatements.SQLCreateTableUsers);
        write(SQLStatements.SQLCreateTableHuds);
        write(SQLStatements.SQLCreateTableCooldowns);
        write(SQLStatements.SQLCreateTableSkills);
        write(SQLStatements.SQLCreateTableExperience);

        try {
            writeThrow(SQLStatements.SQLCheckBlastMining);
        } catch (SQLException e) {
            plugin.getLogger().info("Updating mcMMO MySQL tables for Blast Mining...");
            write(SQLStatements.SQLFixBlastMining);
        }

        try {
            writeThrow(SQLStatements.SQLCheckFishing);
        } catch (SQLException e) {
            plugin.getLogger().info("Updating mcMMO MySQL tables for Fishing...");
            write(SQLStatements.SQLFixFishing1);
            write(SQLStatements.SQLFixFishing2);
        }

        try {
            writeThrow(SQLStatements.SQLCheckHealthbars);
        } catch (SQLException e) {
            plugin.getLogger().info("Updating mcMMO MySQL tables for mob healthbars...");
            write(SQLStatements.SQLFixHealthbars);
        }

        try {
            writeThrow(SQLStatements.SQLFixParties);
        } catch (SQLException e) {
            // Ignore
        }

        if (read(SQLStatements.SQLCheckIndex).size() != 13 && checkConnected()) {
            plugin.getLogger().info("Indexing tables, this may take a while on larger databases");
            write(SQLStatements.SQLFixIndex);
        }

        plugin.getLogger().info("Killing orphans");
        write(SQLStatements.SQLKillOrphansHuds);
        write(SQLStatements.SQLKillOrphansCooldowns);
        write(SQLStatements.SQLKillOrphansSkills);
        write(SQLStatements.SQLKillOrphansExperience);
    }

    /**
     * Attempt to write the SQL query, and rethrow any errors.
     *
     * @param sql
     * @throws SQLException exception thrown
     */
    private void writeThrow(String sql) throws SQLException {
        if (!checkConnected()) {
            return;
        }
        Statement s = connection.createStatement();
        s.execute(sql);
    }

    /**
     * Attempt to write the SQL query. PreparedStatements are not used.
     *
     * @param sql Query to write.
     * @return true if the query was successfully written, false otherwise.
     */
    private boolean write(String sql) {
        if (!checkConnected()) {
            return false;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            return true;
        }
        catch (SQLException ex) {
            printErrors(ex);
            return false;
        }
    }

    /**
     * Returns the number of rows affected by either a DELETE or UPDATE query
     *
     * @param sql SQL query to execute
     * @return the number of rows affected
     */
    private int update(String sql) {
        int rows = 0;

        if (checkConnected()) {
            PreparedStatement statement = null;

            try {
                statement = connection.prepareStatement(sql);
                rows = statement.executeUpdate();
            }
            catch (SQLException ex) {
                printErrors(ex);
            }
            finally {
                if (statement != null) {
                    try {
                        statement.close();
                    }
                    catch (SQLException e) {
                        // Ignore
                    }
                }
            }
        }

        return rows;
    }

    /**
     * Read SQL query.
     *
     * @param sql SQL query to read
     * @return the rows in this SQL query
     */
    private HashMap<Integer, ArrayList<String>> read(String sql) {
        HashMap<Integer, ArrayList<String>> rows = new HashMap<Integer, ArrayList<String>>();

        if (checkConnected()) {
            PreparedStatement statement = null;
            ResultSet resultSet;

            try {
                statement = connection.prepareStatement(sql);
                resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    ArrayList<String> column = new ArrayList<String>();

                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        column.add(resultSet.getString(i));
                    }

                    rows.put(resultSet.getRow(), column);
                }
            }
            catch (SQLException ex) {
                printErrors(ex);
            }
            finally {
                if (statement != null) {
                    try {
                        statement.close();
                    }
                    catch (SQLException e) {
                        // Ignore
                    }
                }
            }
        }

        return rows;
    }

    /**
     * Get the Integer. Only return first row / first field.
     *
     * @param sql SQL query to execute
     * @return the value in the first row / first field
     */
    private int readInt(PreparedStatement statement) {
        int result = -1;

        if (checkConnected()) {
            ResultSet resultSet = null;

            try {
                resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    result = resultSet.getInt(1);
                }
            }
            catch (SQLException ex) {
                printErrors(ex);
            }
            finally {
                if (statement != null) {
                    try {
                        statement.close();
                    }
                    catch (SQLException e) {
                        // Ignore
                    }
                }
            }
        }

        return result;
    }

    private void writeMissingRows(int id) {
        try {
            sqlStatements.missingRowHuds.setInt(1, id);
            sqlStatements.missingRowHuds.executeUpdate();
            sqlStatements.missingRowCooldowns.setInt(1, id);
            sqlStatements.missingRowCooldowns.executeUpdate();
            sqlStatements.missingRowSkills.setInt(1, id);
            sqlStatements.missingRowSkills.executeUpdate();
            sqlStatements.missingRowExperience.setInt(1, id);
            sqlStatements.missingRowExperience.executeUpdate();
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
    }

    private void saveIntegers(String sql, int... args) {
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement(sql);
            int i = 1;

            for (int arg : args) {
                statement.setInt(i++, arg);
            }

            statement.execute();
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }
    }

    private void saveLongs(String sql, int id, long... args) {
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement(sql);
            int i = 1;

            for (long arg : args) {
                statement.setLong(i++, arg);
            }

            statement.setInt(i++, id);
            statement.execute();
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
        finally {
            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Retrieve the database id for a player
     *
     * @param playerName The name of the user to retrieve the id for
     * @return the requested id or -1 if not found
     */
    private int readId(String playerName) {
        int id = -1;

        try {
            PreparedStatement statement = sqlStatements.readUserId;
            statement.setString(1, playerName);
            id = readInt(statement);
        }
        catch (SQLException ex) {
            printErrors(ex);
        }

        return id;
    }

    private void saveLogin(int id, long login) {
        try {
            PreparedStatement statement = sqlStatements.saveLoginTime;
            statement.setLong(1, login);
            statement.setInt(2, id);
            statement.execute();
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
    }

    private void saveHuds(int userId, String hudType, String mobHealthBar) {
        try {
            PreparedStatement statement = sqlStatements.saveHuds;
            statement.setString(1, hudType);
            statement.setString(2, mobHealthBar);
            statement.setInt(3, userId);
            statement.execute();
        }
        catch (SQLException ex) {
            printErrors(ex);
        }
    }

    private TransitPlayerProfile loadFromResult(String playerName, ResultSet result) throws SQLException {
        TransitPlayerProfile profile = new TransitPlayerProfile();
        profile.playerName = playerName;
        Map<TransitSkillType, Integer>   skills     = new HashMap<TransitSkillType, Integer>();   // Skill & Level
        Map<TransitSkillType, Float>     skillsXp   = new HashMap<TransitSkillType, Float>();     // Skill & XP
        Map<TransitAbilityType, Integer> skillsDATS = new HashMap<TransitAbilityType, Integer>(); // Ability & Cooldown

        final int OFFSET_SKILLS = 0; // TODO update these numbers when the query changes (a new skill is added)
        final int OFFSET_XP = 12;
        final int OFFSET_DATS = 24;
        final int OFFSET_OTHER = 36;

        skills.put(TransitSkillType.TAMING, result.getInt(OFFSET_SKILLS + 1));
        skills.put(TransitSkillType.MINING, result.getInt(OFFSET_SKILLS + 2));
        skills.put(TransitSkillType.REPAIR, result.getInt(OFFSET_SKILLS + 3));
        skills.put(TransitSkillType.WOODCUTTING, result.getInt(OFFSET_SKILLS + 4));
        skills.put(TransitSkillType.UNARMED, result.getInt(OFFSET_SKILLS + 5));
        skills.put(TransitSkillType.HERBALISM, result.getInt(OFFSET_SKILLS + 6));
        skills.put(TransitSkillType.EXCAVATION, result.getInt(OFFSET_SKILLS + 7));
        skills.put(TransitSkillType.ARCHERY, result.getInt(OFFSET_SKILLS + 8));
        skills.put(TransitSkillType.SWORDS, result.getInt(OFFSET_SKILLS + 9));
        skills.put(TransitSkillType.AXES, result.getInt(OFFSET_SKILLS + 10));
        skills.put(TransitSkillType.ACROBATICS, result.getInt(OFFSET_SKILLS + 11));
        skills.put(TransitSkillType.FISHING, result.getInt(OFFSET_SKILLS + 12));

        skillsXp.put(TransitSkillType.TAMING, result.getFloat(OFFSET_XP + 1));
        skillsXp.put(TransitSkillType.MINING, result.getFloat(OFFSET_XP + 2));
        skillsXp.put(TransitSkillType.REPAIR, result.getFloat(OFFSET_XP + 3));
        skillsXp.put(TransitSkillType.WOODCUTTING, result.getFloat(OFFSET_XP + 4));
        skillsXp.put(TransitSkillType.UNARMED, result.getFloat(OFFSET_XP + 5));
        skillsXp.put(TransitSkillType.HERBALISM, result.getFloat(OFFSET_XP + 6));
        skillsXp.put(TransitSkillType.EXCAVATION, result.getFloat(OFFSET_XP + 7));
        skillsXp.put(TransitSkillType.ARCHERY, result.getFloat(OFFSET_XP + 8));
        skillsXp.put(TransitSkillType.SWORDS, result.getFloat(OFFSET_XP + 9));
        skillsXp.put(TransitSkillType.AXES, result.getFloat(OFFSET_XP + 10));
        skillsXp.put(TransitSkillType.ACROBATICS, result.getFloat(OFFSET_XP + 11));
        skillsXp.put(TransitSkillType.FISHING, result.getFloat(OFFSET_XP + 12));

        // Taming - Unused - result.getInt(OFFSET_DATS + 1)
        skillsDATS.put(TransitAbilityType.SUPER_BREAKER, result.getInt(OFFSET_DATS + 2));
        // Repair - Unused - result.getInt(OFFSET_DATS + 3)
        skillsDATS.put(TransitAbilityType.TREE_FELLER, result.getInt(OFFSET_DATS + 4));
        skillsDATS.put(TransitAbilityType.BERSERK, result.getInt(OFFSET_DATS + 5));
        skillsDATS.put(TransitAbilityType.GREEN_TERRA, result.getInt(OFFSET_DATS + 6));
        skillsDATS.put(TransitAbilityType.GIGA_DRILL_BREAKER, result.getInt(OFFSET_DATS + 7));
        // Archery - Unused - result.getInt(OFFSET_DATS + 8)
        skillsDATS.put(TransitAbilityType.SERRATED_STRIKES, result.getInt(OFFSET_DATS + 9));
        skillsDATS.put(TransitAbilityType.SKULL_SPLITTER, result.getInt(OFFSET_DATS + 10));
        // Acrobatics - Unused - result.getInt(OFFSET_DATS + 11)
        skillsDATS.put(TransitAbilityType.BLAST_MINING, result.getInt(OFFSET_DATS + 12));

        try {
            profile.hudType = TransitHudType.valueOf(result.getString(OFFSET_OTHER + 1));
        }
        catch (Exception e) {
            profile.hudType = TransitHudType.DEFAULT;
        }

        try {
            profile.mobHealthbarType = TransitMobHealthbarType.valueOf(result.getString(OFFSET_OTHER + 2));
        }
        catch (Exception e) {
            profile.mobHealthbarType = TransitMobHealthbarType.DEFAULT;
        }

        profile.skills = skills;
        profile.skillsXp = skillsXp;
        profile.skillsDATS = skillsDATS;

        profile.verify();

        return profile;
    }

    private void printErrors(SQLException ex) {
        plugin.getLogger().severe("SQLException: " + ex.getMessage());
        plugin.getLogger().severe("SQLState: " + ex.getSQLState());
        plugin.getLogger().severe("VendorError: " + ex.getErrorCode());
    }
}
