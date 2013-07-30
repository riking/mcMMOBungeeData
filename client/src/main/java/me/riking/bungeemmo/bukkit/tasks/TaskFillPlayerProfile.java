package me.riking.bungeemmo.bukkit.tasks;

import java.lang.reflect.Field;

import org.bukkit.ChatColor;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.AbilityType;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;

public class TaskFillPlayerProfile implements Runnable {
    private final String player;
    private final PlayerProfile profile;

    public TaskFillPlayerProfile(String player, PlayerProfile newProfile) {
        this.player = player;
        profile = newProfile;
    }

    private static final Field profileField = getProfileField();

    private static Field getProfileField() {
        try {
            return McMMOPlayer.class.getDeclaredField("profile");
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        McMMOPlayer p = UserManager.getPlayer(player);
        if (p == null) return;

        try {
            profileField.set(p, profile);
            return;
        } catch (NullPointerException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
        PlayerProfile target = p.getProfile();
        for (SkillType skill : SkillType.nonChildSkills()) {
            target.modifySkill(skill, profile.getSkillLevel(skill));
            target.setSkillXpLevel(skill, profile.getSkillXpLevelRaw(skill));
        }
        for (AbilityType ability : AbilityType.values()) {
            target.setSkillDATS(ability, profile.getSkillDATS(ability));
        }

        // TODO what's the callback
        new java.util.ArrayList(); // a reminder
        p.getPlayer().sendMessage(ChatColor.GREEN + "Your McMMO data has been loaded. XP will now count.");
    }
}
