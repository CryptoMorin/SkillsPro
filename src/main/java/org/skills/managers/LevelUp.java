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
    private final String message;
    private final ConfigurationSection title;
    private final String xp;
    private final String souls;
    private final String statPoints;
    private final List<String> commands;

    private Title titleEval;
    private String messageEval;
    private int xpEval;
    private int soulsEval;
    private int statPointsEval;
    private List<String> commandsEval;

    private LevelUp(ConfigurationSection section) {
        this.message = section.getString("message");
        this.title = section.getConfigurationSection("title");
        this.xp = section.getString("xp");
        this.souls = section.getString("souls");
        this.statPoints = section.getString("statpoints");
        this.commands = section.getStringList("commands");

        Validate.isTrue(xp != null, "Could not find XP property for level: " + section.getName());
    }

    public static int getLevel(SkilledPlayer info, int lvl) {
        return (int) MathUtils.evaluateEquation(ServiceHandler.translatePlaceholders(
                info.getOfflinePlayer(), StringUtils.replace(getProperties(lvl).getXp(), "lvl", String.valueOf(lvl))));
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
        return new LevelUp(section);
    }

    public void perform(SkilledPlayer info, Object... edits) {
        OfflinePlayer offPlayer = info.getOfflinePlayer();
        if (offPlayer.isOnline()) {
            Player player = (Player) offPlayer;
            if (messageEval != null) player.sendMessage(MessageHandler.replaceVariables(this.messageEval, edits));
            if (titleEval != null) Titles.sendTitle(player, titleEval.fadeIn, titleEval.stay, titleEval.fadeOut,
                    MessageHandler.replaceVariables(titleEval.title, edits),
                    MessageHandler.replaceVariables(titleEval.subtitle, edits));
        }
        info.addSouls(this.soulsEval);
        info.addStat(Stat.POINTS, this.statPointsEval);

        Player player = offPlayer.getPlayer();
        for (String command : this.commandsEval) {
            CommandSender executor = command.toUpperCase().startsWith("CONSOLE:") ? Bukkit.getConsoleSender() : player;
            if (executor == player && player == null) continue;
            int index = command.indexOf(':');
            String cmd = MessageHandler.replaceVariables(command.substring(index + 1), edits);
            Bukkit.dispatchCommand(executor, cmd);
        }
    }

    public void celebrate(Player player, JavaPlugin plugin, int lvl) {
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

    public LevelUp evaluate(SkilledPlayer info, int lvl) {
        OfflinePlayer player = info.getOfflinePlayer();
        this.xpEval = (int) MathUtils.evaluateEquation(ServiceHandler.translatePlaceholders(player, StringUtils.replace(xp, "lvl", String.valueOf(lvl))));
        this.soulsEval = Strings.isNullOrEmpty(this.souls) ? 0 :
                (int) MathUtils.evaluateEquation(ServiceHandler.translatePlaceholders(player, StringUtils.replace(souls, "lvl", String.valueOf(lvl))));
        this.statPointsEval = Strings.isNullOrEmpty(this.statPoints) ? 0 :
                (int) MathUtils.evaluateEquation(ServiceHandler.translatePlaceholders(player, StringUtils.replace(statPoints, "lvl", String.valueOf(lvl))));
        this.messageEval = Strings.isNullOrEmpty(this.message) ? null :
                MessageHandler.colorize(MessageHandler.replaceVariables(ServiceHandler.translatePlaceholders(player, message),
                        "%lvl%", lvl, "%xp_required%", xpEval, "%gained_stats%", statPointsEval, "%gained_souls%", soulsEval));

        if (this.title != null) {
            String ttile = title.getString("title");
            String tttile = Strings.isNullOrEmpty(ttile) ? "" :
                    MessageHandler.colorize(MessageHandler.replaceVariables(ServiceHandler.translatePlaceholders(player, ttile),
                            "%lvl%", lvl, "%xp_required%", xpEval, "%gained_stats%", statPointsEval, "%gained_souls%", soulsEval));

            String subtitle = title.getString("subtitle");
            String subttitle = Strings.isNullOrEmpty(subtitle) ? "" :
                    MessageHandler.colorize(MessageHandler.replaceVariables(ServiceHandler.translatePlaceholders(player, subtitle),
                            "%lvl%", lvl, "%xp_required%", xpEval, "%gained_stats%", statPointsEval, "%gained_souls%", soulsEval));
            this.titleEval = new Title(tttile, subttitle, title.getInt("fade-in"), title.getInt("stay"), title.getInt("fade-out"));
        }

        List<String> evaledCommands = new ArrayList<>();
        if (this.commands != null) {
            for (String cmd : this.commands) {
                evaledCommands.add(MessageHandler.colorize(ServiceHandler.translatePlaceholders(player, cmd)));
            }
        }
        this.commandsEval = evaledCommands;
        return this;
    }

    public List<String> getCommands() {
        return commands;
    }

    public String getStatPoints() {
        return statPoints;
    }

    public String getSouls() {
        return souls;
    }

    public String getXp() {
        return xp;
    }

    public String getMessage() {
        return message;
    }

    public int getXpEval() {
        return xpEval;
    }

    public void setXpEval(int xpEval) {
        this.xpEval = xpEval;
    }

    public int getSoulsEval() {
        return soulsEval;
    }

    public void setSoulsEval(int soulsEval) {
        this.soulsEval = soulsEval;
    }

    public int getStatPointsEval() {
        return statPointsEval;
    }

    public void setStatPointsEval(int statPointsEval) {
        this.statPointsEval = statPointsEval;
    }

    public List<String> getCommandsEval() {
        return commandsEval;
    }

    public void setCommandsEval(List<String> commandsEval) {
        this.commandsEval = commandsEval;
    }
}
