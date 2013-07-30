package me.riking.bungeemmo.common.messaging;

import java.io.IOException;
import java.io.ObjectOutputStream;

import me.riking.bungeemmo.common.data.LeaderboardRequest;
import me.riking.bungeemmo.common.data.TransitSkillType;

public class LeaderboardPullMessage extends AbstractProxyServerMessage {
    public final TransitSkillType skill;
    public final int page;
    public final int perPage;

    public LeaderboardPullMessage(TransitSkillType skill, int page, int perPage) {
        this.skill = skill;
        this.page = page;
        this.perPage = perPage;
    }

    public LeaderboardPullMessage(LeaderboardRequest request) {
        skill = request.skillType;
        page = request.page;
        perPage = request.perPage;
    }

    public LeaderboardRequest getRequest() {
        return new LeaderboardRequest(skill, page, perPage);
    }

    @Override
    public String getSubchannel() {
        return PluginMessageUtil.PULL_LEADERBOARD_SUBCHANNEL;
    }

    @Override
    protected void writeData(ObjectOutputStream out) throws IOException {
        out.writeObject(skill);
        out.writeInt(page);
        out.writeInt(perPage);
    }
}
