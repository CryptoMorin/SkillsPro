package org.skills.utils.nbt;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static com.cryptomorin.xseries.reflection.XReflection.ofMinecraft;
import static com.cryptomorin.xseries.reflection.XReflection.supports;

public final class ItemNBT {
    public static final boolean CAN_ACCESS_UNBREAKABLE = supports(11), SUPPORTS_COMPONENTS;
    private static final MethodHandle AS_NMS_COPY;
    private static final MethodHandle AS_BUKKIT_COPY;
    private static final MethodHandle SET_TAG, CUSTOM_DATA_CTOR;
    private static final MethodHandle GET_TAG, COPY_TAG;
    private static final Object CUSTOM_DATA_TYPE;

    static {
        MethodHandle asNmsCopy = null;
        MethodHandle asBukkitCopy = null;
        MethodHandle setTag, customDataCtor = null;
        MethodHandle getTag, copyTag = null;
        Object customDataType = null;
        boolean supportsComponents = false;

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> crafItemStack = XReflection.getCraftClass("inventory.CraftItemStack");
        Class<?> nmsItemStack = XReflection.getNMSClass("world.item", "ItemStack");
        Class<?> nbtTagCompound = XReflection.getNMSClass("nbt", "NBTTagCompound");
        // Why does this show up as "CompoundTag" in stacktraces???!?!?? Oh because of paper's remapping...

        try {
            asNmsCopy = lookup.findStatic(crafItemStack, "asNMSCopy", MethodType.methodType(nmsItemStack, ItemStack.class));
            asBukkitCopy = lookup.findStatic(crafItemStack, "asBukkitCopy", MethodType.methodType(ItemStack.class, nmsItemStack));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            // 1.20.5 "components"
            Class<?> DataComponentsClass = ofMinecraft()
                    .inPackage(MinecraftPackage.NMS, "core.component")
                    .map(MinecraftMapping.MOJANG, "DataComponents")
                    .reflect();
            Class<?> DataComponentHolderClass = ofMinecraft()
                    .inPackage(MinecraftPackage.NMS, "core.component")
                    .map(MinecraftMapping.MOJANG, "DataComponents")
                    .reflect();
            XReflection.getNMSClass("core.component", "DataComponentHolder");
            Class<?> DataComponentTypeClass = ofMinecraft()
                    .inPackage(MinecraftPackage.NMS, "core.component")
                    .map(MinecraftMapping.MOJANG, "DataComponentType")
                    .reflect();
            Class<?> CustomDataClass = ofMinecraft()
                    .inPackage(MinecraftPackage.NMS, "world.item.component")
                    .map(MinecraftMapping.MOJANG, "CustomData")
                    .reflect();

            /*
             * @Nullable
             * public <T> T b(DataComponentType<? super T> datacomponenttype, @Nullable T t0) {
             *      return this.r.b(datacomponenttype, t0);
             * }
             */
            setTag = lookup.findVirtual(nmsItemStack, XReflection.v(20, 5, "b").orElse("set"),
                    MethodType.methodType(Object.class, DataComponentTypeClass, Object.class));

            /*
             * @Nullable
             * default <T> T a(DataComponentType<? extends T> var0) {
             *      return this.a().a(var0);
             * }
             */
            getTag = lookup.findVirtual(nmsItemStack, XReflection.v(20, 5, "a").orElse("get"),
                    MethodType.methodType(Object.class, DataComponentTypeClass));

            /*
             * public NBTTagCompound c() {
             *      return this.e.i();
             * }
             */
            copyTag = lookup.findVirtual(CustomDataClass, XReflection.v(20, 5, "c").orElse("copyTag"),
                    MethodType.methodType(nbtTagCompound));

            /*
             * private CustomData(NBTTagCompound var0) {
             *     this.e = var0;
             * }
             */
            Constructor<?> customDataCtorJvm = CustomDataClass.getDeclaredConstructor(nbtTagCompound);
            customDataCtorJvm.setAccessible(true);
            customDataCtor = lookup.unreflectConstructor(customDataCtorJvm);

            // net.minecraft.core.component.DataComponents#CUSTOM_DATA
            Field typeField = DataComponentsClass.getDeclaredField(XReflection.v(20, 5, "b").orElse("CUSTOM_DATA"));
            customDataType = typeField.get(null);

            supportsComponents = true;
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException | ClassNotFoundException ex) {
            try {
                setTag = lookup.findVirtual(nmsItemStack,
                        XReflection.v(18, "c").orElse("setTag"), MethodType.methodType(void.class, nbtTagCompound));

                getTag = lookup.findVirtual(nmsItemStack,
                        XReflection.v(19, "v").v(18, "t").orElse("getTag"), MethodType.methodType(nbtTagCompound));
            } catch (NoSuchMethodException | IllegalAccessException ex2) {
                RuntimeException newEx = new RuntimeException(ex2);
                newEx.addSuppressed(ex);
                throw newEx;
            }
        }

        AS_NMS_COPY = asNmsCopy;
        AS_BUKKIT_COPY = asBukkitCopy;
        SET_TAG = setTag;
        GET_TAG = getTag;
        COPY_TAG = copyTag;
        CUSTOM_DATA_TYPE = customDataType;
        CUSTOM_DATA_CTOR = customDataCtor;
        SUPPORTS_COMPONENTS = supportsComponents;
    }

    private ItemNBT() {
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
     * @return The modified itemStack
     */
    public static ItemStack setTag(ItemStack item, NBTWrappers.NBTTagCompound tag) {
        Object nbtTag = tag.toNBT();
        Object nmsItem = asNMSCopy(item);

        try {
            if (SUPPORTS_COMPONENTS) {
                Object customData = CUSTOM_DATA_CTOR.invoke(nbtTag);
                SET_TAG.invoke(nmsItem, CUSTOM_DATA_TYPE, customData);
            } else {
                SET_TAG.invoke(nmsItem, nbtTag);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return asBukkitCopy(nmsItem);
    }

    /**
     * Gets the NBTTag of an item. In case of any error it returns a blank one.
     *
     * @param item The ItemStack to get the tag for
     * @return The NBTTagCompound of the ItemStack or a new one if it had none or an error occurred
     */
    public static NBTWrappers.NBTTagCompound getTag(ItemStack item) {
        Object nmsItem = asNMSCopy(item);
        Object tag;
        try {
            if (SUPPORTS_COMPONENTS) {
                tag = GET_TAG.invoke(nmsItem, CUSTOM_DATA_TYPE);
                if (tag != null) tag = COPY_TAG.invoke(tag);
            } else {
                tag = GET_TAG.invoke(nmsItem);
            }
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
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
