package org.skills.abilities.eidolon;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.skills.abilities.Ability;
import org.skills.api.events.EidolonImbalanceChangeEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.main.locale.SkillsLang;
import org.skills.utils.versionsupport.VersionSupport;

import java.awt.*;

public class EidolonPassive extends Ability {
    public EidolonPassive() {
        super("Eidolon", "passive");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEidolonChangeForm(EidolonImbalanceChangeEvent event) {
        Player player = event.getPlayer();
        SkilledPlayer info = checkup(player);
        if (info == null) return;

        double scaling = this.getScaling(info, "heal");
        Bukkit.getScheduler().runTask(SkillsPro.get(), () -> VersionSupport.heal(event.getPlayer(), scaling));

        if (!info.showReadyMessage()) return;
        if (event.getNewForm() == EidolonForm.LIGHT) {
            ParticleDisplay.of(XParticle.DUST).withLocation(player.getLocation()).withColor(Color.WHITE, 1.0f).offset(0.5, 0.5, 0.5).withCount(100).spawn();
            playSound(player, info, "imbalance.light");
            SkillsLang.Skill_Eidolon_Turn_Light.sendMessage(player);
        } else {
            ParticleDisplay.of(XParticle.DUST).withLocation(player.getLocation()).withColor(Color.BLACK, 1.0f).withCount(100).offset(0.5, 0.5, 0.5).spawn();
            playSound(player, info, "imbalance.dark");
            SkillsLang.Skill_Eidolon_Turn_Dark.sendMessage(player);
        }
    }
}
