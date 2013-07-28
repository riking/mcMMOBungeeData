package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;

import me.riking.bungeemmo.common.PluginMessageUtil;

public class BadVersionMessage extends AbstractProxyServerMessage {
    public final long versionId;
    public final String prettyVersion;

    public BadVersionMessage(long correctVid, String correctVer) {
        versionId = correctVid;
        prettyVersion = correctVer;
    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.BAD_VERSION_SUBCHANNEL;
    }

    @Override
    protected void writeData(ObjectOutputStream out) throws IOException {
        out.writeLong(versionId);
        out.writeUTF(prettyVersion);
    }
}
