package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import me.riking.bungeemmo.common.data.LeaderboardRequest;
import me.riking.bungeemmo.common.data.TransitLeaderboardValue;
import me.riking.bungeemmo.common.data.TransitSkillType;

public class LeaderboardPushMessage extends AbstractProxyServerMessage {
    public final TransitSkillType skill;
    public final int page;
    public final int perPage;
    public final ArrayList<TransitLeaderboardValue> values;

    public LeaderboardPushMessage(TransitSkillType skill, int page, int perPage, ArrayList<TransitLeaderboardValue> values) {
        this.skill = skill;
        this.page = page;
        this.perPage = perPage;
        this.values = values;
    }

    public LeaderboardPushMessage(LeaderboardRequest request, ArrayList<TransitLeaderboardValue> values) {
        skill = request.skillType;
        page = request.page;
        perPage = request.perPage;
        this.values = values;
    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.PUSH_LEADERBOARD_SUBCHANNEL;
    }

    @Override
    protected void writeData(ObjectOutputStream out) throws IOException {
        out.writeObject(skill);
        out.writeInt(page);
        out.writeInt(perPage);
        out.writeObject(values);
    }

}
