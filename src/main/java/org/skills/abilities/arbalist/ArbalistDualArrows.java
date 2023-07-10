package org.skills.abilities.arbalist;

import net.minecraft.util.MathHelper;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SLogger;
import org.skills.main.SkillsPro;
import org.skills.utils.MathUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ArbalistDualArrows extends Ability {
    public static final String COUNT = "ARBALIST_DUAL_COUNT", SOURCE = "ARBALIST_DUAL_SOURCE";

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

    public static Vector rotateAroundY(Vector vector, double angle) {

        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);
        double x = angleCos * vector.getX() + angleSin * vector.getZ();
        double z = -angleSin * vector.getX() + angleCos * vector.getZ();

        return vector.setX(x).setZ(z);
    }

    public static Vector arrow(Vector direction, double speed, double spread) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        double WTFNumber = 0.007499999832361937;
        return direction.normalize().add(new Vector(
                rand.nextGaussian() * WTFNumber * spread,
                rand.nextGaussian() * WTFNumber * spread,
                rand.nextGaussian() * WTFNumber * spread
//                rand.nextGaussian(0.0, 0.0172275 * spread),
//                rand.nextGaussian(0.0, 0.0172275 * spread),
//                rand.nextGaussian(0.0, 0.0172275 * spread)
        )).multiply(speed);

//        double WTHNumber = 57.2957763671875;
//        double d3 = Math.sqrt(direction.getX() * direction.getX() + direction.getZ() * direction.getZ());
//        this.yaw = (float)(MathHelper.d(direction.getX(), direction.getZ()) * WTHNumber);
//        this.pitch = (float)(MathHelper.d(direction.getY(), d3) * WTHNumber);
        //this.lastYaw = this.yaw;
        //this.lastPitch = this.pitch;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onArrowLaunch(ProjectileLaunchEvent event) {
        Projectile arrow = event.getEntity();
        if (!(arrow instanceof Arrow)) return;
        if (!(arrow.getShooter() instanceof Player)) return;

        Player player = (Player) arrow.getShooter();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;
        if (!arrow.getMetadata(ArbalistFireCrossbow.ARBALIST_FIRECROSSBOW).isEmpty()) return;
        if (!MathUtils.hasChance((int) getScaling(info, "chance"))) return;

//        int times = 1;
//        {
//            List<MetadataValue> arbalistMeta = arrow.getMetadata(COUNT);
//            if (!arbalistMeta.isEmpty()) times += arbalistMeta.get(0).asInt();
//            if (times >= getScaling(info, "limit")) return;
//        }
//
//        Entity src;
//        {
//            List<MetadataValue> arbalistMetaSrc = arrow.getMetadata(SOURCE);
//            if (!arbalistMetaSrc.isEmpty()) {
//                src = (Entity) arbalistMetaSrc.get(0).value();
//            } else src = arrow;
//        }

        double spread = getScaling(info, "spread");
        Vector velocity = arrow.getVelocity().clone();
//        Location loc = player.getLocation().clone();
//        loc.setYaw((float) (loc.getYaw() + MathUtils.rand(-10, 10)));
//        loc.setPitch((float) (loc.getPitch() + MathUtils.rand(-10, 10)));
//        Vector velocity = loc.getDirection();
//        ParticleDisplay.rotateAround(velocity,
//                MathUtils.rand(-spread, spread),
//                MathUtils.rand(-spread, spread),
//                MathUtils.rand(-spread, spread));
//        double speed = getExtraScaling(info, "speed");
        // (vel.getX() > 4.0D) || (vel.getX() < -4.0D) || (vel.getY() > 4.0D) || (vel.getY() < -4.0D) || (vel.getZ() > 4.0D) || (vel.getZ() < -4.0D)))
//        velocity.multiply(speed);
//        double x = velocity.getX(), y = velocity.getY(), z = velocity.getZ();
//        velocity = new Vector(x > 0 ? Math.min(x, 4) : Math.max(x, -4), y > 0 ? Math.min(y, 4) : Math.max(y, -4), z > 0 ? Math.min(z, 4) : Math.max(z, -4));

            float speed = (float) arrow.getVelocity().length();
        do {
            Arrow extraArrow = player.getWorld().spawnArrow(player.getEyeLocation(), velocity, speed, (float) spread); // player.launchProjectile(Arrow.class, velocity);
            extraArrow.setFireTicks((int) getScaling(info, "fire"));
            extraArrow.setMetadata(ArbalistPassive.ARBALIST_ARROW, new FixedMetadataValue(SkillsPro.get(), null));
//            extraArrow.setMetadata(COUNT, new FixedMetadataValue(SkillsPro.get(), times));
//            extraArrow.setMetadata(SOURCE, new FixedMetadataValue(SkillsPro.get(), src));
        } while (MathUtils.hasChance((int) getScaling(info, "chance")));
    }
}
