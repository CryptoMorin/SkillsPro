package org.skills.masteries.efficiency;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.masteries.managers.Mastery;
import org.skills.utils.MathUtils;
import org.skills.utils.versionsupport.VersionSupport;

import java.util.Optional;

public class MasteryMining extends Mastery {
    public MasteryMining() {
        super("Mining", true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onMining(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (isPlaced(event.getBlock())) return;
        if (SkillsConfig.isInDisabledWorld(event.getPlayer().getLocation())) return;

        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        XMaterial type = XMaterial.matchXMaterial(event.getBlock().getType());
        ConfigurationSection section = getExtra("blocks." + type.name()).getSection();
        if (section == null) return;

        int lvl = info.getMasteryLevel(this);
        if (!MathUtils.hasChance((int) getAbsoluteScaling(info, section.getString("chance"), "lvl", lvl))) return;

        int exp = (int) getAbsoluteScaling(info, section.getString("exp"), "lvl", lvl);
        int xp = (int) getAbsoluteScaling(info, section.getString("xp"), "lvl", lvl);
        int souls = (int) getAbsoluteScaling(info, section.getString("souls"), "lvl", lvl);
        Location block = event.getBlock().getLocation();

        info.addXP(xp);
        info.addSouls(souls);
        VersionSupport.dropExp(block, exp);

        ConfigurationSection items = section.getConfigurationSection("drops");
        if (items == null) return;
        for (String item : items.getKeys(false)) {
            Optional<XMaterial> mat = XMaterial.matchXMaterial(items.getString(item + ".material"));
            if (!mat.isPresent()) continue;

            ItemStack drop = mat.get().parseItem();
            int amt = (int) getAbsoluteScaling(info, items.getString(item + ".amount"), "lvl", lvl);
            if (amt > 0) drop.setAmount(amt);
            player.getWorld().dropItemNaturally(block, drop);
        }
    }
}
