package org.skills.managers.blood;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Enums;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.MessageHandler;

public final class BloodManager implements Listener {
    private static void playEffect(LivingEntity entity, String key) {
        String[] split = StringUtils.split(StringUtils.deleteWhitespace(key), ',');

        try {
            Location location = entity.getEyeLocation();
            Effect type = Enums.getIfPresent(Effect.class, split[0]).orNull();
            Material material =
                    XMaterial.matchXMaterial(split[1]).orElseThrow(() -> new IllegalArgumentException("Unable to get blood material: " + split[1])).parseMaterial();
            int radius = Integer.parseInt(split[2]);

            entity.getWorld().playEffect(location, type, material, radius);
        } catch (Exception ex) {
            if (ex instanceof NumberFormatException) {
                MessageHandler.sendConsolePluginMessage("&4There was a problem while getting a number for one of the effects in &econfig.yml&8: &e" + ex.getMessage());
            } else {
                MessageHandler.sendConsolePluginMessage("&4There was a problem while getting an effect or a material type in &econfig.yml&8: &e" + ex.getMessage());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlood(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) return;
        if (entity.getType() == EntityType.ARMOR_STAND) return;

        for (String restrictedentity : SkillsConfig.BLOOD_DISABLED_MOBS.getStringList()) {
            try {
                if (entity.getType() == Enums.getIfPresent(EntityType.class, restrictedentity).orNull()) return;
            } catch (IndexOutOfBoundsException ex) {
                MessageHandler.sendConsolePluginMessage("&4There was something wrong while attempting to get an &eEntity Type &4from &econfig.yml &4in section " +
                        "&erestricted-entites&8: &e" + ex.getMessage());
            }
        }

        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (player.isBlocking() && event.getFinalDamage() == 0) {
                playEffect((LivingEntity) entity, SkillsConfig.BLOOD_SHIELD.getString());
                return;
            }
        }
        ConfigurationSection section = SkillsConfig.BLOOD_CUSTOM_MOBS.getSection();
        for (String mob : section.getKeys(false)) {
            if (entity.getType() == Enums.getIfPresent(EntityType.class, mob).orNull()) {
                playEffect((LivingEntity) entity, section.getString(mob));
                return;
            }
        }

        playEffect((LivingEntity) entity, SkillsConfig.BLOOD_DEFAULT.getString());
    }
}
