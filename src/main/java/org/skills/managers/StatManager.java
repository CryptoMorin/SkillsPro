package org.skills.managers;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.types.StatType;
import org.skills.utils.MathUtils;

public final class StatManager implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void statHandler(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Player damager = DamageManager.getOwningPlayer(event.getDamager());
        if (damager == null && !(victim instanceof Player)) return;
        double additionalDamage = 0;

        if (damager != null) {
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(damager);
            additionalDamage += StatType.DAMAGE.evaluate(info);

            if (MathUtils.hasChance((int) StatType.CRITICAL_CHANCE.evaluate(info))) {
                additionalDamage += StatType.CRITICAL_DAMAGE.evaluate(info);
            }
        }

        if (victim instanceof Player) {
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer((OfflinePlayer) victim);
            additionalDamage -= StatType.DEFENSE.evaluate(info);
        }

        event.setDamage(event.getDamage() + additionalDamage);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        double speed = StatType.SPEED.evaluate(info);
        if (speed == 0) return;

        player.setWalkSpeed((float) speed);
    }
}
