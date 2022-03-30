package org.skills.main.locale;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.skills.main.SkillsConfig;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.StringUtils;

import java.util.EnumSet;

public enum SkillsLang {
    PREFIX("&8[&3Skills&8] &3"),

    COMMAND_HELP_DESCRIPTION("&6Displays all commands", 1, 2),
    COMMAND_HELP_USAGE("/Skills help <page>", 1, 2),
    COMMAND_HELP_GROUPED_HEADER("&8&m---------------=( &c&l%group% &8)=---------------", 1, 2),
    COMMAND_HELP_GROUPED_COMMANDS("COMPLEX:hover:{&8/&2skills %cmd% &7- %description%,&8/&2k %cmd%,|/skills %cmd%}", 1, 2),
    COMMAND_HELP_FOOTER("COMPLEX:hover:{&6←,&6Previous Page,/skills help %previous_page%} %pages%hover:{&6→,&6Next Page,/skills help %next_page%}", 1, 2),
    COMMAND_HELP_FOOTER_PAGE("COMPLEX:hover:{&8[&2%number%&8],&6Go to page &2%number%,/skills help %number%}", 1, 2),
    COMMAND_HELP_FOOTER_CURRENT_PAGE("COMPLEX:hover:{&8[&2&l%number%&8],&2Current Page}", 1, 2),
    COMMAND_HELP_HEADER("&8&m---------------=(&c&l Skills &6%page%&7/&6%max_pages% &8&m)=---------------", 1, 2),
    COMMAND_HELP_COMMANDS("COMPLEX:hover:{&8/&2skills %cmd% &7- %description%,&8/&2skills %cmd%,|/skills %cmd%}", 1, 2),
    COMMAND_HELP_NEGATIVE_PAGES("&cThere are no hidden pages."),

//    COMMAND_HELP_DESCRIPTION("&eUse to display commands", 1, 2),
//    COMMAND_HELP_PAGES_HEADER("&8&l&m--------------(&6&l Skills &e%page%&7/&e%maxpages% &8&l&m)--------------", 1, 2),
//    COMMAND_HELP_NO_HIDDEN_PAGES("&cThere are no hidden pages.", 1, 2),
//    COMMAND_HELP_PAGES_COMMANDS("&7/&3Skills %cmd% &8- &e%description%", 1, 2),
//    COMMAND_HELP_GROUPED_HEADER("&8&l&m--------------(&6&l %group% &8&l&m)--------------", 1, 2),
//    COMMAND_HELP_GROUPED_COMMANDS("&7/&3Skills %cmd% &8- &e%description%", 1, 2),
//    COMMAND_HELP_NEXTPAGE("\n&3Use &e/Skills help %page% &3to see the next page.", 1, 2),
//    COMMAND_HELP_NO_MORE_PAGES("&cNo more pages present.", 1, 2),

    NOT_FOUND_PLAYER("&cThe specified player was not found.", 2),
    Command_Usage("&cUsage&7: &e/Skills ", 1, 2),
    Command_Unknown("&cUnknown command!", 1, 2),
    Command_Not_Number("&e%arg% &cis not a number! &e%needed% &cmust be a number.", 1, 2),
    COMMAND_AT_LEAST_ONE("&e%needed% &camount must be at least 1", 1, 2),

    COMMAND_SELECT_DESCRIPTION("&eUse to select a skill", 1, 2),
    COMMAND_SELECT_ALREADY_CHOSEN("&cYou're already a &e%skill%", 1, 2),

    COMMAND_REFRESH_DESCRIPTION("&eReplenish all your energy/blood/imbalance/mana", 1, 2),
    Command_Refresh_Success("&3Replenished your energy.", 1, 2),

    COMMAND_IMPROVE_DESCRIPTION("&eUse to open your skill's improvment menu", 1, 2),
    COMMAND_STATS_DESCRIPTION("&eUse to manage your statpoints", 1, 2),
    COMMAND_MASTERY_DESCRIPTION("&eUse to manage your masteries", 1, 2),
    COMMAND_MASTERY_OTHERS_PERMISSION("&cYou don't have permission to view others mastery.", 1, 2),
    COMMAND_MASTERY_USAGE("&cUsage&8: &e/skill mastery <player>", 1, 2),
    COMMAND_UPDATES_DESCRIPTION("&eChecks for updates on Spigot.", 1, 2),

    COMMAND_STATISTICS_DESCRIPTION("&eGet skill usage statistics for all the server.", 1, 2),
    COMMAND_SOUND_DESCRIPTION("&eTest a sound in-game before using it in config.", 1, 2),

    COMMAND_RELOAD_DESCRIPTION("&eReloads the plugin.", 1, 2),
    Command_Reload_Done("&3Reloaded.", 1, 2),

    COMMAND_TOP_DESCRIPTION("&eUse to see the players with the highest levels", 1, 2),
    COMMAND_TOP_HEADER("&8&l&m-----------(&6&l Top 10 Players &8&l&m)-----------", 1, 2),
    COMMAND_TOP_ENTRY("&6%number%. &8(&9%skills_skill_displayname%&8) &3%player% &8- &eLvl %skills_level%", 1, 2),
    COMMAND_TOP_LOADING("&eCalculating...", 1, 2),

    EVENTS_NAME_SOULS("Souls", 2),
    EVENTS_NAME_XP("XP", 2),

    GUIS_STATMENU_STATPOINTS_NEEDED("&cAt least 2 stat points are required to auto distribute.", 1, 2, 3),
    GUIS_STATMENU_STATPOINTS_DISTRIBUTION("&e%amount% &3points added into &e%stat%", 1, 2, 3),
    GUIS_STATMENU_STATPOINTS_SPENT("&3Used &e%amount% &3stat points.", 1, 2, 3),
    GUIS_STATMENU_RESET_MESSAGE("&cYour stat points has been reset.", 1, 2, 3),

    COMMAND_USER_DESCRIPTION("&eModify player's skill data.", 1, 2),
    COMMAND_USER_SOULS_DESCRIPTION("&eUse to manipulate player soul count", 1, 2, 3),
    COMMAND_USER_LEVEL_DESCRIPTION("&eUse to manipulate level of a player", 1, 2, 3),
    COMMAND_USER_XP_DESCRIPTION("&eUse to manipulate xp of a player. Player must be online", 1, 2, 3),
    COMMAND_USER_XP_ADD("&3%amount% has been added to the player's XP", 1, 2, 3),
    COMMAND_USER_XP_REMOVE("&3%amount% has been removed to the player's XP.", 1, 2, 3),
    COMMAND_USER_XP_SET("&3%amount% has been set as the player's XP.", 1, 2, 3),
    COMMAND_USER_SKILL_DESCRIPTION("&eUse to manipulate player skill.", 1, 2, 3),

    COMMAND_USER_VULNERABLE_DESCRIPTION("&eUsed to set a player as vulnerable in critical situations.", 1, 2, 3),
    COMMAND_USER_VULNERABLE_ALREADY_VULNERABLE("&e%target% &cis already vulnerable.", 1, 2, 3),
    COMMAND_USER_VULNERABLE_SET("&3%target% &2is now vulnerable.", 1, 2, 3),

