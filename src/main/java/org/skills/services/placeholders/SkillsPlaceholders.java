package org.skills.services.placeholders;

import com.google.common.base.Enums;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.skills.api.events.CustomHudChangeEvent;
import org.skills.data.managers.Cosmetic;
import org.skills.data.managers.SkilledPlayer;
import org.skills.events.SkillsEvent;
import org.skills.events.SkillsEventManager;
import org.skills.events.SkillsEventType;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;
import org.skills.types.SkillScaling;
import org.skills.types.Stat;
import org.skills.utils.MathUtils;
import org.skills.utils.StringUtils;
import org.skills.utils.versionsupport.VersionSupport;

import java.util.Locale;

public enum SkillsPlaceholders {
    // General
    SKILL, SKILL_DISPLAYNAME, LEVEL, SOUL, XP, MAXXP, RAWXP,
    ENERGY, PRECISE_ENERGY, ENERGY_REGEN, MAX_PRECISE_ENERGY, MAX_ENERGY, ENERGY_TYPE, ENERGY_SYMBOL, ENERGY_STRING, ENERGY_COLOR,
    HEALTH, MAX_HEALTH, STATUS, ACTIVE_COOLDOWN, LAST_ABILITY_COOLDOWN,
    PARTY_NAME, PARTY_MEMBERS, PARTY_ONLINE_MEMBERS, PARTY_OFFLINE_MEMBERS, PARTY_RANK, LAST_ABILITY,

    // Events
    SOUL_EVENT_IS_RUNNING, SOUL_EVENT_DURATION, SOUL_EVENT_MULTIPLIER, SOUL_EVENT_ACTIVATED_BY,
    XP_EVENT_IS_RUNNING, XP_EVENT_DURATION, XP_EVENT_MULTIPLIER, XP_EVENT_ACTIVATED_BY,

    // Bonuses
    SOUL_BONUS_IS_RUNNING, XP_BONUS_IS_RUNNING, XP_BONUS_DURATION, SOUL_BONUS_DURATION,
    SOUL_BONUS_MULTIPLIER, XP_BONUS_MULTIPLIER;

    protected static final String IDENTIFIER = "skills";
    private static final String IDENTIFY = '%' + IDENTIFIER + '_';

    public static @NonNull
    String translatePlaceholders(@NonNull OfflinePlayer player, @NonNull String str) {
        return evaluatePlaceholders(SkilledPlayer.getSkilledPlayer(player), str, IDENTIFIER);
    }

    public static String evaluatePlaceholders(SkilledPlayer info, String str, String identifier) {
        char closure = '%';
        int len = str.length();
        StringBuilder builder = new StringBuilder(len);
        StringBuilder pursuit = new StringBuilder(len / 4);
        char[] identifierChars = identifier.toCharArray();
        int stage = -1;

        for (char ch : str.toCharArray()) {
            // Check if the placeholder ends or starts.
            if (ch == closure) {
                // If it's an ending placeholder.
                if (stage == -3) {
                    String placeholder = pursuit.toString();
                    String translated = onRequest(info, placeholder);

                    if (translated == null) builder.append(pursuit).append(ch);
                    else builder.append(translated);
                    stage = -1;
                    continue;
                } else if (stage == -2) {
                    builder.append(closure).append(identifierChars).append(closure);
                }

                pursuit.setLength(0);
                pursuit.append(ch);
                stage = 0;
                continue;
            }

            // Placeholder identifier started.
            if (stage >= 0) {
                // Save in case this wasn't meant to be a placeholder.
                pursuit.append(ch);

                // Compare the current character with the idenfitier's.
                if (ch == identifierChars[stage]) {
                    if (stage + 1 == identifierChars.length) {
                        // Identifier found, search for the placeholder name now...
                        stage = -2;
                        pursuit.setLength(0);
                        continue;
                    }
                    stage++;
                } else {
                    // If it doesn't match, append our pursuit to the main string.
                    builder.append(pursuit);
                    stage = -1;
                }
                continue;
            }

            // Placeholder identifier check passed, now checking for the placeholder name.
            if (stage == -2) {
                if (ch != '_') {
                    builder.append(closure).append(identifierChars).append(ch);
                    pursuit.setLength(0);
                    stage = -1;
                } else stage = -3;
                continue;
            } else if (stage == -3) {
                pursuit.append(ch);
                continue;
            }

            // Nothing placeholder related was found.
            builder.append(ch);
        }

        if (stage == 0) builder.append(pursuit);
        return builder.toString();
    }

