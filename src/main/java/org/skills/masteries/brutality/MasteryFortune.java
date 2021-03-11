package org.skills.masteries.brutality;

import com.cryptomorin.xseries.XItemStack;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.MessageHandler;
import org.skills.masteries.managers.Mastery;
import org.skills.utils.MathUtils;

public class MasteryFortune extends Mastery {
    public MasteryFortune() {
        super("Fortune", true);
    }

    @EventHandler
    public void onHarvest(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player player = entity.getKiller();
        if (player == null) return;

        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (SkillsConfig.isInDisabledWorld(player.getLocation())) return;

        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        EntityType type = entity.getType();
        ConfigurationSection section = getExtra("mobs." + type.name()).getSection();
        if (section == null) return;

        int lvl = info.getMasteryLevel(this);
        Location block = entity.getLocation();

        World world = player.getWorld();
        for (String item : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(item);
            if (!MathUtils.hasChance((int) getAbsoluteScaling(info, itemSection.getString("chance")))) continue;

            ItemStack drop = XItemStack.deserialize(itemSection);
            if (drop == null) {
                MessageHandler.sendConsolePluginMessage("&4Could not parse Fortune mastery item for option: &e" + itemSection.getName() + " &4with properties&8:");
                itemSection.getValues(true).forEach((k, v) -> MessageHandler.sendConsolePluginMessage("&6" + k + "&8: &e" + (v instanceof ConfigurationSection ? "" : v)));
                continue;
            }

            int amt = (int) getAbsoluteScaling(info, itemSection.getString("amount"), "lvl", lvl);
            if (amt > 0) drop.setAmount(amt);
            world.dropItemNaturally(block, drop);
        }
    }
}
