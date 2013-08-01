package me.riking.bungeemmo.bungee.transclude;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import me.riking.bungeemmo.bungee.BungeePlugin;
import me.riking.bungeemmo.common.data.LeaderboardRequest;
import me.riking.bungeemmo.common.data.TransitAbilityType;
import me.riking.bungeemmo.common.data.TransitHudType;
import me.riking.bungeemmo.common.data.TransitLeaderboardValue;
import me.riking.bungeemmo.common.data.TransitMobHealthbarType;
import me.riking.bungeemmo.common.data.TransitPlayerProfile;
import me.riking.bungeemmo.common.data.TransitPlayerRank;
import me.riking.bungeemmo.common.data.TransitSkillType;


public final class FlatfileDatabaseManager implements DatabaseManager {
    private final HashMap<TransitSkillType, List<TransitLeaderboardValue>> playerStatHash = new HashMap<TransitSkillType, List<TransitLeaderboardValue>>();
    private final List<TransitLeaderboardValue> powerLevels = new ArrayList<TransitLeaderboardValue>();
    private long lastUpdate = 0;

    private final BungeePlugin plugin;
    private final long UPDATE_WAIT_TIME = 600000L; // 10 minutes
    private final File usersFile;
    private static final Object fileWritingLock = new Object();

    public FlatfileDatabaseManager(BungeePlugin plugin, File file) {
        this.plugin = plugin;
        usersFile = file;
        checkStructure();
        updateLeaderboards();
    }

    public void purgePowerlessUsers() {
        int purgedUsers = 0;

        plugin.getLogger().info("Purging powerless users...");

        BufferedReader in = null;
        FileWriter out = null;

        // This code is O(n) instead of O(n²)
        synchronized (fileWritingLock) {
            try {
                in = new BufferedReader(new FileReader(usersFile));
                StringBuilder writer = new StringBuilder();
                String line = "";

                while ((line = in.readLine()) != null) {
                    String[] character = line.split(":");
                    Map<TransitSkillType, Integer> skills = getSkillMapFromLine(character);

                    boolean powerless = true;
                    for (int skill : skills.values()) {
                        if (skill != 0) {
                            powerless = false;
                            break;
                        }
                    }

                    // If they're still around, rewrite them to the file.
                    if (!powerless) {
                        writer.append(line).append("\r\n");
                    }
                    else {
                        purgedUsers++;
                        //Misc.profileCleanup(character[0]);
                    }
                }

                // Write the new file
                out = new FileWriter(usersFile);
                out.write(writer.toString());
            }
            catch (IOException e) {
                plugin.getLogger().severe("Exception while reading " + usersFile + " (Are you sure you formatted it correctly?)" + e.toString());
            }
            finally {
                tryClose(in);
                tryClose(out);
            }
        }

        plugin.getLogger().info("Purged " + purgedUsers + " users from the database.");
    }

    public void purgeOldUsers() {
        int removedPlayers = 0;
        long currentTime = System.currentTimeMillis();

        plugin.getLogger().info("Purging old users...");


        BufferedReader in = null;
        FileWriter out = null;

        // This code is O(n) instead of O(n²)
        synchronized (fileWritingLock) {
            try {
                in = new BufferedReader(new FileReader(usersFile));
                StringBuilder writer = new StringBuilder();
                String line = "";

                while ((line = in.readLine()) != null) {
                    // Length checks depend on last character being ':'
                    if (line.charAt(line.length() - 1) != ':') {
                        line = line + ":";
                    }
                    String[] character = line.split(":");
                    String name = character[0];
                    long lastPlayed;
                    boolean rewrite = false;
                    try {
                        lastPlayed = Long.parseLong(character[37]) * MILLIS_CONVERSION_FACTOR;
                    } catch (NumberFormatException e) {
                        rewrite = true;
                        lastPlayed = System.currentTimeMillis();
                    }

                    if (currentTime - lastPlayed > PURGE_TIME) {
                        removedPlayers++;
                    }
                    else {
                        if (rewrite) {
                            // Rewrite their data with a valid time
                            character[37] = Long.toString(lastPlayed);
                            String newLine = org.apache.commons.lang.StringUtils.join(character, ":");
                            writer.append(newLine).append("\r\n");
                        }
                        else {
                            writer.append(line).append("\r\n");
                        }
                    }
                }

                // Write the new file
                out = new FileWriter(usersFile);
                out.write(writer.toString());
            }
            catch (IOException e) {
                plugin.getLogger().severe("Exception while reading " + usersFile + " (Are you sure you formatted it correctly?)" + e.toString());
            }
            finally {
                tryClose(in);
                tryClose(out);
            }
        }

        plugin.getLogger().info("Purged " + removedPlayers + " users from the database.");
    }

