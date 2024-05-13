package org.skills.abilities.mage;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.utils.EntityUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MageChronoprohiberis extends InstantActiveAbility {
    private static final String META = "Chronoprohiberis";

    public MageChronoprohiberis() {
        super("Mage", "chronoprohiberis");
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();

        Set<LivingEntity> entities = new HashSet<>();
        double range = getScaling(info, "range");
        int duration = (int) getScaling(info, "duration") * 20;
        double damage = getScaling(info, "damage");
        List<PotionEffect> effects = getEffects(info, "effects");
        ParticleDisplay display = ParticleDisplay.of(XParticle.LARGE_SMOKE).withCount(50).offset(.5);
        Set<EntityType> blacklisted = getEntityList(info, "blacklisted");

        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (EntityUtil.filterEntity(player, entity)) continue;
            if (blacklisted.contains(entity.getType())) continue;

            LivingEntity livingEntity = (LivingEntity) entity;
            entities.add(livingEntity);
            livingEntity.setMetadata(META, new FixedMetadataValue(SkillsPro.get(), null));
            if (!(livingEntity instanceof Player)) livingEntity.setAI(false);
            livingEntity.damage(damage, player);
            livingEntity.addPotionEffects(effects);

            display.spawn(livingEntity.getEyeLocation());
            XSound.ENCHANT_THORNS_HIT.play(livingEntity, 3.0f, 0.5f);
        }

        if (entities.isEmpty()) return;

        display.withParticle(XParticle.CLOUD);
        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            for (LivingEntity entity : entities) {
                entity.removeMetadata(META, SkillsPro.get());
                if (!(entity instanceof Player)) entity.setAI(true);
                display.spawn(entity.getEyeLocation());
                XSound.BLOCK_BEACON_DEACTIVATE.play(entity);
            }
        }, duration);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getPlayer().hasMetadata(META)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAnimation(PlayerAnimationEvent event) {
        if (event.getPlayer().hasMetadata(META)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.getPlayer().hasMetadata(META)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (event.getPlayer().hasMetadata(META)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (event.getPlayer().hasMetadata(META)) event.setCancelled(true);
    }
}