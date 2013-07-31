package me.riking.bungeemmo.bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.datatypes.MobHealthbarType;
import com.gmail.nossr50.datatypes.database.PlayerStat;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.AbilityType;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.datatypes.spout.huds.HudType;

import me.riking.bungeemmo.common.data.TransitAbilityType;
import me.riking.bungeemmo.common.data.TransitHudType;
import me.riking.bungeemmo.common.data.TransitLeaderboardValue;
import me.riking.bungeemmo.common.data.TransitMobHealthbarType;
import me.riking.bungeemmo.common.data.TransitPlayerProfile;
import me.riking.bungeemmo.common.data.TransitPlayerRank;
import me.riking.bungeemmo.common.data.TransitSkillType;

public class TransitDataConverter {
    public static TransitSkillType toTransit(SkillType mm) {
        if (mm == null) return null;
        return TransitSkillType.valueOf(mm.name());
    }

    public static TransitAbilityType toTransit(AbilityType mm) {
        if (mm == null) return null;
        return TransitAbilityType.valueOf(mm.name());
    }

    public static TransitHudType toTransit(HudType mm) {
        if (mm == null) return TransitHudType.NULL;
        return TransitHudType.valueOf(mm.name());
    }

    public static TransitMobHealthbarType toTransit(MobHealthbarType mm) {
        if (mm == null) return TransitMobHealthbarType.NULL;
        return TransitMobHealthbarType.valueOf(mm.name());
    }

    public static ArrayList<TransitLeaderboardValue> toTransit(List<PlayerStat> mm) {
        if (mm == null) return new ArrayList<TransitLeaderboardValue>();

        ArrayList<TransitLeaderboardValue> ret = new ArrayList<TransitLeaderboardValue>();
        for (PlayerStat mmo : mm) {
            ret.add(new TransitLeaderboardValue(mmo.name, mmo.statVal));
        }
        return ret;
    }

    public static SkillType fromTransit(TransitSkillType co) {
        if (co == null) return null;
        return SkillType.valueOf(co.name());
    }

    public static AbilityType fromTransit(TransitAbilityType co) {
        if (co == null) return null;
        return AbilityType.valueOf(co.name());
    }

    public static HudType fromTransit(TransitHudType co) {
        if (co == null || co == TransitHudType.NULL) {
            return HudType.STANDARD;
        }
        return HudType.valueOf(co.name());
    }

    public static MobHealthbarType fromTransit(TransitMobHealthbarType co) {
        if (co == null || co == TransitMobHealthbarType.NULL) {
            return Config.getInstance().getMobHealthbarDefault();
        }
        return MobHealthbarType.valueOf(co.name());
    }

    public static ArrayList<PlayerStat> fromTransit(List<TransitLeaderboardValue> co) {
        if (co == null) return new ArrayList<PlayerStat>();

        ArrayList<PlayerStat> ret = new ArrayList<PlayerStat>();
        for (TransitLeaderboardValue com : co) {
            ret.add(new PlayerStat(com.name, com.val));
        }
        return ret;
    }

    public static TransitPlayerProfile toTransit(PlayerProfile profile) {
        TransitPlayerProfile prof = new TransitPlayerProfile();
        Map<TransitSkillType, Integer> skills = new HashMap<TransitSkillType, Integer>(); // Skill & Level
        Map<TransitSkillType, Float> skillsXp = new HashMap<TransitSkillType, Float>(); // Skill & XP
        Map<TransitAbilityType, Integer> skillsDATS = new HashMap<TransitAbilityType, Integer>(); // Ability & Cooldown

        for (SkillType sk : SkillType.nonChildSkills()) {
            skills.put(toTransit(sk), profile.getSkillLevel(sk));
            skillsXp.put(toTransit(sk), skillsXp.get(sk));
        }
        // TODO if they change to actually storing longs instead of upcasting, need to change this
        // (will be both a serialversion and mcmmo version increment)
        for (AbilityType ab : AbilityType.values()) {
            skillsDATS.put(toTransit(ab), (int) profile.getSkillDATS(ab));
        }

        prof.playerName = profile.getPlayerName();
        prof.skills = skills;
        prof.skillsXp = skillsXp;
        prof.skillsDATS = skillsDATS;
        prof.hudType = toTransit(profile.getHudType());
        prof.mobHealthbarType = toTransit(profile.getMobHealthbarType());
        return prof;
    }

    public static PlayerProfile fromTransit(TransitPlayerProfile prof) {
        Map<SkillType, Integer> skills = new HashMap<SkillType, Integer>(); // Skill & Level
        Map<SkillType, Float> skillsXp = new HashMap<SkillType, Float>(); // Skill & XP
        Map<AbilityType, Integer> skillsDATS = new HashMap<AbilityType, Integer>(); // Ability & Cooldown

        Map<TransitSkillType, Integer> trskills = prof.skills;
        Map<TransitSkillType, Float> trskillsXp = prof.skillsXp;
        Map<TransitAbilityType, Integer> trskillsDATS = prof.skillsDATS;

        for (SkillType sk : SkillType.nonChildSkills()) {
            Integer skill = trskills.get(toTransit(sk));
            if (skill != null) {
                skills.put(sk, skill);
            } else {
                skills.put(sk, 0);
            }
            Float xp = trskillsXp.get(toTransit(sk));
            if (xp != null) {
                skillsXp.put(sk, xp);
            } else {
                skillsXp.put(sk, 0f);
            }
        }
        for (AbilityType ab : AbilityType.values()) {
            Integer dat = trskillsDATS.get(toTransit(ab));
            if (dat != null) {
                skillsDATS.put(ab, dat);
            } else {
                skillsDATS.put(ab, 0);
            }
        }

        return new PlayerProfile(prof.playerName, skills, skillsXp, skillsDATS, fromTransit(prof.hudType), fromTransit(prof.mobHealthbarType));
    }

    /**
     * @deprecated no use, and would be a waste of time to make
     */
    @Deprecated
    public static TransitPlayerRank toTransit(Map<String, Integer> rank) {
        return null;
    }

    public static Map<SkillType, Integer> fromTransit(TransitPlayerRank rank) {
        Map<SkillType, Integer> ret = new HashMap<SkillType, Integer>();
        for (Map.Entry<TransitSkillType, Integer> entry : rank.rank.entrySet()) {
            ret.put(fromTransit(entry.getKey()), entry.getValue());
        }
        return ret;
    }
}
