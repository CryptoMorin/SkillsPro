package org.skills.abilities.arbalist;

import org.bukkit.entity.Player;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.data.managers.SkilledPlayer;

public class ArbalistCrossExamination extends InstantActiveAbility {
    public static final String ARBALIST_FIRECROSSBOW = "ARBALIST_CROSS";

    public ArbalistCrossExamination() {
        super("Arbalist", "cross_examine");
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();
        player.sendMessage("activating");
        /* TODO
        *
cross-examine:
required-level: 1
toaster: 3
activation:
  idle: 5
  key: LR
  energy: 1
  cooldown: 2
  items: [ "CROSSBOW" ]
         */
    }
}