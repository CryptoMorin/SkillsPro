package org.skills.managers;

import com.google.common.base.Enums;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.skills.abilities.Ability;
import org.skills.api.events.CustomHudChangeEvent;
import org.skills.api.events.SkillLevelUpEvent;
import org.skills.api.events.SkillSoulGainEvent;
import org.skills.api.events.SkillXPGainEvent;
import org.skills.data.managers.PlayerDataManager;
import org.skills.data.managers.SkilledPlayer;
import org.skills.events.SkillsBonus;
import org.skills.events.SkillsEventType;
import org.skills.main.SLogger;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LevelManager implements Listener {
    protected static final String SPAWNER = "SPAWNER";
    private static final List<CustomAmount> CUSTOM_XP = new ArrayList<>(), CUSTOM_SOULS = new ArrayList<>();

    private final SkillsPro plugin;

    public LevelManager(SkillsPro plugin) {
        this.plugin = plugin;
        MessageHandler.sendConsolePluginMessage("&3Setting up Level Manager...");
        load(plugin);
    }

    public static boolean evaluate(Player player, double level, List<String> conditions) {
        for (String condition : conditions) {
            String equation = ServiceHandler.translatePlaceholders(player, condition);
            equation = StringUtils.replace(equation, "%level%", String.valueOf(level));
            if (BooleanEval.evaluate(equation, null)) return true;
        }
        return false;
    }

    public static List<Player> partyMembersInRange(Player player, SkilledPlayer info) {
        double distance = SkillsConfig.PARTY_MEMBERS_MAX_DISTANCE.getDouble();
        if (distance < 1) return info.getParty().getOnlineMembers();

        List<Player> nearby = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(distance, distance, distance)) {
            if (entity instanceof Player) {
                Player member = (Player) entity;
                SkilledPlayer memberInfo = SkilledPlayer.getSkilledPlayer(member);
                if (info.getPartyId().equals(memberInfo.getPartyId())) nearby.add(member);
            }
        }

        return nearby;
    }

    public static void onLevelUp(SkillLevelUpEvent event) {
        Player player = event.getPlayer();
        int newLevel = event.getNewLevel();

        PlayerDataManager.addLevel(player, newLevel);
        LevelUp level = event.getLevelProperties().level(newLevel).forPlayer(player).evaluateRewards().performMessages().performRewards();
        if (SkillsConfig.LEVEL_CELEBRATION.getBoolean()) level.celebrate(player, SkillsPro.get());
        HealthAndEnergyManager.updateStats(player);
    }

    public static void load(JavaPlugin plugin) {
        CUSTOM_XP.clear();
        ConfigurationSection xp = SkillsConfig.XP.getSection();
        if (xp == null) {
            MessageHandler.sendConsolePluginMessage("&4Outdated config.yml missing XP and Souls section.");
            MessageHandler.sendConsolePluginMessage("&4Custom XP and souls will not be loaded.");
            return;
        }
        for (String expression : xp.getKeys(false)) {
            String equation = xp.getString(expression);
            CustomAmount.CustomAmounType customAmounType;
            EntityType entityType = null;

            if (expression.startsWith("TYPE:")) {
                customAmounType = CustomAmount.CustomAmounType.TYPE;
                expression = expression.substring(5);
                String typeName = StringUtils.deleteWhitespace(expression.toUpperCase(Locale.ENGLISH));
                entityType = Enums.getIfPresent(EntityType.class, typeName).orNull();
                if (entityType == null) {
                    MessageHandler.sendConsolePluginMessage("&cInvalid entity type specified for XP&8: &e" + typeName + " &cin &e" + expression);
                    continue;
                }
            } else if (expression.startsWith("CUSTOM:")) {
                expression = expression.substring(7);
                customAmounType = CustomAmount.CustomAmounType.CUSTOM_MOB;
            } else if (expression.startsWith("CONTAINS:")) {
                expression = expression.substring(9);
                customAmounType = CustomAmount.CustomAmounType.CONTAINS;
            } else {
                expression = MessageHandler.colorize(expression);
                customAmounType = CustomAmount.CustomAmounType.NAME;
            }

            CustomAmount customAmount = new CustomAmount(expression, equation, entityType, customAmounType);
            CUSTOM_XP.add(customAmount);
        }

        CUSTOM_SOULS.clear();
        ConfigurationSection souls = plugin.getConfig().getConfigurationSection("souls");
        for (String expression : souls.getKeys(false)) {
            String equation = souls.getString(expression);
            CustomAmount.CustomAmounType customAmounType;
            EntityType entityType = null;

            if (expression.startsWith("TYPE:")) {
                customAmounType = CustomAmount.CustomAmounType.TYPE;
                expression = expression.substring(5);
                String typeName = StringUtils.deleteWhitespace(expression.toUpperCase(Locale.ENGLISH));
                entityType = Enums.getIfPresent(EntityType.class, typeName).orNull();
                if (entityType == null) {
                    MessageHandler.sendConsolePluginMessage("&cInvalid entity type specified for XP&8: &e" + typeName + " &cin &e" + expression);
                    continue;
                }
            } else if (expression.startsWith("CUSTOM:")) {
                expression = expression.substring(7);
                customAmounType = CustomAmount.CustomAmounType.CUSTOM_MOB;
            } else if (expression.startsWith("CONTAINS:")) {
                expression = expression.substring(9);
                customAmounType = CustomAmount.CustomAmounType.CONTAINS;
            } else {
                expression = MessageHandler.colorize(expression);
                customAmounType = CustomAmount.CustomAmounType.NAME;
            }

            CustomAmount customAmount = new CustomAmount(expression, equation, entityType, customAmounType);
            CUSTOM_SOULS.add(customAmount);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onKillGains(EntityDeathEvent event) {
        LivingEntity killed = event.getEntity();
        SLogger.debug(() -> "An entity was killed " + killed + " (" + killed.getType() + ") " + " | " + DamageManager.getKiller(event)
                + " | " + Ability.isSkillEntity(killed) + " | " + ServiceHandler.isKingdomMob(killed) + " | " + SkillsConfig.isInDisabledWorld(killed.getWorld()));
        if (killed.getType() == EntityType.ARMOR_STAND) return;

        Entity killerOpt = DamageManager.getKiller(event);
        if (!(killerOpt instanceof Player)) return;

        if (Ability.isSkillEntity(killed)) return;
        if (ServiceHandler.isKingdomMob(killed)) return;
        if (SkillsConfig.isInDisabledWorld(killed.getWorld())) return;

        Player killer = (Player) killerOpt;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(killer);
        Pair<String, Number> property = ServiceHandler.getMobProperties(killed);
        SLogger debugger = new SLogger();
        debugger.add(() -> "&c" + killer.getName() + " &6killed &2" + killed.getName() + " &8(&2" + killed.getCustomName() + "&7-&2"
                + killed.getType() + "&8) &6with custom property &2" + property + " &6and vanilla EXP of &2" + event.getDroppedExp());

        /// XP ///
        double xp = 0;
        if (killer.hasPermission("skills.xp") && !SkillsConfig.DISABLED_WORLDS_XP_GAIN.getStringList().contains(killer.getWorld().getName())) {
            xp = MathUtils.evaluateEquation(
                    MessageHandler.replace(SkillsConfig.DEFAULT_XP.getString(), "lvl",
                            property == null ? 1 : property.getValue().doubleValue()));

            for (CustomAmount custom : CUSTOM_XP) {
                boolean matched = custom.matches(killed, property);
                debugger.add(() -> "Matching XP " + custom + " returned: " + matched);
                if (matched) {
                    xp = custom.evaluate(killer, property);
                    break;
                }
            }
            if (xp == -1) {
                xp = event.getDroppedExp();
                debugger.add(() -> "No custom XP defined for this entity, using the default XP which is " + event.getDroppedExp());
            }

            // EXP Server-wide boost
            xp *= SkillsBonus.getMultiplierFor(killer, SkillsEventType.XP);

            if (info.hasParty()) {
                List<Player> inRange = partyMembersInRange(killer, info);
                String equation = StringUtils.replace(StringUtils.replace(
                                ServiceHandler.translatePlaceholders(killer, SkillsConfig.PARTY_XP_PER_MEMBER.getString()),
                                "xp", String.valueOf(xp)),
                        "members-in-range", String.valueOf(inRange.size()));
                double evaled = MathUtils.evaluateEquation(equation);

                if (SkillsConfig.PARTY_DISTRIBUTE.getBoolean()) {
                    for (Player player : inRange) {
                        SkilledPlayer member = SkilledPlayer.getSkilledPlayer(player);
                        SkillXPGainEvent expGainEvent = new SkillXPGainEvent(player, killed, evaled);
                        Bukkit.getPluginManager().callEvent(expGainEvent);
                        if (!expGainEvent.isCancelled()) {
                            member.addXP(evaled);
                        }
                    }
                } else xp += evaled;
            }

            // MythicMobs check, check if level difference is not too high
            if (property != null &&
                    SkillsConfig.MYTHICMOBS_WORLDS_WITH_LEVEL_MARGIN.getStringList().contains(killed.getWorld().getName())) {
                double level = property.getValue().doubleValue();
                if (evaluate(killer, level, SkillsConfig.MYTHICMOBS_XP_CONDITIONS.getStringList())) xp = 0;
            }

            for (PermissionAttachmentInfo perm : killer.getEffectivePermissions()) {
                if (!perm.getValue()) continue;
                String name = perm.getPermission();
                if (name.startsWith("skills.xp.")) {
                    name = name.substring(10);
                    double multiplier = NumberUtils.toDouble(name, 1);
                    xp *= multiplier;
                }
            }

            if (killed.hasMetadata(LevelManager.SPAWNER))
                xp = (int) SkillsConfig.SPAWNERS_XP.fromEquation(killer, "%xp%", xp);

            if (xp != 0 && !SkillsConfig.DISABLED_WORLDS_XP_GAIN.getStringList().contains(killed.getWorld().getName())) {
                SkillXPGainEvent expGainEvent = new SkillXPGainEvent(killer, killed, xp);
                Bukkit.getPluginManager().callEvent(expGainEvent);
                if (!expGainEvent.isCancelled()) {
                    if (xp != expGainEvent.getGained())
                        debugger.add("Event changed the gained XP from " + xp + " to " + expGainEvent.getGained());
                    xp = expGainEvent.getGained();
                    info.addXP(xp);
                }
            }
        }

        //// Souls ////
        int gainedSouls = 0;
        if (killer.hasPermission("skills.souls") && !SkillsConfig.DISABLED_WORLDS_XP_GAIN.getStringList().contains(killer.getWorld().getName())) {
            int souls = (int) MathUtils.evaluateEquation(
                    MessageHandler.replace(SkillsConfig.DEFAULT_SOULS.getString(), "lvl",
                            property == null ? 1 : property.getValue().doubleValue()));

            for (CustomAmount custom : CUSTOM_SOULS) {
                debugger.add(() -> "Matching souls '" + custom.matcher + "': '" + custom.equation + "' matches? "
                        + custom.matches(killed, property) + " evaluated: " + custom.evaluate(killer, property));
                if (custom.matches(killed, property)) {
                    souls = (int) custom.evaluate(killer, property);
                    break;
                }
            }

            if (info.hasParty()) {
                List<Player> inRange = partyMembersInRange(killer, info);
                String equation = StringUtils.replace(StringUtils.replace(
                        ServiceHandler.translatePlaceholders(killer, SkillsConfig.PARTY_SOULS_PER_MEMBER.getString()),
                        "souls", String.valueOf(souls)), "members-in-range", String.valueOf(inRange.size()));
                int evaled = (int) MathUtils.evaluateEquation(equation);

                if (SkillsConfig.PARTY_DISTRIBUTE.getBoolean()) {
                    for (Player player : inRange) {
                        SkilledPlayer member = SkilledPlayer.getSkilledPlayer(player);
                        SkillSoulGainEvent soulEvent = new SkillSoulGainEvent(player, killed, souls);
                        Bukkit.getPluginManager().callEvent(soulEvent);
                        if (!soulEvent.isCancelled()) member.addSouls(soulEvent.getGained());
                    }
                } else souls += evaled;
            }

            souls *= (int) SkillsBonus.getMultiplierFor(killer, SkillsEventType.SOUL);

            // MythicMobs check, check if level difference is not too high
            if (property != null &&
                    SkillsConfig.MYTHICMOBS_WORLDS_WITH_LEVEL_MARGIN.getStringList().contains(killed.getWorld().getName())) {
                double level = property.getValue().doubleValue();
                if (evaluate(killer, level, SkillsConfig.MYTHICMOBS_SOUL_CONDITIONS.getStringList())) souls = 0;
            }

            for (PermissionAttachmentInfo perm : killer.getEffectivePermissions()) {
                if (!perm.getValue()) continue;
                String name = perm.getPermission();
                if (name.startsWith("skills.souls.")) {
                    name = name.substring(13);
                    double multiplier = NumberUtils.toDouble(name, 1);
                    souls *= multiplier;
                }
            }

            if (killed.hasMetadata(LevelManager.SPAWNER))
                souls = (int) SkillsConfig.SPAWNERS_SOULS.fromEquation(killer, "%souls%", souls);

            // Check for spawner mob before awarding souls
            if (souls != 0 && !SkillsConfig.DISABLED_WORLDS_SOUL_GAIN.getStringList().contains(killed.getWorld().getName())) {
                SkillSoulGainEvent soulEvent = new SkillSoulGainEvent(killer, killed, souls);
                Bukkit.getPluginManager().callEvent(soulEvent);
                if (!soulEvent.isCancelled()) {
                    info.addSouls(soulEvent.getGained());
                    gainedSouls = soulEvent.getGained();
                }
            }
        }

        StringBuilder list = new StringBuilder();
        for (Ability ability : info.getSkill().getAbilities()) {
            int abilityLvl = info.getAbilityLevel(ability);
            if (ability.getName().equals("passive") || abilityLvl == 3) continue;
            int cost = ability.getCost(info);

            if (info.getSouls() >= cost && info.getSouls() - gainedSouls < cost)
                list.append(SkillsLang.ABILITY_UPGRADE_NOTIFICATION_LIST.parse(killer,
                        "%ability%", ability.getTitle(info), "%ability_level%", abilityLvl + 1));
        }
        if (list.length() != 0) SkillsLang.ABILITY_UPGRADE_NOTIFICATION.sendMessage(killer, "%abilities%", list);

        double roundedXP = MathUtils.roundToDigits(xp, 2);
        if (SkillsConfig.HOLOGRAM_ENABLED.getBoolean()) {
            if (!SkillsConfig.HOLOGRAM_DISABLED_MOBS.getStringList().contains(killed.getType().name())) {
                Hologram.spawn(killed.getLocation().clone().add(0, -2, 0),
                        SkillsConfig.HOLOGRAM_STAY.getLong(),
                        SkillsConfig.HOLOGRAM_LINES.getStringList(), "%xp%", roundedXP, "%souls%", gainedSouls);
            }
        }


        debugger.add("Final XP: " + xp + " Final souls: " + gainedSouls);
        debugger.show();

        if (SkillsConfig.KILL_MESSAGE.getBoolean()) {
            String name = killed.getCustomName() == null ? killed.getName() : killed.getCustomName();
            SkillsLang.KILL_MESSAGE.sendMessage(killer, "%xp%", roundedXP, "%souls%", gainedSouls, "%name%", name);
        }
        CustomHudChangeEvent.call(killer);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFireworkDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework) {
            Firework firework = (Firework) event.getDamager();
            if (firework.hasMetadata("LVLUP")) event.setCancelled(true);
        } else if (event.getDamager() instanceof Player) {
            if (SkillsConfig.HOLOGRAM_ENABLED.getBoolean()) {
                if (!SkillsConfig.HOLOGRAM_DISABLED_MOBS.getStringList().contains(event.getEntity().getType().name())) {
                    Hologram.spawn(event.getEntity().getLocation().add(0, -2, 0),
                            SkillsConfig.HOLOGRAM_STAY.getLong(),
                            SkillsConfig.HOLOGRAM_DAMAGE_INDICATOR.getStringList(),

                            "%damage%", MathUtils.roundToDigits(event.getFinalDamage(), SkillsConfig.HOLOGRAM_DAMAGE_INDICATOR_PRECISION.getInt()),
                            "%direct_damage%", MathUtils.roundToDigits(event.getDamage(), SkillsConfig.HOLOGRAM_DAMAGE_INDICATOR_PRECISION.getInt()));
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (info.getLevel() < SkillsConfig.LOSS_MIN_LEVEL.getInt()) return;
        long soulsLost = 0;
        double xpLost = 0;

        LivingEntity killer = DamageManager.getKiller(event);
        Pair<String, Number> property;
        if (killer != null) property = ServiceHandler.getMobProperties(killer);
        else property = null;

        if (info.getSouls() > 1 && !SkillsConfig.DISABLED_WORLDS_SOUL_LOSS.getStringList().contains(player.getWorld().getName())) {
            if (SkillsConfig.LOSE_SAME_SOULS_AS_KILLER_SOULS_GAIN.getBoolean()) {
                if (killer != null) {
                    soulsLost = 1;
                    for (CustomAmount custom : CUSTOM_SOULS) {
                        if (custom.matches(killer, property)) {
                            soulsLost = (int) custom.evaluate(player, property);
                            break;
                        }
                    }
                } else
                    soulsLost = (int) MathUtils.evaluateEquation(ServiceHandler.translatePlaceholders(player, SkillsConfig.SOULS_LOSS_UPON_DEATH.getString()));
            } else
                soulsLost = (int) MathUtils.evaluateEquation(ServiceHandler.translatePlaceholders(player, SkillsConfig.SOULS_LOSS_UPON_DEATH.getString()));
            info.setSouls(Math.max(0, info.getSouls() - soulsLost));
        }
        if (info.getXP() > 1 && !SkillsConfig.DISABLED_WORLDS_XP_LOSS.getStringList().contains(player.getWorld().getName())) {
            if (SkillsConfig.LOSE_SAME_XP_AS_KILLER_XP_GAIN.getBoolean()) {
                if (killer != null) {
                    xpLost = -1;
                    for (CustomAmount custom : CUSTOM_XP) {
                        if (custom.matches(killer, property)) {
                            xpLost = custom.evaluate(player, property);
                            break;
                        }
                    }
                    if (xpLost == -1) xpLost = event.getDroppedExp();
                } else
                    xpLost = MathUtils.evaluateEquation(ServiceHandler.translatePlaceholders(player, SkillsConfig.XP_LOSS_UPON_DEATH.getString()));
            } else
                xpLost = MathUtils.evaluateEquation(ServiceHandler.translatePlaceholders(player, SkillsConfig.XP_LOSS_UPON_DEATH.getString()));
            info.setAbsoluteXP(Math.max(0, info.getXP() - xpLost));
        }

        SkillsLang.DEATH.sendMessage(player, "%souls%", soulsLost, "%xp%", xpLost);
    }

    @EventHandler
    public void mobSpawnEvent(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER)
            event.getEntity().setMetadata(LevelManager.SPAWNER, new FixedMetadataValue(this.plugin, ""));
    }

    private static final class CustomAmount {
        public CustomAmounType customAmounType;
        public EntityType type;
        public String matcher;
        public String equation;

        public CustomAmount(String expression, String equation, EntityType type, CustomAmounType customAmounType) {
            this.matcher = customAmounType == CustomAmounType.CONTAINS ? expression.toLowerCase() : expression;
            this.type = type;
            this.customAmounType = customAmounType;
            this.equation = equation;
        }

        public boolean matches(LivingEntity entity, Pair<String, Number> customMob) {
            switch (customAmounType) {
                case CONTAINS:
                    return entity.getCustomName() != null && entity.getCustomName().toLowerCase().contains(matcher);
                case CUSTOM_MOB:
                    return customMob != null && customMob.getKey() != null && matcher.equalsIgnoreCase(customMob.getKey());
                case NAME:
                    return entity.getCustomName() != null && entity.getCustomName().equalsIgnoreCase(matcher);
                case TYPE:
                    return entity.getType() == type;
                default:
                    throw new AssertionError("Unknown custom amount type: " + customAmounType);
            }
        }

        @Override
        public String toString() {
            return "CustomAmount:{expression='" + matcher + "', type=" + type + ", customMobType=" + customAmounType + ", equation='" + equation + "'}";
        }

        public double evaluate(Player player, Pair<String, Number> customMob) {
            String translated = ServiceHandler.translatePlaceholders(player, equation);
            translated = MessageHandler.replace(translated, "lvl", customMob == null ? 1 : customMob.getValue().doubleValue());
            return MathUtils.evaluateEquation(translated);
        }

        private enum CustomAmounType {
            CONTAINS, CUSTOM_MOB, TYPE, NAME;
        }
    }
}
