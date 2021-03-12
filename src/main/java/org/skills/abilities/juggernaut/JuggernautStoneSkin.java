package org.skills.abilities.juggernaut;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JuggernautStoneSkin extends Ability {
    public JuggernautStoneSkin() {
        super("Juggernaut", "stone_skin");
    }

    @SuppressWarnings("Guava")
    @EventHandler(ignoreCancelled = true)
    public void onJuggernautDefend(EntityDamageEvent event) {
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            SkilledPlayer info = this.checkup(player);
            if (info == null) return;

            ConfigurationSection protections = getExtra(info, "protections").getSection();
            if (protections == null) return;
            for (String protection : protections.getKeys(false)) {
                Optional<EntityDamageEvent.DamageCause> cause = Enums.getIfPresent(EntityDamageEvent.DamageCause.class, protection.toUpperCase(Locale.ENGLISH));
                if (!cause.isPresent() || event.getCause() != cause.get()) continue;

                event.setDamage(event.getDamage() - getAbsoluteScaling(info, protections.getString(protection), "damage", event.getDamage()));
                break;
            }
        }
    }

    @Override
    public Object[] applyEdits(SkilledPlayer info) {
        ConfigurationSection protections = getExtra(info, "protections").getSection();
        if (protections == null) return new Object[0];

        List<Object> edits = new ArrayList<>();
        for (String protection : protections.getKeys(false)) {
            @SuppressWarnings("Guava") Optional<EntityDamageEvent.DamageCause> cause = Enums.getIfPresent(EntityDamageEvent.DamageCause.class,
                    protection.toUpperCase(Locale.ENGLISH));
            if (!cause.isPresent()) continue;

            String scaling = protections.getString(protection);
            String edit;
            try {
                edit = String.valueOf(getAbsoluteScaling(info, scaling));
            } catch (ArithmeticException ignored) {
                edit = protections.getString(protection);
            }
            edits.add('%' + cause.get().name().toLowerCase(Locale.ENGLISH) + '%');
            edits.add(Ability.getScalingColor(scaling) + edit);
        }

        return edits.toArray();
    }
}