    COMMAND_USER_COSMETIC_DESCRIPTION("&eUse to manipulate player cosmetics.", 1, 2, 3),
    COMMAND_USER_COSMETIC_INVALID_CATEGORY("&cNo such cosmetic category named&8: &e%category%", 1, 2, 3, 4),
    COMMAND_USER_COSMETIC_INVALID_COSMETIC("&cNo such cosmetic in &e%category% &ccategory named&8: &e%cosmetic%", 1, 2, 3, 4),
    COMMAND_USER_COSMETIC_ALREADY_SET("&e%player% &cis already using that cosmetic.", 1, 2, 3),
    COMMAND_USER_COSMETIC_SET("&3Changed &2%player% &3cosmetic type from &2%category% &3category to&8: &2%cosmetic%", 1, 2, 3),
    COMMAND_USER_STATS_DESCRIPTION("&eUse to add stats points to a player", 1, 2, 3),
    COMMAND_USER_RESET_DESCRIPTION("&eUse to wipe the specified player's data", 1, 2, 3),
    COMMAND_USER_RESET_SUCCESS("&3%player%'s stats, level, skill, masteries and improvements reset", 1, 2, 3),
    COMMAND_USER_SOULS_ADD("&3%amount% has been added to the player's souls", 1, 2, 3),
    COMMAND_USER_SOULS_REMOVE("&3%amount% has been removed to the player's souls.", 1, 2, 3),
    COMMAND_USER_SOULS_SET("&3%amount% has been set as the player's souls.", 1, 2, 3),
    COMMAND_USER_SKILL_SUCCESS("&3%player% &eis now a &3%skill%", 1, 2, 3),
    COMMAND_USER_SKILL_NOT_SKILL("&e%skill% &cis not an existing skill&7: &enone&8, &eswordsman&8, &edevourer&8, &emage&8, " +
            "&efiremage&8, &ejuggernaut&8, &earbalist&8, &evampire&8, &eeidolon&8, &ePriest", 1, 2, 3),
    COMMAND_USER_STATS_ADD("&3%amount% has been added to the player's stat points", 1, 2, 3),
    COMMAND_USER_STATS_REMOVE("&3%amount% has been removed to the player's stat points.", 1, 2, 3),
    COMMAND_USER_STATS_SET("&3%amount% has been set as the player's stat points.", 1, 2, 3),
    COMMAND_USER_LEVEL_ADD("&3%amount% has been added to the player's level", 1, 2, 3),
    COMMAND_USER_LEVEL_REMOVE("&3%amount% has been removed to the player's level.", 1, 2, 3),
    COMMAND_USER_LEVEL_SET("&3%amount% has been set as the player's level.", 1, 2, 3),
    COMMAND_USER_LEVEL_NOT_NUMBER("&clevel must be a number!", 1, 2, 3),
    NOT_NUMBER("&e%arg% &cis not a number."),
    Command_User_Invalid_Setter("&cInvalid setter&8: &e%setter% &cUse one of the following keywords&8: &eadd &7| &eremove &7| &eset", 1, 2),
    COMMAND_USER_MASTERY_DESCRIPTION("&eUse to modify a player's mastery levels.", 1, 2, 3),
    COMMAND_USER_IMPROVE_DESCRIPTION("&eUse to modify a player's skill's improvements.", 1, 2, 3),
    Command_User_Mastery_Set_Success("&e%amount% &3levels has been added to the player's &e%mastery% &3mastery, new level&8: &e%new%", 1, 2, 3),
    COMMAND_USER_IMPROVEMENT_SUCCESS("&e%amount% &3levels has been added to the player's &e%ability% &3ability, new level&8: &e%new%", 1, 2, 3),

    COMMAND_STATS_OTHERS_PERMISSION("&4You don't have the permission to see others stats.", 1, 2),

    COMMAND_BONUS_DESCRIPTION("&3Manage players XP/Souls bonuses.", 1, 2),
    COMMAND_BONUS_GIVE_DESCRIPTION("&3Gives a player a bonus.", 1, 2, 3),
    COMMAND_BONUS_GIVE_SUCCESS("&3You have been given a bonus for &e%time%", 1, 2, 3),
    COMMAND_BONUS_GIVE_CONFIRMATION("&3Given &e%player% &3a bonus for &e%time%", 1, 2, 3),
    COMMAND_BONUS_GIVE_MULTIPLIER_ERROR("&cThe multiplier must contain the keyword XP or soul! E.g. &e5 * soul", 1, 2, 3),
    COMMAND_BONUS_GIVE_MULTIPLIER_INVALID("&cInvalid multiplier. Multipliers are: E.g. &e5 * soul, xp + 20, etc.", 1, 2, 3),

    COMMAND_BONUS_TAKE_SUCCESS("&3Removed all the boosts from the player.", 1, 2, 3),
    COMMAND_BONUS_NOT_FOUND("&cCould not find &e%bonus% &cbonus.", 1, 2),
    COMMAND_BONUS_TAKE_DESCRIPTION("&3Takes an active bonus from a player.", 1, 2, 3),
    //COMMAND_BONUS_TIME_DESCRIPTION("&3Change one of a player's bonuses.", 1, 2),

    COMMAND_BOOST_ALREADY_STARTED("&cThe specified event is already in progress! Remaining time&8: &e%time%", 1, 2),
    COMMAND_BOOST_BROADCAST("&e%type% &3boost activated for &e%time%&3! Get &ex%multiplier% &3%type% per kill!", 1, 2),

    COMMAND_SHOWACTIVEMESSAGES_DESCRIPTION("&eUse to toggle active skill messages", 1, 2),
    COMMAND_SHOWACTIVEMESSAGES_ON("&3Active messages will now be shown.", 1, 2),
    COMMAND_SHOWACTIVEMESSAGES_OFF("&cActive messages will not be shown.", 1, 2),

    COMMAND_SHOW_ACTION_BAR_DESCRIPTION("&eUse to toggle the action bar.", 1, 4),
    COMMAND_SHOW_ACTION_BAR_ON("&3ActionBar&8: &2ON", 1, 4),
    COMMAND_SHOW_ACTION_BAR_OFF("&3ActionBar&8: &4OFF", 1, 4),

    COMMAND_ABOUT_DESCRIPTION("&6About the plugin.", 1, 2),

    COMMAND_MUSIC_DESCRIPTION("&6Special Priest ability.", 1, 2),
    COMMAND_MUSIC_PLAYING("&3Playing...", 1, 2),
    COMMAND_MUSIC_DONE("&3Finished playing.", 1, 2),
    COMMAND_MUSIC_USAGE("&4Usage&8: &e/skill music &9<instructions>", 1, 2),
    COMMAND_MUSIC_PRIEST_ONLY("&cOnly priests can use this command!", 1, 2),
    COMMAND_MUSICT_ERROR("&cError while parsing instructions&8: &e%error%", 1, 2),

    COMMAND_DEBUG_DESCRIPTION("&6Enable debug mode.", 1, 2),
    COMMAND_DEBUG_ON("&6Debug mode is&8: &3ON", 1, 2),
    COMMAND_DEBUG_OFF("&6Debug mode is&8: &cOFF", 1, 2),
    COMMAND_DEBUG_MESSAGE("&8[&4DEBUG&7-&5%priority%&8] &e%damager% &7(&e%custom_damager%&7) &6attacked &c%entity% &7(&c%custom_entity%&7)&8: " +
            "&9Damage&8: &2%damage% &7| &9Final Damage&8: &2%final_damage%", 1, 2),

    COMMAND_SCALING_DESCRIPTION("&6Sets your fake health scaling.", 1, 2),
    COMMAND_SCALING_NOT_IN_RANGE("&cHealth scaling must be between 0 and 80 or 0 to disable", 1, 2),
    COMMAND_SCALING_SET("&6Your new fake scaling has been set to &e%scale%", 1, 2),

    COMMAND_EVENT_NOT_FOUND("&cCould not find &e%event% &cevent.", 1, 2),
    COMMAND_EVENT_START_DESCRIPTION("&3Starts an event.", 1, 2, 3),
    COMMAND_EVENT_STOP_DESCRIPTION("&3Stops an event.", 1, 2, 3),
    COMMAND_EVENT_TIME_DESCRIPTION("&3Changes an event's time.", 1, 2, 3),
    COMMAND_EVENT_EVENTS_DESCRIPTION("&3Get the current running events.", 1, 2, 3),
    COMMAND_EVENT_STOP_SUCCESS("&e%player% &3has stopped the &e%event% &3event.", 1, 2, 3),
    COMMAND_EVENT_EVENTS_XP("&3Active XP Event&8: &eDuration&8: &e%xp_duration% &8- &eMultiplier&8: &e%xp_multiplier%", 1, 2, 3),
    COMMAND_EVENT_EVENTS_SOUL("&3Active Soul Event&8: &eDuration&8: &e%soul_duration% &8- &eMultiplier&8: &e%soul_multiplier%", 1, 2, 3),
    COMMAND_EVENT_EVENTS_NOTHING("&cThere are no events running.", 1, 2, 3),
    COMMAND_EVENT_TIME_SET("&3Changed &e%event% &3event time. New time&8: &e%time%", 1, 2, 3),

    COMMAND_BINDINGS_DESCRIPTION("&eSet custom key bindings for active abilities.", 1, 2),
    COMMAND_BINDINGS_NO_CLASS("&cYou don't have any class selected to change its ability bindings.", 1, 2),
    COMMAND_BINDINGS_USAGE("&cUsage&8: &e/skills bindings &2<ability> <key>", 1, 2),
    COMMAND_BINDINGS_NOT_ACTIVE_ABILITY("&e%ability% &cis not an active ability and cannot have key bindings.", 1, 2),
    COMMAND_BINDINGS_UNKNOWN_ABILITY("&cUnknown ability in current active class&8: &e%ability%", 1, 2),
    COMMAND_BINDINGS_UNKNOWN_KEY("&cUnknown binding key&8: &e", 1, 2),
    COMMAND_BINDINGS_INVALID("&cInvalid key binding&8: &e%binding%", 1, 2),
    COMMAND_BINDINGS_CONFLICT("&e%ability% &cability is using the &e%other_binding% &cbinding which will conflict with &e%binding%\n" +
            "&cNote&8: &eTwo abilities cannot start with the same key binding pattern as there is no way to guess which one the player wants to use.", 1, 2),
    COMMAND_BINDINGS_CHANGED("&e%ability% &3ability activation key binding has been changed to&8: &e%binding%", 1, 2),

