package org.skills.abilities.swordsman;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.SkillsLang;
import org.skills.utils.MathUtils;

public class SwordsmanDodge extends Ability {
    public SwordsmanDodge() {
        super("Swordsman", "dodge");
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwordsmanDefend(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        Player player = (Player) event.getEntity();
        ItemStack item = player.getInventory().getItemInMainHand();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        XMaterial match = XMaterial.matchXMaterial(item);
        if (!match.isOneOf(getExtra(info).getStringList("weapons"))) return;

        if (MathUtils.hasChance((int) this.getScaling(info, event))) {
            event.setCancelled(true);
            SkillsLang.Skill_Swordsman_Dodge_Message.sendMessage(player);
            if (event.getDamager() instanceof Player) {
                SkillsLang.Skill_Swordsman_Dodge_Opponent_Message.sendMessage(event.getDamager());
            }
        }
    }
}
