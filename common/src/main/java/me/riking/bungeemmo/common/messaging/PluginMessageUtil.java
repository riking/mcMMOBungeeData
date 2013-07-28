package me.riking.bungeemmo.common.messaging;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

import me.riking.bungeemmo.common.data.TransitPlayerProfile;

public class PluginMessageUtil {
    public static final String MCMMO_CHANNEL_NAME = "mcMMOdatabase";
    public static final String BUNGEE_CHANNEL_NAME = "BungeeCord";

    /**
     * A server announces to others that it has gone online. If the proxy
     * detects that the version is mismatched, it <b>must</b> send a
     * BAD_VERSION_SUBCHANNEL message in response.
     * <p>
     * This is a Server -> Server subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF ANNOUNCE_SUBCHANNEL
     * @param UTF Sending server
     * @param Long the versionUid
     * @param UTF Pretty-print version
     */
    public static final String ANNOUNCE_SUBCHANNEL = "AN";

    /**
     * The proxy tells a server that its version given in Announce is
     * mismatched with its own and requests the server to stop. This <b>must
     * only</b> be sent after an ANNOUNCE_SUBCHANNEL message.
     * <p>
     * This is a Proxy -> Server subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF BAD_VERSION_SUBCHANNEL
     * @param Long Correct versionUid
     * @param UTF Correct pretty-print version
     */
    public static final String BAD_VERSION_SUBCHANNEL = "BV";

    /**
     * A server requests a PlayerProfile from the proxy. The proxy must
     * respond with a PUSH_SERVER_SUBCHANNEL message.
     * <p>
     * This is a Server -> Proxy subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF PULL_PROXY_SUBCHANNEL
     * @param UTF Player Name
     */
    public static final String PULL_PROXY_SUBCHANNEL = "PG";

    /**
     * A server puts a PlayerProfile onto the proxy. No response should be
     * given.
     * <p>
     * This is a Server -> Proxy subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF PUSH_PROXY_SUBCHANNEL
     * @param Boolean false for a failure to comply with a request, true for
     *            success or self-initiated
     * @param Object {@link TransitPlayerProfile}
     */
    public static final String PUSH_PROXY_SUBCHANNEL = "PP";

    /**
     * The proxy requests the PlayerProfile of a player on the server. The
     * server should respond with a PUSH_PROXY_SUBCHANNEL message.
     * <p>
     * This is a Proxy -> Server subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF PULL_SERVER_SUBCHANNEL
     * @param UTF Player name
     */
    public static final String PULL_SERVER_SUBCHANNEL = "SG";

    /**
     * The proxy gives a PlayerProfile to a server. No response should be
     * given.
     * <p>
     * This is a Proxy -> Server subchannel.
     *
     * @param Channel MCMMO_CHANNEL_NAME
     * @param UTF PUSH_SERVER_SUBCHANNEL
     * @param Boolean false for a failure to comply with a request, true for
     *            success or self-initiated
     * @param Object {@link TransitPlayerProfile}
     */
    public static final String PUSH_SERVER_SUBCHANNEL = "SP";

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

    public Message readIncomingMessage(byte[] data) throws IOException {
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
        ObjectInputStream in = new ObjectInputStream(bytesIn);
        String subchannel = in.readUTF();
        if (subchannel == ANNOUNCE_SUBCHANNEL) {
            String sender = in.readUTF();
            long senderVid = in.readLong();
            String senderVer = in.readUTF();
            return new VersionAnnounceMessage(sender, senderVid, senderVer);
        } else if (subchannel == BAD_VERSION_SUBCHANNEL) {
            long correctVid = in.readLong();
            String correctVer = in.readUTF();
            return new BadVersionMessage(correctVid, correctVer);
        } else if (subchannel == PULL_PROXY_SUBCHANNEL) {
            String playerName = in.readUTF();
            return new ProxyPullMessage(playerName);
        } else if (subchannel == PUSH_PROXY_SUBCHANNEL) {
            boolean failure = in.readBoolean();
            TransitPlayerProfile profile;
            try {
                profile = (TransitPlayerProfile) in.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException("Expected a me.riking.bungeemmo.common.data.TransitPlayerProfile", e);
            }
            return new ProxyPushMessage(profile, failure);
        } else if (subchannel == PULL_SERVER_SUBCHANNEL) {
            String playerName = in.readUTF();
            return new ServerPullMessage(playerName);
        } else if (subchannel == PUSH_SERVER_SUBCHANNEL) {
            boolean failure = in.readBoolean();
            TransitPlayerProfile profile;
            try {
                profile = (TransitPlayerProfile) in.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException("Expected a me.riking.bungeemmo.common.data.TransitPlayerProfile", e);
            }
            return new ServerPushMessage(profile, failure);
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
                throw new IOException("Expected a me.riking.bungeemmo.common.data.TransitPlayerProfile", e);
            }
            return new TransferPushMessage(profile, failure);
        }
        return null;
    }
    /*
     * Subchannels:
     * Pull data - request PlayerProfile to be sent
     * Push data - send PlayerProfile
     */

}
