package org.skills.masteries.efficiency;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.masteries.managers.Mastery;
import org.skills.utils.versionsupport.VersionSupport;

public class MasteryRegeneration extends Mastery {
    public MasteryRegeneration() {
        super("Regeneration", true);
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent event) {
        if (SkillsConfig.isInDisabledWorld(event.getPlayer().getLocation())) return;
        Player p = event.getPlayer();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(p);
        int lvl = info.getMasteryLevel(this);
        if (lvl > 0) {
            EntityRegainHealthEvent evnt = new EntityRegainHealthEvent(p, lvl, EntityRegainHealthEvent.RegainReason.EATING);
            Bukkit.getPluginManager().callEvent(evnt);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (SkillsConfig.isInDisabledWorld(player.getLocation())) return;
            SkilledPlayer info = this.checkup(player);
            if (info == null) return;

            int percent = VersionSupport.getHealthPercent(player, event);
            if (percent <= getExtraScaling(info, "percent")) {
                new BukkitRunnable() {
                    int repeat = (int) getScaling(info);

                    @Override
                    public void run() {
                        if (!player.isValid()) {
                            cancel();
                            return;
                        }

                        VersionSupport.heal(player, 1);
                        if (repeat-- <= 0) cancel();
                    }
                }.runTaskTimer(SkillsPro.get(), 0L, 10L);
            }
        }
    }
}
