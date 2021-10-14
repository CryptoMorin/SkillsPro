package org.skills.abilities.mage;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.utils.MathUtils;
import org.spigotmc.event.entity.EntityDismountEvent;

public class MageNeptune extends Ability {
    private static final String NEPTUNE = "NEPTUNE";

    public MageNeptune() {
        super("Mage", "neptune");
    }

    @EventHandler(ignoreCancelled = true)
    public void onTridentLaunch(ProjectileLaunchEvent event) {
        if (!XMaterial.supports(13)) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Trident)) return;
        Trident trident = (Trident) entity;

        ProjectileSource shooter = trident.getShooter();
        if (!(shooter instanceof Player)) return;
        Player player = (Player) shooter;

        SkilledPlayer info = checkup(player);
        if (info == null) return;
        if (info.getAbilityLevel(this) < 3) return;

        if (player.getInventory().getItemInOffHand().getType() == Material.TRIDENT && player.isSneaking()) {
            trident.addPassenger(player);
            trident.setMetadata(NEPTUNE, new FixedMetadataValue(SkillsPro.get(), null));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDismount(EntityDismountEvent event) {
        if (event.getDismounted().hasMetadata(NEPTUNE)) {
            event.setCancelled(true);
            event.getDismounted().removeMetadata(NEPTUNE, SkillsPro.get());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrident(EntityDamageByEntityEvent event) {
        if (!XMaterial.supports(13)) return;
        if (!(event.getDamager() instanceof Trident)) return;

        ProjectileSource shooter = ((Trident) event.getDamager()).getShooter();
        if (!(shooter instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) shooter;
        SkilledPlayer info = checkup(player);
        if (info == null) return;

        event.setDamage(event.getDamage() + getScaling(info, "damage", event));
        LivingEntity entity = (LivingEntity) event.getEntity();

        if (MathUtils.hasChance((int) getScaling(info, "chances.lightning", event))) {
            player.getWorld().strikeLightning(entity.getLocation());
        }
        if (MathUtils.hasChance((int) getScaling(info, "chances.multiply", event))) {
            new BukkitRunnable() {
                int repeat = (int) getScaling(info, "multiply");

                @Override
                public void run() {
                    if (!entity.isValid()) cancel();
                    Location center = entity.getLocation().clone().add(0, 10, 0);
                    int x = MathUtils.randInt(0, 10);
                    int z = MathUtils.randInt(0, 10);

                    Location fire = center.clone().add(x, 0, z);
                    Location to = entity.getEyeLocation();

                    Vector vector = to.toVector().subtract(fire.toVector());
                    center.getWorld().spawnParticle(Particle.CLOUD, fire, 100, 0.5, 0.5, 0.5, 0);
                    Trident trident = (Trident) center.getWorld().spawnEntity(fire, EntityType.TRIDENT);
                    trident.setVelocity(vector.multiply(0.5));
                    if (repeat-- <= 0) cancel();
                }
            }.runTaskTimer(SkillsPro.get(), 10L, 10L);
        }
    }

    // TODO What to do?
    public Object[] applyEdits(SkilledPlayer info) {
        return new Object[]{
                "%chances_lightning%", getScalingDescription(info, getOptions(info).getString("chances.lightning")),
                "%chances_multiply%", getScalingDescription(info, getOptions(info).getString("chances.multiply")),
                "%multiply%", getScalingDescription(info, getOptions(info).getString("multiply"))};
    }
}
