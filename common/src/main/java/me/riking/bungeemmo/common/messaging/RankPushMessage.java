package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;

import me.riking.bungeemmo.common.data.TransitPlayerRank;

public class RankPushMessage extends AbstractProxyServerMessage {
    public final String playerName;
    public final TransitPlayerRank rank;

    public RankPushMessage(String playerName, TransitPlayerRank rank) {
        this.playerName = playerName;
        this.rank = rank;
    }

    @Override
    public void writeData(ObjectOutputStream out) throws IOException {
        out.writeUTF(playerName);
        out.writeObject(rank);
    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.PUSH_PROFILE_SUBCHANNEL;
    }
}
