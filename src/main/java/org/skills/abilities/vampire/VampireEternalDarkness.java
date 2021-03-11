package org.skills.abilities.vampire;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.utils.LocationUtils;
import org.skills.utils.MinecraftTime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class VampireEternalDarkness extends ActiveAbility {
    private final HashSet<UUID> eternalDarkness = new HashSet<>();
    private final HashMap<UUID, Integer> activeTimerCount = new HashMap<>();

    public VampireEternalDarkness() {
        super("Vampire", "eternal_darkness", false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onVampireAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        Player p = (Player) event.getDamager();
        SkilledPlayer info = this.activeCheckup(p);
        if (info == null) return;

        eternalDarkness.add(p.getUniqueId());
        activeTimerCount.put(event.getEntity().getUniqueId(), 0);
        int time = (int) this.getScaling(info);
        int lvl = info.getImprovementLevel(this);

        boolean isPlayer = event.getEntity() instanceof Player;
        new BukkitRunnable() {
            public void run() {
                int i = activeTimerCount.get(event.getEntity().getUniqueId());
                if (i < time) {
                    i++;
                    activeTimerCount.put(event.getEntity().getUniqueId(), i);
                } else {
                    eternalDarkness.remove(p.getUniqueId());
                    sendMessage(p, getAbilityFinished(info));
                    cancel();
                }
            }
        }.runTaskTimer(SkillsPro.get(), 0L, 20L);

        if (isPlayer && lvl > 0) {
            new BukkitRunnable() {
                MinecraftTime timeSet = MinecraftTime.NOON;
                int repeat = lvl * 2;

                @Override
                public void run() {
                    Player target = ((Player) event.getEntity());
                    if (!target.isValid()) {
                        cancel();
                        return;
                    }

                    timeSet.setTime(target);
                    if (repeat-- == 0) {
                        target.resetPlayerTime();
                        cancel();
                        return;
                    }

                    if (timeSet == MinecraftTime.NOON) timeSet = MinecraftTime.MIDNIGHT;
                    else timeSet = MinecraftTime.NOON;
                }
            }.runTaskTimer(SkillsPro.get(), 0L, 15L);
        }
        if (lvl > 2) {
            ParticleDisplay display = new ParticleDisplay(Particle.SPELL, event.getEntity().getLocation(), 100, 1, 1, 1);
            display.spawn();
            LocationUtils.rotate(event.getEntity(), 10, true, true, 100);
        }

        if (eternalDarkness.contains(p.getUniqueId())) {
            ((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVampireDamaged(EntityDamageEvent event) {
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;
        if (eternalDarkness.contains(event.getEntity().getUniqueId())) event.setDamage(0);
    }
}
