package me.riking.bungeemmo.common.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

public class TransitPlayerProfile implements Serializable {
    /**
     * Version 1.4.06
     * <p>
     * TODO change on mcMMO updates to keep data consistency
     */
    private static final long serialVersionUID = 221557388855846398L;

    public static long getVersion() {
        return serialVersionUID;
    }

    public transient String playerName;
    public transient Map<TransitSkillType, Integer>   skills;
    public transient Map<TransitSkillType, Float>     skillsXp;
    public transient Map<TransitAbilityType, Integer> skillsDATS;
    public transient TransitHudType hudType;
    public transient TransitMobHealthbarType mobHealthbarType;

    private void readObject(ObjectInputStream in) {
        skills     = new HashMap<TransitSkillType, Integer>();   // Skill & Level
        skillsXp   = new HashMap<TransitSkillType, Float>();     // Skill & XP
        skillsDATS = new HashMap<TransitAbilityType, Integer>(); // Ability & Cooldown

        try {
            playerName = in.readUTF();

            skills.put(TransitSkillType.TAMING, in.readInt());
            skills.put(TransitSkillType.MINING, in.readInt());
            skills.put(TransitSkillType.REPAIR, in.readInt());
            skills.put(TransitSkillType.WOODCUTTING, in.readInt());
            skills.put(TransitSkillType.UNARMED, in.readInt());
            skills.put(TransitSkillType.HERBALISM, in.readInt());
            skills.put(TransitSkillType.EXCAVATION, in.readInt());
            skills.put(TransitSkillType.ARCHERY, in.readInt());
            skills.put(TransitSkillType.SWORDS, in.readInt());
            skills.put(TransitSkillType.AXES, in.readInt());
            skills.put(TransitSkillType.ACROBATICS, in.readInt());
            skills.put(TransitSkillType.FISHING, in.readInt());

            skillsXp.put(TransitSkillType.TAMING, in.readFloat());
            skillsXp.put(TransitSkillType.MINING, in.readFloat());
            skillsXp.put(TransitSkillType.REPAIR, in.readFloat());
            skillsXp.put(TransitSkillType.WOODCUTTING, in.readFloat());
            skillsXp.put(TransitSkillType.UNARMED, in.readFloat());
            skillsXp.put(TransitSkillType.HERBALISM, in.readFloat());
            skillsXp.put(TransitSkillType.EXCAVATION, in.readFloat());
            skillsXp.put(TransitSkillType.ARCHERY, in.readFloat());
            skillsXp.put(TransitSkillType.SWORDS, in.readFloat());
            skillsXp.put(TransitSkillType.AXES, in.readFloat());
            skillsXp.put(TransitSkillType.ACROBATICS, in.readFloat());
            skillsXp.put(TransitSkillType.FISHING, in.readFloat());

            in.readInt(); // Taming - Unused
            skillsDATS.put(TransitAbilityType.SUPER_BREAKER, in.readInt());
            in.readInt(); // Repair - Unused
            skillsDATS.put(TransitAbilityType.TREE_FELLER, in.readInt());
            skillsDATS.put(TransitAbilityType.BERSERK, in.readInt());
            skillsDATS.put(TransitAbilityType.GREEN_TERRA, in.readInt());
            skillsDATS.put(TransitAbilityType.GIGA_DRILL_BREAKER, in.readInt());
            in.readInt(); // Archery - Unused
            skillsDATS.put(TransitAbilityType.SERRATED_STRIKES, in.readInt());
            skillsDATS.put(TransitAbilityType.SKULL_SPLITTER, in.readInt());
            in.readInt(); // Acrobatics - Unused
            skillsDATS.put(TransitAbilityType.BLAST_MINING, in.readInt());


            try {
                hudType = (TransitHudType) in.readObject();
            } catch (ClassNotFoundException e) {
                hudType = TransitHudType.NULL; // Use server default
            }

            try {
                mobHealthbarType = (TransitMobHealthbarType) in.readObject();
            } catch (ClassNotFoundException e) {
                mobHealthbarType = TransitMobHealthbarType.NULL; // Use server default
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeObject(ObjectOutputStream out) {
        Validate.noNullElements(new Object[] {
                playerName, skills, skillsXp, skillsDATS, hudType, mobHealthbarType },
                "TransitPlayerProfile was not fully constructed!");

        try {
            out.writeUTF(playerName);

            out.writeInt(skills.get(TransitSkillType.TAMING));
            out.writeInt(skills.get(TransitSkillType.MINING));
            out.writeInt(skills.get(TransitSkillType.REPAIR));
            out.writeInt(skills.get(TransitSkillType.WOODCUTTING));
            out.writeInt(skills.get(TransitSkillType.UNARMED));
            out.writeInt(skills.get(TransitSkillType.HERBALISM));
            out.writeInt(skills.get(TransitSkillType.EXCAVATION));
            out.writeInt(skills.get(TransitSkillType.ARCHERY));
            out.writeInt(skills.get(TransitSkillType.SWORDS));
            out.writeInt(skills.get(TransitSkillType.AXES));
            out.writeInt(skills.get(TransitSkillType.ACROBATICS));
            out.writeInt(skills.get(TransitSkillType.FISHING));

            out.writeFloat(skillsXp.get(TransitSkillType.TAMING));
            out.writeFloat(skillsXp.get(TransitSkillType.MINING));
            out.writeFloat(skillsXp.get(TransitSkillType.REPAIR));
            out.writeFloat(skillsXp.get(TransitSkillType.WOODCUTTING));
            out.writeFloat(skillsXp.get(TransitSkillType.UNARMED));
            out.writeFloat(skillsXp.get(TransitSkillType.HERBALISM));
            out.writeFloat(skillsXp.get(TransitSkillType.EXCAVATION));
            out.writeFloat(skillsXp.get(TransitSkillType.ARCHERY));
            out.writeFloat(skillsXp.get(TransitSkillType.SWORDS));
            out.writeFloat(skillsXp.get(TransitSkillType.AXES));
            out.writeFloat(skillsXp.get(TransitSkillType.ACROBATICS));
            out.writeFloat(skillsXp.get(TransitSkillType.FISHING));

            out.writeInt(0); // Taming - Unused
            out.writeInt(skillsDATS.get(TransitAbilityType.SUPER_BREAKER));
            out.writeInt(0); // Repair - Unused
            out.writeInt(skillsDATS.get(TransitAbilityType.TREE_FELLER));
            out.writeInt(skillsDATS.get(TransitAbilityType.BERSERK));
            out.writeInt(skillsDATS.get(TransitAbilityType.GREEN_TERRA));
            out.writeInt(skillsDATS.get(TransitAbilityType.GIGA_DRILL_BREAKER));
            out.writeInt(0); // Archery - Unused
            out.writeInt(skillsDATS.get(TransitAbilityType.SERRATED_STRIKES));
            out.writeInt(skillsDATS.get(TransitAbilityType.SKULL_SPLITTER));
            out.writeInt(0); // Acrobatics - Unused
            out.writeInt(skillsDATS.get(TransitAbilityType.BLAST_MINING));

            out.writeObject(hudType);
            out.writeObject(mobHealthbarType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