    public static String onRequest(SkilledPlayer info, String identifier) {
        boolean shortie = identifier.startsWith("short_");
        boolean fancy = !shortie && identifier.startsWith("fancy_");
        if (shortie || fancy) identifier = identifier.substring(6);

        Object placeholdedObj;
        if (identifier.startsWith("stat_")) {
            String rest = identifier.substring(5);
            int index = rest.indexOf('_');
            Stat stat = Stat.getStat(index == -1 ? rest : rest.substring(index + 1));
            if (index == -1) placeholdedObj = info.getStat(stat);
            else {
                String param = rest.substring(0, index).toLowerCase(Locale.ENGLISH);
                switch (param) {
                    case "color":
                        placeholdedObj = stat.getColor();
                        break;
                    case "maxlvl":
                        placeholdedObj = stat.getMaxLevel();
                        break;
                    case "name":
                        placeholdedObj = stat.getName();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown stat placeholder parameter: '" + param + "' in " + identifier);
                }
            }
        } else if (identifier.startsWith("cosmetic_")) {
            int index = identifier.indexOf('_', 10);
            if (index == -1) {
                if (identifier.length() == 9) placeholdedObj = null;
                else {
                    String category = identifier.substring(9);
                    Cosmetic cosmetic = info.getCosmetic(category);
                    if (cosmetic != null) placeholdedObj = cosmetic.getName();
                    else placeholdedObj = null;
                }
            } else {
                String category = identifier.substring(9, index);
                Cosmetic cosmetic = info.getCosmetic(category);

                if (cosmetic != null) {
                    identifier = identifier.substring(index + 1).toLowerCase(Locale.ENGLISH);
                    if (identifier.equals("displayname")) placeholdedObj = cosmetic.getDisplayname();
                    else if (identifier.equals("color")) placeholdedObj = cosmetic.getColor();
                    else placeholdedObj = null;
                } else placeholdedObj = null;
            }
        } else {
            String finalIdentity = identifier.toUpperCase(Locale.ENGLISH);
            SkillsPlaceholders placeholder = Enums.getIfPresent(SkillsPlaceholders.class, finalIdentity).orNull();
            placeholdedObj = placeholder == null ? null : placeholder.translate(info);
        }

        if (placeholdedObj != null) {
            if (shortie || fancy) {
                if (placeholdedObj instanceof Number) {
                    double number = ((Number) placeholdedObj).doubleValue();
                    if (shortie) return MathUtils.getShortNumber(number);
                    return StringUtils.toFancyNumber(number);
                }
            }
            return placeholdedObj.toString();
        }
        return null;
    }

