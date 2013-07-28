package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class WelcomeMessage extends AbstractProxyServerMessage {
    public final String serverName;
    public final ArrayList<String> otherServers;

    public WelcomeMessage(String serverName, ArrayList<String> otherServers) {
        this.serverName = serverName;
        this.otherServers = otherServers;
    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.WELCOME_SUBCHANNEL;
    }

    @Override
    protected void writeData(ObjectOutputStream out) throws IOException {
        out.writeUTF(serverName);
        out.writeObject(otherServers);
    }
}