    COMMAND_INFO_DESCRIPTION("&eUse to see your skill level, stats and improvements", 1, 2),
    COMMAND_INFO_XP_BONUS("&3Active XP Bonus&8: &eDuration&8: &e%skills_xp_bonus_duration% &8- &eMultiplier&8: &e%skills_xp_bonus_multiplier%", 1, 2),
    COMMAND_INFO_SOUL_BONUS("&3Active Soul Bonus&8: &eDuration&8: &e%skills_soul_bonus_duration% &8- &eMultiplier&8: &e%skills_soul_bonus_multiplier%", 1, 2),
    COMMAND_INFO_OTHERS_PERMISSION("&4You don't have the permission to see others info."),
    COMMAND_INFO_MESSAGE("NOPREFIX:&8&l&m--------------(&6&l %skills_skill_displayname% &8&l&m)--------------\n&3Souls&7: &e%skills_fancy_soul%\n" +
            "&3Level&7: &e%skills_fancy_level%\n" +
            "&3XP&7: &e%skills_fancy_xp%&8/&e%skills_fancy_maxxp%\n&3Total XP&7: &e%skills_fancy_rawxp%\n\n" +
            "&6STR &7| &dDEX &7| &9INT &7| &4DEF\n   " +
            "&6%skills_stat_str% &7| &d%skills_stat_dex% &7| &9%skills_stat_int% &7| &4%skills_stat_def%\n", 1, 2),
    COMMAND_INFO_OTHERS("NOPREFIX:&8&l&m--------------(&6&l %player% &8&l&m)--------------\n&3Level&7: &e%skills_fancy_level%\n" +
            "&3XP&7: &e%skills_fancy_xp%&8/&e%skills_fancy_maxxp%\n&3Total XP&7: &e%skills_fancy_rawxp%\n\n", 1, 2),

    COMMAND_FRIEND_DESCRIPTION("&eAdd a player as your friend.", 1, 2),
    COMMAND_FRIEND_REQUEST("&3A friend request has been sent to &e%invited%", 1, 2),
    COMMAND_FRIEND_SELF("&cAre you that desperate to friendzone yourself?", 1, 2),
    COMMAND_FRIEND_ALREADY_FRIENDS("&cYou're already friends with &e%invited%", 1, 2),
    COMMAND_FRIEND_MAX("&cYou can't make more friends.", 1, 2),
    COMMAND_FRIEND_ALREADY_SENT("&cYou've already sent a friend request to &e%invited%", 1, 2),
    COMMAND_FRIEND_NOTIFICATION("COMPLEX:&e%inviter% &3wants to be friends with you. Do hover:{&e/skill friend %inviter%,&2Accept,/skill friend %inviter%} &3to accept.", 1, 2),
    COMMAND_FRIEND_FRIENDED("&3You're now friends with &e%friend%", 1, 2),

    COMMAND_SHOP_DESCRIPTION("&eBuy skills special items.", 1, 2),
    COMMAND_SHOP_GIVE_PERMISSION("&cYou don't have permission to use that command.", 1, 2, 3),
    COMMAND_SHOP_GIVE_USAGE("&cUsage&8: &e/skills shop give <type> [player] [amount]", 1, 2, 3),
    COMMAND_SHOP_GIVE_INVALID_TYPE("&cInvalid Skills item type&8: &e%type%\n&cAvailable Types&8: &eXP, Energy_Booster, Energy_Potion", 1, 2, 3),

    COMMAND_SOULS_DESCRIPTION("&eDeposit or withdraw souls from your bank.", 1, 2),
    COMMAND_SOULS_SERVICE_UNAVAILABLE("&cBank services are currently unavailable.", 1, 2),
    COMMAND_SOULS_UNKNOWN_TRANSACTION("&cUnknown transaction &e%transaction%", 1, 2),
    COMMAND_SOULS_NOT_ENOUGH_SOULS("&cYou don't have &e%amount% &csouls.", 1, 2),
    COMMAND_SOULS_NOT_ENOUGH_MONEY("&cYou don't have &e%amount% &cmoney.", 1, 2),
    COMMAND_SOULS_DEPOSIT_SUCCESS("&3You've successfully deposited &e%amount% &3souls into your bank for &e%translated%\n&3Your new balance&8: &e%balance%", 1, 2),
    COMMAND_SOULS_DEPOSIT_DISABLED("&cYou cannot deposit souls.", 1, 2),
    COMMAND_SOULS_WITHDRAW_DISABLED("&cYou cannot withdraw souls.", 1, 2),
    COMMAND_SOULS_WITHDRAW_SUCCESS("&3You've successfully withdrawn &e%amount% &3souls from your bank for &e%translated%\n&3Your new balance&8: &e%balance%", 1, 2),

    COMMAND_TPACCEPT_DESCRIPTION("&eAccept a teleportation request from a friend.", 1, 2),
    COMMAND_TPACCEPT_TELEPORING("&3Teleporting to &e%friend% &3in &e%delay% &5seconds. Do not move.", 1, 2),
    COMMAND_TPACCEPT_PRE_NOTIFY("&3Your friend &e%friend% &3will be teleporting to you.", 1, 2),
    COMMAND_TPACCEPT_CANCELED("&cTeleportation has been cancelled!", 1, 2),
    COMMAND_TPACCEPT_CANCELED_NOTIFY("&cYour friend &e%friend% &cteleportation has been canceled.", 1, 2),
    COMMAND_TPACCEPT_TELEPORTING("&3Teleporting in &e%countdown% seconds&3... Don't move.", 1, 2),
    COMMAND_TPACCEPT_TELEPORT_START("&3Teleportation will start in &e%delay% seconds. &3Don't move!", 1, 2),
    COMMAND_TPACCEPT_DISCONNECTED("&cYour friend has been disconnected and the teleportation has been canceled.", 1, 2),
    COMMAND_TPACCEPT_NO_REQUEST("&e%name% &cdid not send you any teleportation requests.", 1, 2),
    COMMAND_TPACCEPT_NO_REQUESTS("&cYou don't have any teleportation requests sent to you.", 1, 2),
    COMMAND_TPACCEPT_MORE_THAN_ONE("&cYou have more than one teleportation requests. Please specify the player name that you want to accept the request from.", 1, 2),
    COMMAND_TPACCEPT_OFFLINE("&cYour friend that sent you a teleprotation request is now offline.", 1, 2),
    COMMAND_TPACCEPT_TELEPORTED("&3Teleported to &e%friend%", 1, 2),
    COMMAND_TPACCEPT_NOTIFY("&3Your friend &e%friend% &3has teleported to you.", 1, 2),

    COMMAND_FRIENDTP_DESCRIPTION("&eTeleport to a friend.", 1, 2),
    COMMAND_FRIENDTP_NOT_FRIENDS("&cYou're not friends with &e%name%", 1, 2),
    COMMAND_FRIENDTP_DISABLED("&cFriend teleportation is disabled.", 1, 2),
    COMMAND_FRIENDTP_TELEPORTED("&3Teleported to &e%friend%", 1, 2),
    COMMAND_FRIENDTP_NOTIFY("&3Your friend &e%friend% &3has teleported to you.", 1, 2),
    COMMAND_FRIENDTP_REQUESTED("&3Sent a teleportation request to &e%friend%", 1, 2),
    COMMAND_FRIENDTP_ALREADY_REQUESTED("&cYou've already sent a teleportation request to &e%friend%", 1, 2),
    COMMAND_FRIENDTP_EXPIRED("&cYou're teleportation request to &e%friend% &chas been expired.", 1, 2),
    COMMAND_FRIENDTP_EXPIRED_NOTIFY("&cYour friends &e%friend% &cteleportation request has expired.", 1, 2),
    COMMAND_FRIENDTP_NOTIFY_REQUEST("COMPLEX:&3Your friend &e%friend% &3wants to teleport to you.\n" +
            "   &3Do hover:{&e/skill tpaccept %friend%,&2Accept,/skill tpaccept %friend%} &3to accept.\n" +
            "   &3Or ignore to &cdecline &3automatically.", 1, 2),