    public boolean removeUser(String playerName) {
        boolean worked = false;

        BufferedReader in = null;
        FileWriter out = null;

        synchronized (fileWritingLock) {
            try {
                in = new BufferedReader(new FileReader(usersFile));
                StringBuilder writer = new StringBuilder();
                String line = "";

                while ((line = in.readLine()) != null) {
                    // Write out the same file but when we get to the player we want to remove, we skip his line.
                    if (!worked && line.split(":")[0].equalsIgnoreCase(playerName)) {
                        plugin.getLogger().info("User found, removing...");
                        worked = true;
                        continue; // Skip the player
                    }

                    writer.append(line).append("\r\n");
                }

                out = new FileWriter(usersFile); // Write out the new file
                out.write(writer.toString());
            }
            catch (Exception e) {
                plugin.getLogger().severe("Exception while reading " + usersFile + " (Are you sure you formatted it correctly?)" + e.toString());
            }
            finally {
                tryClose(in);
                tryClose(out);
            }
        }

        return worked;
    }

    public void saveUser(TransitPlayerProfile profile) {
        String playerName = profile.playerName;

        BufferedReader in = null;
        FileWriter out = null;

        synchronized (fileWritingLock) {
            try {
                // Open the file
                in = new BufferedReader(new FileReader(usersFile));
                StringBuilder writer = new StringBuilder();
                String line;

                // While not at the end of the file
                while ((line = in.readLine()) != null) {
                    // Read the line in and copy it to the output it's not the player we want to edit
                    if (!line.split(":")[0].equalsIgnoreCase(playerName)) {
                        writer.append(line).append("\r\n");
                    }
                    else {
                        // Otherwise write the new player information
                        writer.append(playerName).append(":");
                        writer.append(profile.skills.get(TransitSkillType.MINING)).append(":");
                        writer.append(":");
                        writer.append(":");
                        writer.append(profile.skillsXp.get(TransitSkillType.MINING)).append(":");
                        writer.append(profile.skills.get(TransitSkillType.WOODCUTTING)).append(":");
                        writer.append(profile.skillsXp.get(TransitSkillType.WOODCUTTING)).append(":");
                        writer.append(profile.skills.get(TransitSkillType.REPAIR)).append(":");
                        writer.append(profile.skills.get(TransitSkillType.UNARMED)).append(":");
                        writer.append(profile.skills.get(TransitSkillType.HERBALISM)).append(":");
                        writer.append(profile.skills.get(TransitSkillType.EXCAVATION)).append(":");
                        writer.append(profile.skills.get(TransitSkillType.ARCHERY)).append(":");
                        writer.append(profile.skills.get(TransitSkillType.SWORDS)).append(":");
                        writer.append(profile.skills.get(TransitSkillType.AXES)).append(":");
                        writer.append(profile.skills.get(TransitSkillType.ACROBATICS)).append(":");
                        writer.append(profile.skillsXp.get(TransitSkillType.REPAIR)).append(":");
                        writer.append(profile.skillsXp.get(TransitSkillType.UNARMED)).append(":");
                        writer.append(profile.skillsXp.get(TransitSkillType.HERBALISM)).append(":");
                        writer.append(profile.skillsXp.get(TransitSkillType.EXCAVATION)).append(":");
                        writer.append(profile.skillsXp.get(TransitSkillType.ARCHERY)).append(":");
                        writer.append(profile.skillsXp.get(TransitSkillType.SWORDS)).append(":");
                        writer.append(profile.skillsXp.get(TransitSkillType.AXES)).append(":");
                        writer.append(profile.skillsXp.get(TransitSkillType.ACROBATICS)).append(":");
                        writer.append(":");
                        writer.append(profile.skills.get(TransitSkillType.TAMING)).append(":");
                        writer.append(profile.skillsXp.get(TransitSkillType.TAMING)).append(":");
                        writer.append((int) profile.skillsDATS.get(TransitAbilityType.BERSERK)).append(":");
                        writer.append((int) profile.skillsDATS.get(TransitAbilityType.GIGA_DRILL_BREAKER)).append(":");
                        writer.append((int) profile.skillsDATS.get(TransitAbilityType.TREE_FELLER)).append(":");
                        writer.append((int) profile.skillsDATS.get(TransitAbilityType.GREEN_TERRA)).append(":");
                        writer.append((int) profile.skillsDATS.get(TransitAbilityType.SERRATED_STRIKES)).append(":");
                        writer.append((int) profile.skillsDATS.get(TransitAbilityType.SKULL_SPLITTER)).append(":");
                        writer.append((int) profile.skillsDATS.get(TransitAbilityType.SUPER_BREAKER)).append(":");
                        TransitHudType hudType = profile.hudType;
                        writer.append(hudType == null ? "NULL" : hudType.toString()).append(":");
                        writer.append(profile.skills.get(TransitSkillType.FISHING)).append(":");
                        writer.append(profile.skillsXp.get(TransitSkillType.FISHING)).append(":");
                        writer.append((int) profile.skillsDATS.get(TransitAbilityType.BLAST_MINING)).append(":");
                        writer.append(System.currentTimeMillis() / MILLIS_CONVERSION_FACTOR).append(":");
                        TransitMobHealthbarType mobHealthbarType = profile.mobHealthbarType;
                        writer.append(mobHealthbarType == null ? "NULL" : mobHealthbarType.toString()).append(":");
                        writer.append("\r\n");
                    }
                }

                // Write the new file
                out = new FileWriter(usersFile);
                out.write(writer.toString());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                tryClose(in);
                tryClose(out);
            }
        }
    }

