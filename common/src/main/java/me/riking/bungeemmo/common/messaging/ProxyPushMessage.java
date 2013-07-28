package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;

import me.riking.bungeemmo.common.PluginMessageUtil;
import me.riking.bungeemmo.common.data.TransitPlayerProfile;

/**
 * A server puts a PlayerProfile onto the proxy. No response will be
 * given.
 *
 * @param Channel MCMMO_CHANNEL_NAME
 * @param UTF PUSH_PROXY_SUBCHANNEL
 * @param Boolean false for a failure to comply with a request, true for
 *            success or self-initiated
 * @param Object {@link TransitPlayerProfile}
 */
public class ProxyPushMessage extends AbstractProxyServerMessage {
    public final TransitPlayerProfile profile;
    public final boolean success;

    public ProxyPushMessage(TransitPlayerProfile profile, boolean success) {
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
