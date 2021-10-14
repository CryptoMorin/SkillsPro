package org.skills.abilities.mage;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.types.SkillScaling;

public class MagePassive extends Ability {
    public MagePassive() {
        super("Mage", "passive");
    }

    public static boolean isHoe(ItemStack item) {
        return isHoe(XMaterial.matchXMaterial(item));
    }

    public static boolean isHoe(XMaterial material) {
        switch (material) {
            case NETHERITE_HOE:
            case DIAMOND_HOE:
            case GOLDEN_HOE:
            case IRON_HOE:
            case WOODEN_HOE:
                return true;
            default:
                return false;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMageAttack(EntityDamageByEntityEvent event) {
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getWorld())) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        if (!isHoe(player.getItemInHand())) return;

        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        ConfigurationSection chances = getOptions(info, "hoe-damage").getSection();
        XMaterial material = XMaterial.matchXMaterial(player.getItemInHand());
        double hoeDamage = getAbsoluteScaling(info, chances.getString(material.name()), "damage", event.getDamage());

        int energy = (int) this.getScaling(info, "energy");
        info.setEnergy(Math.min(info.getEnergy() + energy, info.getScaling(SkillScaling.MAX_ENERGY)));
        event.setDamage(event.getDamage() + hoeDamage);
    }
}
