package org.skills.abilities.vergil;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffectType;
import org.skills.abilities.Ability;
import org.skills.api.events.ClassChangeEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SLogger;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class VergilPassive extends Ability {
    // Concentration | 0.0 - 3.0
    public static final Map<UUID, BossBar> MOTIVATION_LEVELS = new HashMap<>();
    /**
     * https://minecraft.fandom.com/wiki/Damage#Attack_cooldown
     * There seems to be already a Paper API for this? {@link Player#hasCooldown(Material)} doesn't work for this tho.
     * Attack speed of 1.6 shown in game. It's 0.625 seconds. So for milliseconds it'd be:
     */
    private static final int SWORD_ATTACK_SPEED_MILLIS = 625;

    /**
     * We're going to put 600 anyway to compensate for lag.
     */
    private static final Cache<UUID, Long> LAST_ATTACK = CacheBuilder.newBuilder()
            .expireAfterAccess(SWORD_ATTACK_SPEED_MILLIS - 25, TimeUnit.MILLISECONDS).build();

    public static int getMotivationLevelFromProgress(double progress) {
        // Since 1/3 is inaccurate we'll just do it manually.
        if (progress >= 0.9) return 3;
        if (progress >= 0.6) return 2;
        if (progress >= 0.3) return 1;
        return 0;
    }

    public static int getMotivationLevel(Player player) {
        BossBar bossBar = MOTIVATION_LEVELS.get(player.getUniqueId());
        return bossBar == null ? -1 : getMotivationLevelFromProgress(bossBar.getProgress());
    }

    public VergilPassive() {
        super("Vergil", "passive");
    }

    static {
        // Potions
        Bukkit.getScheduler().runTaskTimer(SkillsPro.get(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                int motivation = getMotivationLevel(player);
            }
        }, 0L, 20L * 10L);

        ParticleDisplay display = ParticleDisplay.of(Particle.FLAME).offset(0.5).withExtra(0.5);
        // level 3 concentration particles
        Bukkit.getScheduler().runTaskTimerAsynchronously(SkillsPro.get(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                int motivation = getMotivationLevel(player);
                // directional particles from players spine?
                if (motivation >= 2) {
                    Location loc = player.getLocation();
                    for (double i = 0; i < player.getHeight(); i += 0.1) {
                        display.spawn(loc.clone().add(0, i, 0));
                    }
                }
            }
        }, 0L, 1L);
    }

    public void motivate(Player player, double amount) {
        if (amount == 0) return;

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        BossBar bossBar = MOTIVATION_LEVELS.get(player.getUniqueId());
        if (amount != Integer.MAX_VALUE && bossBar != null) {
            if (amount < 0) {
                if (bossBar.getProgress() <= 0) return;
            } else {
                if (bossBar.getProgress() >= 1.0) return;
            }
        }

        ConfigurationSection bossConfig = info
                .getSkill()
                .getAdapter()
                .getConfig()
                .getConfigurationSection("concentration-bossbar");
        if (bossBar == null) {
            bossBar = StringUtils.parseBossBarFromConfig(null, bossConfig);
            bossBar.setProgress(0);
            bossBar.addPlayer(player);
            MOTIVATION_LEVELS.put(player.getUniqueId(), bossBar);
        }

        String title = bossConfig.getString("title");
        if (amount != Integer.MAX_VALUE) {
            amount /= 100;
            bossBar.setProgress(Math.min(1.0, Math.max(0, bossBar.getProgress() + amount)));
        }
        {
            BarColor color;
            switch (getMotivationLevelFromProgress(bossBar.getProgress())) {
                case 3:
                    color = BarColor.BLUE;
                    break;
                case 2:
                    color = BarColor.GREEN;
                    break;
                case 1:
                    color = BarColor.YELLOW;
                    break;
                default:
                    color = BarColor.RED;
            }
            bossBar.setColor(color);
        }
        bossBar.setTitle(MessageHandler.colorize(ServiceHandler.translatePlaceholders(player, title)));
    }

    public static void removeMotivationMeter(UUID id) {
        BossBar bar = MOTIVATION_LEVELS.remove(id);
        if (bar != null) bar.removeAll();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSkillChange(ClassChangeEvent event) {
        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            if (event.getInfo().getSkill().hasAbility(this)) {
                Player player = event.getInfo().getPlayer();
                motivate(player, Integer.MAX_VALUE);
            } else {
                removeMotivationMeter(event.getInfo().getId());
            }
        }, 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(event.getPlayer());
        if (info.getSkill().hasAbility(this)) {
            motivate(event.getPlayer(), Integer.MAX_VALUE);
        }
    }

    /**
     * Checks if the player has the potential to do jump hit/critical hit in their current position.
     * https://www.spigotmc.org/threads/check-if-a-hit-is-a-critical-hit.187105/page-2
     */
    public static boolean isCriticalHit(Player damager) {
        return damager.getFallDistance() > 0.0F &&
                !damager.isOnGround() &&
                !damager.isSprinting() &&
                //damager instanceof LivingEntity &&
                !damager.hasPotionEffect(PotionEffectType.BLINDNESS) &&
                damager.getVehicle() == null;
    }

    @EventHandler
    public void onSkillDamageCap(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (XMaterial.matchXMaterial(player.getItemInHand()) != XMaterial.IRON_SWORD) return;

        LAST_ATTACK.put(player.getUniqueId(), System.currentTimeMillis());

        // Child support evasion by portal opening days
        if (event.getAction() != Action.LEFT_CLICK_AIR) return;
        if (true) return;

        // ParticleDisplay display = ParticleDisplay.of(Particle.CLOUD).withLocation(player.getEyeLocation());
        ParticleDisplay circleDisplay = ParticleDisplay.of(Particle.DRAGON_BREATH)
                .withLocation(player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize().multiply(2)))
                .withCount(1).face(player);
        XParticle.filledCircle(2, 100, 0.5, circleDisplay);
        // int points, int spikes, double rate, double spikeLength, double coreRadius,
        //                                             double neuron, boolean prototype, int speed, ParticleDisplay display
        circleDisplay.withLocation(player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize().multiply(3)));
        XParticle.star(4, 4, 1000, 2, 2, 1, true, 1, circleDisplay).forEach(BooleanSupplier::getAsBoolean);
        XSound.BLOCK_PORTAL_AMBIENT.play(player.getLocation());
        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {

        }, 20L * 5);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onConcentration(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;
        Player player = (Player) event.getDamager();
        SkilledPlayer info = checkup(player);
        if (info == null) return;

        if (player.getInventory().getItemInMainHand().getType().name().endsWith("_SWORD")) {
            if (LAST_ATTACK.getIfPresent(player.getUniqueId()) == null) {
                if (!event.isCancelled()) {
                    SLogger.info("MOTIVATED");
                    boolean isMoistCr1TiKaL = isCriticalHit(player);
                    motivate(player, isMoistCr1TiKaL ? 2 : 1);
                    LAST_ATTACK.put(player.getUniqueId(), System.currentTimeMillis());
                }
            }
        }
    }
}
