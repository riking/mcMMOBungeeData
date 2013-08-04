package me.riking.bungeemmo.bungee.transclude;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import me.riking.bungeemmo.bungee.BungeePlugin;
import me.riking.bungeemmo.bungee.Config;
import me.riking.bungeemmo.common.data.TransitSkillType;

public final class SQLStatements {
    public static String tablePrefix;

    private boolean closed;
    // Prepared statements
    public final Object LOCK_PURGE = new Object();
    public final PreparedStatement purgePowerless;
    public final PreparedStatement purgeOld;
    public final Object LOCK_REMOVEUSER = new Object();
    public final PreparedStatement removeUser;
    public final Object LOCK_READID = new Object();
    public final PreparedStatement readUserId;
    public final Object LOCK_SAVE = new Object();
    public final PreparedStatement saveCooldowns;
    public final PreparedStatement saveSkillLevels;
    public final PreparedStatement saveSkillXps;
    public final PreparedStatement saveHuds;
    public final PreparedStatement saveLoginTime;
    public final Object LOCK_LOADUSER = new Object();
    public final PreparedStatement newUser;
    public final PreparedStatement loadUser;
    // No parameters, no lock is necessary
    public final PreparedStatement getAllUsers;
    public final Object LOCK_MISSINGROW = new Object();
    public final PreparedStatement missingRowHuds;
    public final PreparedStatement missingRowCooldowns;
    public final PreparedStatement missingRowSkills;
    public final PreparedStatement missingRowExperience;
    public final Object LOCK_LEADERBOARD = new Object();
    public final Map<TransitSkillType, PreparedStatement> readLeaderboardMap;
    public final Object LOCK_RANK = new Object();
    public final Map<TransitSkillType, PreparedStatement> countHigherSkillMap;
    public final Map<TransitSkillType, PreparedStatement> orderSameSkillMap;

    // Raw SQL statements
    public static String SQLCreateTableUsers;
    public static String SQLCreateTableHuds;
    public static String SQLCreateTableCooldowns;
    public static String SQLCreateTableSkills;
    public static String SQLCreateTableExperience;

    public static String SQLKillOrphansHuds;
    public static String SQLKillOrphansCooldowns;
    public static String SQLKillOrphansSkills;
    public static String SQLKillOrphansExperience;

    public static String SQLCheckBlastMining;
    public static String SQLFixBlastMining;
    public static String SQLCheckFishing;
    public static String SQLFixFishing1;
    public static String SQLFixFishing2;
    public static String SQLCheckHealthbars;
    public static String SQLFixHealthbars;
    public static String SQLCheckIndex;
    public static String SQLFixIndex;
    public static String SQLFixParties;

    private BungeePlugin plugin;

