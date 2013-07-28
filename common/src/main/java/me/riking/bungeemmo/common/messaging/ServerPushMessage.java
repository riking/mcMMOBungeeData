package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;

import me.riking.bungeemmo.common.data.TransitPlayerProfile;

public class ServerPushMessage extends AbstractProxyServerMessage {
    public final TransitPlayerProfile profile;
    public final boolean success;

    public ServerPushMessage(TransitPlayerProfile profile, boolean success) {
        this.profile = profile;
        this.success = success;
    }

    @Override
    public void writeData(ObjectOutputStream out) throws IOException {
        out.writeBoolean(success);
        out.writeObject(profile);
    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.PUSH_PROXY_SUBCHANNEL;
    }
}
