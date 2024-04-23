package org.skills.utils;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.trait.RotationTrait;
import net.citizensnpcs.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcmonkey.sentinel.SentinelTrait;
import org.skills.main.SkillsPro;
import org.skills.managers.DamageManager;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NPCHandler {
    private static final String METADATA = "DOPPELGANGER";
    private static final Map<UUID, UUID> DOPPEL = new HashMap<>();
    private static final Map<EquipmentSlot, Equipment.EquipmentSlot> SLOT_MAPPING = new EnumMap<>(EquipmentSlot.class);

    static {
        SLOT_MAPPING.put(EquipmentSlot.HEAD, Equipment.EquipmentSlot.HELMET);
        SLOT_MAPPING.put(EquipmentSlot.CHEST, Equipment.EquipmentSlot.CHESTPLATE);
        SLOT_MAPPING.put(EquipmentSlot.LEGS, Equipment.EquipmentSlot.LEGGINGS);
        SLOT_MAPPING.put(EquipmentSlot.FEET, Equipment.EquipmentSlot.BOOTS);
        SLOT_MAPPING.put(EquipmentSlot.HAND, Equipment.EquipmentSlot.HAND);
        SLOT_MAPPING.put(EquipmentSlot.OFF_HAND, Equipment.EquipmentSlot.OFF_HAND);
    }

//    @EventHandler
//    public void onDeath(EntityDeathEvent event) {
//        UUID killerNPCId = PENDING_TARGETS.remove(event.getEntity().getUniqueId());
//        if (killerNPCId == null) return;
//        NPC killerNPC = CitizensAPI.getNPCRegistry().getByUniqueId(killerNPCId);
//
//        SLogger.info("killer is " + killerNPCId + " - " + killerNPC);
//        if (killerNPC == null) return;
//        if (killerNPC != null) {
//            SLogger.info("Chasing: " + killerNPC.getOrAddTrait(SentinelTrait.class).chasing);
//            followOwner(killerNPC);
//        }
//    }

    private static void attack(NPC npc, LivingEntity victim) {
        // Following causes sentinal to stop working.
//        FollowTrait follow = npc.getOrAddTrait(FollowTrait.class);
//        follow.follow(null);

        SentinelTrait sentinal = npc.getOrAddTrait(SentinelTrait.class);
        sentinal.attackHelper.chase(victim);
        sentinal.attackHelper.tryAttack(victim);
    }

    public static NPC getDoppel(Player player) {
        UUID id = DOPPEL.get(player.getUniqueId());
        if (id == null) return null;
        return CitizensAPI.getNPCRegistry().getByUniqueId(id);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDoppelDespawn(NPCDespawnEvent event) {
        // Respawns because of its skin...
        if (event.getReason() == DespawnReason.PENDING_RESPAWN) return;
        Owner owner = event.getNPC().getOrAddTrait(Owner.class);
        if (owner != null && owner.getOwnerId() != null) DOPPEL.remove(owner.getOwnerId());
    }

//    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
//    public void onDoppelRespawn(NPCSpawnEvent event) {
//        if (event.getReason() != SpawnReason.RESPAWN) return;
//        followOwner(event.getNPC());
//    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHit(EntityDamageByEntityEvent event) {
        LivingEntity damager = DamageManager.getDamager(event);
        {
            NPC victimNPC = CitizensAPI.getNPCRegistry().getNPC(event.getEntity());
            if (victimNPC != null && damager.getType() != EntityType.PLAYER) {
                attack(victimNPC, damager);
                return;
            }
        }

        if (damager instanceof Player && event.getEntity() instanceof LivingEntity) {
            NPC guard = getDoppel((Player) damager);
            if (guard != null) {
                attack(guard, (LivingEntity) event.getEntity());
            }
        } else if (event.getEntity() instanceof Player && damager != null) {
            NPC guard = getDoppel((Player) event.getEntity());
            if (guard != null) {
                attack(guard, damager);
            }
        }
    }

    public static void spawnNPC(Player player, LivingEntity victim) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, player.getName());
        Inventory inv = npc.getOrAddTrait(Inventory.class);
        inv.setContents(player.getInventory().getContents());
        Equipment equipment = npc.getOrAddTrait(Equipment.class);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack item = player.getInventory().getItem(equipmentSlot);
            if (item != null) equipment.set(SLOT_MAPPING.get(equipmentSlot), item);
        }
        equipment.run();

        Owner owner = npc.getOrAddTrait(Owner.class);
        owner.setOwner(player);

        // What is this??
//        MirrorTrait mirrorTrait = npc.getOrAddTrait(MirrorTrait.class);
//        mirrorTrait.setMirrorName(true);
//        mirrorTrait.setEnabled(true);

        NavigatorParameters params = npc.getNavigator().getDefaultParameters();
        params.distanceMargin(5);
        // params.destinationTeleportMargin(); This is useless... it teleports the NPC when it reaches this distance, not when it cant path find.
        // params.stationaryTicks(20 * 5);

        npc.spawn(player.getLocation());
        npc.getEntity().setMetadata(METADATA, new FixedMetadataValue(SkillsPro.get(), player.getUniqueId()));

        // https://github.com/CitizensDev/CitizensAPI/blob/master/src/main/java/net/citizensnpcs/api/ai/NavigatorParameters.java
//        FollowTrait follow = npc.getOrAddTrait(FollowTrait.class);
//        follow.setProtect(false);

        // LookClose lookClose = npc.getOrAddTrait(LookClose.class);
        // lookClose.setRandomLook(true);
        // lookClose.setRealisticLooking(true); whether the NPC can see thru walls, not useful for us.

        SentinelTrait sentinal = npc.getOrAddTrait(SentinelTrait.class);
        sentinal.respawnTime = -1;
        sentinal.damage = 5;
        sentinal.attackRate = 20;
        sentinal.healRate = 0;
        sentinal.realistic = true;
        // sentinal.fightback = true;
        // sentinal.itemHelper.swapToMelee();
        attack(npc, victim);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!DOPPEL.containsKey(player.getUniqueId())) {
                    cancel();
                    return;
                }
                if (sentinal.chasing != null || !sentinal.targetingHelper.currentTargets.isEmpty()) return;
                Navigator nav = npc.getNavigator();
                if (nav.isNavigating()) return;

                double distance = LocationUtils.distanceSquared(npc.getEntity().getLocation(), player.getLocation());

                if (distance > 5) {
                    nav.getDefaultParameters().speedModifier((distance > 40) ? 2 : 1);
                    nav.setTarget(player.getLocation().clone());
                } else {
                    double pitchMin = -45, pitchMax = 45;
                    double yawMin = 0, yawMax = 360;

                    float pitch = Util.getFastRandom().doubles(pitchMin, pitchMax).iterator().next().floatValue();
                    float yaw = Util.getFastRandom().doubles(yawMin, yawMax).iterator().next().floatValue();
                    npc.getOrAddTrait(RotationTrait.class).getPhysicalSession().rotateToHave(yaw, pitch);
                }
            }
        }.runTaskTimer(SkillsPro.get(), 0L, 20L * 2L);

        // This is just a stupid chat message, thought it's a chat bubble.
        // npc.getDefaultSpeechController().speak(new SpeechContext(npc, "Thy end is now"));

        DOPPEL.put(player.getUniqueId(), npc.getUniqueId());

        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            if (!npc.isSpawned()) return;
            ParticleDisplay.of(Particle.CLOUD).offset(1).spawn(npc.getEntity().getLocation());
            npc.despawn(DespawnReason.PLUGIN);
        }, 20L * 60L);
    }
}
