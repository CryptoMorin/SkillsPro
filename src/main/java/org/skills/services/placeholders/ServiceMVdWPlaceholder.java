package org.skills.services.placeholders;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import org.bukkit.entity.Player;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;

public class ServiceMVdWPlaceholder {
    public ServiceMVdWPlaceholder(SkillsPro plugin) {
        for (SkillsPlaceholders ph : SkillsPlaceholders.values()) {
            String placeholder = SkillsPlaceholders.IDENTIFIER + '_' + ph.name().toLowerCase();

            PlaceholderAPI.registerPlaceholder(SkillsPro.get(), placeholder,
                    new PlaceholderReplacer() {
                        @Override
                        public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
                            Player p = event.getPlayer();
                            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(p);
                            return ph.translate(info).toString();
                        }
                    });
        }
    }
}