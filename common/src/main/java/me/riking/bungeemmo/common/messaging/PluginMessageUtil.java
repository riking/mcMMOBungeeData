package me.riking.bungeemmo.common.messaging;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import me.riking.bungeemmo.common.data.TransitLeaderboardValue;
import me.riking.bungeemmo.common.data.TransitPlayerProfile;
import me.riking.bungeemmo.common.data.TransitPlayerRank;
import me.riking.bungeemmo.common.data.TransitSkillType;

/**
 * Constants and a read() method for Message classes.
 */
public class PluginMessageUtil {
    public static final String MCMMO_CHANNEL_NAME = "mcMMOdatabase";
    public static final String BUNGEE_CHANNEL_NAME = "BungeeCord";

    /**
     * The queue of pending outgoing messages. Messages are NOT automatically
     * inserted into or removed into the queue, that happens in the
     * plugin-space.
     */
    public static ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<Message>();

    /**
     * A server tells the proxy that it has started up and is running mcMMO.
     * The proxy must respond with either a WELCOME_SUBCHANNEL message or a
     * BAD_VERSION_SUBCHANNEL message.
     * <p>
     * Due to technical limitations, this message can only be sent once a
     * player joins the server.
     * <p>
     * This is a Server -> Proxy subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param Long Version UID from TransitPlayerProfile
     * @param UTF Pretty-printed version name
     */
    public static final String STARTUP_SUBCHANNEL = "AS"; // announce.Start

    /**
     * A proxy welcomes the server into the network, giving it a list of all
     * other connected mcMMO servers.
     * <p>
     * This is a Proxy -> Server subchannel.
     * <p>
     * The sending of this message should be coupled with the sending of an
     * ANNOUNCE_SUBCHANNEL message to all the other connected mcMMO servers.
     * <p>
     * In the rare case that the list of other connected servers exceeds the
     * maximum length of a PluginMessage, this message should be followed by
     * an ANNOUNCE_SUBCHANNEL message for each additional server not yet sent.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF Name of this server (which should be stored in this class).
     * @param ArrayList<String> Names of other connected servers.
     */
    public static final String WELCOME_SUBCHANNEL = "AK"; // announce.acKnowledge

    /**
     * The proxy tells a server that its version given in the
     * STARTUP_SUBCHANNEL message is mismatched with its own. The server
     * should respond to this by stopping to avoid data corruption.
     * (Incidentally, this demonstrates why messages from clients on the
     * channel must be blocked.)
     * <p>
     * This is a Proxy -> Server subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF BAD_VERSION_SUBCHANNEL
     * @param Long Correct versionUid
     * @param UTF Correct pretty-print version
     */
    public static final String BAD_VERSION_SUBCHANNEL = "AB"; // announce.Bad

    /**
     * The proxy tells a server that another mcMMO servers has gone online on
     * the proxy.
     * <p>
     * This is a Proxy -> Server subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF Server name
     */
    public static final String ANNOUNCE_SUBCHANNEL = "AN"; // announce.New

    /**
     * The server tells the proxy that it is shutting down.
     * <p>
     * This is a Server -> Proxy subchannel.
     * <p>
     * There is no data for this subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     */
    public static final String SERVER_STOP_SUBCHANNEL = "AQ"; // announce.Quit

    /**
     * A server requests a PlayerProfile from the proxy. The proxy must
     * respond with a PUSH_PROFILE_SUBCHANNEL message.
     * <p>
     * This is a Server <-> Proxy subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF PULL_PROFILE_SUBCHANNEL
     * @param UTF Player Name
     */
    public static final String PULL_PROFILE_SUBCHANNEL = "PG"; // proxy.Get

    /**
     * A server puts a PlayerProfile onto the proxy. No response should be
     * given.
     * <p>
     * This is a Server <-> Proxy subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF PUSH_PROFILE_SUBCHANNEL
     * @param UTF Player name
     * @param Object {@link TransitPlayerProfile}
     */
    public static final String PUSH_PROFILE_SUBCHANNEL = "PP"; // proxy.Put

    /**
     * The server requests the leaderboard from the proxy.
     * <p>
     * This is a Server -> Proxy subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF PULL_LEADERBOARD_SUBCHANNEL
     * @param TransitSkillType skill or null
     * @param Integer page number
     * @param Integer number per page
     */
    public static final String PULL_LEADERBOARD_SUBCHANNEL = "LG"; // leaderboard.Get

    /**
     * The proxy delivers the leaderboard to the server.
     * <p>
     * This is a Proxy -> Server subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF PUSH_LEADERBOARD_SUBCHANNEL
     * @param TransitSkillType skill or null
     * @param Integer page number
     * @param Integer number per page
     * @param Object ArrayList of TransitLeaderboardValue
     */
    public static final String PUSH_LEADERBOARD_SUBCHANNEL = "LP"; // leaderboard.Put

    /**
     * The server requests a player's rank data from the proxy.
     * <p>
     * This is a Server -> Proxy subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF PULL_RANK_SUBCHANNEL
     * @param UTF Player name
     */
    public static final String PULL_RANK_SUBCHANNEL = "RG"; // rank.Get

    /**
     * The proxy delivers the player's rank data to the server.
     * <p>
     * This is a Proxy -> Server subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF PUSH_RANK_SUBCHANNEL
     * @param UTF Player name
     * @param Object TransitPlayerRank object
     */
    public static final String PUSH_RANK_SUBCHANNEL = "RP"; // rank.Put

    /**
     * A pretty version string to print on mismatches
     */
    public static final String prettyVersion;

    /**
     * This must be filled by the plugin on startup.
     */
    public static String serverName;

