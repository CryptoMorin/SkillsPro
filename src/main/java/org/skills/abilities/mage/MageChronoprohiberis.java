package org.skills.abilities.mage;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
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
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.services.manager.ServiceHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MageChronoprohiberis extends ActiveAbility {
    private static final String META = "Chronoprohiberis";

    public MageChronoprohiberis() {
        super("Mage", "chronoprohiberis", true);
    }

    @Override
    public void useSkill(Player player) {
        SkilledPlayer info = this.activeCheckup(player);
        if (info == null) return;

        Set<LivingEntity> entities = new HashSet<>();
        double range = getExtraScaling(info, "range");
        int duration = (int) getExtraScaling(info, "duration") * 20;
        double damage = getExtraScaling(info, "damage");
        List<PotionEffect> effects = getEffects(info, "effects");
        ParticleDisplay display = ParticleDisplay.simple(null, Particle.SMOKE_LARGE);
        display.withCount(50).offset(0.5, 0.5, 0.5);
        Set<EntityType> blacklisted = getEntityList(info, "blacklisted");

        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entity.getType() == EntityType.ARMOR_STAND) continue;
            LivingEntity livingEntity = (LivingEntity) entity;
            if (livingEntity.isInvulnerable()) continue;
            if (blacklisted.contains(livingEntity.getType())) continue;

            if (livingEntity instanceof Player) {
                Player entityPlayer = (Player) livingEntity;
                GameMode mode = entityPlayer.getGameMode();
                if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) continue;
                if (!ServiceHandler.canFight(player, livingEntity)) continue;
            }

            entities.add(livingEntity);
            livingEntity.setMetadata(META, new FixedMetadataValue(SkillsPro.get(), null));
            if (!(livingEntity instanceof Player)) livingEntity.setAI(false);
            livingEntity.damage(damage, player);
            livingEntity.addPotionEffects(effects);

            display.spawn(livingEntity.getEyeLocation());
            XSound.ENCHANT_THORNS_HIT.play(livingEntity, 3.0f, 0.5f);
        }

        if (entities.isEmpty()) return;

        display.particle = Particle.CLOUD;
        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            for (LivingEntity entity : entities) {
                entity.removeMetadata(META, SkillsPro.get());
                if (!(entity instanceof Player)) entity.setAI(true);
                display.spawn(entity.getEyeLocation());
                XSound.BLOCK_BEACON_DEACTIVATE.play(entity);
            }
        }, duration);
    }

    @Override
    public Object[] applyEdits(SkilledPlayer info) {
        return new Object[]{"%damage%", translate(info, "damage"), "%duration%", translate(info, "duration"), "%range%", translate(info, "range")};
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