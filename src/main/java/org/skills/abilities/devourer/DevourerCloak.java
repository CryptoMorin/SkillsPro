package org.skills.abilities.devourer;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.Particles;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.abilities.Ability;
import org.skills.api.events.SkillToggleAbilityEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.DamageManager;
import org.skills.utils.Cooldown;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DevourerCloak extends Ability {
    private static final String INVIS = "DEVOURER_INVIS", NEUTRAL = "DEVOURER_NEUTRAL";
    private static final Set<EntityType> TYPES = EnumSet.of(EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.ENDERMAN, EntityType.GUARDIAN, EntityType.ENDERMITE);

    public DevourerCloak() {
        super("Devourer", "cloak");
    }

    private static void activateInvisibility(SkilledPlayer info, Player player) {
        if (!player.hasPotionEffect(XPotion.INVISIBILITY.getPotionEffectType())) {
            if (info.showReadyMessage()) player.sendMessage(SkillsLang.Skill_Devourer_Invis_Enabled.parse());
            Particles.helix(SkillsPro.get(), 4, 1, 0.1, 1, 4, 3, 0.5, true, false, ParticleDisplay.of(XParticle.CLOUD).withLocation(player.getLocation()));
        }
        player.addPotionEffect(XPotion.BLINDNESS.buildPotionEffect(200, 2), true);
    }

    @Override
    public void start() {
        addTask(new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    GameMode mode = player.getGameMode();
                    if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) continue;
                    if (SkillsConfig.isInDisabledWorld(player.getLocation())) continue;
                    SkilledPlayer info = checkup(player);
                    if (info == null) continue;

                    if (!Cooldown.isInCooldown(player.getUniqueId(), INVIS)) {
                        Bukkit.getScheduler().runTask(SkillsPro.get(), () -> activateInvisibility(info, player));
                    }
                }
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 100L, 100L));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDevourerAttack(EntityDamageByEntityEvent event) {
        Player player = DamageManager.getOwningPlayer(event.getDamager());
        if (player == null) return;

        if (!player.hasPotionEffect(XPotion.INVISIBILITY.getPotionEffectType())) return;
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        int level = info.getAbilityLevel(this);
        if (info.showReadyMessage()) SkillsLang.Skill_Devourer_Invis_Disabled.sendMessage(player);
        if (level > 2) event.setDamage(event.getDamage() + this.getScaling(info, "damage", event));

        player.removePotionEffect(XPotion.INVISIBILITY.getPotionEffectType());
        Particles.helix(SkillsPro.get(), 4, 1, 0.1, 1, 4, 3, 0.5, true, false, ParticleDisplay.of(XParticle.CLOUD).withLocation(player.getLocation()));
        new Cooldown(player.getUniqueId(), INVIS, (long) this.getScaling(info, "cooldown.invisibility"), TimeUnit.SECONDS);

        if (getOptions(info).getBoolean("neutrality")) {
            if (level > 1 && event.getEntityType() != EntityType.PLAYER) {
                if (!Cooldown.isInCooldown(player.getUniqueId(), NEUTRAL)) {
                    SkillsLang.Skill_Devourer_Neutrality_Disabled.sendMessage(player);
                }
                new Cooldown(player.getUniqueId(), NEUTRAL, (long) this.getScaling(info, "cooldown.neutrality"), TimeUnit.SECONDS);
            }
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(SkillsPro.get(), () -> {
            if (!Cooldown.isInCooldown(player.getUniqueId(), INVIS)) activateInvisibility(info, player);
            if (level > 1 && !Cooldown.isInCooldown(player.getUniqueId(), NEUTRAL)) {
                SkillsLang.Skill_Devourer_Neutrality_Enabled.sendMessage(player);
            }
        }, 30 * 20L);
    }

    @EventHandler
    public void onSkillToggleDisabled(SkillToggleAbilityEvent event) {
        if (event.getAbility() instanceof DevourerCloak) {
            if (event.isDisabled()) {
                Player player = event.getPlayer();
                player.removePotionEffect(XPotion.INVISIBILITY.getPotionEffectType());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player)) return;
        if (TYPES.contains(event.getEntityType()) || event.getEntityType().name().equals("ELDER_GUARDIAN")) return;

        Player player = (Player) event.getTarget();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        if (!getOptions(info).getBoolean("neutrality")) return;
        if (info.getAbilityLevel(this) < 2) return;
        if (!Cooldown.isInCooldown(player.getUniqueId(), NEUTRAL)) event.setCancelled(true);
    }
}
