package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class ServerStopMessage extends AbstractProxyServerMessage {

    public ServerStopMessage() {

    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.SERVER_STOP_SUBCHANNEL;
    }

    @Override
    protected void writeData(ObjectOutputStream out) throws IOException {
        return;
    }
}
