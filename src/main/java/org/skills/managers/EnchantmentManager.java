package org.skills.managers;

import com.cryptomorin.xseries.XSound;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.MessageHandler;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.MathUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EnchantmentManager implements Listener {
    protected static final Map<UUID, Integer> EXPS = new HashMap<>();

    public static void onDisable() {
        for (Map.Entry<UUID, Integer> exp : EXPS.entrySet()) {
            Player player = Bukkit.getPlayer(exp.getKey());
            if (player == null) return;
            player.setTotalExperience(exp.getValue());
        }
        EXPS.clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEnchantmentTableOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        InventoryType type = event.getView().getType();
        if (type != InventoryType.ENCHANTING && type != InventoryType.ANVIL) return;

        if (SkillsConfig.VANILLA_EXP_BAR_ENABLED.getBoolean() && !SkillsConfig.VANILLA_EXP_BAR_REAL_SYNC.getBoolean()) {
            Player player = (Player) event.getPlayer();
            int totalExp = EXPS.getOrDefault(player.getUniqueId(), player.getTotalExperience());
            player.setLevel(0);
            player.setExp(0);
            player.setTotalExperience(0);
            player.giveExp(totalExp);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepare(PrepareItemEnchantEvent event) {
        if (!SkillsConfig.SYNC_ENCHANTMENT_TABLES_ENABLED.getBoolean()) return;
        String costType = SkillsConfig.SYNC_ENCHANTMENT_TABLES_COST_TYPE.getString().toLowerCase(Locale.ENGLISH);
        if (costType.equals("vanilla")) return;

        Player player = event.getEnchanter();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        String equation = SkillsConfig.SYNC_ENCHANTMENT_TABLES_REQUIREMENT_LEVEL_EQUATION.getString();
        equation = ServiceHandler.translatePlaceholders(player, equation);

        for (EnchantmentOffer offer : event.getOffers()) {
            String offerLvlEqn = MessageHandler.replaceVariables(equation, "lvl", offer.getEnchantmentLevel(), "cost", offer.getCost());
            int offerLevel = (int) MathUtils.evaluateEquation(offerLvlEqn);
            offer.setEnchantmentLevel(offerLevel);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        if (!SkillsConfig.SYNC_ENCHANTMENT_TABLES_ENABLED.getBoolean()) return;
        String costType = SkillsConfig.SYNC_ENCHANTMENT_TABLES_COST_TYPE.getString().toLowerCase(Locale.ENGLISH);
        if (costType.equals("vanilla")) return;

        Player player = event.getEnchanter();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        player.setLevel(player.getLevel() - event.getExpLevelCost());

        int amount = event.getExpLevelCost();
        switch (costType) {
            case "souls":
                if (info.hasSounds(amount)) info.addSouls(-amount);
                else {
                    XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
                    event.setCancelled(true);
                }
                break;
            case "xp":
                if (info.getXP() >= amount) info.addXP(-amount);
                else {
                    XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
                    event.setCancelled(true);
                }
                break;
            case "levels":
                if (info.getLevel() >= amount) info.setLevel(info.getLevel() - amount);
                else {
                    XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
                    event.setCancelled(true);
                }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (SkillsConfig.VANILLA_EXP_BAR_ENABLED.getBoolean() && !SkillsConfig.VANILLA_EXP_BAR_REAL_SYNC.getBoolean()) {
            Player player = event.getEntity();
            Integer xp = EXPS.remove(player.getUniqueId());
            if (xp != null) event.setDroppedExp(xp);
        }
    }

    @EventHandler
    public void onEnchantTableReturnFakeXP(InventoryCloseEvent event) {
        InventoryType type = event.getView().getType();
        if (type != InventoryType.ENCHANTING && type != InventoryType.ANVIL) return;
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        if (SkillsConfig.VANILLA_EXP_BAR_ENABLED.getBoolean() && !SkillsConfig.VANILLA_EXP_BAR_REAL_SYNC.getBoolean()) {
            EXPS.put(player.getUniqueId(), player.getTotalExperience());
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
            float percent = (float) (info.getXP() / info.getLevelXP(info.getLevel()));
            Validate.isTrue(percent <= 1.0 && percent >= 0.0,
                    "Invalid BossBar percent for " + player.getName() + ": " + percent + " -> XP: " + info.getXP() +
                            ", Next Level XP: " + info.getLevelXP(info.getLevel()) + " for level " + info.getLevel());
            if (SkillsConfig.VANILLA_EXP_BAR_ENABLED.getBoolean()) {
                String shown = SkillsConfig.VANILLA_EXP_BAR_SHOWN_NUMBER.getString().toLowerCase(Locale.ENGLISH);
                int num;
                switch (shown) {
                    case "xp":
                        num = (int) info.getXP();
                        break;
                    case "level":
                        num = info.getLevel();
                        break;
                    case "souls":
                        num = (int) info.getSouls();
                        break;
                    default:
                        MessageHandler.sendConsolePluginMessage("&4Invalid 'shown-number' option for vanilla EXP bar&8: &e" + shown);
                        num = 0;
                }

                player.setLevel(num);
                player.setExp(percent);
            }
        }
//            Player player = (Player) event.getPlayer();
//            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
//
//            float percent = (float) (info.getXP() / info.getLevelXP(info.getLevel()));
//            int num = SkillsConfig.VANILLA_EXP_BAR_SHOW_LEVEL.getBoolean() ? info.getLevel() : (int) info.getXP();
//            setExp(player, percent, info.getLevel(), num);
//        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExpBar(PlayerExpChangeEvent event) {
        if (!SkillsConfig.VANILLA_EXP_BAR_ENABLED.getBoolean()) return;

        UUID id = event.getPlayer().getUniqueId();
        EXPS.put(id, EXPS.getOrDefault(id, event.getPlayer().getTotalExperience()) + event.getAmount());
        event.setAmount(0);

//        Bukkit.getScheduler().runTaskLaterAsynchronously(SkillsPro.get(), () -> {
//            Player player = event.getPlayer();
//            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
//
//            float percent = (float) (info.getXP() / info.getLevelXP(info.getLevel()));
//            int num = SkillsConfig.VANILLA_EXP_BAR_SHOW_LEVEL.getBoolean() ? info.getLevel() : (int) info.getXP();
//            setExp(player, percent, info.getLevel(), num);
//        }, 1L);
    }
}
