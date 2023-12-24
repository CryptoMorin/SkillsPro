package org.skills.api.events;

import com.cryptomorin.xseries.messages.ActionBar;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.managers.HealthAndEnergyManager;
import org.skills.services.manager.ServiceHandler;

import java.util.*;

public class CustomHudChangeEvent extends Event implements Cancellable {
    public static final Set<UUID> ANIMATIONS = new HashSet<>();
    private static final Map<UUID, Integer> BOSSBAR_SCHEDULERS = new HashMap<>();
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private String hud;
    private boolean cancelled;

    public CustomHudChangeEvent(Player player) {
        super(true);
        this.player = player;
        if (ServiceHandler.isNPC(player)) return;
        if (!ANIMATIONS.remove(player.getUniqueId())) ANIMATIONS.add(player.getUniqueId());

        //         int pos = ANIMATION.getOrDefault(player.getUniqueId(), 0);
        //        if (pos >= 20) ANIMATION.put(player.getUniqueId(), 0);
        //        else ANIMATION.put(player.getUniqueId(), pos + 1);

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        this.hud = info.isActiveReady() ?
                SkillsConfig.ACTIONBAR_ACTIVATED_SKILL.parse(player) :
                SkillsConfig.ACTIONBAR_NORMAL.parse(player);
    }

    public static void call(Player player) {
        if (SkillsConfig.ACTIONBAR_FREQUENCY.getInt() <= 0) {
            Bukkit.getScheduler().runTaskAsynchronously(SkillsPro.get(), () -> {
                if (player.hasPermission("skills.actionbar") && !SkillsConfig.isInDisabledWorld(player.getLocation())) {
                    CustomHudChangeEvent hudEvent = new CustomHudChangeEvent(player);
                    Bukkit.getPluginManager().callEvent(hudEvent);
                    if (!hudEvent.cancelled) ActionBar.sendActionBar(player, hudEvent.hud);
                }
            });
        }

        if (SkillsConfig.BOSSBAR_LEVELS_ENABLED.getBoolean() && player.hasPermission("skills.bossbar") && !SkillsConfig.isInDisabledWorld(player.getLocation())) {
            int frequency = SkillsConfig.BOSSBAR_LEVELS_FREQUENCY.getInt();
            if (frequency < 0) {
                Bukkit.getScheduler().runTaskAsynchronously(SkillsPro.get(), () -> {
                    BossBar bar = HealthAndEnergyManager.getBossBar(player);
                    if (bar == null) return;

                    SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
                    float percent = (float) (info.getXP() / info.getLevelXP(info.getLevel()));

                    bar.setTitle(MessageHandler.colorize(ServiceHandler.translatePlaceholders(player,
                            SkillsConfig.BOSSBAR_LEVELS.getSection().getString("title"))));
                    bar.setProgress(percent);
                    bar.addPlayer(player);

                    Integer previousId = BOSSBAR_SCHEDULERS.remove(player.getUniqueId());
                    if (previousId != null) Bukkit.getScheduler().cancelTask(previousId);

                    int id = Bukkit.getScheduler().runTaskLaterAsynchronously(SkillsPro.get(), () -> bar.removePlayer(player), -frequency * 20L).getTaskId();
                    BOSSBAR_SCHEDULERS.put(player.getUniqueId(), id);
                });
            }
        }
    }

    public static boolean isAnimated(UUID id) {
        return ANIMATIONS.contains(id);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public SkilledPlayer getPlayerInfo() {
        return SkilledPlayer.getSkilledPlayer(player);
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getHud() {
        return this.hud;
    }

    public void setHud(String hud) {
        this.hud = hud;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
