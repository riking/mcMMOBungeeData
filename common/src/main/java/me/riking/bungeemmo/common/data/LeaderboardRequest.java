package me.riking.bungeemmo.common.data;

/**
 * The intent of this class is to serve as a key in HashMap lookups.
 */
public final class LeaderboardRequest {
    /**
     * Can be null
     */
    public final TransitSkillType skillType;
    public final int page;
    public final int perPage;

    public LeaderboardRequest(TransitSkillType skill, int pageNumber, int statsPerPage) {
        skillType = skill;
        page = pageNumber;
        perPage = statsPerPage;
    }

    public boolean isPowerLevel() {
        return skillType == null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((skillType == null) ? 0 : skillType.hashCode());
        result = prime * result + page;
        result = prime * result + perPage;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LeaderboardRequest other = (LeaderboardRequest) obj;
        if (page != other.page)
            return false;
        if (perPage != other.perPage)
            return false;
        if (skillType != other.skillType)
            return false;
        return true;
    }
}
