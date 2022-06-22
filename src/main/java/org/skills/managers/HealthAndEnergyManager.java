package org.skills.managers;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.ActionBar;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.skills.api.events.CustomHudChangeEvent;
import org.skills.api.events.SkillXPGainEvent;
import org.skills.commands.general.CommandSelect;
import org.skills.data.managers.PlayerSkill;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.services.manager.ServiceHandler;
import org.skills.types.Energy;
import org.skills.types.Skill;
import org.skills.types.SkillManager;
import org.skills.types.SkillScaling;
import org.skills.utils.Cooldown;
import org.skills.utils.StringUtils;
import org.skills.utils.versionsupport.VersionSupport;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class HealthAndEnergyManager implements Listener {
    private static final Map<Integer, BossBar> LEVEL_BOSSBARS = new HashMap<>();

    public HealthAndEnergyManager(SkillsPro plugin) {
        // HUD Change
        int hudFrequency = SkillsConfig.ACTIONBAR_FREQUENCY.getInt();
        if (SkillsConfig.ACTIONBAR_ENABLED.getBoolean() && hudFrequency > 0) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("skills.actionbar")
                            && SkilledPlayer.getSkilledPlayer(player).showActionBar()
                            && !SkillsConfig.isInDisabledWorld(player.getLocation())) {
                        CustomHudChangeEvent event = new CustomHudChangeEvent(player);
                        Bukkit.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) ActionBar.sendActionBar(player, event.getHud());
                    }
                }
            }, 100L, hudFrequency);
        }

        int frequency = SkillsConfig.BOSSBAR_LEVELS_FREQUENCY.getInt();
        if (frequency > 0) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                String title = SkillsConfig.BOSSBAR_LEVELS.getSection().getString("title");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    BossBar bar = LEVEL_BOSSBARS.get(player.getEntityId());
                    if (bar != null) bar.setTitle(MessageHandler.colorize(ServiceHandler.translatePlaceholders(player, title)));
                }
            }, 100L, frequency);
        }

        // Energy Gen (Auto Charging)
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (SkillsConfig.isInDisabledWorld(player.getLocation())) continue;
                SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
                if (!info.hasSkill()) continue;

                Energy energy = info.getSkill().getEnergy();
                boolean reverse = energy.getCharging() == Energy.ChargingMethod.AUTO_REVERSE;
                if (energy.getCharging() == Energy.ChargingMethod.AUTO || reverse) {
                    double currEnergy = info.getEnergy();
                    double energyRegen = info.getScaling(SkillScaling.ENERGY_REGEN);
                    double finale = currEnergy;

                    if (reverse) {
                        if (currEnergy - energyRegen <= 0) finale = 0;
                        else finale -= energyRegen;
                    } else {
                        if (!Cooldown.isInCooldown(player.getUniqueId(), "ENERGY_BOOSTER")) info.setEnergyBooster(0);
                        double booster = info.getEnergyBooster();
                        energyRegen += booster;

                        double maxEnergy = info.getScaling(SkillScaling.MAX_ENERGY);
                        if (finale >= maxEnergy) continue;
                        if (currEnergy + energyRegen >= maxEnergy) finale = maxEnergy;
                        else finale += energyRegen;
                        if (finale >= maxEnergy) XSound.play(player, energy.getSoundFull());
                    }

                    if (player.isOnline()) info.setEnergy(finale);
                }
            }
        }, 60L, 20L);
    }

    public static BossBar getBossBar(Player player) {
        return LEVEL_BOSSBARS.get(player.getEntityId());
    }

    public static float updateXPBar(Player player) {
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        float percent = (float) (info.getXP() / info.getLevelXP(info.getLevel()));
        Validate.isTrue(percent <= 1.0 && percent >= 0.0,
                "Invalid BossBar percent for " + player.getName() + ": " + percent + " -> XP: " + info.getXP() +
                        ", Next Level XP: " + info.getLevelXP(info.getLevel()) + " for level " + info.getLevel());

        if (SkillsConfig.VANILLA_EXP_BAR_ENABLED.getBoolean() && !SkillsConfig.VANILLA_EXP_BAR_REAL_SYNC.getBoolean()) {

            if (SkillsConfig.VANILLA_EXP_BAR_ENABLED.getBoolean()) {
                String shown = SkillsConfig.VANILLA_EXP_BAR_SHOWN_NUMBER.getString().toLowerCase(Locale.ENGLISH);
                int num;
                switch (shown) {
                    case "xp":
                        num = (int) info.getXP();
                        break;
                    case "level":
                        num = info.getLevel();
                        break;
                    case "souls":
                        num = (int) info.getSouls();
                        break;
                    default:
                        MessageHandler.sendConsolePluginMessage("&4Invalid 'shown-number' option for vanilla EXP bar&8: &e" + shown);
                        num = 0;
                }

                player.setLevel(num);
                player.setExp(percent);
            }
        }

        return percent;
    }

    public static void updateStats(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(SkillsPro.get(), () -> {
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);

            float percent = updateXPBar(player);

            if (SkillsConfig.BOSSBAR_LEVELS_ENABLED.getBoolean() && player.hasPermission("skills.bossbar")) {
                BossBar bar = LEVEL_BOSSBARS.get(player.getEntityId());
                if (bar != null) {
                    bar.setTitle(MessageHandler.colorize(ServiceHandler.translatePlaceholders(player,
                            SkillsConfig.BOSSBAR_LEVELS.getSection().getString("title"))));
                    bar.setProgress(percent);
                }
            }

            if (!info.hasSkill()) return;
            Skill skill = info.getSkill();

            double maxHp = skill.getScaling(info, SkillScaling.MAX_HEALTH);
            Bukkit.getScheduler().runTask(SkillsPro.get(), () -> VersionSupport.setMaxHealth(player, maxHp));
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onHitEnergyCharge(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (!info.hasSkill()) return;
        Energy energy = info.getSkill().getEnergy();

        if (energy.getCharging() == Energy.ChargingMethod.HIT) {
            if (energy.getElements() == null || energy.getElements().isEmpty()) {
                info.chargeEnergy();
                return;
            }

            for (String elements : energy.getElements()) {
                if (event.getEntityType().name().equals(elements)) {
                    info.chargeEnergy();
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onHitEnergyCharge(EntityDeathEvent event) {
        Player damager = DamageManager.getFinalHitMob(event.getEntity());
        if (damager == null) return;

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(damager);
        if (!info.hasSkill()) return;
        Energy energy = info.getSkill().getEnergy();

        if (energy.getCharging() == Energy.ChargingMethod.KILL) {
            if (energy.getElements() == null || energy.getElements().isEmpty()) {
                info.chargeEnergy();
                return;
            }

            for (String elements : energy.getElements()) {
                if (event.getEntityType().name().equals(elements)) {
                    info.chargeEnergy();
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (SkillsConfig.isInDisabledWorld(event.getPlayer().getLocation())) return;
        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> updateStats(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        info.setScaledHealth();

        XPAndEnchantmentManager.EXPS.put(player.getUniqueId(), player.getTotalExperience());

        if (!player.hasPlayedBefore()) {
            String skill = SkillsConfig.DEFAULT_SKILL.getString();
            if (!skill.equalsIgnoreCase(PlayerSkill.NONE)) {
                Skill defaultSkill = SkillManager.getSkill(skill);
                if (defaultSkill == null) MessageHandler.sendConsolePluginMessage("&4Unknown default skill option&8: &e" + skill);
                else if (info.setActiveSkill(defaultSkill).isCancelled())
                    throw new IllegalStateException("A plugin prevented setting default skill " + skill + " for player " + player.getName());
            }
            if (SkillsConfig.AUTO_SELECT_ON_JOIN.getBoolean()) {
                Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> CommandSelect.openMenu(player, info), 20L);
            }
        }

        if (SkillsConfig.isInDisabledWorld(event.getPlayer().getWorld())) return;
        if (player.hasPermission("skills.bossbar")) {
            if (SkillsConfig.BOSSBAR_LEVELS_ENABLED.getBoolean()) {
                BossBar bar = StringUtils.parseBossBarFromConfig(SkillsConfig.BOSSBAR_LEVELS_FREQUENCY.getInt() < 0 ? null : player, SkillsConfig.BOSSBAR_LEVELS.getSection());
                LEVEL_BOSSBARS.put(player.getEntityId(), bar);
            }
            if (SkillsConfig.BOSSBAR_BONUSES_ENABLED.getBoolean()) info.getBonuses().values().forEach(bonus -> bonus.startBonus(player));
        }
        updateStats(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (SkillsConfig.BOSSBAR_LEVELS_ENABLED.getBoolean()) {
            BossBar bar = LEVEL_BOSSBARS.remove(player.getEntityId());
            if (bar != null) bar.removeAll();

            Integer exp = XPAndEnchantmentManager.EXPS.remove(player.getUniqueId());
            if (exp != null) player.setTotalExperience(exp);
        }
        CustomHudChangeEvent.ANIMATIONS.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (SkillsConfig.isInDisabledWorld(player.getLocation())) {
            BossBar bar = LEVEL_BOSSBARS.get(player.getEntityId());
            if (bar != null) bar.removeAll();
            VersionSupport.setMaxHealth(player, 20);
        } else {
            if (SkillsConfig.isInDisabledWorld(event.getFrom())) {
                if (SkillsConfig.BOSSBAR_LEVELS_ENABLED.getBoolean() && player.hasPermission("skills.bossbar")) {
                    BossBar bar = StringUtils.parseBossBarFromConfig(player, SkillsConfig.BOSSBAR_LEVELS.getSection());
                    LEVEL_BOSSBARS.put(player.getEntityId(), bar);
                }
                updateStats(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExpSkillGain(SkillXPGainEvent event) {
        if (!SkillsConfig.BOSSBAR_LEVELS_ENABLED.getBoolean() &&
                !SkillsConfig.VANILLA_EXP_BAR_ENABLED.getBoolean()) return;
        updateStats(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void hologramFire(EntityCombustEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (((Player) event.getEntity()).getGameMode() != GameMode.CREATIVE) return;
        if (!SkillsConfig.DISABLE_CREATIVE_FIRE.getBoolean()) return;
        event.setCancelled(true);
    }
}