    COMMAND_UNFRIEND_DESCRIPTION("&eRemove a player as your friend.", 1, 2),
    COMMAND_UNFRIEND_REMOVED("&cYou've unfriended &e%removed%", 1, 2),
    COMMAND_UNFRIEND_SELF("&cYou must be really disappointed to unfriend yourself.", 1, 2),
    COMMAND_UNFRIEND_INVALID("&cYou're not friends with &e%removed%", 1, 2),
    COMMAND_UNFRIEND_NOTIFICATION("&e%remover% &chas unfriended you!", 1, 2),

    COMMAND_FRIENDS_DESCRIPTION("&eList of all your friends.", 1, 2),
    COMMAND_FRIENDS_TELEPORT_NOT_ONLINE("&e%name% &cis not online to teleport!", 1, 2),
    COMMAND_FRIENDS_TELEPORT_PERMISSION("&cYou don't have permission to teleport to friends.", 1, 2),

    COMMAND_PARTY_DESCRIPTION("&eParty with your friends.", 1, 2, 3),
    COMMAND_PARTY_CREATE_DESCRIPTION("&3Create a new party.", 1, 2, 3),
    COMMAND_PARTY_CREATE_NAME("&cYou need to choose a name for your party.", 1, 2, 3),
    COMMAND_PARTY_CREATE_CREATED("&3You've created a new party!", 1, 2, 3),
    COMMAND_PARTY_CREATE_ALREADY_PARTYING("&cYou're already in a party.", 1, 2, 3),

    COMMAND_PARTY_RENAME_DESCRIPTION("&eRename your party name.", 1, 2, 3),
    COMMAND_PARTY_RENAME_NAME("&cYou need to choose a name for your party.", 1, 2, 3),
    COMMAND_PARTY_RENAME_RENAMED("&3Party name has been changed to &e%name%", 1, 2, 3),

    COMMAND_PARTY_LEAVE_DESCRIPTION("&eLeave your current party.", 1, 2, 3),
    COMMAND_PARTY_LEAVE_LEFT("&e%player% &chas left the party.", 1, 2, 3),
    COMMAND_PARTY_LEAVE_DISBAND_CONFIRM("&cYou're the party leader. If you leave the party it'll disband it. Do the command again if you wish to continue.", 1, 2, 3),
    COMMAND_PARTY_LEAVE_DISBANDED("&cThe &e%party% &cparty has been disbanded.", 1, 2, 3),

    COMMAND_PARTY_INVITE_DESCRIPTION("&eInvite other players to your party.", 1, 2, 3),
    COMMAND_PARTY_INVITE_SELF("&cYou can't... invite yourself...", 1, 2, 3),
    COMMAND_PARTY_INVITE_INVITED("&3Invited &e%invited% &3to the party.", 1, 2, 3),
    COMMAND_PARTY_INVITE_MAX("&cYou can't invite more members.", 1, 2, 3),
    COMMAND_PARTY_INVITE_PERMISSION("&cOnly party moderators can invite players.", 1, 2, 3),
    COMMAND_PARTY_INVITE_ALREADY_INVITED("&e%invited% &3is already invited to the party.", 1, 2, 3),
    COMMAND_PARTY_INVITE_ARLEADY_PARTYING("&e%invited% &cis already in another party.", 1, 2, 3),
    COMMAND_PARTY_INVITE_NOTIFICATION("COMPLEX:&e%inviter% &3has invited you to their party.\n" +
            "Do hover:{&2/skill party accept %party%,&2Accept,/skill party accept %party%} &3to join the party.\nOr ignore it to refuse.", 1, 2, 3),
    COMMAND_PARTY_INVITE_EXPIRED("&cYour invitation to join &e%inviter% &cparty has expired!", 1, 2, 3),

    COMMAND_PARTY_KICK_DESCRIPTION("&eKick a member from your party", 1, 2, 3),
    COMMAND_PARTY_KICK_KICKED("&e%kicked% &chas been kicked out of the party by &e%kicker%", 1, 2, 3),
    COMMAND_PARTY_KICK_SELF("COMPLEX:&cIf you want to kick yourself, just do hover:{&e/skill party leave,&eLeave,/skill party leave}", 1, 2, 3),
    COMMAND_PARTY_KICK_NOT_IN_PARTY("&e%kicked% &cis not in your party.", 1, 2, 3),
    COMMAND_PARTY_KICK_PERMISSION("&cOnly party moderators can kick members.", 1, 2, 3),

    COMMAND_PARTY_MOD_DESCRIPTION("&ePromote or demote party members.", 1, 2, 3),
    COMMAND_PARTY_MOD_PROMOTED("&e%mod% &3has been promoted to a party moderator.", 1, 2, 3),
    COMMAND_PARTY_MOD_DEMOTED("&e%mod% &chas been demoted.", 1, 2, 3),
    COMMAND_PARTY_MOD_PERMISSION("&cOnly party leader can promote and demote members.", 1, 2, 3),
    COMMAND_PARTY_MOD_SELF("&cYou can't demote yourself...", 1, 2, 3),
    COMMAND_PARTY_MOD_NOT_IN_PARTY("&e%mod% &cis not in your party.", 1, 2, 3),

    COMMAND_PARTY_LEADER_DESCRIPTION("&eSet a new leader for the party.", 1, 2, 3),
    COMMAND_PARTY_LEADER_SET("&e%leader% &3has been set as the new party leader!", 1, 2, 3),
    COMMAND_PARTY_LEADER_PERMISSION("&cOnly party leader can set the leader.", 1, 2, 3),
    COMMAND_PARTY_LEADER_SELF("&cYou're already the leader...", 1, 2, 3),
    COMMAND_PARTY_LEADER_NOT_IN_PARTY("&e%leader% &cis not in your party.", 1, 2, 3),

    COMMAND_PARTY_CHAT_DESCRIPTION("&3Toggle the party chat.", 1, 2, 3),
    COMMAND_PARTY_CHAT_ENABLED("&2Party chat has been enabled.", 1, 2, 3),
    COMMAND_PARTY_CHAT_DISABLED("&cParty chat has been disabled.", 1, 2, 3),

    COMMAND_PARTY_SPY_DESCRIPTION("&3Toggle social spy for parties chat.", 1, 2, 3),
    COMMAND_PARTY_SPY_ON("&2Party Spy Mode&8: &2ON", 1, 2, 3),
    COMMAND_PARTY_SPY_OFF("&2Party Spy Mode&8: &cOFF", 1, 2, 3),

    COMMAND_PARTY_ACCEPT_DESCRIPTION("&eAccept an invitation to a party.", 1, 2, 3),
    COMMAND_PARTY_ACCEPT_JOINED("&9%player% &2has joined the party!", 1, 2, 3),
    COMMAND_PARTY_ACCEPT_NOT_INVITED("&cYou're not invited to any parties.", 1, 2, 3),
    COMMAND_PARTY_ACCEPT_NOT_INVITED_SPECIFIC("&cYou're not invited to &e%party%", 1, 2, 3),
    COMMAND_PARTY_ACCEPT_NAME("&cYou have multiple invites. Please specify the party name you want to join.", 1, 2, 3),
    COMMAND_PARTY_ACCEPT_IN_PARTY("&cYou're already in a party.", 1, 2, 3),

    COMMAND_PARTY_LIST_DESCRIPTION("&eA list of party members.", 1, 2),

    NOT_ENOUGH_SOULS("&cYou don't have enough souls!", 2),
    NOT_ENOUGH_STATPOINTS("&cNot enough stat points.", 2),

    // Misc
    KILL_MESSAGE("&3Gained &e%xp% XP &3and &e%souls% souls &3for killing &e%name%"),
    PLAYER_NOT_FOUND("&cCould not find player &e%name%."),
    INVALID_TIME("&cInvalid time."),
    PLAYERS_ONLY("&cOnly players can use this command."),
    Party_Members_too_far("&cYour party members are too far apart! No bonus is available."),
    NO_SKILL("&cYou don't have a skill!"),
    NO_PARTY("&cYou're not in a party!"),
    NO_SKILL_DISPLAYNAME("None"),
    PERMISSION("&4You don't have permission to use that command!"),
    DEATH("&cYou've lost &e%souls% &csoul(s) &cand &e%xp% &cXP."),
    EVENT_NOT_RUNNING("&e%event% &cevent is not running!"),
    BONUS_INACTIVE("&cBonus inactive"),
    MAX_LEVEL("&cYou have hit the maximum level. You won't be able to level up anymore."),

