package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;


public class ProfilePullMessage extends AbstractProxyServerMessage {
    public final String playerName;
    public final boolean create;

    public ProfilePullMessage(String playerName, boolean create) {
        this.playerName = playerName;
        this.create = create;
    }

    @Override
    protected void writeData(ObjectOutputStream out) throws IOException {
        out.writeUTF(playerName);
        out.writeBoolean(create);
    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.PULL_PROFILE_SUBCHANNEL;
    }
}
