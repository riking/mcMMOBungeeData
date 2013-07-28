package me.riking.bungeemmo.bukkit;

import java.util.HashMap;
import java.util.Map;

import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.datatypes.MobHealthbarType;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.AbilityType;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.datatypes.spout.huds.HudType;

import me.riking.bungeemmo.common.data.TransitAbilityType;
import me.riking.bungeemmo.common.data.TransitHudType;
import me.riking.bungeemmo.common.data.TransitMobHealthbarType;
import me.riking.bungeemmo.common.data.TransitPlayerProfile;
import me.riking.bungeemmo.common.data.TransitSkillType;

public class TransitDataConverter {
    public static TransitSkillType toTransit(SkillType mm) {
        return TransitSkillType.valueOf(mm.name());
    }

    public static TransitAbilityType toTransit(AbilityType mm) {
        return TransitAbilityType.valueOf(mm.name());
    }

    public static TransitHudType toTransit(HudType mm) {
        return TransitHudType.valueOf(mm.name());
    }

    public static TransitMobHealthbarType toTransit(MobHealthbarType mm) {
        return TransitMobHealthbarType.valueOf(mm.name());
    }

    public static SkillType fromTransit(TransitSkillType co) {
        return SkillType.valueOf(co.name());
    }

    public static AbilityType fromTransit(TransitAbilityType co) {
        return AbilityType.valueOf(co.name());
    }

    public static HudType fromTransit(TransitHudType co) {
        if (co == TransitHudType.NULL) {
            return HudType.STANDARD;
        }
        return HudType.valueOf(co.name());
    }

    public static MobHealthbarType fromTransit(TransitMobHealthbarType co) {
        if (co == TransitMobHealthbarType.NULL) {
            return Config.getInstance().getMobHealthbarDefault();
        }
        return MobHealthbarType.valueOf(co.name());
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
            skills.put(sk, trskills.get(toTransit(sk)));
            skillsXp.put(sk, trskillsXp.get(toTransit(sk)));
        }
        for (AbilityType ab : AbilityType.values()) {
            skillsDATS.put(ab, trskillsDATS.get(toTransit(ab)));
        }

        return new PlayerProfile(prof.playerName, skills, skillsXp, skillsDATS, fromTransit(prof.hudType), fromTransit(prof.mobHealthbarType));
    }
}
