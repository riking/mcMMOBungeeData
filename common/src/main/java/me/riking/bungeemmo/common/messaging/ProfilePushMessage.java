package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;

import me.riking.bungeemmo.common.data.TransitPlayerProfile;

public class ProfilePushMessage extends AbstractProxyServerMessage {
    public final String playerName;
    public final TransitPlayerProfile profile;

    public ProfilePushMessage(String playerName, TransitPlayerProfile profile) {
        this.playerName = playerName;
        this.profile = profile;
    }

    @Override
    public void writeData(ObjectOutputStream out) throws IOException {
        out.writeUTF(playerName);
        out.writeObject(profile);
    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.PUSH_PROFILE_SUBCHANNEL;
    }
}