    public static Object translate(@NonNull SkillsPlaceholders holder, @NonNull SkilledPlayer info) {
        switch (holder) {
            case PARTY_NAME:
                return info.hasParty() ? info.getParty().getName() : "";
            case PARTY_MEMBERS:
                return info.hasParty() ? info.getParty().getMembers().size() : 0;
            case PARTY_ONLINE_MEMBERS:
                return info.hasParty() ? info.getParty().getOnlineMembers().size() : 0;
            case PARTY_OFFLINE_MEMBERS:
                return info.hasParty() ? info.getParty().getMembers().size() - info.getParty().getOnlineMembers().size() : 0;
            case PARTY_RANK:
                return info.hasParty() ? info.getRank().toString() : "";
            case ACTIVE_COOLDOWN:
                int timeLeft = (int) Math.ceil(info.getCooldownTimeLeft());

                if (timeLeft <= 0) return SkillsConfig.ACTIONBAR_COOLDOWN_READY.parse(info.getOfflinePlayer());
                return SkillsConfig.ACTIONBAR_COOLDOWN_NOT_READY.parse(info.getOfflinePlayer(), "%time%", (int) Math.ceil(timeLeft / 1000D));
            case LAST_ABILITY_COOLDOWN:
                if (info.getLastAbilityUsed() == null) return 0;
                return (int) info.getLastAbilityUsed().getCooldown(info);
            case HEALTH:
                Player player = info.getPlayer();
                if (player == null) return 0;
                int precision = SkillsConfig.PRECISIONS_HEALTHS.getInt();
                if (precision <= 0) return (int) player.getHealth();
                return MathUtils.roundToDigits(player.getHealth(), precision);
            case MAX_HEALTH:
                player = info.getPlayer();
                if (player == null) return 0;
                precision = SkillsConfig.PRECISIONS_HEALTHS.getInt();
                if (precision <= 0) return (int) VersionSupport.getMaxHealth(player);
                return MathUtils.roundToDigits(VersionSupport.getMaxHealth(player), precision);
            case ENERGY:
                player = info.getPlayer();
                if (player == null) return 0;
                precision = SkillsConfig.PRECISIONS_ENERGY.getInt();
                if (precision <= 0) return (int) info.getEnergy();
                return MathUtils.roundToDigits(info.getEnergy(), precision);
            case PRECISE_ENERGY:
                return info.getEnergy();
            case LAST_ABILITY:
                return info.getLastAbilityUsed() != null ? info.getLastAbilityUsed().getName() : "";
            case MAX_PRECISE_ENERGY:
                return info.getScaling(SkillScaling.MAX_ENERGY);
            case MAX_ENERGY:
                player = info.getPlayer();
                if (player == null) return 0;
                precision = SkillsConfig.PRECISIONS_ENERGY.getInt();
                if (precision <= 0) return (int) info.getScaling(SkillScaling.MAX_ENERGY);
                return MathUtils.roundToDigits(info.getScaling(SkillScaling.MAX_ENERGY), precision);
            case ENERGY_REGEN:
                return info.getScaling(SkillScaling.ENERGY_REGEN);
            case ENERGY_STRING:
                int energyLeft = (int) info.getEnergy();
                int scaling = (int) info.getScaling(SkillScaling.MAX_ENERGY);

                StringBuilder energy = new StringBuilder();
                int roundedEnergy = (int) MathUtils.getAmountFromAmount(20, scaling, energyLeft);
                int substract = 20 - roundedEnergy;

                if (roundedEnergy != 20) {
                    for (int i = roundedEnergy; i > 0; i--)
                        energy.append(SkillsConfig.ACTIONBAR_ENERGY_CHARGED.parse(info.getOfflinePlayer()));

                    for (int i = substract; i > 0; i--)
                        energy.append(SkillsConfig.ACTIONBAR_ENERGY_EMPTY.parse(info.getOfflinePlayer()));
                } else {
//                    int pos = CustomHudChangeEvent.ANIMATION.get(info.getId());
//                    for (int i = 20; i > 0; i--) {
//                        if (i == pos) energy.append(SkillsLang.ActionBar_Energy_Full.parse(info.getOfflinePlayer()));
//                        else energy.append(SkillsLang.ActionBar_Energy_Full_Second.parse(info.getOfflinePlayer()));
//                    }
                    boolean oneTwo = CustomHudChangeEvent.isAnimated(info.getId());
                    for (int i = 20; i > 0; i--) {
                        if (oneTwo) energy.append(SkillsConfig.ACTIONBAR_ENERGY_FULL.parse(info.getOfflinePlayer()));
                        else energy.append(SkillsConfig.ACTIONBAR_ENERGY_FULL_SECOND.parse(info.getOfflinePlayer()));
                        oneTwo = !oneTwo;
                    }
                }

                return energy.toString();
            case ENERGY_COLOR:
                if (!info.hasSkill()) return "";
                return MessageHandler.colorize(info.getSkill().getEnergy().getColor());
            case ENERGY_SYMBOL:
                if (!info.hasSkill()) return "";
                return MessageHandler.colorize(info.getSkill().getEnergy().getSymbol());
            case ENERGY_TYPE:
                if (!info.hasSkill()) return "";
                return MessageHandler.colorize(info.getSkill().getEnergy().getName());
            case LEVEL:
                return info.getLevel();
            case MAXXP:
                return MathUtils.roundToDigits(info.getLevelXP(), 2);
            case RAWXP:
                return MathUtils.roundToDigits(info.getRawXP(), 2);
            case SKILL:
                return info.getSkillName();
            case SKILL_DISPLAYNAME:
                return info.getSkill().getDisplayName();
            case SOUL:
                return info.getSouls();
            case STATUS:
                player = info.getPlayer();
                if (player == null || !player.isOnline()) return "";
                if (player.hasPotionEffect(PotionEffectType.POISON))
                    return SkillsConfig.ACTIONBAR_STATUS_POISONED.parse(player);
                else if (player.getHealth() <= 6)
                    return SkillsConfig.ACTIONBAR_STATUS_LOWHEALTH.parse(player);
                else if (info.isActiveReady())
                    return SkillsConfig.ACTIONBAR_STATUS_ACTIVATED.parse(player);

                return SkillsConfig.ACTIONBAR_STATUS_OK.parse(player);
            case XP:
                return MathUtils.roundToDigits(info.getXP(), 2);
        }

        return translateEvents(holder, info);
    }

