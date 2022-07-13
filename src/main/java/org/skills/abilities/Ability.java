package org.skills.abilities;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.skills.data.managers.PlayerAbilityData;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsSkillConfig;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;
import org.skills.services.manager.ServiceHandler;
import org.skills.types.SkillManager;
import org.skills.types.Stat;
import org.skills.utils.MathUtils;
import org.skills.utils.StringUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Each skill has it's own abilities.<br>
 * There are two types of abilities:<br><br>
 * <b>Passive:</b> Abilities that are always active without manual activation by the player<br>
 * <b>Active:</b> Abilities that need manual activiation by the player to work.
 * <p>
 * If an ability's max level is 0, then that ability is not upgradable.
 *
 * @see ActiveAbility
 */
public abstract class Ability implements Listener {
    protected static final List<Set<Integer>> DISPOSABLE_ENTITIES_SET = new ArrayList<>();
    protected static final List<Map<Integer, ?>> DISPOSABLE_ENTITIES_MAP = new ArrayList<>();

    private static final Map<Integer, Entity> ENTITIES = new ConcurrentHashMap<>();
    private static final List<Integer> TASKS = new ArrayList<>();
    protected final String name;
    private final String skill;

    public Ability(String skill, String name) {
        this.skill = skill;
        if (name.equalsIgnoreCase("passive")) this.name = skill.toLowerCase(Locale.ENGLISH) + "_passive";
        else this.name = name;
    }

    public static void addEntity(Entity entity) {
        ENTITIES.put(entity.getEntityId(), entity);
    }

    @SafeVarargs
    public static void addDisposableHandler(Set<Integer>... sets) {
        DISPOSABLE_ENTITIES_SET.addAll(Arrays.asList(sets));
    }

    @SafeVarargs
    public static void addDisposableHandler(Map<Integer, ?>... maps) {
        DISPOSABLE_ENTITIES_MAP.addAll(Arrays.asList(maps));
    }

    public static boolean isSkillEntity(Entity entity) {
        return ENTITIES.containsKey(entity.getEntityId());
    }

    public static void addAllEntities(Collection<? extends Entity> entities) {
        for (Entity entity : entities) addEntity(entity);
    }

    public static void removeEntity(Entity entity) {
        ENTITIES.remove(entity.getEntityId());
    }

    public static String getScalingColor(String scaling) {
        scaling = scaling.toLowerCase(Locale.ENGLISH);
        for (Stat stat : Stat.STATS.values()) {
            if (scaling.contains(stat.getNode())) return stat.getColor();
        }
        return "&e";
    }

    public static boolean addTask(BukkitTask task) {
        return TASKS.add(task.getTaskId());
    }

    public static void reload() {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        for (int task : TASKS) scheduler.cancelTask(task);
        TASKS.clear();

        for (Ability ability : AbilityManager.getAbilities().values()) HandlerList.unregisterAll(ability);
    }

    public static void onDisable() {
        ENTITIES.values().forEach(Entity::remove);
    }

