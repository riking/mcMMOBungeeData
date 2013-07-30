package me.riking.bungeemmo.common.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

public class TransitPlayerRank implements java.io.Serializable {
    /**
     * Version 1.4.06
     * <p>
     * TODO change on mcMMO updates to keep data consistency
     */
    private static final long serialVersionUID = -3186750015155374346L;

    public transient String playerName;
    public transient Map<TransitSkillType, Integer> rank;

    private void readObject(ObjectInputStream in) {
        rank = new HashMap<TransitSkillType, Integer>();
        try {
            playerName = in.readUTF();

            rank.put(null, in.readInt());
            rank.put(TransitSkillType.TAMING, in.readInt());
            rank.put(TransitSkillType.MINING, in.readInt());
            rank.put(TransitSkillType.REPAIR, in.readInt());
            rank.put(TransitSkillType.WOODCUTTING, in.readInt());
            rank.put(TransitSkillType.UNARMED, in.readInt());
            rank.put(TransitSkillType.HERBALISM, in.readInt());
            rank.put(TransitSkillType.EXCAVATION, in.readInt());
            rank.put(TransitSkillType.ARCHERY, in.readInt());
            rank.put(TransitSkillType.SWORDS, in.readInt());
            rank.put(TransitSkillType.AXES, in.readInt());
            rank.put(TransitSkillType.ACROBATICS, in.readInt());
            rank.put(TransitSkillType.FISHING, in.readInt());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeObject(ObjectOutputStream out) {
        Validate.notNull(playerName, "TransitPlayerRank was not fully constructed!");
        Validate.notNull(rank, "TransitPlayerRank was not fully constructed!");

        try {
            out.writeUTF(playerName);

            out.writeInt(rank.get(null));
            out.writeInt(rank.get(TransitSkillType.TAMING));
            out.writeInt(rank.get(TransitSkillType.MINING));
            out.writeInt(rank.get(TransitSkillType.REPAIR));
            out.writeInt(rank.get(TransitSkillType.WOODCUTTING));
            out.writeInt(rank.get(TransitSkillType.UNARMED));
            out.writeInt(rank.get(TransitSkillType.HERBALISM));
            out.writeInt(rank.get(TransitSkillType.EXCAVATION));
            out.writeInt(rank.get(TransitSkillType.ARCHERY));
            out.writeInt(rank.get(TransitSkillType.SWORDS));
            out.writeInt(rank.get(TransitSkillType.AXES));
            out.writeInt(rank.get(TransitSkillType.ACROBATICS));
            out.writeInt(rank.get(TransitSkillType.FISHING));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
