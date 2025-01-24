package org.skills.abilities.arbalist;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.XParticle;
import com.cryptomorin.xseries.reflection.XReflection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.utils.MathUtils;

public class ArbalistPassive extends Ability {
    public static final String ARBALIST_ARROW = "ARBALIST";

    public ArbalistPassive() {
        super("Arbalist", "passive");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getDamager();
        if (arrow.getShooter() == null || !(arrow.getShooter() instanceof Player)) return;
        if (arrow.hasMetadata(ARBALIST_ARROW)) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        Entity entity = event.getEntity();
        Player shooter = (Player) arrow.getShooter();
        SkilledPlayer info = this.checkup(shooter);
        if (info == null) return;

        double distance = shooter.getLocation().distance(event.getEntity().getLocation());
        event.setDamage(event.getDamage() + (distance / 5));
        World world = entity.getWorld();

        if (!MathUtils.hasChance((int) getScaling(info, "chance"))) return;
        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            Location center = entity.getLocation().add(0, 10, 0);
            world.spawnParticle(XParticle.CLOUD.get(), center, 200, 1, 0, 1, 0);

            for (int i = 0; i < getScaling(info, "arrows"); i++) {
                int x = MathUtils.randInt(0, i);
                int z = MathUtils.randInt(0, i);

                Location fire = center.clone().add(x, 0, z);
                Location to = entity.getLocation().add(x, 0, z);

                Vector vector = to.toVector().subtract(fire.toVector());
                Arrow rain = (Arrow) world.spawnEntity(fire, EntityType.ARROW);
                rain.setShooter(shooter);
                rain.setMetadata(ARBALIST_ARROW, new FixedMetadataValue(SkillsPro.get(), null));
                if (MathUtils.hasChance((int) getScaling(info, "flame-chance"))) rain.setFireTicks(200);
                rain.setVelocity(vector.multiply(0.1));

                if (XReflection.supports(14)) rain.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
            }
        }, 10L);
    }
}
