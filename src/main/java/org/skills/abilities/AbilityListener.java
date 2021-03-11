package org.skills.abilities;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.google.common.base.Enums;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.skills.api.events.CustomHudChangeEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.managers.SkillItemManager;
import org.skills.services.manager.ServiceHandler;
import org.skills.types.SkillScaling;
import org.skills.utils.LocationUtils;
import org.skills.utils.nbt.ItemNBT;
import org.skills.utils.nbt.NBTType;
import org.skills.utils.nbt.NBTWrappers;

import java.util.Map;
import java.util.Set;

public class AbilityListener implements Listener {
    private static boolean activate(Player player, AbilityActivation.ActivationAction action) {
        if (player.getGameMode() == GameMode.CREATIVE && !player.hasPermission("skills.use-creative")) return false;
        if (SkillsConfig.isInDisabledWorld(player.getLocation())) return false;
        if (SkillsConfig.DISABLE_ABILITIES_IN_REGIONS.getBoolean() && ServiceHandler.isPvPOff(player)) return false;

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (!info.hasSkill()) return false;
        if (info.isActiveReady()) return false;

        ActiveAbility ability = null;
        String keys = AbilityActivation.ACTIVATIONS.getIfPresent(player.getUniqueId());
        if (keys == null) keys = String.valueOf(action.shortName);
        else keys += String.valueOf(action.shortName);

        int index = keys.length();
        String ws = keys.substring(0, index - 1) + 'C';

        ItemStack item = player.getItemInHand();
        for (Ability abs : info.getSkill().getAbilities()) {
            if (!abs.isPassive() && info.getImprovementLevel(abs) > 0 && !info.isAbilityDisabled(abs)) {
                ActiveAbility activeAb = (ActiveAbility) abs;
                if (activeAb.isWeaponAllowed(info, item)) {
                    String activation = activeAb.getActivationKey(info);

                    boolean whileSneak = activation.startsWith(ws);
                    if (activation.startsWith(keys) || whileSneak) {
                        if (whileSneak) {
                            if (!player.isSneaking()) {
                                AbilityActivation.ACTIVATIONS.invalidate(player.getUniqueId());
                                return false;
                            }

                            index++;
                            keys = ws + action.shortName;
                        }

                        if (index == activation.length()) {
                            AbilityActivation.ACTIVATIONS.invalidate(player.getUniqueId());
                            ability = activeAb;
                            break;
                        }

                        AbilityActivation.ACTIVATIONS.put(player.getUniqueId(), keys);
                        return true;
                    }
                }
            }
        }

        if (ability == null) return false;

        // Cooldown
        if (info.isInCooldown()) {
            XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
            return true;
        }

        // Energy
        double energy = ability.getEnergy(info);
        if (info.getEnergy() < energy) {
            XSound.play(player, info.getSkill().getEnergy().getSoundNotEnough());
            return true;
        }

        String ready = ability.getAbilityReady(info);
        if (info.showReadyMessage() && ready != null) ability.sendMessage(player, ready);
        if (!info.setActiveReady(ability, true)) return true;
        CustomHudChangeEvent.call(player);
        info.setLastAbilityUsed(ability);

        // Activate instantly if that's what the active requires
        if (ability.activateOnReady) {
            ability.useSkill(player);
            return true;
        }

        ParticleDisplay display = ParticleDisplay.simple(player.getLocation(),
                Enums.getIfPresent(Particle.class, SkillsConfig.READY_PARTICLE_PARTICLE.getString()).or(Particle.SPELL_WITCH));
        display.count = (int) SkillsConfig.READY_PARTICLE_COUNT.eval(info, ability);
        double offset = SkillsConfig.READY_PARTICLE_OFFSET.eval(info, ability);
        display.offset(offset, offset, offset);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                display.spawn(LocationUtils.getHandLocation(player, true));
                if (!info.isActiveReady()) cancel();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 1L);

        // Un-ready the active if the player doesn't do anything with it
        XSound.ITEM_ARMOR_EQUIP_DIAMOND.play(player);
        ActiveAbility finalAb = ability;
        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            task.cancel();
            if (!player.isOnline()) return;
            if (info.setActiveReady(finalAb, false)) {
                String idle = finalAb.getAbilityIdle(info);
                if (info.showReadyMessage() && idle != null) player.sendMessage(idle);
                XSound.ITEM_ARMOR_EQUIP_CHAIN.play(player);
            }
        }, ability.getIdle(info) * 20L);
        return true;
    }

    @EventHandler
    public void onSneakActivate(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking()) activate(player, AbilityActivation.ActivationAction.SNEAK);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwordMove(InventoryPickupItemEvent event) {
        if (Ability.isSkillEntity(event.getItem())) event.setCancelled(true);
    }

    @EventHandler
    public void onQ(PlayerDropItemEvent event) {
        if (activate(event.getPlayer(), AbilityActivation.ActivationAction.DROP)) event.setCancelled(true);
    }

    @EventHandler
    public void onF(PlayerSwapHandItemsEvent event) {
        if (activate(event.getPlayer(), AbilityActivation.ActivationAction.SWITCH)) event.setCancelled(true);
    }

    @EventHandler
    public void onDisposablePlayerHandler(PlayerQuitEvent event) {
        int id = event.getPlayer().getEntityId();
        for (Set<Integer> set : Ability.DISPOSABLE_ENTITIES_SET) set.remove(id);
        for (Map<Integer, ?> map : Ability.DISPOSABLE_ENTITIES_MAP) map.remove(id);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSkillDamageCap(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        double cap = info.getScaling(SkillScaling.DAMAGE_CAP);
        if (cap == 0) return;

        if (event.getFinalDamage() > cap) event.setDamage(cap);
    }

    @EventHandler
    public void onSkillActivate(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        Action action = event.getAction();
        if (action == Action.PHYSICAL) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null) {
            try {
                NBTWrappers.NBTTagCompound nbt = ItemNBT.getTag(item);
                nbt.get(SkillItemManager.SKILL_ITEM, NBTType.STRING);
            } catch (Exception ex) {
                MessageHandler.sendConsolePluginMessage("&cA NBT error has occurred! Please report this to the developer.");
                ex.printStackTrace();
            }

            Material type = item.getType();
            // Don't activate abilities if the player is trying to eat.
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)
                if (type.isEdible()) return;

            // Check if your item in hand is blacklisted
            XMaterial mat = XMaterial.matchXMaterial(item);
            if (mat.isOneOf(SkillsConfig.PREVENT_ACTIVATION_ITEMS.getStringList())) return;

            // If you're trying to place/break blocks, ignore.
            if (action == Action.RIGHT_CLICK_BLOCK) {
                Block clicked = event.getClickedBlock();
                if (item.getType().isBlock()) return;
                if (XMaterial.matchXMaterial(clicked.getType()).isOneOf(SkillsConfig.PREVENT_ACTIVATION_BLOCKS.getStringList())) return;
            }

            AbilityActivation.ActivationAction activationAction =
                    action.name().startsWith("LEFT") ?
                            AbilityActivation.ActivationAction.LEFT_CLICK :
                            AbilityActivation.ActivationAction.RIGHT_CLICK;
            activate(player, activationAction);
        }
    }
}
