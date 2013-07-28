package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class StartupMessage extends AbstractProxyServerMessage {
    public final long versionId;
    public final String prettyVersion;

    public StartupMessage(long versionId, String prettyVersion) {
        this.versionId = versionId;
        this.prettyVersion = prettyVersion;
    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.STARTUP_SUBCHANNEL;
    }

    @Override
    protected void writeData(ObjectOutputStream out) throws IOException {
        out.writeLong(versionId);
        out.writeUTF(prettyVersion);
    }
}
