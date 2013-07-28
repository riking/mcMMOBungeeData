package me.riking.bungeemmo.common.messaging;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import me.riking.bungeemmo.common.data.TransitPlayerProfile;

/**
 * Constants and a read() method for Message classes.
 */
public class PluginMessageUtil {
    public static final String MCMMO_CHANNEL_NAME = "mcMMOdatabase";
    public static final String BUNGEE_CHANNEL_NAME = "BungeeCord";

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
    public static final String ANNOUNCE_SUBCHANNEL = "AN";

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
    public static final String PULL_PROFILE_SUBCHANNEL = "PG";

    /**
     * A server puts a PlayerProfile onto the proxy. No response should be
     * given.
     * <p>
     * This is a Server <-> Proxy subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF PUSH_PROFILE_SUBCHANNEL
     * @param Boolean false for a failure to comply with a request, true for
     *            success or self-initiated
     * @param Object {@link TransitPlayerProfile}
     */
    public static final String PUSH_PROFILE_SUBCHANNEL = "PP";

    /**
     * One server attempts to request a PlayerProfile from another. This
     * should be answered with a PUSH_TRANSFER_SUBCHANNEL message.
     * <p>
     * This is a Server -> Server subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF PULL_TRANSFER_SUBCHANNEL
     * @param UTF Server to reply to
     * @param UTF Player name
     */
    public static final String PULL_TRANSFER_SUBCHANNEL = "TG";

    /**
     * One server is giving a PlayerProfile to another. No response should be
     * given.
     * <p>
     * This is a Server -> Server subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF PUSH_TRANSFER_SUBCHANNEL
     * @param Boolean false for a failure to comply with a request, true for
     *            success or self-initiated
     * @param Object the TransferPlayerProfile
     */
    public static final String PUSH_TRANSFER_SUBCHANNEL = "TP";

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
        } catch (IOException e) {
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

    @SuppressWarnings("unchecked")
    public Message readIncomingMessage(byte[] data) throws IOException {
        try {
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
            ObjectInputStream in = new ObjectInputStream(bytesIn);
            String subchannel = in.readUTF();
            if (subchannel == STARTUP_SUBCHANNEL) {
                long senderVid = in.readLong();
                String senderVer = in.readUTF();
                return new StartupMessage(senderVid, senderVer);

            } else if (subchannel == WELCOME_SUBCHANNEL) {
                String serverName = in.readUTF();
                ArrayList<String> serverList;
                try {
                    serverList = (ArrayList<String>) in.readObject();
                    // Type-check the array
                    for (@SuppressWarnings("unused") String s : serverList) { }
                } catch (ClassNotFoundException e) {
                    throw new MalformedMessageException("Expected an ArrayList of strings", e);
                } catch (ClassCastException e) {
                    throw new MalformedMessageException("Expected an ArrayList of strings", e);
                }
                return new WelcomeMessage(serverName, serverList);

            } else if (subchannel == BAD_VERSION_SUBCHANNEL) {
                long correctVid = in.readLong();
                String correctVer = in.readUTF();
                return new BadVersionMessage(correctVid, correctVer);

            } else if (subchannel == ANNOUNCE_SUBCHANNEL) {
                String newServerName = in.readUTF();
                return new AnnounceMessage(newServerName);

            } else if (subchannel == PULL_PROFILE_SUBCHANNEL) {
                String playerName = in.readUTF();
                return new ProfilePullMessage(playerName);

            } else if (subchannel == PUSH_PROFILE_SUBCHANNEL) {
                boolean failure = in.readBoolean();
                TransitPlayerProfile profile;
                try {
                    profile = (TransitPlayerProfile) in.readObject();
                } catch (ClassNotFoundException e) {
                    throw new MalformedMessageException("Expected a TransitPlayerProfile", e);
                } catch (ClassCastException e) {
                    throw new MalformedMessageException("Expected a TransitPlayerProfile", e);
                }
                return new ProfilePushMessage(profile, failure);

            } else if (subchannel == PULL_TRANSFER_SUBCHANNEL) {
                String replyServer = in.readUTF();
                String playerName = in.readUTF();
                return new TransferPullMessage(playerName, replyServer, null);

            } else if (subchannel == PUSH_TRANSFER_SUBCHANNEL) {
                boolean failure = in.readBoolean();
                TransitPlayerProfile profile;
                try {
                    profile = (TransitPlayerProfile) in.readObject();
                } catch (ClassNotFoundException e) {
                    throw new MalformedMessageException("Expected a TransitPlayerProfile", e);
                } catch (ClassCastException e) {
                    throw new MalformedMessageException("Expected a TransitPlayerProfile", e);
                }
                return new TransferPushMessage(profile, failure);
            }
        } catch (Exception e) {
            throw new MalformedMessageException(e);
        }
        return null;
    }
}
