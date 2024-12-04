package org.skills.abilities.swordsman;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.Particles;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.utils.Cooldown;
import org.skills.utils.EntityUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SwordsmanAnnihilation extends InstantActiveAbility {
    private static final String META = "Annihilation";
    private static final XMaterial[] SWORDS;
    private static final Map<Integer, Runnable> CANCELS = new HashMap<>();

    static {
        List<XMaterial> swords = new ArrayList<>(Arrays.asList(
                XMaterial.WOODEN_SWORD, XMaterial.STONE_SWORD, XMaterial.IRON_SWORD,
                XMaterial.GOLDEN_SWORD, XMaterial.DIAMOND_SWORD));
        if (XMaterial.supports(16)) swords.add(XMaterial.NETHERITE_SWORD);
        SWORDS = swords.toArray(new XMaterial[0]);
    }

    public SwordsmanAnnihilation() {
        super("Swordsman", "annihilation");
    }

    private static double distance(Location i, Location o) {
        World world = o.getWorld();
        if (!world.getName().equals(i.getWorld().getName())) return Double.MAX_VALUE;
        double distance = NumberConversions.square(i.getX() - o.getX()) +
                NumberConversions.square(i.getY() - o.getY()) + NumberConversions.square(i.getZ() - o.getZ());
        return Math.sqrt(distance);
    }

    private static void cancelAnnihilation(Player player) {
        Runnable task = CANCELS.remove(player.getEntityId());
        if (task != null) task.run();
    }

    @EventHandler
    public void onAttack(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.LEFT_CLICK_AIR) return;

        Player player = event.getPlayer();
        if (Cooldown.isInCooldown(player.getUniqueId(), META)) return;

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (!info.getActiveAbilities().contains(this)) return;
        new Cooldown(player.getUniqueId(), META, (long) getScaling(info, "throw.cooldown"), TimeUnit.SECONDS);

        ParticleDisplay remove = ParticleDisplay.of(XParticle.CLOUD).withLocation(null).offset(0.1).withCount(10);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Set<Item> items = ConcurrentHashMap.newKeySet();
        XSound.ENTITY_WARDEN_SONIC_BOOM.or(XSound.ENTITY_WITHER_SHOOT)
                .play(player.getLocation(), 3.0f, 0.1f);

        for (XMaterial sword : SWORDS) {
            Location loc = player.getEyeLocation().add(random.nextDouble(-2, 2), random.nextDouble(0.5, 2), random.nextDouble(-2, 2));
            Item item = player.getWorld().dropItem(loc, sword.parseItem());
            item.setGravity(false);
            item.setPickupDelay(Integer.MAX_VALUE);
            remove.spawn(loc);
            items.add(item);
        }
        addAllEntities(items);

        Location from = player.getEyeLocation();
        Vector direction = player.getLocation().getDirection();
        direction.normalize();

        new BukkitRunnable() {
            final double xDir = Math.toRadians(from.getPitch() + 90);
            final double yDir = Math.toRadians(-from.getYaw());
            final double dist = Particles.PII / items.size();
            final ParticleDisplay display = ParticleDisplay.of(XParticle.DRAGON_BREATH);
            double rate = 1;
            double radius = 1;
            boolean reverse = false;
            double theta = 0;

            @Override
            public void run() {
                double sep = 0;
                Vector middle = direction.clone().multiply(rate);
                Location mid = from.clone().add(middle);
                new BukkitRunnable() {
                    final double damage = getScaling(info, "throw.damage");
                    final ParticleDisplay sweep = ParticleDisplay.of(XParticle.SWEEP_ATTACK).withLocation(null).offset(0.3).withCount(5);

                    @Override
                    public void run() {
                        Set<Integer> mobs = new HashSet<>();
                        for (Entity entity : mid.getWorld().getNearbyEntities(mid, 3, 3, 3)) {
                            if (player == entity) continue;
                            if (mobs.contains(entity.getEntityId())) continue;
                            if (EntityUtil.filterEntity(player, entity)) continue;

                            LivingEntity livingEntity = (LivingEntity) entity;
                            livingEntity.damage(damage, player);
                            XSound.ENTITY_PLAYER_ATTACK_SWEEP.play(player, 5f, 0.5f);
                            sweep.spawn(livingEntity.getEyeLocation());
                            mobs.add(entity.getEntityId());
                        }
                    }
                }.runTask(SkillsPro.get());

                for (Item sword : items) {
                    double x = radius * Math.cos(theta + sep);
                    double y = radius * Math.sin(theta + sep);

                    Vector vector = new Vector(x, 0, y);
                    ParticleDisplay.rotateAround(vector, xDir, yDir, 0);
                    vector = middle.clone().add(vector);
                    Vector finale = from.toVector().add(vector);
                    sword.setVelocity(finale.subtract(sword.getLocation().toVector()));
                    display.spawn(sword.getLocation());
                    sep += dist;
                }

                if (reverse) {
                    radius -= 0.05;
                    if (radius <= 1) reverse = false;
                } else {
                    radius += 0.05;
                    if (radius >= 2) reverse = true;
                }

                theta += Math.PI / 20;
                rate += 0.5;
                if (rate > 20) {
                    cancel();
                    Bukkit.getScheduler().runTask(SkillsPro.get(), () -> {
                        for (Item item : items) {
                            item.remove();
                            removeEntity(item);
                        }
                    });
                }
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 1L);
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();
        info.setActiveAbilitiy(this, true);

        ParticleDisplay remove = ParticleDisplay.of(XParticle.CLOUD).withLocation(null).offset(0.1, 0.1, 0.1).withCount(10);
        List<Item> items = new CopyOnWriteArrayList<>(); // Swords are added one by one, causes looping issues later
        new BukkitRunnable() {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            int i = 0;

            @Override
            public void run() {
                XMaterial mat = SWORDS[i];

                Location loc = player.getEyeLocation().add(random.nextDouble(-2, 2), random.nextDouble(0.5, 2), random.nextDouble(-2, 2));
                remove.spawn(loc);
                Item item = player.getWorld().dropItem(loc, mat.parseItem());
                item.setGravity(false);
                item.setPickupDelay(Integer.MAX_VALUE);
                items.add(item);
                addEntity(item);

                if (++i == SWORDS.length) cancel();
            }
        }.runTaskTimer(SkillsPro.get(), 1, 20L);

        long duration = (long) getScaling(info, "duration") * 20L;
        XSound.MUSIC_DISC_FAR.play(player, 10f, 0.5f);
        BukkitTask curl = Particles.blackhole(SkillsPro.get(), 5, 2, 30, 1, (int) duration, ParticleDisplay.of(XParticle.FLAME).withLocation(player.getLocation()).withEntity(player));
        Map<Integer, Integer> fighting = new HashMap<>();
        Set<Integer> mobs = new HashSet<>();

        final double pi = Math.PI;
        List<double[]> current = new ArrayList<>(Arrays.asList(new double[4], new double[]{0, pi / 2, 0, 0},
                new double[]{0, 0, 0, pi / 4}, new double[]{0, 0, 0, pi / 2}, new double[]{0, pi / 4, 0, 0},
                new double[]{0, pi / 4, pi / 2, pi / 4}));

        AtomicBoolean cancel = new AtomicBoolean();
        AtomicReference<Runnable> canceller = new AtomicReference<>(); // fuck java
        BukkitTask task = new BukkitRunnable() {
            static final double radius = 2;
            final double rateDiv = pi / 30;
            final double rotMin = pi / 100;
            final double rotMax = pi / 50;
            final ParticleDisplay display = ParticleDisplay.of(XParticle.WITCH);
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            long dur = duration;

            @Override
            public void run() {
                int i = 0;
                for (Entity item : items) {
                    if (fighting.containsValue(item.getEntityId())) continue;
                    double[] displayer = current.get(i);
                    double theta = displayer[0];

                    double x = radius * Math.cos(theta);
                    double z = radius * Math.sin(theta);
                    Vector vector = new Vector(x, 0, z);

                    ParticleDisplay.rotateAround(vector, displayer[1], displayer[2], displayer[3]);
                    item.setVelocity(player.getEyeLocation().toVector().add(vector).subtract(item.getLocation().toVector()));

                    display.spawn(item.getLocation());
                    displayer[0] += rateDiv;
                    displayer[1] += random.nextDouble(rotMin, rotMax);
                    displayer[2] += random.nextDouble(rotMin, rotMax);
                    displayer[3] += random.nextDouble(rotMin, rotMax);
                    i++;
                }

                if (--dur <= 0) canceller.get().run();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0, 1);

        BukkitTask trackerTask = new BukkitRunnable() {
            final double range = getScaling(info, "range");
            final double damage = getScaling(info, "damage");
            final double rateDiv = Math.PI / 20;

            final ParticleDisplay display = ParticleDisplay.of(XParticle.SOUL_FIRE_FLAME.or(XParticle.WITCH));
            final ParticleDisplay sweep = ParticleDisplay.of(XParticle.SWEEP_ATTACK).offset(0.3, 0.3, 0.3).withCount(5);

            @Override
            public void run() {
                if (fighting.size() >= SWORDS.length) return;
                for (Entity entity : player.getNearbyEntities(range, range, range)) {
                    if (player == entity) continue;
                    if (mobs.contains(entity.getEntityId())) continue;
                    if (EntityUtil.filterEntity(player, entity)) continue;

                    LivingEntity livingEntity = (LivingEntity) entity;
                    new BukkitRunnable() {
                        final ThreadLocalRandom random = ThreadLocalRandom.current();
                        double t = Math.PI / 2;
                        int dmg = 0;

                        @Override
                        public void run() {
                            if (!livingEntity.isValid() || cancel.get() || distance(player.getLocation(), livingEntity.getLocation()) > range) {
                                fighting.remove(this.getTaskId());
                                mobs.remove(entity.getEntityId());
                                cancel();
                                return;
                            }

                            // https://en.wikipedia.org/wiki/Butterfly_curve_(transcendental)
                            double cos = Math.cos(t);
                            double r = 2 * Math.cos(5 * t);// Math.exp(cos / 30) - 2 * Math.cos(5 * t);;
                            double x = r * Math.sin(t);
                            double y = r * cos;

                            Vector vector = new Vector(x, y, 0);
                            Vector original = livingEntity.getEyeLocation().toVector();

                            int itemId = fighting.getOrDefault(this.getTaskId(), 0);
                            for (Entity item : items) {
                                int id = item.getEntityId();
                                if (itemId != id) {
                                    if (itemId == 0 && !fighting.containsValue(id)) {
                                        fighting.put(this.getTaskId(), id);
                                        mobs.add(entity.getEntityId());
                                    } else continue;
                                }

                                Vector clone = original.clone();
                                ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.X, random.nextDouble(-Math.PI, Math.PI));
                                item.setVelocity(clone.add(vector).subtract(item.getLocation().toVector()));
                                display.spawn(item.getLocation());
                                break;
                            }

                            if (++dmg == 5) {
                                Bukkit.getScheduler().runTask(SkillsPro.get(), () -> livingEntity.damage(damage, player));
                                XSound.ENTITY_PLAYER_ATTACK_SWEEP.play(player, 5f, 0.5f);
                                sweep.spawn(livingEntity.getEyeLocation());
                                dmg = 0;
                            }
                            t += rateDiv;
                        }
                    }.runTaskTimerAsynchronously(SkillsPro.get(), 0, 1);
                }
            }
        }.runTaskTimer(SkillsPro.get(), 20, 20);


        canceller.set(() -> {
            curl.cancel();
            task.cancel();
            trackerTask.cancel();
            cancel.set(true);
            CANCELS.remove(player.getEntityId());
            player.stopSound(XSound.MUSIC_DISC_FAR.parseSound());

            Bukkit.getScheduler().runTask(SkillsPro.get(), () -> {
                for (Entity item : items) {
                    item.remove();
                    remove.spawn(item.getLocation());
                    removeEntity(item);
                }
            });
            XSound.BLOCK_BEACON_DEACTIVATE.play(player.getLocation(), 10f, 0.5f);
            info.setActiveAbilitiy(this, false);
        });
        CANCELS.put(player.getEntityId(), canceller.get());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTp(PlayerTeleportEvent event) {
        cancelAnnihilation(event.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        cancelAnnihilation(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        cancelAnnihilation(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onHoppeerSwordPickup(InventoryPickupItemEvent event) {
        if (isSkillEntity(event.getItem())) event.setCancelled(true);
    }
}