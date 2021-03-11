package org.skills.abilities.priest;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.services.manager.ServiceHandler;

import java.util.EnumSet;
import java.util.List;

public class PriestAsclepius extends ActiveAbility {
    public static final EnumSet<XPotion> DEBUFFS = EnumSet.of(XPotion.BAD_OMEN, XPotion.BLINDNESS, XPotion.CONFUSION, XPotion.HARM, XPotion.HUNGER,
            XPotion.LEVITATION, XPotion.POISON, XPotion.SATURATION,
            XPotion.SLOW, XPotion.SLOW_DIGGING, XPotion.SLOW_FALLING, XPotion.UNLUCK, XPotion.WEAKNESS, XPotion.WITHER);

    public PriestAsclepius() {
        super("Priest", "asclepius", true);
    }

    @Override
    public void useSkill(Player player) {
        SkilledPlayer info = this.activeCheckup(player);
        if (info == null) return;
        int lvl = info.getImprovementLevel(this);

        PriestPurification.spreadFlower(player, (int) getExtraScaling(info, "spread-flower-chance"), 3);
        double damage = getExtraScaling(info, "damage");
        double range = getExtraScaling(info, "range");

        List<Entity> entities = player.getNearbyEntities(range, range, range);
        entities.add(player);
        for (Entity entity : entities) {
            if (!(entity instanceof Player)) continue;
            Player target = (Player) entity;

            if (ServiceHandler.areFriendly(player, entity)) {
                applyEffects(info, target);
                XSound.ENTITY_GENERIC_DRINK.play(entity);
                target.getWorld().spawnParticle(Particle.SPELL_WITCH, target.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);

                if (lvl > 2) {
                    for (PotionEffect debuff : target.getActivePotionEffects()) {
                        if (DEBUFFS.contains(XPotion.matchXPotion(debuff.getType()))) target.removePotionEffect(debuff.getType());
                    }
                }
            } else if (damage > 0) {
                target.getWorld().spawnParticle(Particle.LAVA, target.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
                target.damage(damage);
            }
        }

        XSound.ITEM_TOTEM_USE.play(player);
        XSound.ENTITY_BAT_TAKEOFF.play(player);

        XParticle.circle(range * 2, range * 8, ParticleDisplay.simple(player.getLocation(), Particle.SMOKE_LARGE));
        if (lvl > 1) XParticle.helix(SkillsPro.get(), lvl + 1, 1.5, 0.05, 1, 6, 3, true, false, new ParticleDisplay(Particle.ENCHANTMENT_TABLE, player.getLocation(), 1));
    }
}
