package org.skills.abilities.juggernaut;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.managers.LastHitManager;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.EntityUtil;
import org.skills.utils.MathUtils;

import java.util.HashSet;
import java.util.Set;

public class JuggernautChainSmash extends ActiveAbility {
    private static final Set<Integer> PERFORMING = new HashSet<>();
    private static final String CHAIN_SMASH = "CHAIN_SMASH";

    static {
        addDisposableHandler(PERFORMING);
    }

    public JuggernautChainSmash() {
        super("Juggernaut", "chain_smash", true);
    }

    @SuppressWarnings("SameParameterValue")
    private static void explosionWave(JavaPlugin plugin, double rate, ParticleDisplay display, ParticleDisplay secDisplay) {
        new BukkitRunnable() {
            final double addition = Math.PI * 0.1;
            final double rateDiv = Math.PI / rate;
            double times = Math.PI / 4;

            public void run() {
                times += addition;
                for (double theta = 0; theta <= Math.PI * 2; theta += rateDiv) {
                    double x = times * Math.cos(theta);
                    double y = 2 * Math.exp(-0.1 * times) * Math.sin(times) + 1.5;
                    double z = times * Math.sin(theta);
                    display.spawn(x, y, z);

                    theta = theta + Math.PI / 64;
                    x = times * Math.cos(theta);
                    //y = 2 * Math.exp(-0.1 * times) * Math.sin(times) + 1.5;
                    z = times * Math.sin(theta);
                    secDisplay.spawn(x, y, z);
                }

                if (times > 20) cancel();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void tntDmaage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof TNTPrimed)) return;
        if (!(event.getEntity() instanceof Player)) return;
        Entity tnt = event.getDamager();
        if (!tnt.hasMetadata(CHAIN_SMASH)) return;

        Player player = (Player) tnt.getMetadata(CHAIN_SMASH).get(0).value();
        if (player == event.getEntity()) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFall(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getEntity() instanceof Player &&
                PERFORMING.remove(event.getEntity().getEntityId())) event.setCancelled(true);
    }

    @Override
    protected void useSkill(Player player) {
        SkilledPlayer info = activeCheckup(player);
        if (info == null) return;
        player.setVelocity(new Vector(0, 1, 0));
        XSound.ENTITY_HORSE_JUMP.play(player, 3, 0);
        ParticleDisplay cloud = new ParticleDisplay(Particle.CLOUD, player.getLocation(), 100, 1, 1, 1);
        cloud.spawn();
        PERFORMING.add(player.getEntityId());

        double scaling = getScaling(info);
        double range = getExtraScaling(info, "range");
        double launch = getExtraScaling(info, "launch");

        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            player.setVelocity(new Vector(0, -3, 0));

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isFlying()) return;
                    Location loc = player.getLocation();

                    // If they're not on the ground yet.
                    if (!MathUtils.isInteger(loc.getY())) return;

                    cancel();
                    cloud.spawn(loc);
                    TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
                    tnt.setMetadata(CHAIN_SMASH, new FixedMetadataValue(SkillsPro.get(), player));
                    tnt.setFuseTicks(0);

                    XSound.ENTITY_GENERIC_EXPLODE.play(player, (float) range, XSound.DEFAULT_PITCH);
                    ParticleDisplay display = new ParticleDisplay(Particle.EXPLOSION_LARGE, null, 10, 1, 1, 1);
                    explosionWave(SkillsPro.get(), 20, ParticleDisplay.simple(loc, Particle.FIREWORKS_SPARK),
                            ParticleDisplay.simple(loc, Particle.SPELL_WITCH));

                    for (Entity entity : player.getNearbyEntities(range, range, range)) {
                        if (EntityUtil.isInvalidEntity(entity)) continue;
                        if (!ServiceHandler.canFight(entity, player)) continue;

                        LastHitManager.damage((LivingEntity) entity, player, scaling);
                        entity.setVelocity(new Vector(0, launch, 0));
                        display.spawn(entity.getLocation());
                    }

                    PERFORMING.remove(player.getEntityId());
                }
            }.runTaskTimer(SkillsPro.get(), 1L, 1L);
        }, 20L);
    }
}
