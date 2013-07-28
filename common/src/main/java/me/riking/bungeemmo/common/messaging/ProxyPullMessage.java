package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;

import me.riking.bungeemmo.common.PluginMessageUtil;

public class ProxyPullMessage extends AbstractProxyServerMessage {
    public final String playerName;

    public ProxyPullMessage(String playerName) {
        this.playerName = playerName;
    }

    @Override
    protected void writeData(ObjectOutputStream out) throws IOException {
        out.writeUTF(playerName);
    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.PULL_PROXY_SUBCHANNEL;
    }
}
