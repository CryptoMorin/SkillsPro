package org.skills.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;

import java.util.Arrays;
import java.util.Objects;

public abstract class SkillsCommand {
    private final @NonNull
    String permission;
    private final @NonNull
    String name;
    private final @Nullable
    SkillsCommand group;
    private final @Nullable
    String description;
    private final boolean isGroup;
    private final @NonNull
    String[] aliases;
    public SkillsPro plugin;

    public SkillsCommand(@NonNull String name, @Nullable SkillsCommand group, @Nullable String description, boolean isGroup, String... aliases) {
        this.name = name;
        this.group = group;
        this.description = description;
        this.isGroup = isGroup;
        this.aliases = aliases;
        if (isDisabled()) {
            this.permission = null;
            return;
        }

        // Permission Handler
        SkillsCommand lastGroup = group;
        StringBuilder perms = new StringBuilder();
        while (lastGroup != null) {
            perms.insert(0, lastGroup.name + '.');
            lastGroup = lastGroup.group;
        }
        this.permission = "skills.command." + perms + this.name;

        // Register
        SkillsCommandHandler.commands.add(this);
        if (this instanceof Listener) Bukkit.getPluginManager().registerEvents((Listener) this, SkillsPro.get());
    }

    public SkillsCommand(@NonNull String name, @Nullable SkillsCommand group, @NonNull SkillsLang description, boolean isGroup, String... aliases) {
        this(name, group, description.parse(), isGroup, aliases);
    }

    public SkillsCommand(@NonNull String name, @Nullable SkillsCommand group, @NonNull SkillsLang description, String... aliases) {
        this(name, group, description.parse(), false, aliases);
    }

    public SkillsCommand(@NonNull String name, @NonNull SkillsLang description, boolean isGroup, String... aliases) {
        this(name, null, description.parse(), isGroup, aliases);
    }

    public SkillsCommand(@NonNull String name, @NonNull SkillsLang description, String... aliases) {
        this(name, null, description.parse(), false, aliases);
    }

    public SkillsCommand(@NonNull String name, @NonNull String description, String... aliases) {
        this(name, null, description, false, aliases);
    }

    public void sendUsage(CommandSender sender, SkillsLang cmd) {
        MessageHandler.sendMessage(sender, SkillsLang.Command_Usage.parse() + cmd.parse());
    }

    public boolean hasPermission(@NonNull CommandSender sender) {
        return sender.hasPermission(this.permission);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof SkillsCommand)) return false;
        SkillsCommand cmd = (SkillsCommand) obj;

        return this.isGroup == cmd.isGroup && this.name.equals(cmd.name) &&
                Objects.equals(this.group, cmd.group) &&
                Objects.equals(this.description, cmd.description) &&
                Arrays.equals(this.aliases, cmd.aliases);
    }

    private boolean isDisabled() {
        SkillsCommand lastGroupChk = group;
        StringBuilder command = new StringBuilder(this.name);
        while (lastGroupChk != null) {
            command.insert(0, lastGroupChk.name + ' ');
            lastGroupChk = lastGroupChk.group;
        }

        for (String disabled : SkillsConfig.DISABLED_COMMANDS.getStringList()) {
            disabled = StringUtils.deleteWhitespace(disabled.toLowerCase());
            if (disabled.charAt(0) == '/') disabled = disabled.substring(disabled.indexOf(' ') + 1);
            if (command.toString().equals(disabled)) return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public OfflinePlayer getPlayer(CommandSender sender, String name) {
        OfflinePlayer player = Bukkit.getPlayer(name);
        if (player == null) {
            player = Bukkit.getOfflinePlayer(name);
            if (!player.hasPlayedBefore()) player = null;
        }
        if (sender != null && player == null) SkillsLang.NOT_FOUND_PLAYER.sendMessage(sender, "%name%", name);
        return player;
    }

    public abstract void runCommand(@NonNull CommandSender sender, @NonNull String[] args);

    public abstract String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args);

    public @NonNull
    SkillsCommand[] getSubCommands() {
        if (!this.isGroup) return new SkillsCommand[0];
        return SkillsCommandHandler.commands.stream()
                .filter(c -> this.equals(c.group))
                .toArray(SkillsCommand[]::new);
    }

    public boolean matchesNames(String command) {
        return this.name.equalsIgnoreCase(command) || Arrays.asList(aliases).contains(command.toLowerCase());
    }

    public @NonNull
    String getName() {
        return name;
    }

    public @Nullable
    String[] getAliases() {
        return aliases;
    }

    public @Nullable
    SkillsCommand getGroup() {
        return group;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public @Nullable
    String getDescription() {
        return description;
    }
}
