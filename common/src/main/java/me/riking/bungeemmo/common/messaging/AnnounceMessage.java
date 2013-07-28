package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class AnnounceMessage extends AbstractProxyServerMessage {
    public final String newServerName;
    public final boolean adding;

    public AnnounceMessage(String newServerName, boolean adding) {
        this.newServerName = newServerName;
        this.adding = adding;
    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.ANNOUNCE_SUBCHANNEL;
    }

    @Override
    protected void writeData(ObjectOutputStream out) throws IOException {
        out.writeUTF(newServerName);
        out.writeBoolean(adding);
    }
}
