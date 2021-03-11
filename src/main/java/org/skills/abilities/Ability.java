package org.skills.abilities;

import com.cryptomorin.xseries.XPotion;
import com.google.common.base.Enums;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsSkillConfig;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;
import org.skills.services.manager.ServiceHandler;
import org.skills.types.SkillManager;
import org.skills.types.Stat;
import org.skills.utils.MathUtils;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Each skill has it's own abilities.<br>
 * There are two types of abilities:<br><br>
 * <b>Passive:</b> Abilities that are always active without manual activation by the player<br>
 * <b>Active:</b> Abilities that need manual activiation by the player to work.
 * <p>
 * If an ability's max level is 0, then that ability is not upgradable.
 * @see ActiveAbility
 */
public abstract class Ability implements Listener {
    //    public static final String SKILLS_ENTITY = "SKILLS_ENTITY";
    protected static final List<Set<Integer>> DISPOSABLE_ENTITIES_SET = new ArrayList<>();
    protected static final List<Map<Integer, ?>> DISPOSABLE_ENTITIES_MAP = new ArrayList<>();

    private static final Map<Integer, Entity> ENTITIES = new HashMap<>();
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
//        addMinion(entity);
    }

//    public static void addMinion(Entity entity) {
//        entity.setMetadata(SKILLS_ENTITY, new FixedMetadataValue(SkillsPro.get(), null));
//    }

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
            if (scaling.contains(stat.getNode())) return MessageHandler.colorize(stat.getColor());
        }
        return "";
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

    protected boolean isInvalidTarget(Entity entity) {
        return !(entity instanceof LivingEntity) || entity.getType() == EntityType.ARMOR_STAND || entity.isDead();
    }

    public Set<EntityType> getEntityList(SkilledPlayer info, String list) {
        List<String> blacklist = getExtra(info, list).getStringList();
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
        return getScalingColor(scaling) + MathUtils.roundToDigits(getAbsoluteScaling(info, scaling), 2);
    }

    public void start() {
    }

    public String translate(SkilledPlayer info, String extra) {
        return getScalingDescription(info, getExtra(info, extra).getString());
    }

    public String getScalingEquation(SkilledPlayer info, String scaling) {
        String equation = getExtra(info).getString(scaling);
        if (equation == null) {
            MessageHandler.sendConsolePluginMessage("&cMissing scaling for &e" + info.getSkillName() + " &cconfig&8: " +
                    "&eabilities &7-> &e" + name + " &7-> &e" + scaling.replace(".", " &7-> &e"));
            return "0";
        }
        return equation;
    }

    public double getScaling(SkilledPlayer info, Object... edits) {
        String scaling = getScalingEquation(info, "scaling");
        return getAbsoluteScaling(info, scaling, edits);
    }

    public double getScaling(SkilledPlayer info, EntityDamageByEntityEvent event) {
        LivingEntity entity = (LivingEntity) event.getEntity();
        return getScaling(info, "damage", event.getDamage(), "hp", entity.getHealth(),
                "maxHp", entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    }

    public PotionEffect parseEquationPotion(SkilledPlayer info, @Nullable String potion) {
        if (Strings.isNullOrEmpty(potion) || potion.equalsIgnoreCase("none")) return null;
        String[] split = org.apache.commons.lang.StringUtils.split(org.apache.commons.lang.StringUtils.deleteWhitespace(potion), ',');

        Optional<XPotion> typeOpt = XPotion.matchXPotion(split[0]);
        if (!typeOpt.isPresent()) return null;
        PotionEffectType type = typeOpt.get().parsePotionEffectType();
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
        List<String> effects = getExtra(info).getStringList(option);
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
        equation = StringUtils.replace(equation, "lvl", String.valueOf(info.getImprovementLevel(this)));
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

    public ConfigurationSection getExtra(SkilledPlayer info) {
        return info.getSkill().getAdapter().getConfig().getConfigurationSection("abilities." + this.name.replace('_', '-'));
    }

    public SkillsSkillConfig getExtra(String skill, String extra) {
        return new SkillsSkillConfig(SkillManager.getSkill(skill).getAdapter().getConfig(), "abilities." + this.name.replace('_', '-') + '.' + extra);
    }

    public SkillsSkillConfig getExtra(SkilledPlayer info, String extra) {
        return new SkillsSkillConfig(info.getSkill().getAdapter().getConfig(), "abilities." + this.name.replace('_', '-') + '.' + extra);
    }

    public double getExtraScaling(SkilledPlayer info, String extra, EntityDamageByEntityEvent event) {
        LivingEntity entity = (LivingEntity) event.getEntity();
        return getExtraScaling(info, extra, "damage", event.getDamage(), "hp", entity.getHealth(),
                "maxHp", entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    }

    public double getExtraScaling(SkilledPlayer info, String extra, Object... edits) {
        Objects.requireNonNull(info, "Player info cannot be null");
        String scaling = getScalingEquation(info, extra);
        return getAbsoluteScaling(info, scaling, edits);
    }

    public int getCost(SkilledPlayer info) {
        String cost = getExtra(info).getString("cost");
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

    public SkilledPlayer checkup(Player player) {
        GameMode mode = player.getGameMode();
        if (mode == GameMode.SPECTATOR) return null;
        if (mode == GameMode.CREATIVE && !player.hasPermission("skills.use-creative")) return null;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);

        if (!info.getSkill().hasAbility(this)) return null;
        if (info.isAbilityDisabled(this)) return null;

        if (!this.name.endsWith("passive") && info.getImprovementLevel(this) < 1) return null;
        if (!this.isPassive() && !((ActiveAbility) this).activateOnReady && !info.isActiveReady()) return null;

        if (SkillsConfig.isInDisabledWorld(player.getWorld())) return null;
        if (ServiceHandler.isInRegion(player, getDisabledRegions(info))) return null;
        return info;
    }

    public List<String> getDisabledRegions(SkilledPlayer info) {
        return getExtra(info).getStringList("disabled-regions");
    }

    public Object[] applyEdits(SkilledPlayer info) {
        return new Object[0];
    }

    public Object[] copyEdits(SkilledPlayer info, Object[] edits) {
        Object[] apply = applyEdits(info);
        List<Object> masterEdit = new ArrayList<>(Arrays.asList(edits));
        masterEdit.addAll(Arrays.asList(apply));
        return masterEdit.toArray();
    }

    public int getRequiredLevel(SkilledPlayer info) {
        String scaling = getExtra(info).getString("required-level");
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
