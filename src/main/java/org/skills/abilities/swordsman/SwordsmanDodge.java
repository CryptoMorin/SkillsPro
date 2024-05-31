package org.skills.abilities.swordsman;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XTag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.utils.MathUtils;

public class SwordsmanDodge extends Ability {
    public SwordsmanDodge() {
        super("Swordsman", "dodge");
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwordsmanDefend(EntityDamageByEntityEvent event) {
        if (commonDamageCheckupReverse(event)) return;

        Player player = (Player) event.getEntity();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        XMaterial match = XMaterial.matchXMaterial(item);
        if (!XTag.anyMatchString(match, getOptions(info).getStringList("weapons"))) return;

        if (MathUtils.hasChance((int) this.getScaling(info, "chance", event))) {
            event.setCancelled(true);
            SkillsLang.Skill_Swordsman_Dodge_Message.sendMessage(player);
            if (event.getDamager() instanceof Player) {
                SkillsLang.Skill_Swordsman_Dodge_Opponent_Message.sendMessage(event.getDamager());
            }
        }
    }
}
