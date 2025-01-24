package org.skills.abilities.mage;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.reflection.XReflection;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.managers.DamageManager;
import org.skills.utils.EntityUtil;
import org.skills.utils.LocationUtils;
import org.skills.utils.versionsupport.VersionSupport;

import java.util.HashSet;

public class MageEnergyFlux extends InstantActiveAbility {
    public MageEnergyFlux() {
        super("Mage", "energy_flux");
    }

    private static Color getColor(double level) {
        if (level < 3) return Color.BLACK;
        if (level < 6) return Color.SILVER;
        if (level < 10) return Color.GRAY;
        if (level < 15) return Color.WHITE;
        if (level < 20) return Color.BLUE;
        if (level < 25) return Color.NAVY;
        if (level < 30) return Color.LIME;
        if (level < 35) return Color.GREEN;
        if (level < 40) return Color.AQUA;
        if (level < 45) return Color.TEAL;
        if (level < 50) return Color.YELLOW;
        if (level < 60) return Color.ORANGE;
        if (level < 70) return Color.PURPLE;
        if (level < 100) return Color.MAROON;
        return Color.RED;
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();

        Location start = LocationUtils.getHandLocation(player, false);
        Location end = start.clone().add(player.getEyeLocation().getDirection().multiply(getScaling(info, "range")));
        XSound.ENTITY_FIREWORK_ROCKET_TWINKLE.play(start);

        double damage = this.getScaling(info, "damage");
        int lvl = info.getAbilityLevel(this);
        double distanceLvl = lvl * 0.5;

        World world = player.getWorld();
        Color color = getColor(info.getLevel());
        HashSet<Integer> hits = new HashSet<>();
        hits.add(player.getEntityId());

        Vector distance = end.toVector().subtract(start.toVector());
        double length = distance.length();
        distance.normalize();

        double x = distance.getX();
        double y = distance.getY();
        double z = distance.getZ();

        boolean canPassable = XReflection.supports(13);
        boolean noPassThro = !getOptions(info, "pass-through").getBoolean();

        for (double i = 0.5D; i < length; i += 0.1) {
            Location loc = start.clone().add(x * i, y * i, z * i);
            VersionSupport.spawnColouredDust(loc, new java.awt.Color(color.asRGB()));
            if (noPassThro || lvl < 3) {
                if (canPassable) {
                    if (!loc.getBlock().isPassable()) break;
                } else {
                    if (!loc.getBlock().getType().name().endsWith("AIR")) break;
                }
            }

            for (Entity entity : world.getNearbyEntities(loc, distanceLvl, distanceLvl, distanceLvl)) {
                if (EntityUtil.filterEntity(player, entity)) continue;
                if (!hits.add(entity.getEntityId())) continue;
                DamageManager.damage((LivingEntity) entity, player, damage);
            }
        }
    }
}