    public static void setupRaw(String prefix) {
        tablePrefix = prefix;
        SQLCreateTableUsers = "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "users` ("
                + "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                + "`user` varchar(40) NOT NULL,"
                + "`lastlogin` int(32) unsigned NOT NULL,"
                + "PRIMARY KEY (`id`),"
                + "UNIQUE KEY `user` (`user`)) DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;";
        SQLCreateTableHuds = "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "huds` ("
                + "`user_id` int(10) unsigned NOT NULL,"
                + "`hudtype` varchar(50) NOT NULL DEFAULT 'DEFAULT',"
                + "`mobhealthbar` varchar(50) NOT NULL DEFAULT 'DEFAULT',"
                + "PRIMARY KEY (`user_id`)) "
                + "DEFAULT CHARSET=latin1;";
        SQLCreateTableCooldowns = "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "cooldowns` ("
                + "`user_id` int(10) unsigned NOT NULL,"
                + "`taming` int(32) unsigned NOT NULL DEFAULT '0',"
                + "`mining` int(32) unsigned NOT NULL DEFAULT '0',"
                + "`woodcutting` int(32) unsigned NOT NULL DEFAULT '0',"
                + "`repair` int(32) unsigned NOT NULL DEFAULT '0',"
                + "`unarmed` int(32) unsigned NOT NULL DEFAULT '0',"
                + "`herbalism` int(32) unsigned NOT NULL DEFAULT '0',"
                + "`excavation` int(32) unsigned NOT NULL DEFAULT '0',"
                + "`archery` int(32) unsigned NOT NULL DEFAULT '0',"
                + "`swords` int(32) unsigned NOT NULL DEFAULT '0',"
                + "`axes` int(32) unsigned NOT NULL DEFAULT '0',"
                + "`acrobatics` int(32) unsigned NOT NULL DEFAULT '0',"
                + "`blast_mining` int(32) unsigned NOT NULL DEFAULT '0',"
                + "PRIMARY KEY (`user_id`)) "
                + "DEFAULT CHARSET=latin1;";
        SQLCreateTableSkills = "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "skills` ("
                + "`user_id` int(10) unsigned NOT NULL,"
                + "`taming` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`mining` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`woodcutting` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`repair` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`unarmed` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`herbalism` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`excavation` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`archery` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`swords` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`axes` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`acrobatics` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`fishing` int(10) unsigned NOT NULL DEFAULT '0',"
                + "PRIMARY KEY (`user_id`)) "
                + "DEFAULT CHARSET=latin1;";
        SQLCreateTableExperience = "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "experience` ("
                + "`user_id` int(10) unsigned NOT NULL,"
                + "`taming` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`mining` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`woodcutting` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`repair` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`unarmed` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`herbalism` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`excavation` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`archery` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`swords` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`axes` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`acrobatics` int(10) unsigned NOT NULL DEFAULT '0',"
                + "`fishing` int(10) unsigned NOT NULL DEFAULT '0',"
                + "PRIMARY KEY (`user_id`)) "
                + "DEFAULT CHARSET=latin1;";

        SQLKillOrphansHuds = "DELETE FROM " + tablePrefix + "huds " +
                "WHERE NOT EXISTS (SELECT * FROM " +
                tablePrefix + "users u WHERE " +
                tablePrefix + "huds.user_id = u.id);";
        SQLKillOrphansCooldowns = "DELETE FROM " + tablePrefix + "cooldowns " +
                "WHERE NOT EXISTS (SELECT * FROM " +
                tablePrefix + "users u WHERE " +
                tablePrefix + "cooldowns.user_id = u.id);";
        SQLKillOrphansSkills = "DELETE FROM " + tablePrefix + "skills " +
                "WHERE NOT EXISTS (SELECT * FROM " +
                tablePrefix + "users u WHERE " +
                tablePrefix + "skills.user_id = u.id);";
        SQLKillOrphansExperience = "DELETE FROM " + tablePrefix + "experience " +
                "WHERE NOT EXISTS (SELECT * FROM " +
                tablePrefix + "users u WHERE " +
                tablePrefix + "experience.user_id = u.id);";

        SQLCheckBlastMining = "SELECT * FROM `" + tablePrefix + "cooldowns` ORDER BY `" + tablePrefix + "cooldowns`.`blast_mining` ASC LIMIT 0 , 30;";
        SQLFixBlastMining = "ALTER TABLE `"+tablePrefix + "cooldowns` ADD `blast_mining` int(32) NOT NULL DEFAULT '0' ;";
        SQLCheckFishing = "SELECT * FROM `" + tablePrefix + "experience` ORDER BY `" + tablePrefix + "experience`.`fishing` ASC LIMIT 0 , 30;";
        SQLFixFishing1 = "ALTER TABLE `"+tablePrefix + "skills` ADD `fishing` int(10) NOT NULL DEFAULT '0' ;";
        SQLFixFishing2 = "ALTER TABLE `"+tablePrefix + "experience` ADD `fishing` int(10) NOT NULL DEFAULT '0' ;";
        SQLCheckHealthbars = "SELECT * FROM `" + tablePrefix + "huds` ORDER BY `" + tablePrefix + "huds`.`mobhealthbar` ASC LIMIT 0 , 30;";
        SQLFixHealthbars = "ALTER TABLE `" + tablePrefix + "huds` ADD `mobhealthbar` varchar(50) NOT NULL DEFAULT 'DEFAULT' ;";
        SQLCheckIndex = "SHOW INDEX FROM " + tablePrefix + "skills;";
        SQLFixIndex = "ALTER TABLE `" + tablePrefix + "skills` ADD INDEX `idx_taming` (`taming`) USING BTREE, "
                + "ADD INDEX `idx_mining` (`mining`) USING BTREE, "
                + "ADD INDEX `idx_woodcutting` (`woodcutting`) USING BTREE, "
                + "ADD INDEX `idx_repair` (`repair`) USING BTREE, "
                + "ADD INDEX `idx_unarmed` (`unarmed`) USING BTREE, "
                + "ADD INDEX `idx_herbalism` (`herbalism`) USING BTREE, "
                + "ADD INDEX `idx_excavation` (`excavation`) USING BTREE, "
                + "ADD INDEX `idx_archery` (`archery`) USING BTREE, "
                + "ADD INDEX `idx_swords` (`swords`) USING BTREE, "
                + "ADD INDEX `idx_axes` (`axes`) USING BTREE, "
                + "ADD INDEX `idx_acrobatics` (`acrobatics`) USING BTREE, "
                + "ADD INDEX `idx_fishing` (`fishing`) USING BTREE;";
        SQLFixParties = "ALTER TABLE `" + tablePrefix + "users` DROP COLUMN `party` ;";
    }

    public SQLStatements(BungeePlugin plugin, Connection conn) {
        this.plugin = plugin;
        closed = false;
        if (conn == null) {
            closed = true;
            purgePowerless = null;
            purgeOld = null;
            removeUser = null;
            readUserId = null;
            saveCooldowns = null;
            saveSkillLevels = null;
            saveSkillXps = null;
            saveHuds = null;
            saveLoginTime = null;
            newUser = null;
            loadUser = null;
            getAllUsers = null;
            missingRowHuds = null;
            missingRowCooldowns = null;
            missingRowSkills = null;
            missingRowExperience = null;
            readLeaderboardMap = null;
            countHigherSkillMap = null;
            orderSameSkillMap = null;
            return;
        }
        purgePowerless = tryPrepare(conn, "DELETE FROM u, e, h, s, c USING " + tablePrefix + "users u " +
            "JOIN " + tablePrefix + "experience e ON (u.id = e.user_id) " +
            "JOIN " + tablePrefix + "huds h ON (u.id = h.user_id) " +
            "JOIN " + tablePrefix + "skills s ON (u.id = s.user_id) " +
            "JOIN " + tablePrefix + "cooldowns c ON (u.id = c.user_id) " +
            "WHERE (s.taming+s.mining+s.woodcutting+s.repair+s.unarmed+s.herbalism+s.excavation+s.archery+s.swords+s.axes+s.acrobatics+s.fishing) = 0");
        purgeOld = tryPrepare(conn, "DELETE FROM u, e, h, s, c USING " + tablePrefix + "users u " +
            "JOIN " + tablePrefix + "experience e ON (u.id = e.user_id) " +
            "JOIN " + tablePrefix + "huds h ON (u.id = h.user_id) " +
            "JOIN " + tablePrefix + "skills s ON (u.id = s.user_id) " +
            "JOIN " + tablePrefix + "cooldowns c ON (u.id = c.user_id) " +
            "WHERE ((? - lastlogin * 1000) > ?)");
        loadUser = tryPrepare(conn, "SELECT "
                + "s.taming, s.mining, s.repair, s.woodcutting, s.unarmed, s.herbalism, s.excavation, s.archery, s.swords, s.axes, s.acrobatics, s.fishing, "
                + "e.taming, e.mining, e.repair, e.woodcutting, e.unarmed, e.herbalism, e.excavation, e.archery, e.swords, e.axes, e.acrobatics, e.fishing, "
                + "c.taming, c.mining, c.repair, c.woodcutting, c.unarmed, c.herbalism, c.excavation, c.archery, c.swords, c.axes, c.acrobatics, c.blast_mining, "
                + "h.hudtype, h.mobhealthbar "
                + "FROM " + tablePrefix + "users u "
                + "JOIN " + tablePrefix + "skills s ON (u.id = s.user_id) "
                + "JOIN " + tablePrefix + "experience e ON (u.id = e.user_id) "
                + "JOIN " + tablePrefix + "cooldowns c ON (u.id = c.user_id) "
                + "JOIN " + tablePrefix + "huds h ON (u.id = h.user_id) "
                + "WHERE u.user = ?");
        newUser = tryPrepareNewUser(conn, "INSERT INTO " + tablePrefix + "users (user, lastlogin) VALUES (?, ?)");
        removeUser = tryPrepare(conn, "DELETE FROM u, e, h, s, c " +
            "USING " + tablePrefix + "users u " +
            "JOIN " + tablePrefix + "experience e ON (u.id = e.user_id) " +
            "JOIN " + tablePrefix + "huds h ON (u.id = h.user_id) " +
            "JOIN " + tablePrefix + "skills s ON (u.id = s.user_id) " +
            "JOIN " + tablePrefix + "cooldowns c ON (u.id = c.user_id) " +
            "WHERE u.user = '?'");
        getAllUsers = tryPrepare(conn, "SELECT user FROM " + tablePrefix + "users");
        saveCooldowns = tryPrepare(conn, "UPDATE " + tablePrefix + "cooldowns SET "
                + "  mining = ?, woodcutting = ?, unarmed = ?"
                + ", herbalism = ?, excavation = ?, swords = ?"
                + ", axes = ?, blast_mining = ? WHERE user_id = ?");
        saveSkillLevels = tryPrepare(conn, "UPDATE " + tablePrefix + "skills SET "
                + " taming = ?, mining = ?, repair = ?, woodcutting = ?"
                + ", unarmed = ?, herbalism = ?, excavation = ?"
                + ", archery = ?, swords = ?, axes = ?, acrobatics = ?"
                + ", fishing = ? WHERE user_id = ?");
        saveSkillXps = tryPrepare(conn, "UPDATE " + tablePrefix + "experience SET "
                + " taming = ?, mining = ?, repair = ?, woodcutting = ?"
                + ", unarmed = ?, herbalism = ?, excavation = ?"
                + ", archery = ?, swords = ?, axes = ?, acrobatics = ?"
                + ", fishing = ? WHERE user_id = ?");
        saveLoginTime = tryPrepare(conn, "UPDATE " + tablePrefix + "users SET lastlogin = ? WHERE id = ?");
        saveHuds = tryPrepare(conn, "UPDATE " + tablePrefix + "huds SET hudtype = ?, mobhealthbar = ? WHERE user_id = ?");
        readUserId = tryPrepare(conn, "SELECT id FROM " + tablePrefix + "users WHERE user = ?");
        missingRowHuds = tryPrepare(conn, "INSERT IGNORE INTO " + tablePrefix + "huds (user_id, mobhealthbar) VALUES (? ,'DEFAULT')");
        missingRowCooldowns = tryPrepare(conn, "INSERT IGNORE INTO " + tablePrefix + "cooldowns (user_id) VALUES (?)");
        missingRowSkills = tryPrepare(conn, "INSERT IGNORE INTO " + tablePrefix + "skills (user_id) VALUES (?)");
        missingRowExperience = tryPrepare(conn, "INSERT IGNORE INTO " + tablePrefix + "experience (user_id) VALUES (?)");

        readLeaderboardMap = new HashMap<>();
        countHigherSkillMap = new HashMap<>();
        orderSameSkillMap = new HashMap<>();
        String skill;
        for (TransitSkillType type : TransitSkillType.values()) {
            skill = type.toString().toLowerCase();
            readLeaderboardMap.put(type, tryPrepare(conn, format("SELECT %2$s, user, NOW() FROM %1$susers JOIN %1$sskills ON (user_id = id) WHERE %2$s > 0 ORDER BY %2$s DESC, user LIMIT ?, ?", skill)));
            countHigherSkillMap.put(type, tryPrepare(conn, format("SELECT COUNT(*) AS rank FROM %1$susers JOIN %1$sskills ON user_id = id WHERE %2$s > 0 " +
                                 "AND %2$s > (SELECT %2$s FROM %1$susers JOIN %1$sskills ON user_id = id " +
                                 "WHERE user = ?)", skill)));
            orderSameSkillMap.put(type, tryPrepare(conn, format("SELECT user, %2$s FROM %1$susers JOIN %1$sskills ON user_id = id WHERE %2$s > 0 " +
                    "AND %2$s = (SELECT %2$s FROM %1$susers JOIN %1$sskills ON user_id = id " +
                    "WHERE user = ?) ORDER BY user", skill)));
        }
        /* do once */ {
            TransitSkillType type = null;
            skill = "taming+mining+woodcutting+repair+unarmed+herbalism+excavation+archery+swords+axes+acrobatics+fishing";
            readLeaderboardMap.put(type, tryPrepare(conn, format("SELECT %2$s, user, NOW() FROM %1$susers JOIN %1$sskills ON (user_id = id) WHERE %2$s > 0 ORDER BY %2$s DESC, user LIMIT ?, ?", skill)));
            countHigherSkillMap.put(type, tryPrepare(conn, format("SELECT COUNT(*) AS rank FROM %1$susers JOIN %1$sskills ON user_id = id WHERE %2$s > 0 " +
                    "AND %2$s > (SELECT %2$s FROM %1$susers JOIN %1$sskills ON user_id = id " +
                    "WHERE user = ?)", skill)));
            orderSameSkillMap.put(type, tryPrepare(conn, format("SELECT user, %2$s FROM %1$susers JOIN %1$sskills ON user_id = id WHERE %2$s > 0 " +
                    "AND %2$s = (SELECT %2$s FROM %1$susers JOIN %1$sskills ON user_id = id " +
                    "WHERE user = ?) ORDER BY user", skill)));
        }
    }

    private PreparedStatement tryPrepare(Connection connection, String sql) {
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            printErrors(e);
        }
        return null;
    }

    private PreparedStatement tryPrepareNewUser(Connection connection, String sql) {
        try {
            return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            printErrors(e);
        }
        return null;
    }

    /**
     * Puts the table prefix into %1$s and arg1 into %2%s
     */
    private static String format(String sql, Object arg1) {
        return String.format(sql, tablePrefix, arg1);
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() throws SQLException {
        closed = true;
        purgePowerless.close();
        purgeOld.close();
        removeUser.close();
        readUserId.close();
        saveCooldowns.close();
        saveSkillLevels.close();
        saveSkillXps.close();
        saveHuds.close();
        saveLoginTime.close();
        newUser.close();
        loadUser.close();
        getAllUsers.close();
        missingRowHuds.close();
        missingRowCooldowns.close();
        missingRowExperience.close();
        for (PreparedStatement ps : readLeaderboardMap.values()) {
            ps.close();
        }
        for (PreparedStatement ps : countHigherSkillMap.values()) {
            ps.close();
        }
        for (PreparedStatement ps : orderSameSkillMap.values()) {
            ps.close();
        }
    }

    private void printErrors(SQLException ex) {
        plugin.getLogger().severe("SQLException: " + ex.getMessage());
        plugin.getLogger().severe("SQLState: " + ex.getSQLState());
        plugin.getLogger().severe("VendorError: " + ex.getErrorCode());
    }
}
