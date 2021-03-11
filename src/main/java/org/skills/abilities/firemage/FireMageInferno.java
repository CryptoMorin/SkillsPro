package org.skills.abilities.firemage;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.utils.MathUtils;

import java.util.HashSet;
import java.util.Set;

public class FireMageInferno extends ActiveAbility {
    private static final String NO_SPREAD = "NO_SPREAD";

    public FireMageInferno() {
        super("FireMage", "inferno", false);
    }

    private static void spreadFire(Location location, int range) {
        if (range == 0) return;
        Block floor = location.getBlock().getRelative(BlockFace.DOWN);
        Material fire = XMaterial.FIRE.parseMaterial();

        Set<Block> blocks = new HashSet<>();
        for (int i = -range; i < range; i++) {
            for (int j = -range; j < range; j++) {
                Block block = floor.getRelative(i, 0, j);
                if (!XBlock.isAir(block.getType())) {
                    for (int k = -2; k < 4; k++) {
                        Block upper = block.getRelative(0, k, 0);
                        if (XBlock.isAir(upper.getType())) {
                            block = upper;
                            break;
                        }
                    }
                }

                if (!MathUtils.hasChance(70)) continue;
                if (XBlock.isAir(block.getType())) {
                    block.setType(fire);
                    block.setMetadata(NO_SPREAD, new FixedMetadataValue(SkillsPro.get(), null));
                    blocks.add(block);
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            for (Block block : blocks) {
                block.removeMetadata(NO_SPREAD, SkillsPro.get());
                if (block.getType() == fire) block.setType(Material.AIR);
            }
        }, 20 * 10);
    }

    @EventHandler
    public void spread(BlockSpreadEvent event) {
        Block source = event.getSource();
        if (source.hasMetadata(NO_SPREAD)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireMageAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.activeCheckup(player);
        if (info == null) return;
        Entity entity = event.getEntity();

        int lvl = info.getImprovementLevel(this);
        double scaling = this.getScaling(info);
        double damage = scaling + event.getEntity().getFireTicks() / 20D;

        event.setDamage(event.getDamage() + damage);
        player.getWorld().playEffect(entity.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        if (entity instanceof Player) ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 8, 0));
        XSound.BLOCK_LAVA_POP.play(entity);

        if (lvl > 1) {
            player.setFireTicks((int) (player.getFireTicks() + (scaling * 20)));
            spreadFire(player.getLocation(), (int) getExtraScaling(info, "range"));
            if (lvl > 2) XParticle.helix(SkillsPro.get(), lvl, 1, 0.1, 1, 3, 1, false, true, ParticleDisplay.simple(entity.getLocation(), Particle.FLAME));
        }
    }
}