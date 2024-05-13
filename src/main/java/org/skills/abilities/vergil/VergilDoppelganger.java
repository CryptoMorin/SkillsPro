package org.skills.abilities.vergil;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.utils.NPCHandler;

public class VergilDoppelganger extends ActiveAbility {
    public VergilDoppelganger() {
        super("Vergil", "doppelganger");
        setPvPBased(true);
    }

    public boolean isSupported() {
        try {
            Class.forName("net.citizensnpcs.api.npc.NPC");
            Class.forName("org.mcmonkey.sentinel.SentinelTrait");
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHit(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;
        Player player = (Player) event.getDamager();
        SkilledPlayer info = checkup(player);
        if (info == null) return;
        if (!isSupported()) {
            player.sendMessage("You can't use this ability because Citizens and Sentinel are not installed.");
            return;
        }

        LivingEntity victim = (LivingEntity) event.getEntity();
        ParticleDisplay display = ParticleDisplay.of(XParticle.CLOUD).withCount(100).offset(2);
        display.spawn(player.getLocation());
        NPCHandler.spawnNPC(player, victim);
    }
}
