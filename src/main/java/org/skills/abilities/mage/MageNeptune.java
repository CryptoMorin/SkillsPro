package org.skills.abilities.mage;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.managers.MoveManager;
import org.skills.utils.Cooldown;
import org.skills.utils.MathUtils;
import org.skills.utils.ParticleUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MageNeptune extends Ability {
    private static final String NEPTUNE = "NEPTUNE";
    private static final String NEPTUNE_AERIAL = "NEPTUNE_AERIAL";
    private static final String NEPTUNE_JUMP_FALL_DAMAGE = "NEPTUNE_FALL";
    private static final String NEPTUNE_MINION = "NEPTUNE_MINION";
    private static final Cache<UUID, Location> SAFE_LOCATION_FIX = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.SECONDS).build();

    static {
        MoveManager.registerPlayerHitGround(player -> {
            List<MetadataValue> meta = player.getMetadata(NEPTUNE_AERIAL);
            if (!meta.isEmpty()) {
                AtomicInteger aerials = (AtomicInteger) meta.get(0).value();
                aerials.set(0);
            }
        });
    }

    public MageNeptune() {
        super("Mage", "neptune");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTridentHit(ProjectileHitEvent event) {
        if (event.getEntity().hasMetadata(NEPTUNE_MINION)) {
            XSound.ITEM_TRIDENT_RETURN.record().withPitch(2f).soundPlayer().atLocation(event.getEntity().getLocation()).play();
            Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
                ParticleUtil.cloudParticle(event.getEntity().getLocation());
                event.getEntity().remove();
            }, 20 * 15);
        }

        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player shooter = (Player) event.getEntity().getShooter();

        SkilledPlayer info = checkup(shooter);
        if (info == null) return;
        if (info.getAbilityLevel(this) < 3) return;
        if (!event.getEntity().hasMetadata(NEPTUNE)) return;
        if (event.getHitBlock() != null) {
            Block block = event.getHitBlock();
            SAFE_LOCATION_FIX.put(shooter.getUniqueId(), block.getRelative(event.getHitBlockFace()).getLocation());
        }

        if (event.getEntity() instanceof Trident) {
            Trident trident = (Trident) event.getEntity();
            if (trident.getItem().containsEnchantment(XEnchantment.LOYALTY.getEnchant())) {
                // Setting the owner of a loyal trident as passenger will cause
                // the trident to chase the player indefinitely vertically up in the air.
                event.getEntity().removePassenger(event.getEntity().getPassenger());
            }
        }

        if (!MathUtils.hasChance((int) getScaling(info, "chances.lightning"))) return;

        Location hitLocation = event.getEntity().getLocation();
        LightningStrike lightning = hitLocation.getWorld().strikeLightning(hitLocation);
        lightning.setMetadata(NEPTUNE, new FixedMetadataValue(SkillsPro.get(), shooter));
        new Cooldown(shooter.getUniqueId(), NEPTUNE, 10, TimeUnit.SECONDS);
        XSound.ITEM_TRIDENT_THUNDER.record().withPitch(0.1f).soundPlayer().atLocation(hitLocation).play();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPick(PlayerPickupArrowEvent event) {
        // For some reasons this doesn't work well with EntityPickupItemEvent
        if (event.getArrow().hasMetadata(NEPTUNE)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onTridentLaunch(ProjectileLaunchEvent event) {
        if (!XMaterial.supports(13)) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof Trident)) return;
        Trident trident = (Trident) entity;

        ProjectileSource shooter = trident.getShooter();
        if (!(shooter instanceof Player)) return;
        Player player = (Player) shooter;

        SkilledPlayer info = checkup(player);
        if (info == null) return;
        if (info.getAbilityLevel(this) < 3) return;

        if (player.getInventory().getItemInOffHand().getType() == Material.TRIDENT && player.isSneaking()) {
            {
                List<MetadataValue> meta = player.getMetadata(NEPTUNE_AERIAL);
                AtomicInteger aerials;

                if (meta.isEmpty()) {
                    player.setMetadata(NEPTUNE_AERIAL, new FixedMetadataValue(SkillsPro.get(), aerials = new AtomicInteger(0)));
                } else {
                    aerials = (AtomicInteger) meta.get(0).value();
                }

                int maxAerials = (int) getScaling(info, "aerial-limit");
                if (aerials.addAndGet(1) > maxAerials) {
                    XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
                    event.setCancelled(true);
                    return;
                }
            }

            player.setSneaking(false);
            trident.addPassenger(player);
            trident.setMetadata(NEPTUNE, new FixedMetadataValue(SkillsPro.get(), player));
            XSound.ITEM_TRIDENT_RIPTIDE_3.record().withPitch(0.1f).soundPlayer().atLocation(trident.getLocation()).play();

            new BukkitRunnable() {
                int tenTickTimer = 10;
                final ParticleDisplay particle = ParticleDisplay.of(XParticle.DUST)
                        .withColor(Color.CYAN, 1).offset(0.3, 0.3, 0.3).withCount(10).withEntity(trident);

                @Override
                public void run() {
                    if (--tenTickTimer <= 0) {
                        tenTickTimer = 10;
                        // There is no sound that really fits here? It needs to be repeated smoothly.
                        // XSound.ITEM_TRIDENT_THUNDER.record().withPitch(0.1f).soundPlayer().atLocation(trident.getLocation()).play();
                    }

                    if (!trident.isValid()) cancel();
                    particle.spawn();
                }
            }.runTaskTimerAsynchronously(SkillsPro.get(), 1L, 1L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDismount(EntityDismountEvent event) {
        Entity trident = event.getDismounted();
        if (trident.hasMetadata(NEPTUNE)) {
            Trident tridentEntity = (Trident) trident;
            if (event.getEntity() instanceof Player) {
                Player playerRiding = (Player) event.getEntity();
                new Cooldown(playerRiding.getUniqueId(), NEPTUNE_JUMP_FALL_DAMAGE, 20, TimeUnit.SECONDS);

                SkilledPlayer info = SkilledPlayer.getSkilledPlayer(playerRiding);
                int lvl = info.getAbilityLevel(this);
                boolean spawnParticles = false;

                if (lvl >= getScaling(info, "jump.required-level")) {
                    spawnParticles = true;
                    double height = getScaling(info, "jump.height");
                    playerRiding.setVelocity(playerRiding.getLocation().getDirection().normalize().multiply(-1).setY(height));
                }
                List<Trident> stylishThrows = new CopyOnWriteArrayList<>();

                if (lvl >= getScaling(info, "minion-trident.required-level") && !Cooldown.isInCooldown(playerRiding.getUniqueId(), NEPTUNE_MINION)) {
                    spawnParticles = true;
                    new Cooldown(playerRiding.getUniqueId(), NEPTUNE_MINION, (long) getScaling(info, "minion-trident.cooldown"), TimeUnit.SECONDS);
                    XSound.ITEM_TRIDENT_RETURN.record().withPitch(0f).soundPlayer().atLocation(playerRiding.getLocation()).play();

                    Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
                        int amount = (int) getScaling(info, "minion-trident.trident-amount");
                        boolean even = MathUtils.isEven(amount);
                        int divided = (int) Math.floor(amount / 2.0);

                        List<Double> rotations = new ArrayList<>(amount);
                        if (!even) rotations.add(0.0);

                        for (int i = 1; i <= divided; i++) {
                            double factor;
                            if (amount <= 3) factor = 20;
                            else if (amount <= 5) factor = 10;
                            else factor = 5;

                            // The degree is distributed between the two tridents in the center.
                            if (i == 1 && even) factor /= 2;
                            else factor *= i;

                            rotations.add(Math.toRadians(factor));
                            rotations.add(-Math.toRadians(factor));
                        }

                        for (Double rotation : rotations) {
                            Vector tridentVelo = playerRiding.getLocation().getDirection().normalize();
                            Vector perpendicular = ParticleUtil.getPerpendicularVector(tridentVelo);
                            Vector rotated = ParticleDisplay.Quaternion.rotate(tridentVelo,
                                    ParticleDisplay.Quaternion.rotation(rotation, perpendicular));

                            Trident stylishTrident = (Trident) playerRiding.getWorld().spawnEntity(playerRiding.getEyeLocation().clone().add(tridentVelo.multiply(0.5)), EntityType.TRIDENT);
                            stylishTrident.setVelocity(rotated);
                            stylishTrident.setMetadata(NEPTUNE_MINION, new FixedMetadataValue(SkillsPro.get(), playerRiding));
                            stylishThrows.add(stylishTrident);
                        }
                    }, 5L);
                }

                if (spawnParticles) {
                    new BukkitRunnable() {
                        int ticks = 5 * 20;
                        final ParticleDisplay particle = ParticleDisplay.of(XParticle.DUST)
                                .withColor(Color.RED, 1).offset(0.3, 0.3, 0.3).withCount(10).withEntity(playerRiding);

                        @Override
                        public void run() {
                            if (--ticks <= 0) {
                                cancel();
                            }

                            if (stylishThrows.isEmpty()) {
                                particle.spawn();
                            } else {
                                for (Trident stylishThrow : stylishThrows) {
                                    if (!stylishThrow.isValid()) {
                                        cancel();
                                        return;
                                    }
                                    particle.spawn(stylishThrow.getLocation());
                                }
                            }
                        }
                    }.runTaskTimerAsynchronously(SkillsPro.get(), 1L, 1L);
                }

                if (playerRiding.getGameMode() != GameMode.CREATIVE) {
                    PlayerInventory inv = playerRiding.getInventory();
                    if (inv.getItemInOffHand().getType() == Material.AIR) inv.setItemInOffHand(tridentEntity.getItem());
                    else {
                        // https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/inventory/Inventory.html#addItem(org.bukkit.inventory.ItemStack...)
                        HashMap<Integer, ItemStack> leftOvers = inv.addItem(tridentEntity.getItem());
                        if (!leftOvers.isEmpty()) return; // Don't remove the trident if inv is full.
                    }
                }
            }
            trident.remove();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTridentLightningFire(EntityDamageEvent event) {
        UUID id = event.getEntity().getUniqueId();
        switch (event.getCause()) {
            case FIRE_TICK:
            case FIRE:
            case FALL:
                break;
            case SUFFOCATION:
                Location safeLoc = SAFE_LOCATION_FIX.getIfPresent(id);
                if (safeLoc != null) {
                    event.getEntity().teleport(safeLoc);
                    SAFE_LOCATION_FIX.invalidate(id);
                }
                break;
            default:
                return;
        }
        if (Cooldown.isInCooldown(id, NEPTUNE)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTridentHitLightning(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LightningStrike)) return;
        List<MetadataValue> metadata = event.getDamager().getMetadata(NEPTUNE);
        if (metadata.isEmpty()) return;
        Player shooter = (Player) metadata.get(0).value();
        if (event.getEntity() == shooter) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrident(EntityDamageByEntityEvent event) {
        if (!XMaterial.supports(13)) return;
        if (!(event.getDamager() instanceof Trident)) return;

        ProjectileSource shooter = ((Trident) event.getDamager()).getShooter();
        if (!(shooter instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) shooter;
        SkilledPlayer info = checkup(player);
        if (info == null) return;

        event.setDamage(event.getDamage() + getScaling(info, "damage", event));
        LivingEntity entity = (LivingEntity) event.getEntity();

        if (MathUtils.hasChance((int) getScaling(info, "chances.lightning", event))) {
            player.getWorld().strikeLightning(entity.getLocation());
        }
        if (MathUtils.hasChance((int) getScaling(info, "chances.multiply", event))) {
            new BukkitRunnable() {
                int repeat = (int) getScaling(info, "multiply");

                @Override
                public void run() {
                    if (!entity.isValid()) cancel();
                    Location center = entity.getLocation().clone().add(0, 10, 0);
                    int x = MathUtils.randInt(0, 10);
                    int z = MathUtils.randInt(0, 10);

                    Location fire = center.clone().add(x, 0, z);
                    Location to = entity.getEyeLocation();

                    Vector vector = to.toVector().subtract(fire.toVector());
                    center.getWorld().spawnParticle(XParticle.CLOUD.get(), fire, 100, 0.5, 0.5, 0.5, 0);
                    Trident trident = (Trident) center.getWorld().spawnEntity(fire, EntityType.TRIDENT);
                    trident.setVelocity(vector.multiply(0.5));
                    if (repeat-- <= 0) cancel();
                }
            }.runTaskTimer(SkillsPro.get(), 10L, 10L);
        }
    }

    // TODO What to do?
    public Object[] applyEdits(SkilledPlayer info) {
        return new Object[]{
                "%chances_lightning%", getScalingDescription(info, getOptions(info).getString("chances.lightning")),
                "%chances_multiply%", getScalingDescription(info, getOptions(info).getString("chances.multiply")),
                "%multiply%", getScalingDescription(info, getOptions(info).getString("multiply"))};
    }
}