    public List<TransitLeaderboardValue> readLeaderboard(LeaderboardRequest request) {
        updateLeaderboards();
        List<TransitLeaderboardValue> statsList = playerStatHash.get(request.skillType);
        int fromIndex = (Math.max(request.page, 1) - 1) * request.perPage;

        return statsList.subList(Math.min(fromIndex, statsList.size()), Math.min(fromIndex + request.perPage, statsList.size()));
    }

    public TransitPlayerRank readRank(String playerName) {
        updateLeaderboards();

        Map<TransitSkillType, Integer> skills = new HashMap<TransitSkillType, Integer>();

        for (TransitSkillType skill : TransitSkillType.values()) {
            skills.put(skill, getPlayerRank(playerName, playerStatHash.get(skill)));
        }

        skills.put(null, getPlayerRank(playerName, powerLevels));

        TransitPlayerRank ret = new TransitPlayerRank();
        ret.playerName = playerName;
        ret.rank = skills;
        return ret;
    }

    public void newUser(String playerName) {
        BufferedWriter out = null;
        synchronized (fileWritingLock) {
            try {
                // Open the file to write the player
                out = new BufferedWriter(new FileWriter(usersFile, true));

                // Add the player to the end
                out.append(playerName).append(":");
                out.append("0:"); // Mining
                out.append(":");
                out.append(":");
                out.append("0:"); // Xp
                out.append("0:"); // Woodcutting
                out.append("0:"); // WoodCuttingXp
                out.append("0:"); // Repair
                out.append("0:"); // Unarmed
                out.append("0:"); // Herbalism
                out.append("0:"); // Excavation
                out.append("0:"); // Archery
                out.append("0:"); // Swords
                out.append("0:"); // Axes
                out.append("0:"); // Acrobatics
                out.append("0:"); // RepairXp
                out.append("0:"); // UnarmedXp
                out.append("0:"); // HerbalismXp
                out.append("0:"); // ExcavationXp
                out.append("0:"); // ArcheryXp
                out.append("0:"); // SwordsXp
                out.append("0:"); // AxesXp
                out.append("0:"); // AcrobaticsXp
                out.append(":");
                out.append("0:"); // Taming
                out.append("0:"); // TamingXp
                out.append("0:"); // DATS
                out.append("0:"); // DATS
                out.append("0:"); // DATS
                out.append("0:"); // DATS
                out.append("0:"); // DATS
                out.append("0:"); // DATS
                out.append("0:"); // DATS
                out.append("STANDARD").append(":"); // HUD
                out.append("0:"); // Fishing
                out.append("0:"); // FishingXp
                out.append("0:"); // Blast Mining
                out.append(String.valueOf(System.currentTimeMillis() / MILLIS_CONVERSION_FACTOR)).append(":"); // LastLogin
                out.append("NULL").append(":"); // Mob Healthbar HUD

                // Add more in the same format as the line above

                out.newLine();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                tryClose(out);
            }
        }
    }

