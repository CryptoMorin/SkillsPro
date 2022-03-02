package org.skills.utils;

import com.cryptomorin.xseries.XPotion;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.MessageHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Hologram implements Listener {
    private static final String HOLOGRAM = "HOLOGRAM";
    private static final Set<Entity> ARMORSTANDS = new HashSet<>();
    private static double
            maxx, minx,
            maxy, miny,
            maxz, minz;
    private static JavaPlugin plugin;

    public Hologram(JavaPlugin owner) {
        plugin = owner;
        Bukkit.getPluginManager().registerEvents(this, owner);
        load();
    }

    public static void load() {
        ConfigurationSection section = SkillsConfig.HOLOGRAM_OFFSET.getSection();

        String axis = section.getString("x");
        String[] offset = StringUtils.split(axis, ',');
        minx = NumberUtils.toDouble(offset[0], 0.7);
        maxx = NumberUtils.toDouble(offset[1], -0.7);

        axis = section.getString("y");
        offset = StringUtils.split(axis, ',');
        miny = NumberUtils.toDouble(offset[0], 1);
        maxy = NumberUtils.toDouble(offset[1], 0.8);

        axis = section.getString("z");
        offset = StringUtils.split(axis, ',');
        minz = NumberUtils.toDouble(offset[0], 0.7);
        maxz = NumberUtils.toDouble(offset[1], -0.7);
    }

    public static List<ArmorStand> spawn(Location location, List<String> text, Object... edits) {
        List<ArmorStand> stands = new ArrayList<>();
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        boolean staticArmor = SkillsConfig.HOLOGRAM_STATIC.getBoolean();
        XPotion.Effect effect = XPotion.parseEffect(SkillsConfig.HOLOGRAM_EFFECT.getString());

        for (String str : text) {
            Location spawn = staticArmor ?
                    location.clone().add(rand.nextDouble(minx, maxx), rand.nextDouble(miny, maxy), rand.nextDouble(minz, maxz)) : location;
            ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(spawn, EntityType.ARMOR_STAND);

            armorStand.setMetadata(HOLOGRAM, new FixedMetadataValue(plugin, null));
            armorStand.setInvulnerable(true);
            armorStand.setVisible(false);
            armorStand.setCollidable(false);
            armorStand.setCustomNameVisible(true);
            if (staticArmor) armorStand.setMarker(true);
            else armorStand.setVelocity(new Vector(rand.nextDouble(minx, maxx), rand.nextDouble(miny, maxy), rand.nextDouble(minz, maxz)));
            if (effect != null && effect.hasChance()) armorStand.addPotionEffect(effect.getEffect());

            for (int i = edits.length; i > 0; i -= 2) {
                String variable = String.valueOf(edits[i - 2]);
                String replacement = String.valueOf(edits[i - 1]);
                str = StringUtils.replace(str, variable, replacement);
            }
            armorStand.setCustomName(MessageHandler.colorize(str));
            stands.add(armorStand);
        }

        return stands;
    }

    public static void spawn(Location location, long stay, List<String> text, Object... edits) {
        List<ArmorStand> stands = spawn(location, text, edits);
        ARMORSTANDS.addAll(stands);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (ArmorStand armorStand : stands) armorStand.remove();
                stands.forEach(ARMORSTANDS::remove);
            }
        }.runTaskLater(plugin, stay);
    }

    public static void onDisable() {
        ARMORSTANDS.forEach(Entity::remove);
    }

    public static boolean isHologram(Entity entity) {
        return entity.hasMetadata(HOLOGRAM);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.hasMetadata(HOLOGRAM)) entity.remove();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void hologramFire(EntityCombustEvent event) {
        if (event.getEntity().hasMetadata(HOLOGRAM)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void manipulate(PlayerArmorStandManipulateEvent event) {
        if (event.getRightClicked().hasMetadata(HOLOGRAM)) event.setCancelled(true);
    }
}
