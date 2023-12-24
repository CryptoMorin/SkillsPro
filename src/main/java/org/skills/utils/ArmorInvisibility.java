package org.skills.utils;

import com.cryptomorin.xseries.ReflectionUtils;
import com.cryptomorin.xseries.XMaterial;
import com.mojang.datafixers.util.Pair;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.List;

/**
 * Currently not used. Supposed to be used to hide Devourer's armor using {@link org.skills.abilities.devourer.DevourerCloak} ability.
 */
public final class ArmorInvisibility {
    private static final Object AIR_ARMOR, SLOT_HEAD, SLOT_CHEST, SLOT_LEGS, SLOT_FEET;
    private static final MethodHandle PACKET, AS_NMS_COPY;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> craftItemStack = ReflectionUtils.getCraftClass("inventory.CraftItemStack");
        Class<?> nmsItem = ReflectionUtils.getCraftClass("inventory.CraftItemStack");
        Class<?> packetClass = ReflectionUtils.getNMSClass("PacketPlayOutEntityEquipment");
        Class<?> enumItemSlot = ReflectionUtils.getNMSClass("EnumItemSlot");

        MethodHandle packet = null, asNmsCopy = null;
        Object head = null, chest = null, legs = null, feet = null;
        Object airArmor = null;

        try {
            asNmsCopy = lookup.findStatic(craftItemStack, "asNMSCopy", MethodType.methodType(nmsItem, ItemStack.class));
            airArmor = asNmsCopy.invoke(new ItemStack(Material.AIR));

            if (XMaterial.supports(16))
                packet = lookup.findConstructor(packetClass, MethodType.methodType(void.class, int.class, List.class));
            else
                packet = lookup.findConstructor(packetClass, MethodType.methodType(void.class, int.class, enumItemSlot, nmsItem));

            for (Object slot : enumItemSlot.getEnumConstants()) {
                String name = slot.toString();
                switch (name) {
                    case "HEAD":
                        head = slot;
                        break;
                    case "CHEST":
                        chest = slot;
                        break;
                    case "LEGS":
                        legs = slot;
                        break;
                    case "FEET":
                        feet = slot;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        PACKET = packet;
        AIR_ARMOR = airArmor;
        AS_NMS_COPY = asNmsCopy;

        SLOT_HEAD = head;
        SLOT_CHEST = chest;
        SLOT_LEGS = legs;
        SLOT_FEET = feet;
    }

    private ArmorInvisibility() {
    }

    private static void sendAllPacket(World world, Object... packets) {
        for (Player player : world.getPlayers()) ReflectionUtils.sendPacket(player, packets);
    }

    public void setArmorInvisible(LivingEntity entity) {
        int id = entity.getEntityId();
        try {
            Object head = PACKET.invoke(id, Collections.singletonList(Pair.of(SLOT_HEAD, AIR_ARMOR)));
            Object chest = PACKET.invoke(id, Collections.singletonList(Pair.of(SLOT_CHEST, AIR_ARMOR)));
            Object legs = PACKET.invoke(id, Collections.singletonList(Pair.of(SLOT_LEGS, AIR_ARMOR)));
            Object feet = PACKET.invoke(id, Collections.singletonList(Pair.of(SLOT_FEET, AIR_ARMOR)));
            sendAllPacket(entity.getWorld(), head, chest, legs, feet);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void setArmorVisible(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        ItemStack helmet = equipment.getHelmet();
        ItemStack chestplate = equipment.getChestplate();
        ItemStack leggings = equipment.getLeggings();
        ItemStack boots = equipment.getBoots();
        int id = entity.getEntityId();

        try {
            Object head = PACKET.invoke(id, Collections.singletonList(Pair.of(SLOT_HEAD, AS_NMS_COPY.invoke(helmet))));
            Object chest = PACKET.invoke(id, Collections.singletonList(Pair.of(SLOT_CHEST, AS_NMS_COPY.invoke(chestplate))));
            Object legs = PACKET.invoke(id, Collections.singletonList(Pair.of(SLOT_LEGS, AS_NMS_COPY.invoke(leggings))));
            Object feet = PACKET.invoke(id, Collections.singletonList(Pair.of(SLOT_FEET, AS_NMS_COPY.invoke(boots))));
            sendAllPacket(entity.getWorld(), head, chest, legs, feet);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
