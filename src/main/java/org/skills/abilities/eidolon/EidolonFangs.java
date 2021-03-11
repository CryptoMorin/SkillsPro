package org.skills.abilities.eidolon;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BlockIterator;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.managers.LastHitManager;
import org.skills.services.manager.ServiceHandler;

import java.util.Iterator;

public class EidolonFangs extends ActiveAbility {
    private static final String FANGS = "EIDOLON_FANGS";

    public EidolonFangs() {
        super("Eidolon", "fangs", true);
    }

    @Override
    protected void useSkill(Player player) {
        SkilledPlayer info = activeCheckup(player);
        if (info == null) return;
        int amount = (int) getExtraScaling(info, "fangs");

        EntityType type = XMaterial.supports(11) ? EntityType.EVOKER_FANGS : EntityType.FIREBALL;
        ParticleDisplay display = new ParticleDisplay(Particle.DRAGON_BREATH, null, 20, 1, 1, 1);

        if (player.isSneaking()) {
            for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof LivingEntity && entity.isValid() && entity.getType() != EntityType.ARMOR_STAND) {
                    Location loc = entity.getLocation();
                    Entity entityFang = player.getWorld().spawnEntity(loc, type);

                    entityFang.setMetadata(FANGS, new FixedMetadataValue(SkillsPro.get(), null));
                    if (type != EntityType.FIREBALL) {
                        EvokerFangs fangs = (EvokerFangs) entityFang;
                        fangs.setOwner(player);
                    }
                    display.spawn(loc);
                }
            }

            return;
        }

        Iterator<Block> blocks = new BlockIterator(player, amount);
        boolean isNew = XMaterial.isNewVersion();
        while (blocks.hasNext()) {
            Block block = blocks.next();
            Block corrected = null;
            if (block.getRelative(BlockFace.DOWN).getType().isSolid()) corrected = block;
            else {
                for (int i = 0; i > -5; i--) {
                    Block newBlock = block.getRelative(0, i, 0);
                    boolean isSolid = newBlock.getRelative(BlockFace.DOWN).getType().isSolid();
                    if (isNew) {
                        if (newBlock.isPassable() && isSolid) {
                            corrected = newBlock;
                            break;
                        }
                    } else {
                        if (!newBlock.getType().isSolid() && isSolid) {
                            corrected = newBlock;
                            break;
                        }
                    }
                    if (isSolid) break;
                }
                if (corrected == null) {
                    for (int i = 0; i < 5; i++) {
                        Block newBlock = block.getRelative(0, i, 0);
                        boolean isSolid = newBlock.getRelative(BlockFace.DOWN).getType().isSolid();

                        if (isNew) {
                            if (newBlock.isPassable() && isSolid) {
                                corrected = newBlock;
                                break;
                            }
                        } else {
                            if (!newBlock.getType().isSolid() && isSolid) {
                                corrected = newBlock;
                                break;
                            }
                        }
                    }
                }
            }
            if (corrected == null) corrected = player.getLocation().getBlock();
            Location loc = corrected.getLocation();

            Entity entity = player.getWorld().spawnEntity(loc, type);
            entity.setMetadata(FANGS, new FixedMetadataValue(SkillsPro.get(), null));
            if (type != EntityType.FIREBALL) {
                EvokerFangs fangs = (EvokerFangs) entity;
                fangs.setOwner(player);
            }
            display.spawn(loc);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFangsBite(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        Entity damager = event.getDamager();
        if (!damager.getType().name().endsWith("FANGS")) return;
        if (!damager.hasMetadata(FANGS)) return;
        EvokerFangs fangs = (EvokerFangs) damager;

        event.setCancelled(true);
        if (!ServiceHandler.canFight(fangs.getOwner(), event.getEntity())) return;

        Player player = (Player) fangs.getOwner();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        ParticleDisplay display = ParticleDisplay.colored(damager.getLocation(), 255, 0, 0, 1);

        display.spawn();
        LastHitManager.damage((LivingEntity) event.getEntity(), player, getScaling(info));
    }
}
