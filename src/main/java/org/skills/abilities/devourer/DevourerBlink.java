package org.skills.abilities.devourer;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.utils.Cooldown;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class DevourerBlink extends ActiveAbility {
    private static final Map<UUID, Integer> HITS = new HashMap<>();

    public DevourerBlink() {
        super("Devourer", "blink");
    }

    private static boolean safeTp(Entity entity, Location location) {
        if (XMaterial.supports(13)) {
            if (location.getBlock().isPassable() && location.getBlock().getRelative(BlockFace.UP).isPassable()) {
                entity.teleport(location);
                return true;
            }
        } else {
            if (!location.getBlock().getType().isSolid() && !location.getBlock().getRelative(BlockFace.UP).getType().isSolid()) {
                entity.teleport(location);
                return true;
            }
        }
        return false;
    }

    private static Location getBack(Location entity) {
        float nang = entity.getYaw() + 90;
        if (nang < 0) nang += 360;
        double nX = Math.cos(Math.toRadians(nang));
        double nZ = Math.sin(Math.toRadians(nang));
        return new Location(entity.getWorld(), entity.getX() - nX, entity.getY(), entity.getZ() - nZ, entity.getYaw(), entity.getPitch());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDevourerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        Player player = (Player) event.getDamager();
        int hits = HITS.getOrDefault(player.getUniqueId(), 0);
        if (hits == -1 || (hits != 0 && !Cooldown.isInCooldown(player.getUniqueId(), "BLINK"))) {
            HITS.remove(player.getUniqueId());
            return;
        }

        SkilledPlayer info;
        if (hits == 0) info = this.checkup(player);
        else info = SkilledPlayer.getSkilledPlayer(player);
        if (info == null) return;

        hits++;
        int lvl = info.getAbilityLevel(this);
        Entity entity = event.getEntity();
        ParticleDisplay.simple(player.getLocation(), Particle.CLOUD).offset(1).withCount(100).spawn();

        player.addPotionEffect(XPotion.SPEED.getPotionEffectType().createEffect(20 * 10, 1));
        XSound.ENTITY_ENDERMAN_TELEPORT.play(player);
        int maxHits = (int) getScaling(info, "hits");

        if (lvl == 1) {
            safeTp(player, getBack(entity.getLocation()));
            if (hits > maxHits) hits = -1;
        } else if (lvl == 2) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            double x = 2 * Math.cos(random.nextDouble(0, Math.PI * 2));
            double z = 2 * Math.sin(random.nextDouble(0, Math.PI * 2));
            Location circle = entity.getLocation().clone().add(x, 0, z);
            circle.setDirection(entity.getLocation().toVector().subtract(circle.toVector()));
            safeTp(player, circle);
            if (hits > maxHits) hits = -1;

        } else {
            ThreadLocalRandom random = ThreadLocalRandom.current();

            double radius = random.nextDouble(0, 3);
            double x = radius * Math.cos(random.nextDouble(0, Math.PI * 2));
            double y = random.nextDouble(0.5, 2);
            double z = radius * Math.sin(random.nextDouble(0, Math.PI * 2));
            Location circle = entity.getLocation().clone().add(x, y, z);

            circle.setDirection(entity.getLocation().toVector().subtract(circle.toVector()));
            safeTp(player, circle);
            if (hits > maxHits) hits = -1;
        }

        if (hits != -1) new Cooldown(player.getUniqueId(), "BLINK", 2 * 1000 + lvl * 500L, TimeUnit.MILLISECONDS);
        HITS.put(player.getUniqueId(), hits);
    }
}
