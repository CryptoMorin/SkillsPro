package org.skills.abilities.priest;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.utils.Cooldown;
import org.skills.utils.MathUtils;
import org.skills.utils.versionsupport.VersionSupport;

import java.util.concurrent.TimeUnit;

public class PriestSealOfLife extends Ability {
    private static final String SEAL_OF_LIFE = "SEAL_OF_LIFE";

    public PriestSealOfLife() {
        super("Priest", "seal_of_life");
    }

    private static Location generateRandomCoords(Location loc) {
        while (true) {
            double x = MathUtils.randInt(20, 50);
            double y = MathUtils.randInt(20, 50);
            double z = MathUtils.randInt(20, 50);

            Location random = loc.clone().add(x, y, z);
            Block block = random.getBlock();
            if (block.getType().name().endsWith("AIR")) continue;
            if (block.getRelative(BlockFace.UP).getType().name().endsWith("AIR")) continue;
            Block down = block.getRelative(BlockFace.DOWN);
            return random;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onNearDeathEscape(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;
        Player p = (Player) event.getEntity();

        SkilledPlayer info = this.checkup(p);
        if (info == null) return;

        if (!Cooldown.isInCooldown(p.getUniqueId(), SEAL_OF_LIFE)) {
            int percent = VersionSupport.getHealthPercent(p, event);
            if (percent > getScaling(info, "health", event)) return;

            event.setCancelled(true);
            if (XMaterial.supports(11)) p.playEffect(EntityEffect.TOTEM_RESURRECT);
            applyEffects(info, p);

            new BukkitRunnable() {
                int i = 30;

                @Override
                public void run() {
                    p.spawnParticle(Particle.VILLAGER_HAPPY, p.getLocation(), 50, 1, 1, 1, 0);
                    if (--i == 0) cancel();
                }
            }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 5L);
            new Cooldown(p.getUniqueId(), SEAL_OF_LIFE, (long) this.getScaling(info, "interval"), TimeUnit.SECONDS);
        }
    }
}
