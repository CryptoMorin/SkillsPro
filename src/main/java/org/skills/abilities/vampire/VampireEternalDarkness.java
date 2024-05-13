package org.skills.abilities.vampire;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
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
        super("Vampire", "eternal_darkness");
    }

    @EventHandler(ignoreCancelled = true)
    public void onVampireAttack(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;
        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        eternalDarkness.add(player.getUniqueId());
        activeTimerCount.put(event.getEntity().getUniqueId(), 0);
        int time = (int) this.getScaling(info, "duration", event);
        int lvl = info.getAbilityLevel(this);

        boolean isPlayer = event.getEntity() instanceof Player;
        new BukkitRunnable() {
            public void run() {
                int i = activeTimerCount.get(event.getEntity().getUniqueId());
                if (i < time) {
                    i++;
                    activeTimerCount.put(event.getEntity().getUniqueId(), i);
                } else {
                    eternalDarkness.remove(player.getUniqueId());
                    sendMessage(player, getAbilityFinished(info));
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
            ParticleDisplay.of(XParticle.WITCH).withLocation(event.getEntity().getLocation()).withCount(100).offset(1).spawn();
            LocationUtils.rotate(event.getEntity(), 10, true, true, 100);
        }

        if (eternalDarkness.contains(player.getUniqueId())) {
            ((LivingEntity) event.getEntity()).addPotionEffect(XPotion.BLINDNESS.buildPotionEffect(40, 1));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVampireDamaged(EntityDamageEvent event) {
        if (eternalDarkness.contains(event.getEntity().getUniqueId())) event.setDamage(0);
    }
}
