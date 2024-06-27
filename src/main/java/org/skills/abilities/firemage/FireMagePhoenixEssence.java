package org.skills.abilities.firemage;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.XTag;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.Particles;
import com.cryptomorin.xseries.particles.XParticle;
import com.cryptomorin.xseries.reflection.XReflection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.abilities.mage.MagePassive;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.managers.DamageManager;
import org.skills.utils.Cooldown;
import org.skills.utils.EntityUtil;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FireMagePhoenixEssence extends InstantActiveAbility {
    private static final Map<Integer, AtomicBoolean> ACITVATED = new HashMap<>();
    private static final Team GLOW_TEAM;
    private static final boolean SUPPORTS_GLOW = XReflection.of(Entity.class)
            .method("void setGlowing(boolean glowing);").exists();
    private static final boolean SUPPORTS_PERSISTENCE = XReflection.of(Entity.class)
            .method("void setPersistent(boolean persistent);").exists();

    private static final Set<XMaterial> IGNITE_MATERIALS = EnumSet.of(XMaterial.TALL_GRASS, XMaterial.SHORT_GRASS);

    static {
        IGNITE_MATERIALS.addAll(XTag.AIR.getValues());
        IGNITE_MATERIALS.addAll(XTag.FLOWERS.getValues());
    }

    static {
        addDisposableHandler(ACITVATED);

        if (SUPPORTS_GLOW) {
            // It persists between restarts...
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
            Team team = board.getTeam("SkillsProPhoenixEss");
            if (team == null) GLOW_TEAM = board.registerNewTeam("SkillsProPhoenixEss");
            else GLOW_TEAM = team;
        } else {
            GLOW_TEAM = null;
        }
    }

    public FireMagePhoenixEssence() {
        super("FireMage", "phoenix_essence");
    }

    public static void forwardSlash(double distance, ParticleDisplay display) {
        new BukkitRunnable() {
            double limit = 0;
            private final Location loc = display.getLocation().clone();

            @Override
            public void run() {
//                Particles.ellipse(
//                        0, Math.PI,
//                        Math.PI / 30,
//                        3, 4,
//                        display
//                );

                for (double theta = 0; theta <= Math.PI; theta += Math.PI / 30) {
                    double x = 3 * Math.cos(theta);
                    double z = 3 * Math.sin(theta);
                    // Location spawnAt = rotate(display, display.getLocation(), x, 0, z);
                    Vector local = new Vector(x, 0, z);
                    Location spawnAt = display.getLocation().clone();
                    ParticleDisplay.Quaternion rot = ParticleDisplay.Quaternion.rotation(loc.getYaw(), new Vector(0, 1, 0));
                    rot = rot.mul(ParticleDisplay.Quaternion.rotation(-loc.getPitch(), new Vector(1, 0, 0)));
                    rot = rot.mul(ParticleDisplay.Quaternion.rotation(45, new Vector(0, 0, 1)));
                    Vector vec = ParticleDisplay.Quaternion.rotate(local, rot);
                    spawnAt = spawnAt.add(vec);
                    display.spawn(spawnAt);
                }

                if (limit++ >= distance) cancel();
                else display.advanceInDirection(0.1);
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 1L, 1L);
    }

    public static BukkitTask volcano(int times, long ticks, double radius, ParticleDisplay display) {
        return new BukkitRunnable() {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            int count = times;

            @Override
            public void run() {
                display.offset(
                        random.nextDouble(-radius, radius),
                        random.nextDouble(0.5, 1),
                        random.nextDouble(-radius, radius)
                ).spawn();
                if (count-- <= 0) cancel();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, ticks);
    }

    private static void thunderTunnel(Location location, double step, Runnable end) {
        new BukkitRunnable() {
            int count = 5;

            @Override
            public void run() {
                Particles.circle(0.7, 30, ParticleDisplay.of(XParticle.FLAME).withLocation(location.add(0, step, 0))
                        .directional().withExtra(.07).offset(0, 0, -0.03));
                float pitch = 1f;

                if (count-- == 0) {
                    cancel();
                    pitch = 2f;
                    Bukkit.getScheduler().runTask(SkillsPro.get(), end);
                }

                XSound.BLOCK_CONDUIT_DEACTIVATE.or(XSound.ENTITY_ENDERMAN_TELEPORT)
                        .record().withVolume(10).withPitch(pitch)
                        .soundPlayer().atLocation(location).play();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 5L);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        Player player = event.getPlayer();
        AtomicBoolean state = ACITVATED.get(player.getEntityId());
        if (state == null) return;
        if (!MagePassive.isHoe(player.getInventory().getItemInMainHand())) return;

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        int lvl = info.getAbilityLevel(this);

        if (event.getAction() == Action.LEFT_CLICK_AIR && lvl >= getScaling(info, "levels.slash")) {
            if (Cooldown.isInCooldown(player.getUniqueId(), "FIREMAGE_SLASH")) return;

            ParticleDisplay display = ParticleDisplay.of(XParticle.SOUL_FIRE_FLAME.or(XParticle.FLAME)).withLocation(player.getEyeLocation());
            AtomicInteger i = new AtomicInteger();
            double zRot = Math.toRadians(45);
            if (state.getAndSet(!state.get())) {
                zRot = -zRot;
                new Cooldown(player.getUniqueId(), "FIREMAGE_SLASH", (long) getScaling(info, "cooldown.slash"), TimeUnit.SECONDS);
            }
            double slashDamage = getScaling(info, "damage.slash");

            display.face(player)
                    .rotate(0, 0, zRot)
                    //.rotationOrder(ParticleDisplay.Axis.X, ParticleDisplay.Axis.Z, ParticleDisplay.Axis.Y)
                    .postCalculation(ctx -> {
                        if (i.incrementAndGet() == 5) {
                            i.set(0);
                            Location loc = ctx.getLocation();
                            Bukkit.getScheduler().runTask(SkillsPro.get(), () -> {
                                for (Entity entity : loc.getWorld().getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                                    if (EntityUtil.filterEntity(player, entity)) continue;
                                    DamageManager.damage((LivingEntity) entity, player, slashDamage);
                                }
                            });
                        }
                    });

            forwardSlash(getScaling(info, "distance.slash"), display);
            playSound(player, info, "slash");
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR && lvl >= getScaling(info, "levels.volcano")) {
            if (Cooldown.isInCooldown(player.getUniqueId(), "FIREMAGE_VOLCANO")) return;
            new Cooldown(player.getUniqueId(), "FIREMAGE_VOLCANO", (long) getScaling(info, "cooldown.volcano"), TimeUnit.SECONDS);

            Location location = player.getLocation();
            double throwUpRadius = getScaling(info, "radius.volcano");
            new BukkitRunnable() {
                static final double radius = 1;
                final double maxDistance = getScaling(info, "distance.volcano");
                final double throwForce = getScaling(info, "knockback.volcano");
                final Vector directionIgnorePitch = location.getDirection().setY(0).normalize();
                final double lastY = location.getY();
                final Vector zipZag = ParticleDisplay.rotateAround(directionIgnorePitch.clone(), ParticleDisplay.Axis.Y, Math.PI / 2);
                double distance = 1;
                boolean odd;

                @Override
                public void run() {
                    double offset = radius * ((odd = !odd) ? -1 : 1);
                    double volcanoDamage = getScaling(info, "damage.volcano");
                    Location loc = location.clone()
                            .add(directionIgnorePitch.clone().multiply(distance))
                            .add(zipZag.clone().multiply(offset));
                    loc.setY(lastY);

                    Block block = null;
                    int blockRetries = 8; // 4 down 4 up
                    while (blockRetries > 0) {
                        block = loc.getBlock();
                        if (IGNITE_MATERIALS.contains(XMaterial.matchXMaterial(block.getType()))) break;
                        else {
                            block = null;
                            if (blockRetries == 8) loc = loc.add(0, -4, 0);
                            else loc = loc.add(0, 1, 0);
                        }
                        blockRetries--;
                    }

                    volcano(20, 1, 0.1, ParticleDisplay.of(XParticle.FLAME).withLocation(loc).directional().withExtra(1));
                    ParticleDisplay.of(XParticle.LAVA).withLocation(loc).directional().withExtra(1).spawn();
                    playSound(player, info, "volcano");

                    Block finalBlock = block;
                    Location finalLoc = loc;
                    Bukkit.getScheduler().runTask(SkillsPro.get(), () -> {
                        if (finalBlock != null) finalBlock.setType(Material.FIRE);
                        for (Entity entity : player.getWorld().getNearbyEntities(finalLoc, throwUpRadius, throwUpRadius, throwUpRadius)) {
                            if (EntityUtil.filterEntity(player, entity)) continue;
                            entity.setFireTicks(5 * 20);
                            entity.setVelocity(new Vector(0, throwForce, 0));
                            DamageManager.damage((LivingEntity) entity, player, volcanoDamage);
                        }
                    });

                    if ((distance += 2) >= maxDistance) cancel();
                }
            }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 3L);
        }
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        playSound(player, context.getInfo(), "music");

        new BukkitRunnable() {
            static final double rand = 10;
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            int count = 100;

            @Override
            public void run() {
                Location location = player.getLocation().add(
                        random.nextDouble(-rand, rand),
                        random.nextDouble(-1, rand),
                        random.nextDouble(-rand, rand)
                );
                ParticleDisplay.of(XParticle.FLAME).withCount(30).offset(5).withLocation(location).spawn();
                if (count-- == 0) {
                    cancel();
                    ACITVATED.put(player.getEntityId(), new AtomicBoolean());
                    Bukkit.getScheduler().runTask(SkillsPro.get(), () -> start(context));
                }
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 1L, 1L);
    }

    private void start(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();

        int lvl = info.getAbilityLevel(this);
        double radius = getScaling(info, "radius.initial");
        double lightningRadius = getScaling(info, "radius.lightning");
        double initialDamage = getScaling(info, "damage.initial");
        double knockback = getScaling(info, "knockback.initial");

        Particles.sphere(3, 40, ParticleDisplay
                .of(XParticle.SOUL_FIRE_FLAME.or(XParticle.FLAME))
                .withLocation(player.getLocation())
                .directional().withExtra(0.5).offset(0.5));

        Location loc = player.getLocation();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (EntityUtil.filterEntity(player, entity)) continue;
            if (entity instanceof Player)
                ParticleDisplay.of(XParticle.FLASH).withLocation(null).onlyVisibleTo((Player) entity).spawn(entity.getLocation());

            DamageManager.damage((LivingEntity) entity, player, initialDamage);
            EntityUtil.knockBack(entity, loc, knockback);
        }

        XSound.ENTITY_WARDEN_ROAR.or(XSound.ENTITY_SKELETON_HORSE_DEATH).play(loc);
        SkeletonHorse horse = (SkeletonHorse) player.getWorld().spawnEntity(player.getLocation(), EntityType.SKELETON_HORSE);

        // The docs says: You cannot set a jump strength to a value below 0 or above 2.
        // Well, that's a fucking lie.
        horse.setJumpStrength(3.0);
        horse.getInventory().setSaddle(XMaterial.SADDLE.parseItem());
        // horse.getInventory().setItem(1, XMaterial.GOLDEN_HORSE_ARMOR.parseItem()); they can't have this
        horse.setPassenger(player);
        horse.setInvulnerable(true);
        horse.setRemoveWhenFarAway(true);
        if (SUPPORTS_PERSISTENCE) horse.setPersistent(false);

        // Visuals
        horse.setFireTicks(100_000);
        horse.setArrowsInBody(10);
        if (SUPPORTS_GLOW) {
            horse.setGlowing(true);
            GLOW_TEAM.setColor(ChatColor.DARK_RED);
            GLOW_TEAM.addEntry(horse.getUniqueId().toString());
        }

        // Owner
        horse.setDomestication(horse.getMaxDomestication());
        horse.setOwner(player);
        horse.setTamed(true);

        applyEffects(info, "horse-effects", horse);

        BukkitTask thunderTask;
        if (lvl >= getScaling(info, "levels.lightning")) {
            thunderTask = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Entity entity : player.getNearbyEntities(lightningRadius, lightningRadius, lightningRadius)) {
                        if (entity == horse) continue;
                        if (EntityUtil.filterEntity(player, entity)) continue;
                        if (entity instanceof Player)
                            ParticleDisplay.of(XParticle.FLASH).withLocation(null).onlyVisibleTo((Player) entity).spawn(entity.getLocation());
                        entity.setFireTicks(10 * 20);
                        Location loc = entity.getLocation();
                        thunderTunnel(loc.clone(), 1, () -> {
                            entity.getWorld().strikeLightning(loc);
                            XSound.ENTITY_LIGHTNING_BOLT_THUNDER.play(loc);
                        });
                    }
                }
            }.runTaskTimer(SkillsPro.get(), 5 * 20L, 5 * 20L);
        } else thunderTask = null;

        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            ACITVATED.remove(player.getEntityId());
            if (thunderTask != null) thunderTask.cancel();
            horse.setHealth(0);

            XSound.Record record = getSound(info, "music");
            if (record != null) {
                float volume = record.getVolume();
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, volume, volume, volume)) {
                    if (entity instanceof Player) record.soundPlayer().forPlayers((Player) entity).stopSound();
                }
            }

            playSound(player, info, "end");
        }, (long) (getScaling(info, "duration") * 20L));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!ACITVATED.containsKey(event.getEntity().getEntityId())) return;

        Particles.circle(2, 30, ParticleDisplay.of(XParticle.FLAME)
                .directional().withExtra(1)
                .withLocation(event.getEntity().getLocation()));
        event.setCancelled(true);
    }
}