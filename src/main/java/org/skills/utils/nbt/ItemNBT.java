package org.skills.utils.nbt;

import com.cryptomorin.xseries.ReflectionUtils;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static com.cryptomorin.xseries.ReflectionUtils.supports;

public final class ItemNBT {
    public static final boolean CAN_ACCESS_UNBREAKABLE = supports(11);
    private static final MethodHandle AS_NMS_COPY;
    private static final MethodHandle AS_BUKKIT_COPY;
    private static final MethodHandle SET_TAG;
    private static final MethodHandle GET_TAG;

    static {
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

            setTag = lookup.findVirtual(nmsItemStack,
                    supports(18) ? "c" : "setTag", MethodType.methodType(void.class, nbtTagCompound));
            getTag = lookup.findVirtual(nmsItemStack,
                    supports(18) ? "s" : "getTag", MethodType.methodType(nbtTagCompound));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }

        AS_NMS_COPY = asNmsCopy;
        AS_BUKKIT_COPY = asBukkitCopy;
        SET_TAG = setTag;
        GET_TAG = getTag;
    }

    private ItemNBT() {}

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

    /**
     * Sets the NBT tag of an item
     *
     * @param tag  The new tag
     * @param item The ItemStack
     *
     * @return The modified itemStack
     */
    public static ItemStack setTag(ItemStack item, NBTWrappers.NBTTagCompound tag) {
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
    public static NBTWrappers.NBTTagCompound getTag(ItemStack item) {
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

    public static ItemStack addSimpleTag(ItemStack item, String tag, String value) {
        NBTWrappers.NBTTagCompound compound = ItemNBT.getTag(item);
        compound.setString(tag, value);
        return ItemNBT.setTag(item, compound);
    }
}