    static {
        // Read version
        String result = null;
        try {
            InputStream ins = PluginMessageUtil.class.getResourceAsStream("version.props");
            BufferedReader read = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
            result = read.readLine();
            read.close();
            ins.close();
        } catch (Exception e) {
            if (result == null) {
                System.err.println("[mcMMOBungeeData] Error - could not read version file?");
                e.printStackTrace();
                // Give backup text
                result = "(Error reading version string. VID: " + TransitPlayerProfile.getVersion() + ")";
            } else {
                // Ignore, it was just a problem with closing
            }
        }
        prettyVersion = result;
    }

    public static byte[] writeMessage(Message m) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        m.write(out);
        // org.bukkit.plugin.messaging.Messenger.MAX_MESSAGE_SIZE
        if (out.size() > 32766) {
            throw new IOException("Message is too large! Cannot send!");
        }
        return out.toByteArray();
    }

    public static Message readIncomingMessage(byte[] data) throws IOException {
        try {
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
            ObjectInputStream in = new ObjectInputStream(bytesIn);
            String subchannel = in.readUTF();
            if (subchannel.equals(STARTUP_SUBCHANNEL)) {
                long senderVid = in.readLong();
                String senderVer = in.readUTF();
                in.close();
                return new StartupMessage(senderVid, senderVer);

            } else if (subchannel.equals(WELCOME_SUBCHANNEL)) {
                String serverName = in.readUTF();
                ArrayList<String> serverList = tryReadStringList(in);
                in.close();
                return new WelcomeMessage(serverName, serverList);

            } else if (subchannel.equals(BAD_VERSION_SUBCHANNEL)) {
                long correctVid = in.readLong();
                String correctVer = in.readUTF();
                in.close();
                return new BadVersionMessage(correctVid, correctVer);

            } else if (subchannel.equals(ANNOUNCE_SUBCHANNEL)) {
                String newServerName = in.readUTF();
                boolean stopping = in.readBoolean();
                in.close();
                return new AnnounceMessage(newServerName, stopping);

            } else if (subchannel.equals(SERVER_STOP_SUBCHANNEL)) {
                in.close();
                return new ServerStopMessage();

            } else if (subchannel.equals(PULL_PROFILE_SUBCHANNEL)) {
                String playerName = in.readUTF();
                boolean create = in.readBoolean();
                in.close();
                return new ProfilePullMessage(playerName, create);

            } else if (subchannel.equals(PUSH_PROFILE_SUBCHANNEL)) {
                String playerName = in.readUTF();
                TransitPlayerProfile profile = tryReadPlayerProfile(in);
                in.close();
                return new ProfilePushMessage(playerName, profile);

            } else if (subchannel.equals(PULL_LEADERBOARD_SUBCHANNEL)) {
                TransitSkillType skill = tryReadSkillType(in);
                int page = in.readInt();
                int perPage = in.readInt();
                in.close();
                return new LeaderboardPullMessage(skill, page, perPage);

            } else if (subchannel.equals(PUSH_LEADERBOARD_SUBCHANNEL)) {
                TransitSkillType skill = tryReadSkillType(in);
                int page = in.readInt();
                int perPage = in.readInt();
                ArrayList<TransitLeaderboardValue> values = tryReadLeaderboardList(in);
                in.close();
                return new LeaderboardPushMessage(skill, page, perPage, values);

            } else if (subchannel.equals(PULL_RANK_SUBCHANNEL)) {
                String name = in.readUTF();
                in.close();
                return new RankPullMessage(name);

            } else if (subchannel.equals(PUSH_RANK_SUBCHANNEL)) {
                String name = in.readUTF();
                TransitPlayerRank rank = tryReadPlayerRank(in);
                in.close();
                return new RankPushMessage(name, rank);

            } else {
                throw new MalformedMessageException("Unknown message type '" + subchannel + "'. Is McMMOBungeeData out of date?");
            }
        } catch (Exception e) {
            throw new MalformedMessageException(e);
        }
    }

    private static TransitPlayerProfile tryReadPlayerProfile(ObjectInputStream in) throws MalformedMessageException {
        TransitPlayerProfile profile;
        try {
            profile = (TransitPlayerProfile) in.readObject();
        } catch (Exception e) {
            throw new MalformedMessageException("Expected a TransitPlayerProfile", e);
        }
        return profile;
    }

    private static TransitPlayerRank tryReadPlayerRank(ObjectInputStream in) throws MalformedMessageException {
        TransitPlayerRank rank;
        try {
            rank = (TransitPlayerRank) in.readObject();
        } catch (Exception e) {
            throw new MalformedMessageException("Expected a TransitPlayerRank", e);
        }
        return rank;
    }

    private static TransitSkillType tryReadSkillType(ObjectInputStream in) throws MalformedMessageException {
        TransitSkillType skill;
        try {
            skill = (TransitSkillType) in.readObject();
        } catch (Exception e) {
            throw new MalformedMessageException("Expected a TransitPlayerProfile", e);
        }
        return skill;
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<TransitLeaderboardValue> tryReadLeaderboardList(ObjectInputStream in) throws MalformedMessageException {
        ArrayList<TransitLeaderboardValue> values;
        try {
            values = (ArrayList<TransitLeaderboardValue>) in.readObject();
            // Type-check the array
            for (@SuppressWarnings("unused")
            TransitLeaderboardValue s : values) {
            }
        } catch (Exception e) {
            throw new MalformedMessageException("Expected an ArrayList of LeaderboardValues", e);
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<String> tryReadStringList(ObjectInputStream in) throws MalformedMessageException {
        ArrayList<String> serverList;
        try {
            serverList = (ArrayList<String>) in.readObject();
            // Type-check the array
            for (@SuppressWarnings("unused")
            String s : serverList) {
            }
        } catch (Exception e) {
            throw new MalformedMessageException("Expected an ArrayList of strings", e);
        }
        return serverList;
    }
}
