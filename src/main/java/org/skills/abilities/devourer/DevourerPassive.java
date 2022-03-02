package org.skills.abilities.devourer;

import com.cryptomorin.xseries.XPotion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.utils.MathUtils;

import java.util.List;
import java.util.Optional;

public class DevourerPassive extends Ability {
    private static final String PASSIVE = "DEV_PASSIVE";

    public DevourerPassive() {
        super("Devourer", "passive");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPoisonAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        if (MathUtils.hasChance((int) getScaling(info, "chance", event))) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            Optional<XPotion> potion = XPotion.matchXPotion(getOptions(info).getString("effect"));

            if (potion.isPresent()) {
                double damage = getScaling(info, "damage", event);
                int duration = (int) getScaling(info, "duration", event) * 20;
                int amplifier = (int) (getScaling(info, "amplifier", event) - 1);

                entity.addPotionEffect(new PotionEffect(potion.get().getPotionEffectType(), duration, amplifier));
                entity.setMetadata(PASSIVE, new FixedMetadataValue(SkillsPro.get(), damage));
                Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> entity.removeMetadata(PASSIVE, SkillsPro.get()), duration);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPoisoned(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.POISON && event.getCause() != EntityDamageEvent.DamageCause.WITHER) return;
        Entity entity = event.getEntity();
        List<MetadataValue> meta = entity.getMetadata(PASSIVE);
        if (meta.isEmpty()) return;

        double damage = meta.get(0).asDouble();
        event.setDamage(event.getDamage() + damage);
    }
}
