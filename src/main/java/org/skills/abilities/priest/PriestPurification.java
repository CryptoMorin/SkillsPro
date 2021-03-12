package org.skills.abilities.priest;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.utils.MathUtils;

import java.util.*;

public class PriestPurification extends Ability {
    private static final Map<UUID, Set<UUID>> BATERAYED = new HashMap<>();
    private static final ImmutableList<XMaterial> FLOWERS = ImmutableList.of(XMaterial.BLUE_ORCHID, XMaterial.DANDELION,
            XMaterial.ALLIUM, XMaterial.AZURE_BLUET, XMaterial.LILY_OF_THE_VALLEY, XMaterial.CORNFLOWER, XMaterial.LILAC,
            XMaterial.OXEYE_DAISY);

    private static final EnumSet<XMaterial> CROPS = EnumSet.of(
            XMaterial.WHEAT, XMaterial.POTATOES, XMaterial.CARROTS,
            XMaterial.CARROT, XMaterial.POTATO, XMaterial.NETHER_WART, XMaterial.WHEAT_SEEDS, XMaterial.PUMPKIN_STEM,
            XMaterial.MELON_STEM, XMaterial.BEETROOTS, XMaterial.SUGAR_CANE, XMaterial.BAMBOO_SAPLING, XMaterial.CHORUS_PLANT,
            XMaterial.KELP, XMaterial.SEA_PICKLE
    );

    public PriestPurification() {
        super("Priest", "purification");
    }

    protected static void spreadFlower(Player player, int chance, int radius) {
        if (chance <= 0) return;
        Block floor = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        List<Block> blocks = new ArrayList<>();
        blocks.add(floor);

        int baseX = floor.getX();
        int baseZ = floor.getZ();

        for (int x = baseX - radius; x < baseX + radius; x++) {
            for (int z = baseZ - radius; z < baseZ + radius; z++) {
                blocks.add(new Location(floor.getWorld(), x, floor.getY(), z).getBlock());
            }
        }

        int maxFlowers = FLOWERS.size() - 1;

        Material grassBlock = XMaterial.GRASS_BLOCK.parseMaterial();
        Material grass = XMaterial.TALL_GRASS.parseMaterial();
        for (Block block : blocks) {
            if (block.getType() == Material.DIRT) block.setType(grassBlock);
            else if (block.getType() != grassBlock) continue;

            Block up = block.getRelative(BlockFace.UP);
            if (up.getType().name().endsWith("AIR")) {
                if (MathUtils.hasChance(chance / 2)) {
                    up.setType(grass);
                    player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation(), 10, 0.5, 0.5, 0.5, 0);
                } else if (MathUtils.hasChance(chance)) {
                    up.setType(FLOWERS.get(MathUtils.randInt(0, maxFlowers)).parseMaterial());
                    player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation(), 10, 0.5, 0.5, 0.5, 0);
                }
            }
        }
    }

    @Override
    public void start() {
        if (!XMaterial.isNewVersion()) return;
        ParticleDisplay display = new ParticleDisplay(Particle.VILLAGER_HAPPY, null, 30, 1, 1, 1);

        addTask(new BukkitRunnable() {

            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    SkilledPlayer info = PriestPurification.this.checkup(player);
                    if (info == null) continue;
                    int lvl = info.getImprovementLevel(PriestPurification.this);
                    if (lvl < 3) continue;
                    int radius = (int) getScaling(info);

                    for (int x = -radius; x < radius; x++) {
                        for (int z = -radius; z < radius; z++) {
                            Block block = player.getLocation().getBlock().getRelative(x, 0, z);
                            XMaterial matched = XMaterial.matchXMaterial(block.getType());
                            if (!CROPS.contains(matched)) continue;
                            // !matched.name().endsWith("SAPLING") &&

                            BlockData data = block.getBlockData();
//                                if (data instanceof Sapling) {
//                                    Sapling sapling = (Sapling) data;
//                                    sapling.setStage(sapling.getMaximumStage() + 1);
//                                    Bukkit.getScheduler().runTask(SkillsPro.get(), () -> block.setBlockData(data));
////
////                                    BlockState state = block.getState();
////                                    MaterialData dat = state.getData();
////                                        TreeSpecies species = ((Wood) dat).getSpecies();
//                                    MessageHandler.sendConsolePluginMessage("CALLED " + sapling.getStage() + " And " + sapling.getMaximumStage());
////                                    Bukkit.getScheduler().runTask(SkillsPro.get(), () ->
////                                            block.getWorld().generateTree(block.getLocation(), TreeType.DARK_OAK));
//                                }

                            if (data instanceof Ageable) {
                                Ageable age = (Ageable) data;
                                if (age.getAge() != age.getMaximumAge()) {
                                    age.setAge(age.getAge() + 1);
                                    Bukkit.getScheduler().runTask(SkillsPro.get(), () -> block.setBlockData(age));
                                    display.spawn(block.getLocation());
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 60L, getExtra("Priest", "rate").getInt()));

        /*
        BlockState state = block.getState();
        Crops crops = (Crops) state.getData();
        if (crops.getState() != CropState.RIPE) {
            Bukkit.getScheduler().runTask(SkillsPro.get(), () -> {
                crops.setState(CropState.getByData((byte) (crops.getState().getData() + 1)));
                state.update(true);
            });
            display.spawn(block.getLocation());
        }
         */
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobPeaceBateryal(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (event.getEntity() instanceof Player) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        Set<UUID> list = BATERAYED.get(player.getUniqueId());
        if (list != null) list.add(event.getEntity().getUniqueId());
        else BATERAYED.put(player.getUniqueId(), new HashSet<>(Collections.singleton(event.getEntity().getUniqueId())));
    }

    @EventHandler
    public void onBateryalKill(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null) return;
        Set<UUID> betray = BATERAYED.get(player.getUniqueId());
        if (betray == null) return;

        if (betray.remove(event.getEntity().getUniqueId())) {
            if (betray.isEmpty()) BATERAYED.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onMobPeace(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player)) return;
        Player player = (Player) event.getTarget();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        int lvl = info.getImprovementLevel(this);
        if (lvl < 1) return;

        Entity mob = event.getEntity();
        Set<UUID> betray = BATERAYED.get(player.getUniqueId());
        if (betray != null && betray.contains(mob.getUniqueId())) return;

        switch (mob.getType()) {
            case GUARDIAN:
            case SPIDER:
            case SLIME:
                event.setCancelled(true);
                return;
        }

        if (lvl > 2) {
            String type = mob.getType().name();
            if (type.equals("ENDERMAN") || type.equals("ELDER_GUARDIAN") || type.equals("PHANTOM")) event.setCancelled(true);
        }
    }
}
