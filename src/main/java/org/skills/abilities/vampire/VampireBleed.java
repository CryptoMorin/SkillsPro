package org.skills.abilities.vampire;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.utils.MathUtils;
import org.skills.utils.versionsupport.VersionSupport;

public class VampireBleed extends Ability {
    public VampireBleed() {
        super("Vampire", "bleed");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVampireAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        ParticleDisplay gain = ParticleDisplay.colored(player.getLocation(), 0, 255, 0, 1);
        gain.count = 30;
        gain.offset(0.5, 0.5, 0.5);
        ParticleDisplay bleed = ParticleDisplay.colored(player.getLocation(), 255, 0, 0, 1);
        bleed.count = 30;
        bleed.offset(0.5, 0.5, 0.5);

        LivingEntity entity = (LivingEntity) event.getEntity();
        if (!MathUtils.hasChance((int) getExtraScaling(info, "chance", "damage", event.getDamage()))) return;
        new BukkitRunnable() {
            final int duration = (int) getExtraScaling(info, "duration", event);
            final double scaling = getScaling(info, event);
            int repeat = 0;

            @Override
            public void run() {
                if (!entity.isValid()) {
                    cancel();
                    return;
                }
                repeat++;
                gain.spawn(player.getLocation());
                bleed.spawn(entity.getLocation());

                entity.damage(scaling);
                VersionSupport.heal(player, scaling);
                if (repeat > duration) cancel();
            }
        }.runTaskTimer(SkillsPro.get(), 0, 10L);
    }

    @Override
    public Object[] applyEdits(SkilledPlayer info) {
        return new Object[]{"%chance%", getExtraScaling(info, "chance")};
    }
}
