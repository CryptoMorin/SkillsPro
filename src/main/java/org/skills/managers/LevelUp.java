package org.skills.managers;

import com.cryptomorin.xseries.messages.Titles;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.google.common.base.Strings;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.MessageHandler;
import org.skills.services.manager.ServiceHandler;
import org.skills.types.Stat;
import org.skills.utils.MathUtils;
import org.skills.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LevelUp {
    private final ConfigurationSection section;
    private int xp, souls, statPoints;
    private List<String> commands;
    private OfflinePlayer player;
    private SkilledPlayer info;
    private int lvl;

    private LevelUp(ConfigurationSection section) {
        this.section = section;
        this.commands = section.getStringList("commands");
    }

    public static int getLevel(SkilledPlayer info, int lvl) {
        return getProperties(lvl).forPlayer(info.getOfflinePlayer()).evaluateRewards().xp;
    }

    public static LevelUp getProperties(int level) {
        Validate.isTrue(level >= 0, "No levelup properties for levels lower than 0");
        ConfigurationSection lvlSection = SkillsConfig.LEVELS.getSection();
        ConfigurationSection section = lvlSection.getConfigurationSection(String.valueOf(level));
        if (section == null) {
            Set<String> keys = SkillsConfig.LEVELS.getSectionSet();
            int i = 0;

            for (String key : keys) {
                int k = NumberUtils.toInt(key, i);
                if (k > level && i <= level) {
                    section = lvlSection.getConfigurationSection(String.valueOf(i));
                    break;
                }

                i = k;
            }
            if (section == null) section = lvlSection.getConfigurationSection(String.valueOf(i));
        }
        return new LevelUp(section).level(level);
    }

    public LevelUp forPlayer(OfflinePlayer player) {
        this.player = player;
        this.info = SkilledPlayer.getSkilledPlayer(player);
        return this;
    }

    public LevelUp level(int lvl) {
        this.lvl = lvl;
        return this;
    }

    public LevelUp performRewards() {
        info.addSouls(this.souls);
        info.addStat(Stat.POINTS, this.statPoints);

        Player player = info.getPlayer();
        for (String command : this.commands) {
            CommandSender executor = command.toUpperCase().startsWith("CONSOLE:") ? Bukkit.getConsoleSender() : player;
            if (executor == player && player == null) continue;
            int index = command.indexOf(':');
            String cmd = command.substring(index + 1);
            Bukkit.dispatchCommand(executor, cmd);
        }
        return this;
    }

    public LevelUp performMessages() {
        OfflinePlayer offPlayer = info.getOfflinePlayer();
        if (offPlayer.isOnline()) {
            Player player = (Player) offPlayer;

            Object[] edits = {
                    "%lvl%", lvl,
                    "%xp_required%", StringUtils.toFancyNumber(xp),
                    "%gained_stats%", StringUtils.toFancyNumber(statPoints),
                    "%gained_souls%", StringUtils.toFancyNumber(souls),
                    "%next_maxxp%", StringUtils.toFancyNumber(info.getLevelXP(lvl))
            };
            String message = section.getString("message");
            if (!Strings.isNullOrEmpty(message)) {
                player.sendMessage(translateMessage(message, edits));
            }
            ConfigurationSection titleSection = section.getConfigurationSection("title");
            if (titleSection != null) {
                String title = titleSection.getString("title");
                if (!Strings.isNullOrEmpty(title)) title = translateMessage(title, edits);

                String subtitle = titleSection.getString("subtitle");
                if (!Strings.isNullOrEmpty(subtitle)) subtitle = translateMessage(subtitle, edits);

                Titles.sendTitle(player,
                        titleSection.getInt("fade-in"), titleSection.getInt("stay"), titleSection.getInt("fade-out"),
                        title, subtitle
                );
            }
        }
        return this;
    }

    private String translateMessage(String msg, Object... edits) {
        return MessageHandler.colorize(MessageHandler.replaceVariables(ServiceHandler.translatePlaceholders(player, msg), edits));
    }

    private int eval(String expression) {
        if (Strings.isNullOrEmpty(expression)) return 0;
        return (int) Math.round(MathUtils.evaluateEquation(ServiceHandler.translatePlaceholders(player, StringUtils.replace(expression, "lvl", String.valueOf(lvl)))));
    }

    public void celebrate(Player player, JavaPlugin plugin) {
        ParticleDisplay display = ParticleDisplay.simple(player.getLocation(), Particle.CRIT_MAGIC);
        int height = Math.min(Math.max(5, lvl / 2), 15);
        XParticle.dnaReplication(plugin, 1, 0.2, 3, 1, height, 2, display);

        int fireworks = MathUtils.randInt(2, 5);
        int lowLv2 = Math.max(1, lvl / 2);
        List<Color> colors = new ArrayList<>();
        for (int j = 0; j < lowLv2; j++) colors.add(Color.fromRGB(MathUtils.randInt(0, 255), MathUtils.randInt(0, 255), MathUtils.randInt(0, 255)));

        for (int i = 0; i < fireworks; i++) {
            Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
            FireworkMeta meta = firework.getFireworkMeta();
            FireworkEffect effect =
                    FireworkEffect.builder().withColor(colors.stream().limit(lowLv2).collect(Collectors.toList()))
                            .with(FireworkEffect.Type.values()[MathUtils.randInt(0, FireworkEffect.Type.values().length - 1)])
                            .withTrail().build();
            if (height > 10) meta.setPower(1);
            meta.addEffect(effect);
            firework.setMetadata("LVLUP", new FixedMetadataValue(plugin, null));
            firework.setFireworkMeta(meta);
        }
    }

    public LevelUp add(LevelUp other) {
        this.xp += other.xp;
        this.souls += other.souls;
        this.statPoints += other.statPoints;
        this.commands.addAll(other.commands);
        return this;
    }

    public LevelUp evaluateRewards() {
        this.xp = eval(section.getString("xp"));
        this.souls = eval(section.getString("souls"));
        this.statPoints = eval(section.getString("statpoints"));

        List<String> evaledCommands = new ArrayList<>();
        if (this.commands != null) {
            for (String cmd : this.commands) {
                evaledCommands.add(translateMessage(cmd));
            }
        }
        this.commands = evaledCommands;
        return this;
    }
}
