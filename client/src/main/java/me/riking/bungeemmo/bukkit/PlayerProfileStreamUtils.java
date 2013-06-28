package me.riking.bungeemmo.bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.datatypes.MobHealthbarType;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.AbilityType;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.datatypes.spout.huds.HudType;

public class PlayerProfileStreamUtils {

    public PlayerProfile readProfile(ObjectInputStream in) {
        Map<SkillType, Integer>   skills     = new HashMap<SkillType, Integer>();   // Skill & Level
        Map<SkillType, Float>     skillsXp   = new HashMap<SkillType, Float>();     // Skill & XP
        Map<AbilityType, Integer> skillsDATS = new HashMap<AbilityType, Integer>(); // Ability & Cooldown
        HudType hudType;
        MobHealthbarType mobHealthbarType;

        try {
            String playerName = (String) in.readObject();
            skills.put(SkillType.TAMING, in.readInt());
            skills.put(SkillType.MINING, in.readInt());
            skills.put(SkillType.REPAIR, in.readInt());
            skills.put(SkillType.WOODCUTTING, in.readInt());
            skills.put(SkillType.UNARMED, in.readInt());
            skills.put(SkillType.HERBALISM, in.readInt());
            skills.put(SkillType.EXCAVATION, in.readInt());
            skills.put(SkillType.ARCHERY, in.readInt());
            skills.put(SkillType.SWORDS, in.readInt());
            skills.put(SkillType.AXES, in.readInt());
            skills.put(SkillType.ACROBATICS, in.readInt());
            skills.put(SkillType.FISHING, in.readInt());

            skillsXp.put(SkillType.TAMING, in.readFloat());
            skillsXp.put(SkillType.MINING, in.readFloat());
            skillsXp.put(SkillType.REPAIR, in.readFloat());
            skillsXp.put(SkillType.WOODCUTTING, in.readFloat());
            skillsXp.put(SkillType.UNARMED, in.readFloat());
            skillsXp.put(SkillType.HERBALISM, in.readFloat());
            skillsXp.put(SkillType.EXCAVATION, in.readFloat());
            skillsXp.put(SkillType.ARCHERY, in.readFloat());
            skillsXp.put(SkillType.SWORDS, in.readFloat());
            skillsXp.put(SkillType.AXES, in.readFloat());
            skillsXp.put(SkillType.ACROBATICS, in.readFloat());
            skillsXp.put(SkillType.FISHING, in.readFloat());

            in.readInt(); // Taming - Unused
            skillsDATS.put(AbilityType.SUPER_BREAKER, in.readInt());
            in.readInt(); // Repair - Unused
            skillsDATS.put(AbilityType.TREE_FELLER, in.readInt());
            skillsDATS.put(AbilityType.BERSERK, in.readInt());
            skillsDATS.put(AbilityType.GREEN_TERRA, in.readInt());
            skillsDATS.put(AbilityType.GIGA_DRILL_BREAKER, in.readInt());
            in.readInt(); // Archery - Unused
            skillsDATS.put(AbilityType.SERRATED_STRIKES, in.readInt());
            skillsDATS.put(AbilityType.SKULL_SPLITTER, in.readInt());
            in.readInt(); // Acrobatics - Unused
            skillsDATS.put(AbilityType.BLAST_MINING, in.readInt());

            try {
                hudType = (HudType) in.readObject();
            }
            catch (Exception e) {
                hudType = HudType.STANDARD; // Shouldn't happen unless database is being tampered with
            }

            try {
                mobHealthbarType = (MobHealthbarType) in.readObject();
            }
            catch (Exception e) {
                mobHealthbarType = Config.getInstance().getMobHealthbarDefault();
            }

            return new PlayerProfile(playerName, skills, skillsXp, skillsDATS, hudType, mobHealthbarType);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
