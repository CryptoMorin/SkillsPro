package org.skills.abilities.juggernaut;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;

public class JuggernautHeavyStrikes extends Ability {
    public JuggernautHeavyStrikes() {
        super("Juggernaut", "heavy_strikes");
    }

    @EventHandler(ignoreCancelled = true)
    public void onJuggernautAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getWorld())) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        XMaterial match = XMaterial.matchXMaterial(item);
        if (!match.isOneOf(getExtra(info, "weapons").getStringList())) return;

        LivingEntity victim = (LivingEntity) event.getEntity();
        ItemStack[] armors = victim.getEquipment().getArmorContents();
        for (ItemStack armor : armors) {
            if (armor == null) continue;
            double scaling = this.getScaling(info, "durability", armor.getDurability());
            armor.setDurability((short) (armor.getDurability() + scaling));
        }
        victim.getEquipment().setArmorContents(armors);
    }
}
