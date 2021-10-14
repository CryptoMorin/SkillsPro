package org.skills.main;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.skills.abilities.Ability;
import org.skills.data.managers.PlayerSkill;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.LanguageManager;
import org.skills.main.locale.MessageHandler;
import org.skills.utils.MathUtils;
import org.skills.utils.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public enum SkillsConfig {
    PREFIX(false),
    LANG("en"),
    DEBUG(false),
    CHECK_UPDATES(true),
    DATABASE("JSON"),
    //    DATABASE_USER("", 1),
//    DATABASE_PASSWORD("", 1),
    LOAD_ALL_DATA_ON_STARTUP(true),
    AUTOSAVE_INTERVAL("30mins"),
    ANNOUNCE_AUTOSAVES(true),
    COMMANDS_EACH_PAGE(5),
    SMART_DAMAGE("smart-damage"),
    BACKUPS_ENABLED(true, 1),
    BACKUPS_IGNORE_TODAYS_BACKUP(false, 1),
    BACKUPS_DELETE_BACKUPS_OLDER_THAN(30, 1),
    //    TOP_SORT_BY(2), TODO....
//    TOP_AMOUNT(2),
    BUY_CLASS_ONCE(true),
    ACTIONBAR_ENABLED(true, 1),
    ACTIONBAR_DEFAULT(true, 1),
    ACTIONBAR_FREQUENCY(10, 1),
    ACTIONBAR_NORMAL("&8-=( %skills_status% &9∫ %skills_energy_string%&l%skills_energy_symbol% &9∫ &3Lvl. &e%skills_short_level% &9∫" +
            " &3%skills_short_xp%&8/&3%skills_short_maxxp% &9∫ &3%skills_short_soul% &9∫ &3%skills_active_cooldown% &8)=-", 1),
    ACTIONBAR_ACTIVATED_SKILL("&4-=<=[ %skills_status% &c︴ %skills_energy_string%&l%skills_energy_symbol% &c︴ &6ℒνℓ. &e%skills_short_level% &c︴" +
            " &6%skills_short_xp%&8/&6%skills_short_maxxp% &c︴ &6%skills_short_soul% &4]=>=-", 1),
    ACTIONBAR_STATUS_OK("&2❤", 1, 2), // ⌇
    ACTIONBAR_STATUS_POISONED("&5☣", 1, 2), // ℒｖℓ - ℒγℓ - ℒνℓ
    ACTIONBAR_STATUS_LOWHEALTH("&4♥", 1, 2),
    ACTIONBAR_STATUS_ACTIVATED("&0&o❦", 1, 2),
    ACTIONBAR_COOLDOWN_READY("&200 ⌛", 1, 2),
    ACTIONBAR_COOLDOWN_NOT_READY("&c%time% ⌛", 1, 2),
    ACTIONBAR_ENERGY_CHARGED("%skills_energy_color%❘", 1, 2),
    ACTIONBAR_ENERGY_EMPTY("&8❘", 1, 2),
    ACTIONBAR_ENERGY_FULL("%skills_energy_color%&o❘", 1, 2),
    ACTIONBAR_ENERGY_FULL_SECOND("&0&o┃", 1, 2),
    LOSS_MIN_LEVEL(0),

    PRECISIONS_HEALTHS(2, 1),
    PRECISIONS_ENERGY(1, 1),

    DEFAULT_HEALTH_SCALING(40),
    LEVEL_CELEBRATION(true),

    SKILL_CHANGE_RESET_LEVEL(false, 3),
    SKILL_CHANGE_RESET_SOULS(false, 3),
    SKILL_CHANGE_RESET_STATS(false, 3),
    SKILL_CHANGE_RESET_ABILITIES(false, 3),
    SKILL_CHANGE_RESET_MASTERIES(false, 3),

    BOSSBAR_LEVELS_ENABLED(true, 1, 2),
    BOSSBAR_LEVELS_FREQUENCY(0, 1, 2),
    BOSSBAR_LEVELS(null, 1),
    BOSSBAR_EVENTS_ENABLED(true, 1, 2),
    BOSSBAR_EVENTS(null, 1),
    BOSSBAR_BONUSES_ENABLED(true, 1, 2),
    BOSSBAR_BONUSES(null, 1),
    AUTO_SELECT_ON_JOIN(false),
    DEFAULT_SKILL(PlayerSkill.NONE),
    VANILLA_EXP_BAR_ENABLED(true, 3),
    VANILLA_EXP_BAR_SHOWN_NUMBER(true, 3),
    VANILLA_EXP_BAR_REAL_SYNC(false, 3),
    SYNC_ENCHANTMENT_TABLES_ENABLED(false, 3),
    SYNC_ENCHANTMENT_TABLES_REQUIREMENT_LEVEL_EQUATION("%skills_level%", 3),
    SYNC_ENCHANTMENT_TABLES_COST_TYPE("souls", 3),
    TIME_FORMAT("hha:mm:ss"),
    DISCORDSRV_PARTY_CHANNEL("$main"),
    ECONOMY_DEPOSIT_ENABLED(true, 1, 2),
    ECONOMY_DEPOSIT_SOUL_WORTH(50.0, 1, 2),
    ECONOMY_WITHDRAW_ENABLED(true, 1, 2),
    ECONOMY_WITHDRAW_SOUL_WORTH(true, 1, 2),
    DISABLED_WORLDS_PLUGIN(new String[0], 2),
    DISABLED_WORLDS_SOUL_LOSS(new String[0], 2),
    DISABLED_WORLDS_SOUL_GAIN(new String[0], 2),
    DISABLE_ABILITIES_IN_REGIONS(true),
    DISABLED_WORLDS_XP_GAIN(new String[0], 2),
    DISABLED_WORLDS_XP_LOSS(new String[0], 2),
    SKILL_ACTIVATION_TIME(500),
    XP(new String[0]),
    DEFAULT_XP(-1),
    DEFAULT_SOULS(1),
    SOULS(new String[0]),
    DISABLE_CREATIVE_FIRE(true),

    STARTER_ENABLED(false, 1),
    STARTER_LEVEL(0, 1),
    STARTER_ABILITIES_LEVEL(0, 1),
    STARTER_MASTERIES(0, 1),
    STARTER_SOULS(0, 1),
    STARTER_STATS(null, 1),

    ARMOR_WEIGHTS_ENABLED(false, 2),
    ARMOR_WEIGHTS_RESET_SPEEDS_ENABLED(false, 2, 4),
    ARMOR_WEIGHTS_RESET_SPEEDS_AMOUNT(0.2f, 2, 4),
    ARMOR_WEIGHTS_WEIGHTS(new String[0], 2),
    ARMOR_WEIGHTS_CUSTOM(new String[0], 2),

    SPAWNERS_XP("%xp% / 3", 1),
    SKILLS_SHARED_DATA_SOULS(true, 3),
    SKILLS_SHARED_DATA_STATS(false, 3),
    SKILLS_SHARED_DATA_LEVELS(false, 3),
    CLOSE_GUI_ON_DAMAGE(false),
    SPAWNERS_SOULS(0, 1),
    PREVENT_ACTIVATION_ITEMS(new String[0], 2),
    PREVENT_ACTIVATION_BLOCKS(new String[0], 2),
    DEFAULT_MAX_LEVEL(100),
    LEVELS(null),

    READY_PARTICLE_PARTICLE("SPELL_WITCH", 2),
    READY_PARTICLE_COUNT("lvl * 10", 2),
    READY_PARTICLE_OFFSET(0.1, 2),

    BLOOD_ENABLED(true, 1),
    BLOOD_DEFAULT("STEP_SOUND, REDSTONE_BLOCK, 10", 1),
    BLOOD_SHIELD("STEP_SOUND, OAK_WOOD, 10", 1),
    BLOOD_CUSTOM_MOBS(null, 1),
    BLOOD_DISABLED_MOBS(new String[0], 1),

    RED_SCREEN_ENABLED(true, 2),
    RED_SCREEN_DURATION(10, 2),
    RED_SCREEN_HEALTH(50, 2),

    LAST_BREATH_ENABLED(false, 2),
    LAST_BREATH_INVULNERABILITY(15, 2),
    LAST_BREATH_INTENSITY_RESISTANCE(10, 2),
    LAST_BREATH_BLEED_OUT(60, 2),
    LAST_BREATH_DAMAGE(0.001, 2),
    LAST_BREATH_REVIVE_TIME(10, 2, 3),
    LAST_BREATH_REVIVE_DISTANCE(10, 2, 3),

    PULSE_ENABLED(true, 1),
    PULSE_DURATION(2000, 1),
    PULSE_HEALTH(20, 1),
    PULSE_LUB("BLOCK_NOTE_BLOCK_BASEDRUM", 1),
    PULSE_DUB("BLOCK_NOTE_BLOCK_BASEDRUM", 1),

    SOULS_LOSS_UPON_DEATH(5),
    XP_LOSS_UPON_DEATH(5),
    LOSE_SAME_SOULS_AS_KILLER_SOULS_GAIN(true),
    LOSE_SAME_XP_AS_KILLER_XP_GAIN(true),
    SKILL_CHANGE_COOLDOWN(60),
    COMMANDS_TO_PERFORM_SELECT(new String[0], 1),
    COMMANDS_TO_PERFORM_CHANGE(new String[0], 1),
    DISABLED_COMMANDS(new String[0]),
    ACTIVATION_KEY("R", 1),
    DEFAULT_GUI_CLICK_SOUND(null),
    MASTERY_LOGGING_MATERIALS(new String[]{
            "OAK_LOG:OAK_LOG, 3, 100", "ACACIA_LOG:ACACIA_LOG, 2, 100", "BIRCH_LOG:BIRCH_LOG, 2, 100",
            "DARK_OAK_LOG:DARK_OAK_LOG, 4, 70", "JUNGLE_LOG:JUNGLE_LOG, 4, 70", "SPRUCE_LOG:SPRUCE_LOG, 2, 100"
    }),
    MASTERIES_ENABLED(true, 1),
    STATS_ALLOW_RESET(true, 1),
    STATS_RESET_COST(0, 1),
    FRIENDS_FRIENDLY_FIRE(false, 1),
    FRIENDS_MAX_FRIENDS(50, 1),
    FRIENDS_TELEPORT_ENABLED(true, 1, 2),
    FRIENDS_TELEPORT_TIMER(5, 1, 2),
    FRIENDS_TELEPORT_SHOULD_NOT_MOVE(true, 1, 2),
    FRIENDS_TELEPORT_DELAY_BEFORE_MOVE_CHECK(3, 1, 2),
    FRIENDS_TELEPORT_REQUEST_TIME(60, 1, 2),
    PARTY_FRIENDLY_FIRE(false, 1),
    PARTY_DISTRIBUTE(true, 1),
    PARTY_MAX_MEMBERS(50, 1),
    PARTY_SOULS_PER_MEMBER("%skills_party% / 5 + souls", 1),
    PARTY_XP_PER_MEMBER("%skills_party% / 2 + exp", 1),
    PARTY_MEMBERS_MAX_DISTANCE(0, 1),
    PARTY_CHAT_FORMAT("&7[&5%skills_party_name%&7]&8|7[&5%skills_party_name%&7]&8|&7[&5%skills_party_rank%&7] %displayname% &8» &d", 1),
    PARTY_SPY_FORMAT("&7[&5SPY&7] &7[&5%skills_party_name%&7]&8|7[&5%skills_party_name%&7]&8|&7[&5%skills_party_rank%&7] %displayname% &8» &d", 1),
    KILL_MESSAGE(false),
    HOLOGRAM_ENABLED(true, 1),
    HOLOGRAM_EFFECT("LEVITATION, 1, 2", 1),
    HOLOGRAM_STAY(40, 1),
    HOLOGRAM_OFFSET(null, 1),
    HOLOGRAM_STATIC(false, 1),
    HOLOGRAM_LINES(new String[]{"&3Souls&8: &2+%souls%", "&3XP&8: &2+%xp%"}, 1),
    HOLOGRAM_DAMAGE_INDICATOR(new String[]{"&4%damage%"}, 1),
    HOLOGRAM_DAMAGE_INDICATOR_PRECISION(2, 1),
    HOLOGRAM_DISABLED_MOBS(new String[0], 1),
    MYTHICMOBS_WORLDS_WITH_LEVEL_MARGIN(new String[0], 1),
    MYTHICMOBS_XP_CONDITIONS(new String[0], 1),
    MYTHICMOBS_SOUL_CONDITIONS(new String[0], 1);

    private static final SkillsPro plugin = SkillsPro.get();
    private final String option;
    private final Object defaultValue;

    SkillsConfig(Object defaultValue) {
        this.option = this.name().toLowerCase().replace('_', '-');
        this.defaultValue = defaultValue;
    }

    SkillsConfig(Object defaultValue, int... grouped) {
        this.option = StringUtils.getGroupedOption(this.name(), grouped);
        this.defaultValue = defaultValue;
    }

    public static boolean isInDisabledWorld(Location loc) {
        return SkillsConfig.DISABLED_WORLDS_PLUGIN.getStringList().contains(loc.getWorld().getName());
    }

    public static boolean isInDisabledWorld(World world) {
        return SkillsConfig.DISABLED_WORLDS_PLUGIN.getStringList().contains(world.getName());
    }

    public static int getClosestLevelSection(ConfigurationSection masterSection, int level) {
        Validate.isTrue(level >= 0, "No level properties for levels lower than 1");
        Objects.requireNonNull(masterSection, "Cannot get closest level section from null master section");

        ConfigurationSection section = masterSection.getConfigurationSection(String.valueOf(level));
        int lvl = -1;
        if (section == null) {
            Set<String> keys = masterSection.getKeys(false);
            int i = 0;

            for (String key : keys) {
                int k = NumberUtils.toInt(key, i);
                if (k > level && i <= level) {
                    lvl = i;
                    break;
                }

                i = k;
            }
            if (lvl == -1) lvl = i;
        } else return level;
        return lvl;
    }

    public boolean isSet() {
        return plugin.getConfig().isSet(this.option);
    }

    public String getOption() {
        return this.option;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getString() {
        return plugin.getConfig().getString(this.option);
    }

    public String parse(OfflinePlayer player, Object... edits) {
        return LanguageManager.buildMessage(getString(), player, edits);
    }

    public List<String> getStringList() {
        return plugin.getConfig().getStringList(this.option);
    }

    public Set<String> getSectionSet() {
        return plugin.getConfig().getConfigurationSection(this.option).getKeys(false);
    }

    public ConfigurationSection getSection() {
        return plugin.getConfig().getConfigurationSection(this.option);
    }

    public double getDouble() {
        return plugin.getConfig().getDouble(this.option);
    }

    public double fromEquation(OfflinePlayer placeholder, Object... edits) {
        return MathUtils.evaluateEquation(MessageHandler.replaceVariables(parse(placeholder, edits)));
    }

    public long getTimeMillis() {
        return getTimeMillis(TimeUnit.MILLISECONDS);
    }

    public long getTimeMillis(TimeUnit timeUnit) {
        Long parsed = MathUtils.calcMillis(getString(), timeUnit);
        return parsed == null ? 0L : parsed;
    }

    public double eval(SkilledPlayer info, Ability ability) {
        String expression = getString();
        if (expression == null) return 0;
        return MathUtils.evaluateEquation(ability.getTranslatedScaling(info, expression));
    }

    public long getLong() {
        return plugin.getConfig().getLong(this.option);
    }

    public boolean getBoolean() {
        return plugin.getConfig().getBoolean(this.option);
    }

    public int getInt() {
        return plugin.getConfig().getInt(this.option);
    }
}