    ABILITY_UPGRADED("&3Skill ability upgraded!", 1),
    ABILITY_MAXXED("&cSkill ability is at maximum level.", 1),
    ABILITY_NOT_FOUND("&e%ability% &3ability not found.", 1),
    ABILITY_INVALID_LEVEL("&cAbility level must be a value between 0-3", 1),
    ABILITY_REQUIRED_LEVEL("&cYou need to be at least level &e%level% &cto use this ability.", 1),
    ABILITY_UPGRADE_NOTIFICATION("&3You have &e%skills_soul% &3souls. You can now upgrade&8: %abilities%", 1),
    ABILITY_UPGRADE_NOTIFICATION_LIST("&6%ability% &7➛ &3%ability_level% ", 1),

    SKILLS_ITEM_ENERGY_BOOSTER("&3Energy Booster &e%booster% &3activated for &e%time%", 2),
    SKILLS_ITEM_COST("&cYou need &e%cost% &csouls to buy this item.", 2),
    SKILLS_ITEM_NO_FREE_SLOT("&cYou don't have a free slot in your inventory.", 2),

    // Skill
    SKILL_NO_PERMISSION("&cYou don't have permission to use &e%skill%", 1),
    SKILL_OWNED_ERROR("&4You already own a skill!", 1),
    SKILL_REQUIRED_LEVEL("&cYou need to be at least level &e%level% &cto use this skill.", 1),
    SKILL_SELECTED("COMPLEX:&3You are now a &e%skills_skill_displayname%\n&3You can upgrade your abilities from hover:{&e/skill improve,&eClick to open,/skill improve}"),
    SKILL_CHANGE_COOLDOWN_ERROR("&cYou need to wait &e%cooldown% &cto change your skill.", 1),
    SKILL_SELECT_COST("&cYou need &e%cost% &csouls to select this skill.", 1),
    SKILL_AFFECTED_BY("&3This improvement's effectivity is\nalso affected by &e%stat%", 1),

    SKILL_MAXIMUM_LEVEL("&cThis improvement is upgraded to the max level.", 1),
    ABILITY_TITLE("&6&lPassive", 1, 2),
    ABILITY_SWORDSMAN_PASSIVE_TITLE("&6Passive", 1, 3),
    ABILITY_ARBALIST_PASSIVE_TITLE("&6Passive", 1, 3),
    ABILITY_EIDOLON_PASSIVE_TITLE("&6Passive", 1, 3),
    ABILITY_DEVOURER_PASSIVE_TITLE("&6Passive", 1, 3),
    ABILITY_MAGE_PASSIVE_TITLE("&6Passive", 1, 3),
    ABILITY_FIREMAGE_PASSIVE_TITLE("&6Passive", 1, 3),
    ABILITY_PRIEST_PASSIVE_TITLE("&6Passive", 1, 3),
    ABILITY_VAMPIRE_PASSIVE_TITLE("&6Passive", 1, 3),
    ABILITY_JUGGERNAUT_PASSIVE_TITLE("&6Passive", 1, 3),
    ABILITY_NATURES_FORCE_TITLE("&2Nature's Force", 1, 3),
    ABILITY_NATURES_CALL_TITLE("&2Nature's Call", 1, 3),

    ABILITY_DODGE_TITLE("&6Dodge", 1, 2),
    ABILITY_PARRY_TITLE("&6Parry", 1, 2),
    ABILITY_PIERCE_TITLE("&6Pierce", 1, 2),
    ABILITY_THOUSAND_CUTS_TITLE("&6Thousand Cuts", 1, 3),
    ABILITY_GLIDERS_TITLE("&6Gliders", 1, 2),
    ABILITY_CLOAK_TITLE("&6Cloak", 1, 2),
    ABILITY_BLINK_TITLE("&6Blink", 1, 2),
    ABILITY_HOOK_TITLE("&6Hook", 1, 2),
    ABILITY_REFLECT_TITLE("&6Reflect", 1, 2),
    ABILITY_EXPLOSION_SPELL_TITLE("&6Explosion Spell", 1, 3),
    ABILITY_HEAL_SPELL_TITLE("&6Heal Spell", 1, 3),
    ABILITY_ENERGY_FLUX_TITLE("&6Energy Flux", 1, 3),
    ABILITY_ABSORB_ENERGY_TITLE("&6Absorb Energy", 1, 3),
    ABILITY_PHOENIX_ESSENCE_TITLE("&6Phoenix Essence", 1, 3),
    ABILITY_BLACK_FIRE_TITLE("&6Black Fire", 1, 3),
    ABILITY_INFERNO_TITLE("&6Inferno", 1, 2),
    ABILITY_HEAVY_STRIKES_TITLE("&6Heavy Strikes", 1, 3),
    ABILITY_STONE_SKIN_TITLE("&6Stone Skin", 1, 3),
    ABILITY_AEGIS_PROTECTION_TITLE("&6Aegis Protection", 1, 3),
    ABILITY_THROW_TITLE("&6Throw", 1, 2),
    ABILITY_EXECUTE_TITLE("&6Execute", 1, 2),
    ABILITY_DUAL_ARROWS_TITLE("&6Dual Arrows", 1, 3),
    ABILITY_MINIONS_TITLE("&6Minions Arrow", 1, 2),
    ABILITY_FIRE_CROSSBOW_TITLE("&6Fire Crossbow", 1, 3),
    ABILITY_LOCATE_TITLE("&6Locate", 1, 2),
    ABILITY_BLOOD_LUST_TITLE("&6Blood Lust", 1, 3),
    ABILITY_BLOOD_WELL_TITLE("&6Blood Well", 1, 3),
    ABILITY_BLEED_TITLE("&6Bleed", 1, 2),
    ABILITY_ETERNAL_DARKNESS_TITLE("&6Eternal Darkness", 1, 3),
    ABILITY_PURIFY_TITLE("&6Purify", 1, 2),
    ABILITY_SPIRIT_FIRE_TITLE("&6Spirit Fire", 1, 3),
    ABILITY_SPECTRE_TITLE("&6Spectre", 1, 2),
    ABILITY_SHAPE_SHIFTER_TITLE("&6Shape Shifter", 1, 3),
    ABILITY_CHRONOPROHIBERIS_TITLE("&8Chronoprohiberis", 1, 2),
    ABILITY_DEFILE_TITLE("&6Defile", 1, 2),
    ABILITY_SWORDSMAN_PASSIVE_DESCRIPTION("&3You can use your offhand to attack now!\n" +
            "Your attack is increased by &e%damage%", 1, 3),
    ABILITY_DODGE_DESCRIPTION("&3You have a %chance%% &3of dodging\nyour opponent's attack while holding a sword.", 1, 2),
    Skill_Swordsman_Dodge_Opponent_Message("&cYour attack was dodged!", 1, 2),
    Skill_Swordsman_Dodge_Message("&3Dodged attack!", 1, 2),
    ABILITY_PARRY_DESCRIPTION("&3If you are holding a sword,\nand your opponent has lesser\n" +
            "&3stat points &3than you, you reflect &e%damage-percent%%\n" +
            "&3damage back to your opponent", 1, 2),
    ABILITY_PIERCE_DESCRIPTION("&3If your &6STR &3is higher\nthan your opponent's &4DEF&3,\nyour attack will deal &e%damage-percent%%\n&3more damage", 1, 2),
    ABILITY_THOUSAND_CUTS_DESCRIPTION("&3Right click to activate\nwhile holding a sword\n&3Does &e%damage% &3damage per\nhit to the next target you attack,\nwhile disorienting " +
            "them each hit\nfor the next 2 seconds.", 1, 3),
    Skill_Swordsman_Active_Activation_Message("&3&lThousand Cuts activated! Hit your target as many times as you can!", 1, 2),
    Skill_Swordsman_Active_Finished_Message("&c&lThousand Cuts has worn off", 1, 2),
    Skill_Swordsman_Ready_Activation_Message("&3*You raise your weapon* Hit a target to activate Thousand Cuts", 1, 2),
    Skill_Swordsman_NotReady_Activation_Message("&7*You lower your weapon*", 1, 2),

