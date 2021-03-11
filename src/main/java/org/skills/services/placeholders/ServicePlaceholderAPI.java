package org.skills.services.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.skills.data.managers.SkilledPlayer;

import java.nio.charset.StandardCharsets;

public class ServicePlaceholderAPI extends PlaceholderExpansion {
    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return SkillsPlaceholders.IDENTIFIER;
    }

    @Override
    public String getAuthor() {
        return "Crypto Morin";
    }

    @Override
    public String getVersion() {
        return "4.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        String str = "";
        str.getBytes(StandardCharsets.UTF_8);
        return SkillsPlaceholders.onRequest(info, identifier);
    }
}
