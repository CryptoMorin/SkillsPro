package org.skills.abilities.juggernaut;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;

public class JuggernautHeavyStrikes extends Ability {
    public JuggernautHeavyStrikes() {
        super("Juggernaut", "heavy_strikes");
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onJuggernautAttack(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;
        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        XMaterial match = XMaterial.matchXMaterial(item);
        if (!match.isOneOf(getOptions(info, "weapons").getStringList())) return;

        LivingEntity victim = (LivingEntity) event.getEntity();
        ItemStack[] armors = victim.getEquipment().getArmorContents();
        for (ItemStack armor : armors) {
            if (armor == null) continue;
            if (XMaterial.supports(11) && armor.hasItemMeta() && armor.getItemMeta().isUnbreakable()) continue;

            double scaling = this.getScaling(info, "durability-damage", "durability", armor.getDurability());
            if (scaling != 0) armor.setDurability((short) (armor.getDurability() + scaling));
        }
        victim.getEquipment().setArmorContents(armors);
    }
}