    ABILITY_CONSUME_TITLE("&6Consume", 1, 2),
    ABILITY_CONSUME_DESCRIPTION("&3Have a %chance%% chance &3to steal\n%souls% &3souls from your opponents\nor deal %damage% damage\nif they have no souls.", 1, 2),
    ABILITY_DEVOURER_PASSIVE_DESCRIPTION("&3Your attacks inflict corrosion\nto your enemy, causing them\nto lose &e%damage% &3health\nevery time they're affected.", 1, 3),
    ABILITY_GLIDERS_DESCRIPTION("&3Level 1&8: &eWhen sprinting,\ngain &e5 seconds &espeed\ndepending on your level.\n&3Level 2&8: &eNo fall damage." +
            "\n&3Level 3&8: &eDouble jump!", 1, 2),
    ABILITY_CLOAK_DESCRIPTION("&3Level 1&8: &eGain invisibility permanently.\nWhen you attack, the invisibility\nis disabled for 30 seconds." +
            "\n&3Level 2: &eHostile mobs will naturally\nbe neutral to you.\nIf you attack a mob, neutrality is\ndisabled for 30 seconds.\n" +
            "Some mobs like Enderman, endermite,\nwither, guardians and the ender dragon\nwill be still able to see you." +
            "\n&3Level 3&8: &eThe first attack you make\nbefore becoming visible will\ndeal &6%damage% &emore damage.", 1, 2),
    Skill_Devourer_Invis_Disabled("&cYour invisibility is disabled for 30 seconds!", 1, 2),
    Skill_Devourer_Invis_Enabled("&1You fade into the darkness...", 1, 2),
    Skill_Devourer_Neutrality_Disabled("&cYour neutrality is disabled for 30 seconds!", 1, 2),
    Skill_Devourer_Neutrality_Enabled("&1You are now neutral to mobs.", 1, 2),
    ABILITY_BLINK_DESCRIPTION("&3Teleport around your target\nrandomly, creating a TPAura.\nThe teleportation randomness and counts\nincrease for each level.", 1, 2),
    ABILITY_HOOK_DESCRIPTION("&3Right-click to get a grappling hook.\nUseful to escape from your enemies.\nLeveling up only\n" +
            "increases your hook (arrow) range.\n\n" +
            "&3Level 2: &eYou will not take fall\ndamage from hooks.", 1, 2),
    ABILITY_MAGE_PASSIVE_DESCRIPTION("&3When attacking with staffs (hoe),\nyou gain back &e%energy% &3mana per hit.\nAttacking with hoes will deal more damage,\n" +
            "ranging from 4 for a wooden hoe,\nto 8 for a diamond hoe.", 1, 3),
    ABILITY_REFLECT_DESCRIPTION("&3When you are attacked,\nand you're holding\na staff, have &e%chance%% &3chance\n" +
            "(based on your staff material)\nto deal &e%damage% &3to the attacker.", 1, 2),
    ABILITY_CHRONOPROHIBERIS_DESCRIPTION("&3Stop time for nearby enemies\nthat are &2%range% &3blocks\naway from you for &2%duration% " +
            "&3seconds\nwhile holding a clock in your hand\nand do &2%damage% &3damage.", 1, 2),
    ABILITY_EXPLOSION_SPELL_DESCRIPTION("&3When you are attacking, and you're holding a\nstaff, have a chance\n(based on your staff material)\n" +
            "to deal &e%damage%% &3more damage to your enemy.", 1, 3),
    ABILITY_HEAL_SPELL_DESCRIPTION("&3Regenerate &e%regain%&3 additional health,\nif your health is below &e%health-percent%%", 1, 3),
    ABILITY_ENERGY_FLUX_DESCRIPTION("&3When right-clicking with a hoe,\nrapidly fire energy bolts that\ndeal &e%damage% &3damage in a line.\n" +
            "More levels gives you more\ndistance to hit your target.", 1, 3),
    ABILITY_DISARM_TITLE("&6Disarm", 1, 2),
    ABILITY_DISARM_DESCRIPTION("&3Level 1&8: &ePlace your opponents weapon\nin their inventory if\nthey have a free space.\n" +
            "&3Level 2&8: &eReplace your opponents weapon\nwith an item in their inventory.\n" +
            "&3Level 3&8: &eAlso change the hotbar\nslot they're using.", 1, 2),
    ABILITY_LOCATE_DESCRIPTION("&3Locate nearby players within &2%range% &3range\nwith the power of the wind.\n\n" +
            "&eHold a compass while activating\nthis ability.", 1, 2),
    ABILITY_LOCATE_NOT_FOUND("&cNo nearby players found.", 1, 2),
    Skill_Mage_Activation_Message("&3&l*WHOOSH!*", 1, 2),
    ABILITY_FIREMAGE_PASSIVE_DESCRIPTION("&3When on fire, regain &e%heal% &3extra\nhealth when healing.\nYou are also immune to fire damage.", 1, 3),
    ABILITY_ABSORB_ENERGY_DESCRIPTION("&3When attacked, your next attack will\nset your targets on fire\nfor up to &e%fire% &3seconds.", 1, 3),
    ABILITY_PHOENIX_ESSENCE_DESCRIPTION("&3After a few seconds a huge flaming ball will\nthrow your enemies backwards and\n" +
            "you'll summon a dead horse with\nhigh speed and jump strenght.\n" +
            "&3Level 1&8: &eSlash forward by left-clicking\nthat deals &e%damage_slash% damage&3.\n" +
            "&3Level 2&8: &eSummon small volcanos in front of you\nthat throw your enemies\nin the air and sets them on fire.\n" +
            "&3Level 3&8: &eLightning will strike any nearby\nenemies within %radius_lightning% radius.", 1, 3),
    ABILITY_BLACK_FIRE_DESCRIPTION("&3When targets in a radius\nof &65 blocks &3to you,\nincrease their fire damage by &e%damage%", 1, 3),
    ABILITY_NATURES_FORCE_DESCRIPTION("&3Spawns bees every %interval% seconds\n&3for %duration% seconds &3from a beehive\nto fight for you.\nThese bees have %damage% &3damage.\n" +
            "If someone breaks the beehive,\nbees will stop spawning.", 1, 3),
    ABILITY_NATURES_CALL_DESCRIPTION("&3Spawns %minions% &3foxes to fight\nfor you. Each fox has\nan attack power of %damage%", 1, 3),
    ABILITY_DASH_TITLE("&7Dash", 1, 2),
    ABILITY_ANNIHILATION_TITLE("&4&lAnnihilation", 1, 2),
    ABILITY_ANNIHILATION_DESCRIPTION("&3Summon multiple swords\n&3to fight for you\nin a rhythm.\nLeft-click to shoot swords.", 1, 2),
    ABILITY_DASH_DESCRIPTION("&3Level 1&8: &eDash while holding a sword.\nYour dash will be stronger\nwith higher levels.\nYour current power&8: &2%velocity%\n" +
            "&3Level 2&8: &eKnock your targets\nwhile dashing.\n" +
            "&3Level 3&8: &eDamage your targets while dashing.\n\n" +
            "&6Range&8: &2%range%\n" +
            "&6Damage&8: &2%damage%\n" +
            "&6Knockback Power&8: &2%knockback%", 1, 2),
    ABILITY_INFERNO_DESCRIPTION("&3Right-click and hit a target\nwith your staff to activate.\nImmediately deal &e%damage% damage&3,\n" +
            "plus 1 damage per &e%per-fire-tick% fireticks &3the target has.\nThis will also increase your own fire ticks.", 1, 2),
    Skill_FireMage_Ready_Activation_Message("&3*Your hands burns with power* Hit a target to activate Inferno", 1, 2),
    Skill_FireMage_NotReady_Activation_Message("&7*The power dissipates*", 1, 2),
    Skill_FireMage_Activation_Message("&3&lYour target is blasted with infernal power!", 1, 2),
    SKILL_JUGGERNAUT_CHAIN_SMASH_TELEPORT("&cYou can't teleport while performing chain smash.", 1, 2, 4),

    ABILITY_NEPTUNE_TITLE("&9Neptune", 1, 2),
    ABILITY_NEPTUNE_DESCRIPTION("&3Level 1&8: &eDeal %damage% &emore damage with tridents.\n" +
            "Have a %chances_lightning% &echance to\nstrike your opponent with\nlightning when using tridents.\n" +
            "&3Level 2&8: &eHave a %chances_multiply% &echance to\nspawn %multiply% &emultiply tridents\ntargetting your opponent after\na successful hit with trident.\n" +
            "&3Level 3&8: &eYou can launch yourself with\nyour trident while holding\nit in your offhand and sneaking" +
            "\nThis is a little tricky.\nYou need to let go of the sneak\n" +
            "button once you released the\nright-click button. You can\ntechnically fly with this thing.", 1, 2),

