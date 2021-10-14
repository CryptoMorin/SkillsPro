package org.skills.abilities.juggernaut;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;

import java.util.Locale;

public class JuggernautStoneSkin extends Ability {
    public JuggernautStoneSkin() {
        super("Juggernaut", "stone_skin");
    }

    @SuppressWarnings("Guava")
    @EventHandler(ignoreCancelled = true)
    public void onJuggernautDefend(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        ConfigurationSection protections = getOptions(info, "protections").getSection();
        if (protections == null) return;
        for (String protection : protections.getKeys(false)) {
            Optional<EntityDamageEvent.DamageCause> cause = Enums.getIfPresent(EntityDamageEvent.DamageCause.class, protection.toUpperCase(Locale.ENGLISH));
            if (!cause.isPresent() || event.getCause() != cause.get()) continue;

            event.setDamage(event.getDamage() - getAbsoluteScaling(info, protections.getString(protection), "damage", event.getDamage()));
            break;
        }
    }
}
