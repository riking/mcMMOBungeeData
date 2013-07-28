package me.riking.bungeemmo.common.messaging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import me.riking.bungeemmo.common.PluginMessageUtil;

public abstract class AbstractProxyServerMessage implements Message {
    public String getSendingChannelName() {
        return PluginMessageUtil.MCMMO_CHANNEL_NAME;
    }

    @Override
    public final void write(ByteArrayOutputStream byteOut) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeUTF(getSubchannel());
        writeData(out);
        out.close();
    }

    protected abstract void writeData(ObjectOutputStream out) throws IOException;
}
