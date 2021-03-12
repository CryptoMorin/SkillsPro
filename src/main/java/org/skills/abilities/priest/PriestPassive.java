package org.skills.abilities.priest;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.abilities.Ability;
import org.skills.api.events.SkillToggleAbilityEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.utils.LocationUtils;

import java.util.HashSet;
import java.util.Set;

public class PriestPassive extends Ability {
    /**
     * No need for disposable handler. Already handled here.
     */
    private static final Set<Integer> JESUS = new HashSet<>();

    public PriestPassive() {
        super("Priest", "passive");
    }

    @Override
    public void start() {
        addTask(new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    World.Environment environment = player.getWorld().getEnvironment();
                    if (environment == World.Environment.NORMAL) continue;
                    SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
                    if (!info.hasAbility(PriestPassive.this)) continue;

                    if (environment == World.Environment.NETHER) {
                        Bukkit.getScheduler().runTask(SkillsPro.get(), () -> applyEffects(info, "effects.nether", player));
                        XSound.ENTITY_WITHER_AMBIENT.play(player);
                    } else {
                        Bukkit.getScheduler().runTask(SkillsPro.get(), () -> applyEffects(info, "effects.end", player));
                        XSound.ENTITY_ENDER_DRAGON_AMBIENT.play(player);
                    }
                }
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 100, getExtra("Priest", "interval").getInt() * 20L));
    }

    @EventHandler(ignoreCancelled = true)
    public void jesus(PlayerMoveEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(SkillsPro.get(), () -> {
            if (!LocationUtils.hasMoved(event.getFrom(), event.getTo())) return;
            Player player = event.getPlayer();
            if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;

            SkilledPlayer info = this.checkup(player);
            if (info == null) return;
            if (!getExtra(info, "jesus").getBoolean()) return;

            Block block = player.getLocation().getBlock();
            String standing = block.getType().name();
            String under = block.getRelative(BlockFace.DOWN).getType().name();

            if (standing.endsWith("WATER") || under.endsWith("WATER")) {
                if (player.getAllowFlight()) {
                    player.spawnParticle(Particle.CLOUD, player.getLocation(), 5, 0.1, 0.1, 0.1, 0.1);
                    return;
                }

                Bukkit.getScheduler().runTask(SkillsPro.get(), () -> {
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    JESUS.add(player.getEntityId());
                });
                player.spawnParticle(Particle.CLOUD, player.getLocation(), 30, 0.3, 0, 0.3, 0.3);
            } else {
                if (!player.getAllowFlight()) return;
                if (!JESUS.remove(player.getEntityId())) return;
                Bukkit.getScheduler().runTask(SkillsPro.get(), () -> {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                });
            }
        });
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (JESUS.remove(player.getEntityId())) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (JESUS.remove(player.getEntityId())) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    @EventHandler
    public void onPassiveDisable(SkillToggleAbilityEvent event) {
        if (!event.isDisabled()) return;
        if (!event.getAbility().getName().endsWith("passive")) return;
        if (!event.getInfo().getSkillName().equalsIgnoreCase("priest")) return;

        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        if (!JESUS.remove(player.getEntityId())) return;
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    @EventHandler
    public void lavaEscape(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.LAVA) return;

        Player player = (Player) event.getEntity();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        event.setCancelled(true);
        player.setFireTicks(0);

        Vector vector = new Vector(-player.getEyeLocation().getDirection().getX(), 1.5, -player.getEyeLocation().getDirection().getZ());
        player.setVelocity(vector);

        XSound.ENTITY_GENERIC_EXTINGUISH_FIRE.play(player);

        new BukkitRunnable() {
            int i = 5;

            @Override
            public void run() {
                player.spawnParticle(Particle.FLAME, player.getLocation(), 10, 0.01, 0.01, 0.01, 0.1);
                i--;
                if (i == 0) cancel();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0, 5);
    }
}
