package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;

import me.riking.bungeemmo.common.PluginMessageUtil;

public class TransferPullMessage extends AbstractServerServerMessage {
    public String playerName;
    public String replyServer;

    /**
     * Sender constructor
     */
    public TransferPullMessage(String playerName, String destinationServer) {
        super(destinationServer);
        this.playerName = playerName;
        this.replyServer = PluginMessageUtil.serverName;
    }

    /**
     * Recipient constructor
     *
     * @param flag Ignored
     */
    public TransferPullMessage(String playerName, String replyServer, Object flag) {
        super(null);
        this.playerName = playerName;
        this.replyServer = replyServer;
    }

    @Override
    protected void writeData(ObjectOutputStream out) throws IOException {
        out.writeUTF(replyServer);
        out.writeUTF(playerName);
    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.PULL_TRANSFER_SUBCHANNEL;
    }
}
