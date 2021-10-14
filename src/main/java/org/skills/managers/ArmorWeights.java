package org.skills.managers;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;

import java.util.Optional;

public final class ArmorWeights implements Listener {
    public static void trigger(Player player, ItemStack armor, boolean put) {
        ConfigurationSection section = SkillsConfig.ARMOR_WEIGHTS_CUSTOM.getSection();
        if (section == null) return;
        double weight = 0;

        if (armor.hasItemMeta()) {
            ItemMeta meta = armor.getItemMeta();
            String finalName = null;
            String finalLore = null;

            if (meta.hasDisplayName()) finalName = MessageHandler.stripColors(meta.getDisplayName(), true);
            if (meta.hasLore()) {
                StringBuilder publicLore = new StringBuilder();
                for (String lore : meta.getLore()) publicLore.append(lore);
                finalLore = MessageHandler.stripColors(publicLore.toString(), true);
            }

            for (String key : section.getKeys(false)) {
                ConfigurationSection sec = section.getConfigurationSection(key);
                String typeStr = sec.getString("type");
                if (typeStr != null) {
                    XMaterial mat = XMaterial.matchXMaterial(armor);
                    Optional<XMaterial> type = XMaterial.matchXMaterial(typeStr);
                    if (mat == type.orElse(null)) continue;
                }

                String name = sec.getString("name");
                String lore = sec.getString("lore");

                if ((finalName != null && !Strings.isNullOrEmpty(name) && finalName.contains(name)) ||
                        (finalLore != null && !Strings.isNullOrEmpty(lore) && finalLore.contains(lore))) weight = sec.getDouble("weight");
            }
        }

        if (weight == 0) {
            section = SkillsConfig.ARMOR_WEIGHTS_WEIGHTS.getSection();
            weight = section.getDouble(XMaterial.matchXMaterial(armor).name());
            if (weight == 0) return;
        }

        float finalModifier = (float) (put ? -weight : weight);
        player.setWalkSpeed(player.getWalkSpeed() + finalModifier);
    }

    public static void fullCheckup(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setWalkSpeed(0.2f);
                if (isInvalidGameMode(player.getGameMode())) return;
                if (player.hasPermission("skills.armorweight.bypass")) return;
                PlayerInventory inv = player.getInventory();

                if (inv.getHelmet() != null) trigger(player, inv.getHelmet(), true);
                if (inv.getChestplate() != null) trigger(player, inv.getChestplate(), true);
                if (inv.getLeggings() != null) trigger(player, inv.getLeggings(), true);
                if (inv.getBoots() != null) trigger(player, inv.getBoots(), true);
            }
        }.runTaskAsynchronously(SkillsPro.get());
    }

    private static boolean isInvalidGameMode(GameMode mode) {
        return mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR;
    }

    private static boolean isArmor(ItemStack item) {
        switch (XMaterial.matchXMaterial(item)) {
            case NETHERITE_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:

            case NETHERITE_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:

            case NETHERITE_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case LEATHER_LEGGINGS:

            case NETHERITE_BOOTS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
                return true;
            default:
                return false;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void dispenseArmorEvent(BlockDispenseArmorEvent event) {
        if (event.getTargetEntity() instanceof Player) {
            Player player = (Player) event.getTargetEntity();
            if (!isInvalidGameMode(player.getGameMode())) trigger(player, event.getItem(), true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) fullCheckup(player);
        }
    }

    @EventHandler
    public void onArmorBreak(PlayerItemBreakEvent event) {
        ItemStack item = event.getBrokenItem();
        if (isArmor(item)) trigger(event.getPlayer(), item, false);
    }

    @EventHandler
    public void onArmorWear(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (event.getHand() == EquipmentSlot.OFF_HAND && player.getMainHand() == MainHand.RIGHT) return;
        if (isInvalidGameMode(player.getGameMode())) return;

        ItemStack item = event.getItem();
        if (item == null || !isArmor(item)) return;
        int slot = player.getInventory().getHeldItemSlot();

        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            ItemStack newItem = player.getInventory().getItem(slot);
            if (newItem == null) trigger(player, item, true);
        }, 1L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        fullCheckup(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        fullCheckup(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        fullCheckup(event.getPlayer());
    }
}
