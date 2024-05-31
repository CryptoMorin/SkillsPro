package org.skills.abilities.vampire;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XTag;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.types.SkillScaling;

import java.util.concurrent.TimeUnit;

public class VampirePassive extends Ability {
    public VampirePassive() {
        super("Vampire", "passive");
    }

    @Override
    public void start() {
        addTask(new BukkitRunnable() {
            final Cache<Integer, Integer> SUN = Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build();

            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isInvulnerable()) continue;

                    GameMode mode = player.getGameMode();
                    if (mode == GameMode.SPECTATOR || mode == GameMode.CREATIVE) continue;

                    World world = player.getWorld();
                    if (world.getEnvironment() != World.Environment.NORMAL) continue;

                    long time = world.getTime();
                    if (!(time < 12300 || time > 23850)) continue;

                    SkilledPlayer info = checkup(player);
                    if (info == null) continue;

                    ItemStack helment = player.getInventory().getHelmet();
                    if (helment != null) {
                        XMaterial xmat = XMaterial.matchXMaterial(helment);
                        if (XTag.anyMatchString(xmat, getOptions(info, "light-level.prevents").getStringList())) return;
                    }

                    int burnLevel = (int) getScaling(info, "light-level.burn-activation");
                    if (burnLevel <= 0) continue;
                    int lightning = player.getEyeLocation().getBlock().getLightFromSky();
                    lightning -= 7;

                    int finalLight = lightning;
                    int max = SUN.asMap().compute(player.getEntityId(), (a, b) -> b == null ? 1 : Math.max(Math.min(burnLevel, b + finalLight), 0));

                    if (max >= burnLevel) {
                        Bukkit.getScheduler().runTask(SkillsPro.get(), () -> {
                            int burn = (int) getScaling(info, "light-level.burn");
                            player.setFireTicks(burn * 20);
                            applyEffects(info, "light-level.burn", player);
                        });
                    }
                }
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 20, 10));
    }

    @EventHandler(ignoreCancelled = true)
    public void onVampireAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player p = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(p);
        if (info == null) return;

        double blood = info.getEnergy();
        int damage = (int) ((blood / getScaling(info, "blood", event)) * this.getScaling(info, "damage", event));

        event.setDamage(event.getDamage() + damage);
        info.chargeEnergy(info.getScaling(SkillScaling.ENERGY_REGEN) / 2);
    }
}
