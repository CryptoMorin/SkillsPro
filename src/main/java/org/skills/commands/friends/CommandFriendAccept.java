package org.skills.commands.friends;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.SkillsLang;
import org.skills.utils.LocationUtils;

import java.util.*;

public class CommandFriendAccept extends SkillsCommand implements Listener {
    protected static final Map<UUID, Set<Teleportation>> requests = new HashMap<>();
    private static final Map<UUID, Teleportation> teleporting = new HashMap<>();

    public CommandFriendAccept() {
        super("tpaccept", SkillsLang.COMMAND_TPACCEPT_DESCRIPTION, "accepttp", "tpa");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }

        Player player = (Player) sender;
        Set<Teleportation> reqs = requests.get(player.getUniqueId());

        if (reqs == null) {
            SkillsLang.COMMAND_TPACCEPT_NO_REQUESTS.sendMessage(player);
            return;
        }

        Player requester;
        Teleportation requested;

        if (args.length == 0) {
            if (reqs.size() > 1) {
                SkillsLang.COMMAND_TPACCEPT_MORE_THAN_ONE.sendMessage(player);
                return;
            } else {
                requested = reqs.iterator().next();
                requester = Bukkit.getPlayer(requested.from);
                if (requester == null) {
                    SkillsLang.COMMAND_TPACCEPT_OFFLINE.sendMessage(player, "%friend%", args[0]);
                    requests.remove(player.getUniqueId());
                    return;
                }
            }
        } else {
            requester = Bukkit.getPlayer(args[0]);
            if (requester == null) {
                SkillsLang.PLAYER_NOT_FOUND.sendMessage(player, "%name%", args[0]);
                return;
            }

            requested = reqs.stream().filter(x -> x.from.equals(requester.getUniqueId())).findFirst().orElse(null);
            if (requested == null) {
                SkillsLang.COMMAND_TPACCEPT_NO_REQUEST.sendMessage(player, "%name%", requester.getName());
                return;
            }
            if (reqs.size() == 1) requests.remove(player.getUniqueId());
        }
        Bukkit.getScheduler().cancelTask(requested.task);

        int timer = SkillsConfig.FRIENDS_TELEPORT_TIMER.getInt();
        if (timer <= 0) {
            requester.teleport(player);
            SkillsLang.COMMAND_TPACCEPT_TELEPORTED.sendMessage(requester, "%friend%", player.getName());
            SkillsLang.COMMAND_TPACCEPT_NOTIFY.sendMessage(player, "%friend%", requester.getName());
            return;
        }

        int preDelay = SkillsConfig.FRIENDS_TELEPORT_DELAY_BEFORE_MOVE_CHECK.getInt();
        preDelay = Math.max(0, preDelay);
        SkillsLang.COMMAND_TPACCEPT_TELEPORT_START.sendMessage(requester, "%delay%", preDelay);
        SkillsLang.COMMAND_TPACCEPT_PRE_NOTIFY.sendMessage(player, "%friend%", requester.getName());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int id = new BukkitRunnable() {
                int timed = timer;

                @Override
                public void run() {
                    if (!player.isOnline()) {
                        SkillsLang.COMMAND_TPACCEPT_DISCONNECTED.sendMessage(player);
                        teleporting.remove(requester.getUniqueId());
                        cancel();
                        return;
                    }

                    if (timed <= 0) {
                        teleporting.remove(requester.getUniqueId());
                        requester.teleport(player);

                        SkillsLang.COMMAND_TPACCEPT_TELEPORTED.sendMessage(requester, "%friend%", player.getName());
                        SkillsLang.COMMAND_TPACCEPT_NOTIFY.sendMessage(player, "%friend%", requester.getName());
                        cancel();
                    } else {
                        SkillsLang.COMMAND_TPACCEPT_TELEPORTING.sendMessage(requester, "%countdown%", timed);
                    }
                    timed--;
                }
            }.runTaskTimer(plugin, 0L, 20L).getTaskId();
            teleporting.put(requester.getUniqueId(), new Teleportation(player.getUniqueId(), id));
        }, preDelay * 20L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Teleportation task = teleporting.remove(player.getUniqueId());

        if (task != null) {
            Bukkit.getScheduler().cancelTask(task.task);
            Player to = Bukkit.getPlayer(task.from);

            SkillsLang.COMMAND_TPACCEPT_CANCELED.sendMessage(player, "%friend%", to.getName());
            if (to != null) SkillsLang.COMMAND_TPACCEPT_CANCELED_NOTIFY.sendMessage(to, "%friend%", player.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!SkillsConfig.FRIENDS_TELEPORT_SHOULD_NOT_MOVE.getBoolean()) return;
        if (!LocationUtils.hasMoved(event.getFrom(), event.getTo())) return;
        Player player = event.getPlayer();
        Teleportation task = teleporting.remove(player.getUniqueId());

        if (task != null) {
            Bukkit.getScheduler().cancelTask(task.task);
            Player to = Bukkit.getPlayer(task.from);

            SkillsLang.COMMAND_TPACCEPT_CANCELED.sendMessage(player, "%friend%", to.getName());
            if (to != null) SkillsLang.COMMAND_TPACCEPT_CANCELED_NOTIFY.sendMessage(to, "%friend%", player.getName());
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        List<String> players = new ArrayList<>();
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Set<Teleportation> req = requests.get(player.getUniqueId());

            if (req != null) {
                for (Teleportation request : req) {
                    Player requester = Bukkit.getPlayer(request.from);
                    if (requester == null) continue;
                    players.add(requester.getName());
                }

                return players.toArray(new String[0]);
            }
        }

        return new String[0];
    }

    protected static class Teleportation {
        protected final UUID from;
        protected final int task;

        protected Teleportation(UUID from, int task) {
            this.from = from;
            this.task = task;
        }
    }
}