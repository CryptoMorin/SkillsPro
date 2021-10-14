package org.skills.abilities.eidolon;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.utils.Cooldown;

import java.util.concurrent.TimeUnit;

public class EidolonSpectre extends Ability {
    public EidolonSpectre() {
        super("Eidolon", "spectre");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEidolonAttack(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;
        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);

        if (info == null) return;
        if (info.getForm() != EidolonForm.DARK) return;
        if (Cooldown.isInCooldown(player.getUniqueId(), "EShield")) return;

        int time = (int) this.getScaling(info, "cooldown");
        double attack = getScaling(info, "dark-damage");
        double multiplier = 1 + (attack / 100f);

        event.setDamage(event.getDamage() * multiplier);
        SkillsLang.Skill_Eidolon_Attack_Boost.sendMessage(player, "%damage%", attack);
        new Cooldown(player.getUniqueId(), "EShield", time, TimeUnit.SECONDS);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEidolonDefend(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        SkilledPlayer info = this.checkup(player);

        if (info == null) return;
        if (info.getForm() != EidolonForm.LIGHT) return;
        if (Cooldown.isInCooldown(player.getUniqueId(), "EShield")) return;

        int time = (int) this.getScaling(info, "cooldown");
        int speed = (int) getScaling(info, "speed");
        if (speed < 0) speed = 0;
        if (speed > 3) speed = 3;

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, speed));
        SkillsLang.Skill_Eidolon_Shield_Speed.sendMessage(player);
        new Cooldown(player.getUniqueId(), "EShield", time, TimeUnit.SECONDS);
    }
}