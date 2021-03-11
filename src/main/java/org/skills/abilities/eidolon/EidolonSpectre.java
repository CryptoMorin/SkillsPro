package org.skills.abilities.eidolon;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.SkillsLang;
import org.skills.utils.Cooldown;
import org.skills.utils.StringUtils;

import java.util.concurrent.TimeUnit;

public class EidolonSpectre extends Ability {
    public EidolonSpectre() {
        super("Eidolon", "spectre");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEidolonAttack(EntityDamageByEntityEvent event) {
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;
        if (!(event.getEntity() instanceof Damageable)) return;
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            SkilledPlayer info = this.checkup(player);
            if (info == null) return;

            if (!Cooldown.isInCooldown(player.getUniqueId(), "EShield")) {
                if (info.getForm() == EidolonForm.DARK) {
                    int time = (int) this.getScaling(info);
                    double attack = getExtraScaling(info, "dark-scaling");
                    double multiplier = 1 + (attack / 100f);
                    event.setDamage(event.getDamage() * multiplier);
                    SkillsLang.Skill_Eidolon_Attack_Boost.sendMessage(player, "%damage%", String.valueOf(attack));
                    new Cooldown(player.getUniqueId(), "EShield", time, TimeUnit.SECONDS);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEidolonDefend(EntityDamageByEntityEvent event) {
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            SkilledPlayer info = this.checkup(p);
            if (info == null) return;

            if (!Cooldown.isInCooldown(p.getUniqueId(), "EShield")) {

                if (info.getForm() == EidolonForm.LIGHT) {
                    int time = (int) this.getScaling(info);
                    int speed = (int) getExtraScaling(info, "speed");
                    if (speed < 0) speed = 0;
                    if (speed > 3) speed = 3;

                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, speed));
                    SkillsLang.Skill_Eidolon_Shield_Speed.sendMessage(p);
                    new Cooldown(p.getUniqueId(), "EShield", time, TimeUnit.SECONDS);
                }
            }
        }
    }

    @Override
    public Object[] applyEdits(SkilledPlayer info) {
        return new Object[]{"%speed%", getScalingDescription(info, getExtra(info, "speed").getString()),
                "%dark%", getScalingDescription(info, getExtra(info, "dark-scaling").getString())};
    }
}