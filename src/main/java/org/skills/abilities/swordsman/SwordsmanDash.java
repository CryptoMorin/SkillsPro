package org.skills.abilities.swordsman;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.services.manager.ServiceHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SwordsmanDash extends ActiveAbility {
    public SwordsmanDash() {
        super("Swordsman", "dash", true);
    }

    @Override
    protected void useSkill(Player player) {
        SkilledPlayer info = activeCheckup(player);
        if (info == null) return;

        new BukkitRunnable() {
            final ParticleDisplay display = ParticleDisplay.simple(null, Particle.CLOUD).withCount(10).offset(0.5, 0.2, 0.5);
            int repeat = 20;

            @Override
            public void run() {
                display.spawn(player.getLocation());
                if (--repeat == 0) cancel();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0, 1);

        int lvl = info.getImprovementLevel(SwordsmanDash.this);
        int kbLvl = (int) getExtraScaling(info, "charge.level");

        if (lvl >= kbLvl) {
            new BukkitRunnable() {
                final List<PotionEffect> effects = getEffects(info, "effects");
                final double range = getExtraScaling(info, "range");
                final double kb = getExtraScaling(info, "charge.knockback");
                final double damage = getExtraScaling(info, "charge.damage");
                final ParticleDisplay masterDisplay = ParticleDisplay.simple(null, Particle.SWEEP_ATTACK).withCount(10).offset(1, 1, 1);
                final Set<Integer> targets = new HashSet<>();
                int repeat = 20;

                @Override
                public void run() {
                    for (Entity entity : player.getNearbyEntities(range, range, range)) {
                        if (!(entity instanceof LivingEntity)) continue;
                        if (entity.getType() == EntityType.ARMOR_STAND) continue;
                        LivingEntity livingEntity = (LivingEntity) entity;
                        if (livingEntity.isInvulnerable() || livingEntity.isDead()) continue;
                        if (!targets.add(entity.getEntityId())) continue;

                        if (livingEntity instanceof Player) {
                            Player targetPlayer = (Player) livingEntity;
                            GameMode mode = targetPlayer.getGameMode();
                            if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) continue;
                            if (!ServiceHandler.canFight(player, targetPlayer)) continue;
                        }

                        livingEntity.addPotionEffects(effects);
                        livingEntity.setVelocity(livingEntity.getVelocity().subtract(player.getVelocity()).multiply(kb));
                        livingEntity.damage(damage, player);

                        Location loc = livingEntity.getLocation();
                        XSound.ENTITY_PLAYER_ATTACK_SWEEP.play(loc, 3f, 1.0f);
                        masterDisplay.spawn(loc);
                    }
                    if ((repeat -= 2) == 0) cancel();
                }
            }.runTaskTimer(SkillsPro.get(), 0, 2);
        }

        int verticalSlashLvl = (int) getExtraScaling(info, "vertical-slash-level");
        Vector dir = player.getLocation().getDirection();
        Vector direction = new Vector(dir.getX(), (lvl >= verticalSlashLvl ? dir.getY() : 0), dir.getZ()).normalize();
        double mod = getScaling(info);
        player.setVelocity(direction.multiply(mod));
    }


    @Override
    public Object[] applyEdits(SkilledPlayer info) {
        return new Object[]{"%damage%", translate(info, "charge.damage"), "%knockback%", translate(info, "charge.knockback"), "%range%", translate(info, "range")};
    }
}