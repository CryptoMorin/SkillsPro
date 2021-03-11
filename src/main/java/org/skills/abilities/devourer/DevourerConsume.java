package org.skills.abilities.devourer;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.managers.LastHitManager;
import org.skills.utils.MathUtils;

public class DevourerConsume extends Ability {
    public DevourerConsume() {
        super("Devourer", "consume");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSuckSoul(EntityDamageByEntityEvent event) {
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = checkup(player);
        if (info == null) return;

        if (MathUtils.hasChance((int) getExtraScaling(info, "chance", event))) {
            Player victim = (Player) event.getEntity();
            SkilledPlayer victimInfo = SkilledPlayer.getSkilledPlayer(victim);
            long take = (int) getScaling(info, "damage", event.getDamage(), "souls", victimInfo.getSouls());

            ParticleDisplay pdisplay = new ParticleDisplay(Particle.VILLAGER_HAPPY, player.getLocation(), 30, 1, 1, 1);
            ParticleDisplay vdisplay = new ParticleDisplay(Particle.VILLAGER_ANGRY, victim.getLocation(), 30, 1, 1, 1);
            pdisplay.spawn();
            vdisplay.spawn();

            if (victimInfo.getSouls() < 1) {
                double damage = getExtraScaling(info, "damage", event);
                LastHitManager.damage(victim, player, damage);
                XSound.BLOCK_GLASS_BREAK.play(player);
            } else {
                if (take > victimInfo.getSouls()) take = victimInfo.getSouls();
                victimInfo.addSouls(-take);
                info.addSouls(take);
                XSound.ENTITY_GENERIC_DRINK.play(player);
            }
        }
    }

    @Override
    public Object[] applyEdits(SkilledPlayer info) {
        return new Object[]{"%chance%", getScalingDescription(info, getExtra(info).getString("chance"))};
    }
}