    ABILITY_BLOOD_CIRCUIT_TITLE("&4Blood Circuit", 1, 3),
    ABILITY_BLOOD_CIRCUIT_DESCRIPTION("&3Level 1&8: &eSpawn an ender crystal that\nacts like a blood circuit between\nyou and your opponent for %duration% seconds.\n" +
            "Every %inferno% ticks, the ender crystal will\nactivate and damage your opponent by %damage%\nand increases this damage by %damage-modifier%\n" +
            "Your inferno will stop activating if your\ntarget is %distance% blocks &efurther than the crystal.\n" +
            "&3Level 2&8: &eYou'll be healed the same amount of\ndamage your infernodeals to your opponent.\n" +
            "&3Level 3&8: &eSpawn vex minions every time\nyour inferno activates.", 1, 3),

    ABILITY_JUGGERNAUT_PASSIVE_DESCRIPTION("&3Reduce entity attacks by &e%shield%", 1, 3),
    ABILITY_HEAVY_STRIKES_DESCRIPTION("&3If you are using an axe to attack,\ndeal &e%durability-damage% &3more damage to your\ntargets armors.", 1, 3),
    ABILITY_STONE_SKIN_DESCRIPTION("&3Level 1&8: &eFall damage is reduced by &6%protections_fall%\n" +
            "&3Level 2&8: &eFire/lava damage is\nreduced by &6%protections_fire%/%protections_lava%\n" +
            "&3Level 3&8: &eThorns damage is\nis reduced by %protections_thorns%", 1, 3),
    ABILITY_AEGIS_PROTECTION_DESCRIPTION("&eLevel 1&8: &3Every &e%damage% &3seconds,\nrefresh a shield that negates all\ndamage for the next attack.\n" +
            "&eLevel 2&8: &3Reflect &e%damage% &3damage.\n" +
            "&eLevel 3&8: &3Knockback your target\nif your health is under 5.", 1, 3),
    ABILITY_THROW_DESCRIPTION("&3Right-click and hit\nyour target to activate.\nCarry your target for &e%carry-time% &3seconds,\n" +
            "making you and your target unable\nto attack for the" +
            "duration,\nthen toss your enemy in the\ndirection you're looking at.\nOnce your target hits the ground,\ndeal &e%damage% &3damage to your target." +
            "\nWhile you are carrying your target,\nall incoming damage from the\ncarrying target is reduced by &e%shield-percent%%", 1, 2),
    Skill_Juggernaut_Active_Ready_Message("&3*You ready yourself* Hit a target to carry and throw it", 1, 2),
    Skill_Juggernaut_Active_NotReady_Message("&7*You lower your fists*", 1, 2),
    Skill_Juggernaut_Active_Activated_Message("&3&lYou grab your opponent above your head!", 1, 2),
    Skill_Juggernaut_Active_Throw_Message_Countdown("&c&lAIM! Throwing opponent in your direction in &e&l%countdown%", 1, 2),
    Skill_Juggernaut_Active_Throw_Success("&3&lThrew target at location!", 1, 2),
    Skill_Juggernaut_Aegis_Success("&3Your aegis shield blocks the attack!", 1, 2),

    ABILITY_ARBALIST_PASSIVE_DESCRIPTION("&3The longer the distance\nbetween you and your target,\nthe higher the damage.\n\nHave a chance to spawn\na cloud to shoot arrows.\n" +
            "The higher your skill level\nis the higher your chance gets.", 1, 3),
    ABILITY_EXECUTE_DESCRIPTION("&3After shooting a single target\n2 times in a row, have a chance to\nperform one of these combos.\n\n" +
            "&eLevel 1&8: &3Have a chance to set\nyour target on fire for 5 seconds.\n" +
            "&eLevel 2&8: &3Have a chance to poison\nyour target.\n" +
            "&eLevel 3&8: &3Have a chance to strike\na lightning on your target.\n\n" +
            "Chances will increase the more\nyou shoot the target.\nYour combo will reset if you miss\nor hit another target.", 1, 2),
    ABILITY_DUAL_ARROWS_DESCRIPTION("&3Your bow has %chance%% chance \nto fire two arrows at once.\n" +
            "&3Level 1&8: &eIncrease your second \narrow damage by &3%damage%&e.\n" +
            "&3Level 2&8: &eThe second arrow will\nhave a higher velocity.\n" +
            "&3Level 3&8: &eThe second arrow will\nbe a flame arrow.", 1, 3),
    ABILITY_MINIONS_DESCRIPTION("&3Right-click and shoot \nwith a bow to\nspawn minion skeletons that will\nfight for you.\n\n&eLevel 1&8: &3One skeleton\n" +
            "&eLevel 2&8: &3Two skeletons with invisibility\neffect and better bows.\n&eLevel 3&8: &33 Pillagers with invisibility\nand flame arrows.", 1, 2),
    SKILL_ARBALIST_MINIONS_MSG("&cYou spawned your minions.", 1, 2),
    ABILITY_FIRE_CROSSBOW_DESCRIPTION("&3Fire an arrow at your enemy\nby punching. Does not use arrows,\nand instead uses energy.\nUpgrade to reduce cooldown", 1, 3),
    ABILITY_VAMPIRE_PASSIVE_DESCRIPTION("&3You'll lose blood over time.\nAttacking gives you a few\nblood per hit. " +
            "For every\n%blood% blood you have, deal an additional\n&e%damage% &3melee damage to your targets.", 1, 3),
    ABILITY_BLOOD_LUST_DESCRIPTION("&3Level 1: If you have more than 70% of\nyour blood well full, go into a frenzy,\nallowing you to deal\n&e%damage% &3more damage than " +
            "usual.\nLevel 2: Your natural health regeneration is\nincreased by %regain% during this time. \nLevel 3: Attacks during this duration\n" +
            "have a chance (based on your\ncurrent blood) to heal you by 2", 1, 3),
    ABILITY_BLOOD_WELL_DESCRIPTION("&3Increases your maximum blood levels by a large\namount every upgrade. Has a &e%chance%% &3chance\nto stop the loss of blood every 4 seconds."
            , 1, 3),
    ABILITY_BLEED_DESCRIPTION("&3Your target will bleed for\n5 seconds. Every time\nyour target gets damaged %damage-heal%\n" +
            "and you get health for the same amount.", 1, 2),
    ABILITY_ETERNAL_DARKNESS_DESCRIPTION("&3Right-click and hit a target to become\ninvincible for &e%duration% &3seconds.\nDuring this time, " +
            "you blind the targets\nyou attack for 1 second per hit.", 1, 3),
    ABILITY_EIDOLON_PASSIVE_DESCRIPTION("&3Eidolon has a light and dark form.\nWhen your Imbalance hits &6100&3,\nyou change to another form.\n" +
            "Imbalance is gained over time.\nUpon changing, restore &e%heal% &3health.", 1, 3),
    ABILITY_PURIFY_DESCRIPTION("&3Deal &e%damage% &3damage for\nevery %hp% health your target has,\nup to a maximum of %max-damage%&3.\n" +
            "&6%energy-charge% &3imbalance is gained when you attack.", 1, 2),
    ABILITY_SPIRIT_FIRE_DESCRIPTION("&3In light form, absorb &e%light-damage-absorption-percent%%\n" +
            "&3damage when hit, and %max-damage-absorption% overall damage.\n" +
            "&3This amount is stored. \n&3In Dark form, expell the stored\n" +
            "amount in the first attack to deal\n%dark-damage-release% &3damage absorbed", 1, 3),
    ABILITY_SPECTRE_DESCRIPTION("&3In Light form, gain a shield, that will\ngive speed &e%speed% &3for &e3 seconds\n" +
            "&3when broken by another mob.\nShield refreshes every &e%cooldown% &3seconds.\n" +
            "In Dark form, your attack will\nbe charged every &e%cooldown% &3seconds.\n" +
            "Charged attacks deal &6%dark-damage%%\n&3additional damage.", 1, 2),
    ABILITY_SHAPE_SHIFTER_DESCRIPTION("&3When in dark, you'll deal more\ndamage to the enemy.\nThe darker the more damage you deal.\n" +
            "When you're in light places,\nyou'll take less damage.\nThe brighter the fewer damage you'll take.", 1, 3),
    ABILITY_DEFILE_DESCRIPTION("&3Right-click and attack to tip\nthe balance in your target,\nincreasing its damage taken by %damage%&3,\n" +
            "and reducing its damage dealt to\nyou by %shield%% &3for &e%time% seconds&3.", 1, 2),
    Skill_Eidolon_Active_Ready("&3*Disruptive energy radiates from you* Punch a target to defile it", 1, 2),
    Skill_Eidolon_Active_Not_Ready("&c*The energy dissipates*", 1, 2),
    Skill_Eidolon_Active_Activation_Message("&3&lYou disrupted your target's spiritual balance!", 1, 2),
    Skill_Eidolon_Turn_Light("", 1, 2),
    Skill_Eidolon_Turn_Dark("", 1, 2),
    Skill_Eidolon_Spirit_Expell("&3You blast your target with %damage% extra damage!", 1, 2),
    Skill_Eidolon_Shield_Refresh("&3You feel a surge of energy", 1, 2),
    Skill_Eidolon_Shield_Speed("&3Your shield shatters to give you speed!", 1, 2),
    Skill_Eidolon_Attack_Boost("&3You expell your charged attack to deal %damage%% more damage!", 1, 2),
    ABILITY_PURIFICATION_TITLE("&6Purification", 1, 2),
    ABILITY_PURIFICATION_DESCRIPTION("&eLevel 1&8: &3Guardians, endermen and\nslimes will no longer target you.\n" +
            "&eLevel 2&8: &3Elder guardians, spiders and\nphantoms will no longer target you.\nNot that if you attack them they\nwill attack back.\n" +
            "&eLevel 3&8: &3Crops around you will\ngrow faster.", 1, 2),
    ABILITY_SEAL_OF_LIFE_TITLE("&6Seal of Life", 1, 4),
    ABILITY_SEAL_OF_LIFE_DESCRIPTION("&3Have a chance to escape enemies\nwhen severely wounded.\nBe invincible and increase regeneration.", 1, 4),
    ABILITY_PRIEST_PASSIVE_DESCRIPTION("&3Walk on water like Jesus.\nYour body automatically rejects lava.\n" +
            "&cYou become weak and\nvulnerable in nether and the end!", 1, 3),

