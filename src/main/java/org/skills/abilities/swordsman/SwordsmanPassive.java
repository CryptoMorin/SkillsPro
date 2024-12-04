package org.skills.abilities.swordsman;

import com.cryptomorin.xseries.*;
import com.cryptomorin.xseries.particles.XParticle;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.NMSExtras;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.utils.EntityUtil;
import org.skills.utils.MathUtils;

import java.util.HashMap;
import java.util.Map;

public class SwordsmanPassive extends Ability {
    public static final Map<Integer, EntityDamageByEntityEvent> OFFHAND = new HashMap<>();

    public SwordsmanPassive() {
        super("Swordsman", "passive");
    }

    protected static boolean isSword(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return isSword(XMaterial.matchXMaterial(item));
    }

    protected static boolean isSword(XMaterial material) {
        switch (material) {
            case NETHERITE_SWORD:
            case DIAMOND_SWORD:
            case GOLDEN_SWORD:
            case IRON_SWORD:
            case STONE_SWORD:
            case WOODEN_SWORD:
                return true;
            default:
                return false;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onOffhandInvalidate(EntityDamageByEntityEvent event) {
        OFFHAND.remove(event.getEntity().getEntityId());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        OFFHAND.remove(event.getPlayer().getEntityId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSwordsmanAttack(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;

        Player player = (Player) event.getDamager();
        if (!isSword(player)) return;

        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        event.setDamage(event.getDamage() + this.getScaling(info, "damage", event));
    }

    @EventHandler
    public void onDuelWieldInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(SkillsPro.get(), () -> {
            Player player = event.getPlayer();
            SkilledPlayer info = this.checkup(player);
            if (info == null) return;

            XMaterial match = XMaterial.matchXMaterial(item);
            if (!XTag.anyMatchString(match, getOptions(info, "weapons").getStringList())) return;

            NMSExtras.animation(player.getWorld().getPlayers(), player, NMSExtras.Animation.SWING_OFF_HAND);
            if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                if (getOptions(info, "cooldown").getBoolean()) {
                    int cooldown = (int) (1 / player.getAttribute(XAttribute.ATTACK_SPEED.get()).getValue() * 20);
                    Bukkit.getScheduler().runTask(SkillsPro.get(), () -> player.setCooldown(item.getType(), cooldown));
                }
            }
        });
    }

    @EventHandler
    public void onDuelWieldAttack(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND) return;
        Entity entity = event.getRightClicked();
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity livingEntity = (LivingEntity) entity;

        if (livingEntity.isInvulnerable() || !livingEntity.isValid()) return;
        if (livingEntity instanceof Player && ((Player) livingEntity).getGameMode() == GameMode.CREATIVE) return;
        if (livingEntity.getNoDamageTicks() > livingEntity.getMaximumNoDamageTicks() / 2) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInOffHand();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        XMaterial match = XMaterial.matchXMaterial(item);
        if (!XTag.anyMatchString(match, getOptions(info, "weapons").getStringList())) return;

        double damage = player.getAttribute(XAttribute.ATTACK_DAMAGE.get()).getValue();
        int sharpness = item.getEnchantmentLevel(XEnchantment.SHARPNESS.getEnchant());
        if (sharpness != 0) damage += 0.5 * (sharpness - 1) + 1;

        double armor = livingEntity.getAttribute(XAttribute.ARMOR.get()).getValue();
        double toughness = livingEntity.getAttribute(XAttribute.ARMOR_TOUGHNESS.get()).getValue();
        damage = damage * (1 - ((Math.min(20, Math.max(armor / 5, armor - (damage / (2 + (toughness / 4)))))) / 25));

        NMSExtras.animation(player.getWorld().getPlayers(), player, NMSExtras.Animation.SWING_OFF_HAND);

        EntityDamageByEntityEvent damageEvent;
        if (XReflection.supports(20, 5)) {
            damageEvent = new EntityDamageByEntityEvent(player, entity,
                    EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                    DamageSource.builder(DamageType.PLAYER_ATTACK).withCausingEntity(player).withDirectEntity(player).build(),
                    damage
            );
        } else {
            damageEvent = new EntityDamageByEntityEvent(player, entity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage);
        }
        Bukkit.getPluginManager().callEvent(damageEvent);
        if (damageEvent.isCancelled()) return;
        OFFHAND.put(entity.getEntityId(), damageEvent);
        livingEntity.damage(damageEvent.getDamage());

        int cooldown = 0;
        if (getOptions(info, "cooldown").getBoolean()) {
            cooldown = player.getCooldown(item.getType());
            if (cooldown != 0) damage /= cooldown;

            double attackSpeed = player.getAttribute(XAttribute.ATTACK_SPEED.get()).getValue();
            if (attackSpeed >= 20) player.setCooldown(item.getType(), (int) (20 - attackSpeed));
        }

        boolean sweep = cooldown == 0 && !player.isSprinting();
        if (sweep) {
            int sweeping = item.getEnchantmentLevel(XEnchantment.SWEEPING_EDGE.getEnchant());
            int sweepDmg = 1;
            if (sweeping != 0) sweepDmg += MathUtils.percentOfAmount(50 + ((sweeping - 1) * 25), damage);
            for (Entity nearby : livingEntity.getNearbyEntities(1, 1, 1)) {
                if (nearby.getEntityId() == player.getEntityId()) continue;
                if (EntityUtil.isInvalidEntity(nearby)) continue;
                LivingEntity nearbyEntity = (LivingEntity) nearby;
                nearbyEntity.damage(sweepDmg, player);
            }
        }
        if (sweep) {
            player.spawnParticle(XParticle.SWEEP_ATTACK.get(), entity.getLocation().clone().add(0, 0.75, 0), 1);
            XSound.ENTITY_PLAYER_ATTACK_SWEEP.play(player.getLocation());
        }
        player.spawnParticle(XParticle.DAMAGE_INDICATOR.get(), entity.getLocation().clone().add(0, 0.75, 0.5),
                MathUtils.randInt(1, 4), 0.1, 0.1, 1.0, 0.2);

        if (item.getDurability() + 1 < item.getType().getMaxDurability())
            item.setDurability((short) (item.getDurability() + 1));
        else {
            XSound.ENTITY_ITEM_BREAK.play(player.getLocation());
            player.getInventory().setItemInOffHand(null);
        }

        double kb = item.getEnchantmentLevel(XEnchantment.KNOCKBACK.getEnchant()) + 1 * 0.5;
        Vector knockback = player.getLocation().getDirection().add(new Vector(0, 0.7, 0)).multiply(kb);
        entity.setVelocity(knockback);

        int fire = item.getEnchantmentLevel(XEnchantment.FIRE_ASPECT.getEnchant());
        if (fire != 0) entity.setFireTicks(fire * 80);
    }
}
