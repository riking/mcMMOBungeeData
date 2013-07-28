package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;

import me.riking.bungeemmo.common.PluginMessageUtil;
import me.riking.bungeemmo.common.data.TransitPlayerProfile;

public class VersionAnnounceMessage extends AbstractServerServerMessage {
    public final String sendingServer;
    public final String prettyVersion;
    public final long version;

    /**
     * Sender constructor
     */
    public VersionAnnounceMessage() {
        super("ALL");
        version = TransitPlayerProfile.getVersion();
        sendingServer = PluginMessageUtil.serverName;
        prettyVersion = PluginMessageUtil.prettyVersion;
    }

    /**
     * Recipient constructor
     */
    public VersionAnnounceMessage(String sender, long vid, String prettyVersion) {
        super(PluginMessageUtil.serverName);
        this.version = vid;
        this.sendingServer = sender;
        this.prettyVersion = prettyVersion;
    }

    @Override
    protected void writeData(ObjectOutputStream out) throws IOException {
        out.writeUTF(sendingServer);
        out.writeLong(version);
        out.writeUTF(prettyVersion);
    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.ANNOUNCE_SUBCHANNEL;
    }
}
