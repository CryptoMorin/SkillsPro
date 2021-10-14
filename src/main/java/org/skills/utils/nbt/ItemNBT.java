package org.skills.utils.nbt;

import com.cryptomorin.xseries.ReflectionUtils;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class ItemNBT {
    public static final boolean CAN_ACCESS_UNBREAKABLE;
    private static final MethodHandle AS_NMS_COPY;
    private static final MethodHandle AS_BUKKIT_COPY;
    private static final MethodHandle SET_TAG;
    private static final MethodHandle GET_TAG;

    static {
        String majorVersion = XMaterial.getMajorVersion(Bukkit.getVersion());
        CAN_ACCESS_UNBREAKABLE = majorVersion.startsWith("1.1") && Integer.parseInt(majorVersion.substring(3, 4)) != 0;

        MethodHandle asNmsCopy = null;
        MethodHandle asBukkitCopy = null;
        MethodHandle setTag = null;
        MethodHandle getTag = null;

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> crafItemStack = ReflectionUtils.getCraftClass("inventory.CraftItemStack");
        Class<?> nmsItemStack = ReflectionUtils.getNMSClass("world.item", "ItemStack");
        Class<?> nbtTagCompound = ReflectionUtils.getNMSClass("nbt", "NBTTagCompound");

        try {
            asNmsCopy = lookup.findStatic(crafItemStack, "asNMSCopy", MethodType.methodType(nmsItemStack, ItemStack.class));
            asBukkitCopy = lookup.findStatic(crafItemStack, "asBukkitCopy", MethodType.methodType(ItemStack.class, nmsItemStack));

            setTag = lookup.findVirtual(nmsItemStack, "setTag", MethodType.methodType(void.class, nbtTagCompound));
            getTag = lookup.findVirtual(nmsItemStack, "getTag", MethodType.methodType(nbtTagCompound));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }

        AS_NMS_COPY = asNmsCopy;
        AS_BUKKIT_COPY = asBukkitCopy;
        SET_TAG = setTag;
        GET_TAG = getTag;
    }

    private static Object asNMSCopy(ItemStack item) {
        try {
            return AS_NMS_COPY.invoke(item);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    /**
     * Only pass a NMS Itemstack!
     *
     * @param nmsItem The NMS item to convert
     *
     * @return The converted Item
     */
    private static ItemStack asBukkitCopy(Object nmsItem) {
        try {
            return (ItemStack) AS_BUKKIT_COPY.invoke(nmsItem);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    public static ItemStack addSimpleTag(ItemStack item, String tag, String value) {
        NBTWrappers.NBTTagCompound compound = ItemNBT.getTag(item);
        compound.setString(tag, value);
        return ItemNBT.setTag(item, compound);
    }

    /**
     * Sets the NBT tag of an item
     *
     * @param tag  The new tag
     * @param item The ItemStack
     *
     * @return The modified itemStack
     */
    @NonNull
    public static ItemStack setTag(@NonNull ItemStack item, NBTWrappers.NBTTagCompound tag) {
        Object nbtTag = tag.toNBT();
        Object nmsItem = asNMSCopy(item);

        try {
            SET_TAG.invoke(nmsItem, nbtTag);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return asBukkitCopy(nmsItem);
    }

    /**
     * Gets the NBTTag of an item. In case of any error it returns a blank one.
     *
     * @param item The ItemStack to get the tag for
     *
     * @return The NBTTagCompound of the ItemStack or a new one if it had none or an error occurred
     */
//    @NonNull
    public static NBTWrappers.NBTTagCompound getTag(@NonNull ItemStack item) {
        Object nmsItem = asNMSCopy(item);
        Object tag = null;
        try {
            tag = GET_TAG.invoke(nmsItem);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if (tag == null) return new NBTWrappers.NBTTagCompound();

        NBTWrappers.NBTTagCompound base = NBTWrappers.NBTTagCompound.fromNBT(tag);
        return base == null ? new NBTWrappers.NBTTagCompound() : base;
    }
}
