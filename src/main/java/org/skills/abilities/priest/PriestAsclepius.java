package org.skills.abilities.priest;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.Particles;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.services.manager.ServiceHandler;

import java.util.List;

public class PriestAsclepius extends InstantActiveAbility {
    public PriestAsclepius() {
        super("Priest", "asclepius");
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();
        int lvl = info.getAbilityLevel(this);

        PriestPurification.spreadFlower(player, (int) getScaling(info, "spread-flower-chance"), 3);
        double damage = getScaling(info, "damage");
        double range = getScaling(info, "range");

        List<Entity> entities = player.getNearbyEntities(range, range, range);
        entities.add(player);
        for (Entity entity : entities) {
            if (!(entity instanceof Player)) continue;
            Player target = (Player) entity;

            if (ServiceHandler.areFriendly(player, entity)) {
                applyEffects(info, target);
                XSound.ENTITY_GENERIC_DRINK.play(entity);
                target.getWorld().spawnParticle(XParticle.WITCH.get(), target.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);

                if (lvl > 2) {
                    for (PotionEffect debuff : target.getActivePotionEffects()) {
                        if (XPotion.DEBUFFS.contains(XPotion.matchXPotion(debuff.getType())))
                            target.removePotionEffect(debuff.getType());
                    }
                }
            } else if (damage > 0) {
                target.getWorld().spawnParticle(XParticle.LAVA.get(), target.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
                target.damage(damage);
            }
        }

        XSound.ITEM_TOTEM_USE.play(player);
        XSound.ENTITY_BAT_TAKEOFF.play(player);

        Particles.circle(range * 2, range * 8, ParticleDisplay.of(XParticle.LARGE_SMOKE).withLocation(player.getLocation()));
        if (lvl > 1)
            Particles.helix(SkillsPro.get(), lvl + 1, 1.5, 0.05, 1, 6, 3, 0.5, true, false, ParticleDisplay.of(XParticle.ENCHANT).withLocation(player.getLocation()));
    }
}