    public TransitPlayerProfile loadPlayerProfile(String playerName, boolean create) {
        BufferedReader in = null;

        synchronized (fileWritingLock) {
            try {
                // Open the user file
                in = new BufferedReader(new FileReader(usersFile));
                String line;

                while ((line = in.readLine()) != null) {
                    // Find if the line contains the player we want.
                    String[] character = line.split(":");

                    if (!character[0].equalsIgnoreCase(playerName)) {
                        continue;
                    }

                    TransitPlayerProfile p = loadFromLine(character);
                    in.close();
                    return p;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                tryClose(in);
            }
        }

        if (create) {
            newUser(playerName);
            TransitPlayerProfile profile = new TransitPlayerProfile();
            profile.playerName = playerName;
            return profile;
        }
        // Special value - does not exist
        return null;
    }

    public void convertUsers(DatabaseManager destination) {
        BufferedReader in = null;

        synchronized (fileWritingLock) {
            try {
                // Open the user file
                in = new BufferedReader(new FileReader(usersFile));
                String line;

                while ((line = in.readLine()) != null) {
                    String[] character = line.split(":");

                    try {
                        destination.saveUser(loadFromLine(character));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                tryClose(in);
            }
        }
    }

    public boolean checkConnected() {
        // Not implemented
        return true;
    }

    public List<String> getStoredUsers() {
        ArrayList<String> users = new ArrayList<String>();
        BufferedReader in = null;

        synchronized (fileWritingLock) {
            try {
                // Open the user file
                in = new BufferedReader(new FileReader(usersFile));
                String line;

                while ((line = in.readLine()) != null) {
                    String[] character = line.split(":");
                    users.add(character[0]);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                tryClose(in);
            }
        }
        return users;
    }

    /**
     * Update the leader boards.
     */
    private void updateLeaderboards() {
        // Only update FFS leaderboards every 10 minutes.. this puts a lot of strain on the server (depending on the size of the database) and should not be done frequently
        if (System.currentTimeMillis() < lastUpdate + UPDATE_WAIT_TIME) {
            return;
        }

        lastUpdate = System.currentTimeMillis(); // Log when the last update was run
        powerLevels.clear(); // Clear old values from the power levels

        // Initialize lists
        List<TransitLeaderboardValue> mining = new ArrayList<TransitLeaderboardValue>();
        List<TransitLeaderboardValue> woodcutting = new ArrayList<TransitLeaderboardValue>();
        List<TransitLeaderboardValue> herbalism = new ArrayList<TransitLeaderboardValue>();
        List<TransitLeaderboardValue> excavation = new ArrayList<TransitLeaderboardValue>();
        List<TransitLeaderboardValue> acrobatics = new ArrayList<TransitLeaderboardValue>();
        List<TransitLeaderboardValue> repair = new ArrayList<TransitLeaderboardValue>();
        List<TransitLeaderboardValue> swords = new ArrayList<TransitLeaderboardValue>();
        List<TransitLeaderboardValue> axes = new ArrayList<TransitLeaderboardValue>();
        List<TransitLeaderboardValue> archery = new ArrayList<TransitLeaderboardValue>();
        List<TransitLeaderboardValue> unarmed = new ArrayList<TransitLeaderboardValue>();
        List<TransitLeaderboardValue> taming = new ArrayList<TransitLeaderboardValue>();
        List<TransitLeaderboardValue> fishing = new ArrayList<TransitLeaderboardValue>();

        BufferedReader in = null;
        // Read from the FlatFile database and fill our arrays with information
        synchronized (fileWritingLock) {
            try {
                in = new BufferedReader(new FileReader(usersFile));
                String line = "";
                ArrayList<String> players = new ArrayList<String>();

                while ((line = in.readLine()) != null) {
                    String[] data = line.split(":");
                    String playerName = data[0];
                    int powerLevel = 0;

                    // Prevent the same player from being added multiple times (I'd like to note that this shouldn't happen...)
                    if (players.contains(playerName)) {
                        continue;
                    }

                    players.add(playerName);

                    Map<TransitSkillType, Integer> skills = getSkillMapFromLine(data);

                    powerLevel += putStat(acrobatics, playerName, skills.get(TransitSkillType.ACROBATICS));
                    powerLevel += putStat(archery, playerName, skills.get(TransitSkillType.ARCHERY));
                    powerLevel += putStat(axes, playerName, skills.get(TransitSkillType.AXES));
                    powerLevel += putStat(excavation, playerName, skills.get(TransitSkillType.EXCAVATION));
                    powerLevel += putStat(fishing, playerName, skills.get(TransitSkillType.FISHING));
                    powerLevel += putStat(herbalism, playerName, skills.get(TransitSkillType.HERBALISM));
                    powerLevel += putStat(mining, playerName, skills.get(TransitSkillType.MINING));
                    powerLevel += putStat(repair, playerName, skills.get(TransitSkillType.REPAIR));
                    powerLevel += putStat(swords, playerName, skills.get(TransitSkillType.SWORDS));
                    powerLevel += putStat(taming, playerName, skills.get(TransitSkillType.TAMING));
                    powerLevel += putStat(unarmed, playerName, skills.get(TransitSkillType.UNARMED));
                    powerLevel += putStat(woodcutting, playerName, skills.get(TransitSkillType.WOODCUTTING));

                    putStat(powerLevels, playerName, powerLevel);
                }
            }
            catch (Exception e) {
                plugin.getLogger().severe("Exception while reading " + usersFile + " (Are you sure you formatted it correctly?)" + e.toString());
            }
            finally {
                tryClose(in);
            }
        }

        SkillComparator c = new SkillComparator();

        Collections.sort(mining, c);
        Collections.sort(woodcutting, c);
        Collections.sort(repair, c);
        Collections.sort(unarmed, c);
        Collections.sort(herbalism, c);
        Collections.sort(excavation, c);
        Collections.sort(archery, c);
        Collections.sort(swords, c);
        Collections.sort(axes, c);
        Collections.sort(acrobatics, c);
        Collections.sort(taming, c);
        Collections.sort(fishing, c);
        Collections.sort(powerLevels, c);

        playerStatHash.put(TransitSkillType.MINING, mining);
        playerStatHash.put(TransitSkillType.WOODCUTTING, woodcutting);
        playerStatHash.put(TransitSkillType.REPAIR, repair);
        playerStatHash.put(TransitSkillType.UNARMED, unarmed);
        playerStatHash.put(TransitSkillType.HERBALISM, herbalism);
        playerStatHash.put(TransitSkillType.EXCAVATION, excavation);
        playerStatHash.put(TransitSkillType.ARCHERY, archery);
        playerStatHash.put(TransitSkillType.SWORDS, swords);
        playerStatHash.put(TransitSkillType.AXES, axes);
        playerStatHash.put(TransitSkillType.ACROBATICS, acrobatics);
        playerStatHash.put(TransitSkillType.TAMING, taming);
        playerStatHash.put(TransitSkillType.FISHING, fishing);
    }

    /**
     * Checks that the file is present and valid
     */
    private void checkStructure() {
        if (usersFile.exists()) {
            BufferedReader in = null;
            FileWriter out = null;

            synchronized (fileWritingLock) {
                try {
                    in = new BufferedReader(new FileReader(usersFile));
                    StringBuilder writer = new StringBuilder();
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        String[] character = line.split(":");

                        // If they're valid, rewrite them to the file.
                        if (character.length > 38) {
                            writer.append(line).append("\r\n");
                        } else if (character.length < 33) {
                            // Before Version 1.0 - Drop
                            plugin.getLogger().warning("Dropping malformed or before version 1.0 line from database - " + line);
                        } else {
                            String oldVersion = null;
                            StringBuilder newLine = new StringBuilder(line);
                            boolean announce = false;
                            if (character.length <= 33) {
                                // Introduction of HUDType
                                // Version 1.1.06
                                // commit 78f79213cdd7190cd11ae54526f3b4ea42078e8a
                                newLine.append("STANDARD").append(":");
                                oldVersion = "1.1.06";
                            }
                            if (character.length <= 35) {
                                // Introduction of Fishing
                                // Version 1.2.00
                                // commit a814b57311bc7734661109f0e77fc8bab3a0bd29
                                newLine.append(0).append(":");
                                newLine.append(0).append(":");
                                if (oldVersion == null) oldVersion = "1.2.00";
                            }
                            if (character.length <= 36) {
                                // Introduction of Blast Mining cooldowns
                                // Version 1.3.00-dev
                                // commit fadbaf429d6b4764b8f1ad0efaa524a090e82ef5
                                newLine.append((int) 0).append(":");
                                if (oldVersion == null) oldVersion = "1.3.00";
                            }
                            if (character.length <= 37) {
                                // Making old-purge work with flatfile
                                // Version 1.4.00-dev
                                // commmit 3f6c07ba6aaf44e388cc3b882cac3d8f51d0ac28
                                String playerName = character[0];
                                long time = System.currentTimeMillis();
                                newLine.append(time / MILLIS_CONVERSION_FACTOR).append(":");
                                announce = true; // TODO move this down
                                if (oldVersion == null) oldVersion = "1.4.00";
                            }
                            if (character.length <= 38) {
                                // Addition of mob healthbars
                                // Version 1.4.06
                                // commit da29185b7dc7e0d992754bba555576d48fa08aa6
                                newLine.append("NULL").append(":");
                                if (oldVersion == null) oldVersion = "1.4.06";
                            }
                            if (announce) {
                                plugin.getLogger().info("Updating database line for player " + character[0] + " from before version " + oldVersion);
                            }
                            writer.append(newLine).append("\r\n");
                        }
                    }

                    // Write the new file
                    out = new FileWriter(usersFile);
                    out.write(writer.toString());
                }
                catch (IOException e) {
                    plugin.getLogger().severe("Exception while reading " + usersFile + " (Are you sure you formatted it correctly?)" + e.toString());
                }
                finally {
                    tryClose(in);
                    tryClose(out);
                }
            }
            return;
        }

        usersFile.getParentFile().mkdir();

        try {
            plugin.getLogger().info("Creating mcmmo.users file...");
            usersFile.createNewFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tryClose(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Integer getPlayerRank(String playerName, List<TransitLeaderboardValue> statsList) {
        if (statsList == null) {
            return null;
        }

        int currentPos = 1;

        for (TransitLeaderboardValue stat : statsList) {
            if (stat.name.equalsIgnoreCase(playerName)) {
                return currentPos;
            }

            currentPos++;
        }

        return null;
    }

    private int putStat(List<TransitLeaderboardValue> statList, String playerName, int statValue) {
        statList.add(new TransitLeaderboardValue(playerName, statValue));
        return statValue;
    }

    private class SkillComparator implements Comparator<TransitLeaderboardValue> {
        @Override
        public int compare(TransitLeaderboardValue o1, TransitLeaderboardValue o2) {
            return (o2.val - o1.val);
        }
    }

    private TransitPlayerProfile loadFromLine(String[] character) throws Exception {
        TransitPlayerProfile profile = new TransitPlayerProfile();
        Map<TransitSkillType, Integer>   skills     = getSkillMapFromLine(character);      // Skill levels
        Map<TransitSkillType, Float>     skillsXp   = new HashMap<TransitSkillType, Float>();     // Skill & XP
        Map<TransitAbilityType, Integer> skillsDATS = new HashMap<TransitAbilityType, Integer>(); // Ability & Cooldown

        // TODO on updates, put new values in a try{} ?

        skillsXp.put(TransitSkillType.TAMING, (float) Integer.valueOf(character[25]));
        skillsXp.put(TransitSkillType.MINING, (float) Integer.valueOf(character[4]));
        skillsXp.put(TransitSkillType.REPAIR, (float) Integer.valueOf(character[15]));
        skillsXp.put(TransitSkillType.WOODCUTTING, (float) Integer.valueOf(character[6]));
        skillsXp.put(TransitSkillType.UNARMED, (float) Integer.valueOf(character[16]));
        skillsXp.put(TransitSkillType.HERBALISM, (float) Integer.valueOf(character[17]));
        skillsXp.put(TransitSkillType.EXCAVATION, (float) Integer.valueOf(character[18]));
        skillsXp.put(TransitSkillType.ARCHERY, (float) Integer.valueOf(character[19]));
        skillsXp.put(TransitSkillType.SWORDS, (float) Integer.valueOf(character[20]));
        skillsXp.put(TransitSkillType.AXES, (float) Integer.valueOf(character[21]));
        skillsXp.put(TransitSkillType.ACROBATICS, (float) Integer.valueOf(character[22]));
        skillsXp.put(TransitSkillType.FISHING, (float) Integer.valueOf(character[35]));

        // Taming - Unused
        skillsDATS.put(TransitAbilityType.SUPER_BREAKER, Integer.valueOf(character[32]));
        // Repair - Unused
        skillsDATS.put(TransitAbilityType.TREE_FELLER, Integer.valueOf(character[28]));
        skillsDATS.put(TransitAbilityType.BERSERK, Integer.valueOf(character[26]));
        skillsDATS.put(TransitAbilityType.GREEN_TERRA, Integer.valueOf(character[29]));
        skillsDATS.put(TransitAbilityType.GIGA_DRILL_BREAKER, Integer.valueOf(character[27]));
        // Archery - Unused
        skillsDATS.put(TransitAbilityType.SERRATED_STRIKES, Integer.valueOf(character[30]));
        skillsDATS.put(TransitAbilityType.SKULL_SPLITTER, Integer.valueOf(character[31]));
        // Acrobatics - Unused
        skillsDATS.put(TransitAbilityType.BLAST_MINING, Integer.valueOf(character[36]));

        try {
            profile.hudType = TransitHudType.valueOf(character[33]);
        }
        catch (Exception e) {
            profile.hudType = TransitHudType.NULL; // Shouldn't happen unless database is being tampered with
        }

        try {
            profile.mobHealthbarType = TransitMobHealthbarType.valueOf(character[38]);
        }
        catch (Exception e) {
            profile.mobHealthbarType = TransitMobHealthbarType.NULL;
        }
        profile.playerName = character[0];
        profile.skills = skills;
        profile.skillsXp = skillsXp;
        profile.skillsDATS = skillsDATS;

        return profile;
    }

    private Map<TransitSkillType, Integer> getSkillMapFromLine(String[] character) {
        Map<TransitSkillType, Integer> skills = new HashMap<TransitSkillType, Integer>();   // Skill & Level

        skills.put(TransitSkillType.TAMING, Integer.valueOf(character[24]));
        skills.put(TransitSkillType.MINING, Integer.valueOf(character[1]));
        skills.put(TransitSkillType.REPAIR, Integer.valueOf(character[7]));
        skills.put(TransitSkillType.WOODCUTTING, Integer.valueOf(character[5]));
        skills.put(TransitSkillType.UNARMED, Integer.valueOf(character[8]));
        skills.put(TransitSkillType.HERBALISM, Integer.valueOf(character[9]));
        skills.put(TransitSkillType.EXCAVATION, Integer.valueOf(character[10]));
        skills.put(TransitSkillType.ARCHERY, Integer.valueOf(character[11]));
        skills.put(TransitSkillType.SWORDS, Integer.valueOf(character[12]));
        skills.put(TransitSkillType.AXES, Integer.valueOf(character[13]));
        skills.put(TransitSkillType.ACROBATICS, Integer.valueOf(character[14]));
        skills.put(TransitSkillType.FISHING, Integer.valueOf(character[34]));

        return skills;
    }
}
