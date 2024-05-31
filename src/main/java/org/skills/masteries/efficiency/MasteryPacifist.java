package org.skills.masteries.efficiency;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XTag;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.masteries.managers.Mastery;
import org.skills.utils.versionsupport.VersionSupport;

import java.util.List;

public class MasteryPacifist extends Mastery {
    public MasteryPacifist() {
        super("Pacifist", true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPacifistGain(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Block block = event.getBlock();
        if (VersionSupport.isCropFullyGrown(block)) {
            XMaterial type = XMaterial.matchXMaterial(block.getType());
            List<String> disabled = getExtra("disabled-blocks").getStringList();
            if (XTag.anyMatchString(type, disabled)) return;

            SkilledPlayer info = this.checkup(player);
            if (info == null) return;
            info.addXP((int) getScaling(info));

            int exp = (int) getExtraScaling(info, "exp");
            if (exp != 0) player.giveExp(exp);
        }
    }
}
