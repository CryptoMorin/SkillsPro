package org.skills.abilities;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Player;
import org.skills.main.SkillsConfig;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AbilityActivation {
    protected static final Cache<UUID, String> ACTIVATIONS = CacheBuilder.newBuilder()
            .expireAfterAccess(SkillsConfig.SKILL_ACTIVATION_TIME.getInt(), TimeUnit.MILLISECONDS).build();

    protected static boolean performActivations(Player player, ActivationAction action, String activation, boolean input) {
        String keys = ACTIVATIONS.getIfPresent(player.getUniqueId());
        if (keys == null) keys = String.valueOf(action.shortName);
        else if (input) keys += String.valueOf(action.shortName);
        int index = keys.length();
        int len = activation.length();

        // They'll be different
        if (activation.startsWith(keys)) {
            if (index == len) {
                ACTIVATIONS.invalidate(player.getUniqueId());
                return true;
            }
            if (input) ACTIVATIONS.put(player.getUniqueId(), keys);
        }
        return false;
    }

    protected enum ActivationAction {
        RIGHT_CLICK('R'), LEFT_CLICK('L'), SNEAK('S'), WHILE_SNEAK('C'), DROP('Q'), SWITCH('F');
        public char shortName;

        ActivationAction(char shortName) {
            this.shortName = shortName;
        }
    }
}