    private static Object translateEvents(@NonNull SkillsPlaceholders holder, @NonNull SkilledPlayer info) {
        SkillsEvent soul = null;
        SkillsEvent exp = null;
        SkillsLang msg = null;

        if (holder.name().contains("EVENT")) {
            if (holder.name().startsWith("XP_EVENT")) exp = SkillsEventManager.getEvent(SkillsEventType.XP);
            else if (holder.name().startsWith("SOUL_EVENT")) soul = SkillsEventManager.getEvent(SkillsEventType.SOUL);

            if (exp != null || soul != null) msg = SkillsLang.EVENT_NOT_RUNNING;
        } else if (holder.name().contains("BONUS")) {
            if (holder.name().startsWith("XP_BONUS")) exp = info.getBonus(SkillsEventType.XP);
            else if (holder.name().startsWith("SOUL_BONUS")) soul = info.getBonus(SkillsEventType.SOUL);

            if (exp != null || soul != null) msg = SkillsLang.BONUS_INACTIVE;
        }

        if (msg == null) return "";
        if (exp == null && soul == null) return msg.parse(info.getOfflinePlayer());

        switch (holder) {
            // Running
            case SOUL_EVENT_IS_RUNNING:
            case SOUL_BONUS_IS_RUNNING:
                return soul.isActive() ? "&aActive" : msg.parse(info.getOfflinePlayer());
            case XP_EVENT_IS_RUNNING:
            case XP_BONUS_IS_RUNNING:
                return exp.isActive() ? "&aActive" : msg.parse(info.getOfflinePlayer());
            // Duration
            case SOUL_EVENT_DURATION:
            case SOUL_BONUS_DURATION:
                return soul.getDisplayTime();
            case XP_EVENT_DURATION:
            case XP_BONUS_DURATION:
                return exp.getDisplayTime();
            // Multiplier
            case SOUL_EVENT_MULTIPLIER:
            case SOUL_BONUS_MULTIPLIER:
                return soul.calcMultiplier(info.getOfflinePlayer());
            case XP_EVENT_MULTIPLIER:
            case XP_BONUS_MULTIPLIER:
                return exp.calcMultiplier(info.getOfflinePlayer());
            case SOUL_EVENT_ACTIVATED_BY:
                return soul.getId();
            case XP_EVENT_ACTIVATED_BY:
                return exp.getId();
        }
        return null;
    }

    public Object translate(SkilledPlayer player) {
        return translate(this, player);
    }

    public String getPlaceholder() {
        return '%' + IDENTIFIER + '_' + this.name().toLowerCase() + '%';
    }
}