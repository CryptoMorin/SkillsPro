package org.skills.abilities.devourer;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.utils.MathUtils;

public class DevourerDisarm extends ActiveAbility {
    public DevourerDisarm() {
        super("Devourer", "disarm");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDevourerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        int lvl = info.getAbilityLevel(this);
        Player victim = (Player) event.getEntity();
        ParticleDisplay.simple(victim.getLocation(), Particle.VILLAGER_ANGRY).withCount(100).offset(1).spawn();
        XSound.ENTITY_ITEM_BREAK.play(victim);

        ItemStack item = victim.getItemInHand();
        PlayerInventory inv = victim.getInventory();
        if (lvl > 2) {
            int randSlot = MathUtils.randInt(9, 30);
            ItemStack replace = inv.getItem(randSlot);
            inv.setItem(randSlot, item);
            inv.setItem(inv.getHeldItemSlot(), replace);
        } else {
            for (int i = 9; i < 35; i++) {
                ItemStack replace = inv.getItem(i);
                if (replace == null) {
                    inv.setItem(i, item);
                    inv.setItem(inv.getHeldItemSlot(), replace);
                    break;
                }
            }
        }
        if (lvl > 1) inv.setHeldItemSlot(MathUtils.randInt(0, 8));
    }
}