    ABILITY_FANGS_TITLE("&6Fangs", 1, 2),
    ABILITY_FANGS_DESCRIPTION("&3Ever wanted to get rid of those\nannoying evokers?\nNow you can kill them with their\nown ability and even better!\n" +
            "Sneak while activating\nthe ability to use fangs\non surrounding mobs\nor activate it normally to\nspawn fangs in a straight line.", 1, 2),
    ABILITY_BLACKHOLE_TITLE("&6Blackhole", 1, 2),
    ABILITY_BLACKHOLE_DESCRIPTION("&3Create a blackhole and pull\nenemies around into it.\nThis blackhole will deal\n" +
            "massive damage if someone gets close to it.", 1, 2),

    ABILITY_METEORITE_TITLE("&6Meteorite", 1, 2),
    ABILITY_METEORITE_DESCRIPTION("&3Fire %fireballs% fireballs &3at the targetted location.\n" +
            "Once a meteorite has landed,\nit'll deal %damage% damage\nto all enemies within &e%range% blocks&3.\n", 1, 2),

    ABILITY_CHAIN_SMASH_TITLE("&6Chain Smash", 1, 2),
    ABILITY_CHAIN_SMASH_DESCRIPTION("&3Jump and do a\nheavy punch to the\nground and blow everyone\ninto the air.", 1, 2),

    ABILITY_MIND_POSSESSION_TITLE("&6Mind Possession", 1, 3),
    ABILITY_MIND_POSSESSION_DESCRIPTION("&3Have &3%chance%% chance &3to paralyze your\nenemies with slowness and blindness.\n" +
            "Higher levels give higher chances and duration.", 1, 3),
    ABILITY_KINDLING_OF_LIFE_TITLE("&6Kindling of Life", 1, 4),
    ABILITY_KINDLING_OF_LIFE_DESCRIPTION("&3Heal your team mates by &e%heal% &3amount.\nHigher levels gives higher velocity\nand higher health.\n" +
            "Activating the ability while sneaking,\nwill heal yourself.", 1, 4),
    ABILITY_BARRIER_TITLE("&4Barrier", 1, 2),
    ABILITY_BARRIER_DESCRIPTION("&3Activate a shield while holding\na golden hoe to push all the\nenemies out of the barrier.\nEnemies that try to enter this shield\n" +
            "are affected and knocked back.\n\nThis ability is specially useful in\nclose spaces as enemies can't\nescape and keep getting damaged.", 1, 2),
    ABILITY_ASCLEPIUS_TITLE("&6Asclepius", 1, 2),
    ABILITY_ASCLEPIUS_DESCRIPTION("&3Give effects to players around you\n while holding a hoe.\nSupport your teammates before a fight\nwith some buffs.\n" +
            "Only works on party\nmembers or factions.", 1, 2),

    // Masteries
    MASTERY_MAXIMUM_LEVEL("&cThis mastery is upgraded to the max level.", 1),
    MASTERY_NOT_FOUND("&e%mastery% &3mastery not found.", 1),
    MASTERY_UPGRADED("&3Upgraded &e%mastery% &3mastery for &e%cost% &3souls.", 1),
    MASTERY_DOWNGRADED("&3Downgraded &e%mastery% &3mastery and gained &e%cost% &3souls.", 1),
    MASTERY_REQUIRED_LEVEL("&cYou need to be at least level &3%level% &cto use this mastery.", 1),
    MASTERY_CANT_DOWNGRADE("&cYou can't downgrade &e%mastery% &cmastery anymore.", 1);


    public static final EnumSet<SkillsLang> VALUES = EnumSet.allOf(SkillsLang.class);
    private final String defaultValue;
    private final String path;

    SkillsLang(String defaultValue, String path) {
        this.defaultValue = defaultValue;
        this.path = path;
    }

    SkillsLang(String defaultValue, int... group) {
        this.defaultValue = defaultValue;
        this.path = StringUtils.getGroupedOption(this.name(), group);
    }

    SkillsLang(String defaultValue) {
        this.defaultValue = defaultValue;
        this.path = this.name().toLowerCase().replace('_', '-');
    }

    public String parse(CommandSender sender, Object... edits) {
        return parse((OfflinePlayer) sender, edits);
    }

    public String parse(OfflinePlayer player, Object... edits) {
        String parsed = parse(edits);
        return player == null ? parsed : ServiceHandler.translatePlaceholders(player, parsed);
    }

    public String getLang() {
        return LanguageManager.getMessage(this);
    }

    public String parse(Player player, Object... edits) {
        return parse((OfflinePlayer) player, edits);
    }

    public String parse(Object... edits) {
        return MessageHandler.colorize(MessageHandler.replaceVariables(LanguageManager.getMessage(this), edits));
    }

    public String parse(OfflinePlayer sender) {
        return parse(sender, new Object[0]);
    }

    public void sendMessage(Player player, Object... edits) {
        sendMessage(player, player, edits);
    }

    public void sendMessage(Player player, OfflinePlayer placeholder, Object... edits) {
        TextComponent main = HoverLang.getComplexMessage(placeholder, LanguageManager.getMessage(this), edits);
        if (main != null) {
            player.spigot().sendMessage(main);
            return;
        }

        String msg = parse(placeholder, edits);
        boolean prefix = SkillsConfig.PREFIX.getBoolean();
        if (msg.startsWith("NOPREFIX:")) {
            msg = msg.substring(9);
            prefix = false;
        }

        MessageHandler.sendMessage(player, msg, prefix);
    }

    public void sendMessage(CommandSender sender, OfflinePlayer placeholder, Object... edits) {
        if (sender instanceof Player)
            sendMessage((Player) sender, placeholder, edits);
        else
            sendConsoleMessage(placeholder, edits);
    }

    public void sendMessage(CommandSender sender, Object... edits) {
        sendMessage(sender, (OfflinePlayer) (sender instanceof OfflinePlayer ? sender : null), edits);
    }

    public void sendPlayersMessage(Object... edits) {
        for (Player players : Bukkit.getOnlinePlayers()) sendMessage(players, edits);
    }

    public void sendConsoleMessage(OfflinePlayer placeholder, Object... edits) {
        TextComponent main = HoverLang.getComplexMessage(placeholder, LanguageManager.getMessage(this), edits);
        String complex = null;
        if (main != null) complex = main.toLegacyText();
        if (complex == null) complex = parse(placeholder, edits);

        boolean prefix = SkillsConfig.PREFIX.getBoolean();
        if (complex.startsWith("NOPREFIX:")) {
            complex = complex.substring(9);
            prefix = false;
        }
        MessageHandler.sendMessage(Bukkit.getConsoleSender(), complex, prefix);
    }

    public void sendConsoleMessage(Object... edits) {
        sendConsoleMessage(null, edits);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getPath() {
        return path;
    }
}
