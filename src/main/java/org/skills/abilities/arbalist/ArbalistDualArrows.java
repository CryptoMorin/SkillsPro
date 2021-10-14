package org.skills.abilities.arbalist;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.utils.MathUtils;

public class ArbalistDualArrows extends Ability {
    public ArbalistDualArrows() {
        super("Arbalist", "dual_arrows");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (isInvalidTarget(event.getEntity())) return;
        Entity arrow = event.getDamager();

        if (!(arrow instanceof Arrow)) return;
        if (arrow.hasMetadata(ArbalistPassive.ARBALIST_ARROW)) return;
        if (!(((Arrow) arrow).getShooter() instanceof Player)) return;

        Player player = (Player) ((Arrow) event.getDamager()).getShooter();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        double damage = this.getScaling(info, "damage", event);
        event.setDamage(event.getDamage() + damage);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onArrowLaunch(ProjectileLaunchEvent event) {
        Projectile arrow = event.getEntity();
        if (!(arrow instanceof Arrow)) return;
        if (!(arrow.getShooter() instanceof Player)) return;

        Player player = (Player) arrow.getShooter();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        if (!MathUtils.hasChance((int) getScaling(info, "chance"))) return;

        Vector velocity = arrow.getVelocity();
        velocity.add(new Vector(MathUtils.rand(-0.5, 0.5), MathUtils.rand(-0.5, 0.5), MathUtils.rand(-0.5, 0.5)));
//        double speed = getExtraScaling(info, "speed");
        // (vel.getX() > 4.0D) || (vel.getX() < -4.0D) || (vel.getY() > 4.0D) || (vel.getY() < -4.0D) || (vel.getZ() > 4.0D) || (vel.getZ() < -4.0D)))
//        velocity.multiply(speed);
//        double x = velocity.getX(), y = velocity.getY(), z = velocity.getZ();
//        velocity = new Vector(x > 0 ? Math.min(x, 4) : Math.max(x, -4), y > 0 ? Math.min(y, 4) : Math.max(y, -4), z > 0 ? Math.min(z, 4) : Math.max(z, -4));

        Arrow extraArrow = player.launchProjectile(Arrow.class, velocity);
        extraArrow.setFireTicks((int) getScaling(info, "fire"));
        extraArrow.setMetadata(ArbalistPassive.ARBALIST_ARROW, new FixedMetadataValue(SkillsPro.get(), null));
    }
}
