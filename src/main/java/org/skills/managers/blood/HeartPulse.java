package org.skills.managers.blood;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.utils.MathUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HeartPulse {
    private static final Map<UUID, Integer> PULSE = new HashMap<>();

    public static void remove(Player player) {
        Integer task = PULSE.remove(player.getUniqueId());
        if (task != null) Bukkit.getScheduler().cancelTask(task);
    }

    public static CompletableFuture<Void> pulse(Player player, int percent) {
        return CompletableFuture.runAsync(() -> {
            remove(player);
            Sound dub = XSound.matchXSound(SkillsConfig.PULSE_DUB.getString()).orElse(null).parseSound();
            Sound lub = XSound.matchXSound(SkillsConfig.PULSE_LUB.getString()).orElse(null).parseSound();
            int duration = SkillsConfig.PULSE_DURATION.getInt();
            int percentOfStart = (int) MathUtils.getPercent(percent, SkillsConfig.PULSE_HEALTH.getInt());
            float left = 100 - percentOfStart;

            int heartbeat = new BukkitRunnable() {
                static final int beatSpeed = 2;
                float sound = left / 100f;
                final float soundDiv = sound / duration;
                int total = 0;
                int beats = 0;
                int delay = percentOfStart / 5;
                boolean start = false;

                @Override
                public void run() {
                    total++;
                    beats++;
                    sound -= soundDiv;

                    if (start) {
                        if (beats >= beatSpeed) {
                            player.playSound(player.getLocation(), lub, sound, 0.7f);
                            beats = 0;
                            start = false;
                        }
                        return;
                    }
                    if (beats >= delay) {
                        start = true;
                        beats = 0;
                        delay++;
                        player.playSound(player.getLocation(), dub, sound, 0.5f);
                    }
                    if (total > duration) cancel();
                }
            }.runTaskTimerAsynchronously(SkillsPro.get(), 0, 1L).getTaskId();
            PULSE.put(player.getUniqueId(), heartbeat);
        }).exceptionally((ex) -> {
            MessageHandler.sendConsolePluginMessage("&cAn error occurred while performing heartbeat:");
            ex.printStackTrace();
            return null;
        });
    }
}
