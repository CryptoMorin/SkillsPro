package org.skills.abilities.swordsman;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.utils.MathUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SwordsmanThousandCuts extends ActiveAbility {
    /**
     * No need for disposable handler. Timer takes care of it.
     */
    private static final Map<Integer, Integer> activeTrapcount = new HashMap<>(), thousandcuts = new HashMap<>();
    private static final Map<Integer, Location> activeLocation = new HashMap<>();

    public SwordsmanThousandCuts() {
        super("Swordsman", "thousand_cuts", false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwordsmanAttack(EntityDamageByEntityEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info;

        Integer activeTarget = thousandcuts.get(player.getEntityId());
        if (activeTarget == null) {
            info = this.activeCheckup(player);
        } else {
            info = SkilledPlayer.getSkilledPlayer(player);
        }
        if (info == null) return;
        LivingEntity entity = (LivingEntity) event.getEntity();
        int lvl = info.getImprovementLevel(this);

        if (activeTarget != null && activeTarget == entity.getEntityId()) {
            event.setDamage(event.getDamage() + this.getScaling(info));
            Color color = lvl == 1 ? Color.CYAN : lvl == 2 ? Color.ORANGE : Color.RED;
            ParticleDisplay display = ParticleDisplay.colored(null, color.getRed(), color.getGreen(), color.getBlue(), 1.0f);

            for (int i = MathUtils.randInt(1, lvl + 2); i > 0; i--) {
                Location start = entity.getEyeLocation().add(MathUtils.rand(-3, 3), MathUtils.rand(-0.5, 2), MathUtils.rand(-3, 3));
                Vector endVect = entity.getEyeLocation().subtract(start).toVector().multiply(MathUtils.rand(1.2, 1.8));
                Location end = start.clone().add(endVect);
                XParticle.line(start, end, 0.1, display);
            }
        }

        thousandcuts.put(player.getEntityId(), event.getEntity().getEntityId());
        activeTrapcount.put(entity.getEntityId(), 0);
        activeLocation.put(entity.getEntityId(), entity.getLocation());

        new BukkitRunnable() {
            public void run() {
                Integer i = activeTrapcount.getOrDefault(entity.getEntityId(), null);
                if (i == null) return;
                if (i < getExtraScaling(info, "cut-count") && entity.isValid()) {
                    activeTrapcount.put(entity.getEntityId(), i + 1);

                    //disorienter
                    Location activeLocationOrienter = activeLocation.get(entity.getEntityId());
                    Location loc = new Location(activeLocationOrienter.getWorld(),
                            activeLocationOrienter.getX(),
                            activeLocationOrienter.getY(),
                            activeLocationOrienter.getZ(),
                            MathUtils.randInt(0, 360), MathUtils.randInt(-90, 90));
                    entity.teleport(loc);
                } else {
                    thousandcuts.remove(player.getEntityId());
                    activeTrapcount.remove(entity.getEntityId());
                    activeLocation.remove(entity.getEntityId());

                    sendMessage(player, getAbilityFinished(info));
                    cancel();
                }
            }
        }.runTaskTimer(SkillsPro.get(), 0L, 5L);
    }
}