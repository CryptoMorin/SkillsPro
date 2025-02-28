package org.skills.managers;

import com.cryptomorin.commons.inventory.XInventoryView;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.MathEval;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class XPAndEnchantmentManager implements Listener {
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

            player.setTotalExperience(0);
            player.giveExp(totalExp);
        }
    }

    static double getCost(Player player, int enchantLvl, int cost, SkillsConfig option) {
        String eqn = option.getString();
        if (eqn == null || eqn.isEmpty() || eqn.equals("0")) return 0;

        eqn = MessageHandler.replaceVariables(eqn, "lvl", enchantLvl, "cost", cost);
        eqn = ServiceHandler.translatePlaceholders(player, eqn);
        return MathEval.evaluate(eqn);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);

        Integer firstEnchantLvl = event.getEnchantsToAdd().entrySet().iterator().next().getValue();
        int vanillaCost = event.getExpLevelCost();

        int lvlCost = (int) getCost(player, firstEnchantLvl, vanillaCost, SkillsConfig.ENCHANTMENT_TABLES_ADDITIONAL_COSTS_SKILL_LEVEL);
        double xpCost = getCost(player, firstEnchantLvl, vanillaCost, SkillsConfig.ENCHANTMENT_TABLES_ADDITIONAL_COSTS_SKILL_XP);
        long soulsCost = (long) getCost(player, firstEnchantLvl, vanillaCost, SkillsConfig.ENCHANTMENT_TABLES_ADDITIONAL_COSTS_SOULS);


        if (lvlCost >= 0) {
            if (info.getLevel() >= lvlCost) info.setLevel(info.getLevel() - lvlCost);
            else {
                XSound.BLOCK_NOTE_BLOCK_BASS.record().soundPlayer().forPlayers(player).play();
                SkillsLang.ENCHANTMENTS_COSTS_NOT_ENOUGH_SKILL_LEVEL.sendMessage(player, "%amount%", lvlCost);
                event.setCancelled(true);
                return;
            }
        }

        if (xpCost >= 0) {
            if (info.getXP() >= xpCost) info.addXP(-xpCost);
            else {
                XSound.BLOCK_NOTE_BLOCK_BASS.record().soundPlayer().forPlayers(player).play();
                SkillsLang.ENCHANTMENTS_COSTS_NOT_ENOUGH_SKILL_XP.sendMessage(player, "%amount%", xpCost);
                event.setCancelled(true);
                return;
            }
        }

        if (soulsCost >= 0) {
            if (info.getSouls() >= soulsCost) info.addSouls(-soulsCost);
            else {
                XSound.BLOCK_NOTE_BLOCK_BASS.record().soundPlayer().forPlayers(player).play();
                SkillsLang.ENCHANTMENTS_COSTS_NOT_ENOUGH_SOULS.sendMessage(player, "%amount%", soulsCost);
                event.setCancelled(true);
            }
        }
    }

    /**
     * Calculates total experience based on level.
     * https://minecraft.gamepedia.com/Experience#Leveling_up
     */
    static int getExpFromLevel(int level) {
        if (level >= 32) {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
        if (level >= 17) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        }
        return level * level + 6 * level;
    }

    static int getLevelFromExp(long exp) {
        if (exp > 1395) {
            return (int) Math.floor(((Math.sqrt(72 * exp - 54215) + 325) / 18));
        }
        if (exp > 315) {
            return (int) Math.floor((Math.sqrt(40 * exp - 7839) / 10 + 8.1));
        }
        if (exp > 0) {
            return (int) Math.floor((Math.sqrt(exp + 9) - 3));
        }
        return 0;
    }

    static int getExpDiffFrom(int level, int decrement) {
        if (decrement > level) throw new IllegalArgumentException(level + " < " + decrement);
        if (level - decrement <= 0) throw new IllegalArgumentException(level + " - " + decrement);
        return getExpFromLevel(level) - getExpFromLevel(level - decrement);
    }

    static int getExpToNext(int level) {
        if (level > 30) return 9 * level - 158;
        if (level > 15) return 5 * level - 38;
        return 2 * level + 7;
    }

    /**
     * Note Bukkit API's stupid ass named "getExpLevelCost()" incorrectly. It's not the cost, but the required level for the enchantment.
     * Required level and the level cost are different. The required level is on the right hand side of the offer and the cost level
     * is on the left hand side of the offer.
     * <p>
     * getTotalExp() isn't always updated for some fucking reasons.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEnchantFinalizeEXP(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            int neededExp = getExpToNext(player.getLevel());
            int progressExptoAmount = (int) (neededExp * player.getExp());
            int totalExp = getExpFromLevel(player.getLevel()) + progressExptoAmount;
            EXPS.put(event.getEnchanter().getUniqueId(), totalExp);
        }, 1L);
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
        InventoryType type = XInventoryView.of(event.getView()).getType();
        if (type != InventoryType.ENCHANTING && type != InventoryType.ANVIL) return;
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        HealthAndEnergyManager.updateXPBar(player);
    }

    /**
     * This is always called in all situations unlike PlayerExpChangeEvent.
     */
//    @EventHandler(priority =  EventPriority.HIGH)
//    public void onLvL(PlayerLevelChangeEvent event) {
//    }

    /**
     * Isn't called when using /experience, when enchanting items or giveExp()
     * Called for EXP bottles
     */
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
