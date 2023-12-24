package org.skills.services;

import com.google.common.base.Strings;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.entity.Player;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.MessageHandler;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.StringUtils;

import java.util.List;

public class ServiceDiscordSRV {
    private static final ServiceDiscordSRV instance = new ServiceDiscordSRV();
    private static TextChannel partyChannel;

    public static void init() {
        if (!ServiceHandler.isAvailable("DiscordSRV")) return;
        MessageHandler.sendConsolePluginMessage("&2Initiating DiscordSRV support...");

        String privateNameChan = SkillsConfig.DISCORDSRV_PARTY_CHANNEL.getString();
        if (!Strings.isNullOrEmpty(privateNameChan)) {
            partyChannel = getChannel(privateNameChan);
            if (partyChannel == null) {
                MessageHandler.sendConsolePluginMessage("&cInvalid channel specified &e'" + privateNameChan + "' &cfor DiscordSRV private channel.");
            }
        }
    }

    public static void subscribe() {
        DiscordSRV.api.subscribe(instance);
    }

    public static void unsubscribe() {
        DiscordSRV.api.unsubscribe(instance);
    }

    public static void logPartyChat(Player player, String str) {
        if (partyChannel == null) return;
        str = org.apache.commons.lang.StringUtils.replace(str, "@", "@\u200B");
        partyChannel.sendMessage(str).queue();
    }

    private static TextChannel getChannel(String str) {
        if (str.equalsIgnoreCase("$console")) return DiscordSRV.getPlugin().getConsoleChannel();
        else if (str.equalsIgnoreCase("$main")) return DiscordSRV.getPlugin().getMainTextChannel();
        else {
            Guild guild = DiscordSRV.getPlugin().getMainGuild();
            int len = str.length();
            if (len > 15 && len < 20 && StringUtils.isPureNumber(str))
                return guild.getTextChannelById(Long.parseUnsignedLong(str));
            else {
                List<TextChannel> chans = guild.getTextChannelsByName(str, true);
                return chans.isEmpty() ? null : chans.get(0);
            }
        }
    }

    @Subscribe(priority = ListenerPriority.MONITOR)
    public void onBotLoad(DiscordReadyEvent event) {
        init();
    }
}