    public boolean commonDamageCheckup(EntityDamageByEntityEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) return true;
        if (!(event.getEntity() instanceof LivingEntity)) return true;
        return !(event.getDamager() instanceof Player);
    }

    public boolean commonDamageCheckupReverse(EntityDamageByEntityEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) return true;
        return !(event.getEntity() instanceof Player);
    }

    protected boolean isInvalidTarget(Entity entity) {
        return !(entity instanceof LivingEntity) || entity.getType() == EntityType.ARMOR_STAND;
    }

    public Set<EntityType> getEntityList(SkilledPlayer info, String list) {
        List<String> blacklist = getOptions(info, list).getStringList();
        Set<EntityType> blacklisted = EnumSet.noneOf(EntityType.class);
        for (String type : blacklist) {
            EntityType entityType = Enums.getIfPresent(EntityType.class, type.toUpperCase(Locale.ENGLISH)).orNull();
            if (entityType == null) continue;
            blacklisted.add(entityType);
        }
        return blacklisted;
    }

    public String getTitle(SkilledPlayer info) {
        return SkillsLang.valueOf("ABILITY_" + this.name.toUpperCase(Locale.ENGLISH) + "_TITLE").parse(info.getOfflinePlayer());
    }

    public String getDescription(SkilledPlayer info) {
        return SkillsLang.valueOf("ABILITY_" + this.name.toUpperCase(Locale.ENGLISH) + "_DESCRIPTION").parse(info.getOfflinePlayer());
    }

    public String getScalingDescription(SkilledPlayer info, String scaling) {
        return getScalingColor(scaling) + StringUtils.toFancyNumber(getAbsoluteScaling(info, scaling));
    }

    public void start() {}

    public String translate(SkilledPlayer info, String scaling) {
        return getScalingDescription(info, getOptions(info, scaling).getString());
    }

    public void playSound(Player player, SkilledPlayer info, String option) {
        XSound.play(player, getOptions(info, "sounds." + option).getString());
    }

    public XSound.Record getSound(SkilledPlayer info, String option) {
        return XSound.parse(getOptions(info, "sounds." + option).getString());
    }

    public PotionEffect parseEquationPotion(SkilledPlayer info, @Nullable String potion) {
        if (Strings.isNullOrEmpty(potion) || potion.equalsIgnoreCase("none")) return null;
        String[] split = org.apache.commons.lang.StringUtils.split(org.apache.commons.lang.StringUtils.deleteWhitespace(potion), ',');

        Optional<XPotion> typeOpt = XPotion.matchXPotion(split[0]);
        if (!typeOpt.isPresent()) return null;
        PotionEffectType type = typeOpt.get().getPotionEffectType();
        if (type == null) return null;

        int duration = 2400; // 20 ticks * 60 seconds * 2 minutes
        int amplifier = 0;
        try {
            if (split.length > 3) {
                if (!MathUtils.hasChance((int) getAbsoluteScaling(info, split[3]))) return null;
            }

            if (split.length > 1) {
                duration = (int) (getAbsoluteScaling(info, split[1]) * 20);
                if (split.length > 2) amplifier = (int) (getAbsoluteScaling(info, split[2]) - 1);
            }
        } catch (NumberFormatException ignored) {
        }

        return new PotionEffect(type, duration, amplifier);
    }

    public List<PotionEffect> getEffects(SkilledPlayer info, String option) {
        List<String> effects = getOptions(info).getStringList(option);
        List<PotionEffect> effectsList = new ArrayList<>(effects.size());

        for (String effect : effects) {
            PotionEffect effect1 = parseEquationPotion(info, effect);
            if (effect1 != null) effectsList.add(effect1);
        }
        return effectsList;
    }

    public List<PotionEffect> applyEffects(SkilledPlayer info, String option, LivingEntity entity) {
        List<PotionEffect> effects = getEffects(info, option);
        entity.addPotionEffects(effects);
        return effects;
    }

    public List<PotionEffect> applyEffects(SkilledPlayer info, LivingEntity entity) {
        return applyEffects(info, "effects", entity);
    }

    public String getTranslatedScaling(SkilledPlayer info, String scaling, Object... edits) {
        Objects.requireNonNull(scaling, () -> "One of the scalings for " + info.getSkillName() + " -> " + name + " is missing");

        String equation = ServiceHandler.translatePlaceholders(info.getOfflinePlayer(), scaling);
        equation = MessageHandler.replace(equation, "lvl", (Supplier<String>) () -> String.valueOf(info.getAbilityLevel(this)));
        equation = MessageHandler.replaceVariables(equation, edits);

        for (Stat stats : Stat.STATS.values()) {
            String stat = String.valueOf(info.getStat(stats));
            equation = MessageHandler.replace(equation, stats.getNode(), stat);
            equation = MessageHandler.replace(equation, stats.getNode().toUpperCase(Locale.ENGLISH), stat);
        }

        return equation;
    }

    public double getAbsoluteScaling(SkilledPlayer info, String scaling, Object... edits) {
        return MathUtils.evaluateEquation(getTranslatedScaling(info, scaling, edits));
    }

    public ConfigurationSection getOptions(SkilledPlayer info) {
        return Objects.requireNonNull(
                info.getSkill().getAdapter().getConfig().getConfigurationSection("abilities." + this.name.replace('_', '-')),
                () -> "Could not find configuration options for " + this.name + " ability in " + info.getSkillName() + " class."
        );
    }

    public SkillsSkillConfig getOptions(String skill, String option) {
        return new SkillsSkillConfig(SkillManager.getSkill(skill).getAdapter().getConfig(), "abilities." + this.name.replace('_', '-') + '.' + option);
    }

    public SkillsSkillConfig getOptions(SkilledPlayer info, String scaling) {
        return new SkillsSkillConfig(info.getSkill().getAdapter().getConfig(), "abilities." + this.name.replace('_', '-') + '.' + scaling);
    }

    public double getScaling(SkilledPlayer info, String scaling, EntityDamageByEntityEvent event) {
        LivingEntity entity;
        if (event.getEntity() instanceof LivingEntity) entity = (LivingEntity) event.getEntity();
        else entity = null;

        return getScaling(info, scaling,
                "damage", event.getDamage(),
                "hp", entity == null ? 0 : entity.getHealth(),
                "maxHp", entity == null ? 0 : entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()
        );
    }

    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    public double getScaling(SkilledPlayer info, String option, Object... edits) {
        Objects.requireNonNull(info, "Player info cannot be null");
        Object value = getOptions(info).get(option);
        if (value == null) {
            MessageHandler.sendConsolePluginMessage("&cMissing scaling for &e" + info.getSkillName() + " &cconfig&8: " +
                    "&eabilities &7-> &e" + name + " &7-> &e" + option.replace(".", " &7-> &e"));
            return 0;
        }

        if (value instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) value;
            int closest = SkillsConfig.getClosestLevelSection(section, info.getAbilityLevel(this));
            return getAbsoluteScaling(info, section.getString(String.valueOf(closest)), edits);
        }
        if (value instanceof Number) return ((Number) value).doubleValue();
        return getAbsoluteScaling(info, value.toString(), edits);
    }

    public int getCost(SkilledPlayer info) {
        String cost = getOptions(info).getString("cost");
        if (cost == null) return 0;
        return (int) getAbsoluteScaling(info, cost);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Ability)) return false;

        Ability ability = (Ability) obj;
        return this.name.equals(ability.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public SkilledPlayer checkup(Player player) {
        GameMode mode = player.getGameMode();
        if (mode == GameMode.SPECTATOR) return null;
        if (mode == GameMode.CREATIVE && !player.hasPermission("skills.use-creative")) return null;

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (info.getActiveAbilities().contains(this)) return null;
        if (!isPassive() && !info.isActiveReady((ActiveAbility) this)) return null;

        PlayerAbilityData data = info.getAbilityData(this);
        if (data == null) return null; // The player's active skill doesn't have this ability
        if (data.isDisabled()) return null;
        if (data.getLevel() < 1 && !this.name.endsWith("passive")) return null;

        if (SkillsConfig.isInDisabledWorld(player.getWorld())) return null;
        if (ServiceHandler.isInRegion(player, getDisabledRegions(info))) return null;
        return info;
    }

    public List<String> getDisabledRegions(SkilledPlayer info) {
        return getOptions(info).getStringList("disabled-regions");
    }

    public List<Object> getEdits(List<Object> edits, ConfigurationSection options, String previousKeys, SkilledPlayer info) {
        for (String option : options.getKeys(false)) {
            if (option.equals("required-level") || option.equals("activation") || option.equals("cost")) continue;

            Object value = options.get(option);
            boolean isSection = value instanceof ConfigurationSection;
            if (!isSection && !(value instanceof String) && !(value instanceof Number)) continue;
            if (isSection) {
                ConfigurationSection section = (ConfigurationSection) value;
                if (!section.isSet("1")) {
                    option = StringUtils.toLatinLowerCase(option, '*', '*');
                    getEdits(
                            edits, section,
                            (previousKeys.isEmpty() ? "" : previousKeys + '_') + option + '_',
                            info
                    );
                    continue;
                }
            }

            Supplier<String> supplier = () -> {
                if (isSection) {
                    ConfigurationSection section = (ConfigurationSection) value;
                    int closest = SkillsConfig.getClosestLevelSection(section, info.getAbilityLevel(this));
                    return getScalingDescription(info, section.getString(String.valueOf(closest)));
                }
                if (value instanceof Number) return value.toString();
                return getScalingDescription(info, value.toString());
            };

            edits.add('%' + previousKeys + StringUtils.toLatinLowerCase(option, '*', '*') + '%');
            edits.add(supplier);
        }

        return edits;
    }

    public List<Object> getEdits(SkilledPlayer info) {
        List<Object> edits = new ArrayList<>((8 + 5) * 2); // The other 8 are for the edits that will be added soon.
        return getEdits(edits, getOptions(info), "", info);
    }

    public int getRequiredLevel(SkilledPlayer info) {
        String scaling = getOptions(info).getString("required-level");
        if (Strings.isNullOrEmpty(scaling)) return 0;
        return (int) getAbsoluteScaling(info, scaling);
    }

    public boolean canUse(SkilledPlayer info) {
        return getRequiredLevel(info) <= info.getLevel();
    }

    public boolean isPassive() {
        return !(this instanceof ActiveAbility);
    }

    public String getName() {
        return name;
    }

    public String getSkill() {
        return skill;
    }
}
