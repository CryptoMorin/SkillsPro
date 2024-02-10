package org.skills.utils.nbt;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("deprecation")
public class NBTType<T> {
    public static final NBTType<Byte> BYTE;
    public static final NBTType<Boolean> BOOLEAN;
    public static final NBTType<Short> SHORT;
    public static final NBTType<Integer> INTEGER;
    public static final NBTType<Long> LONG;
    public static final NBTType<Float> FLOAT;
    public static final NBTType<Double> DOUBLE;
    public static final NBTType<String> STRING;
    public static final NBTType<byte[]> BYTE_ARRAY;
    public static final NBTType<int[]> INTEGER_ARRAY;
    public static final NBTType<long[]> LONG_ARRAY;
    public static final NBTType<NBTTagCompound> TAG_COMPOUND;

    static {
        if (XMaterial.supports(13)) {
            if (XMaterial.supports(14)) {
                BYTE = new NBTType<>(ItemTagType.BYTE, PersistentDataType.BYTE);
                BOOLEAN = new NBTType<>(ItemTagType.BYTE, PersistentDataType.BYTE);
                SHORT = new NBTType<>(ItemTagType.SHORT, PersistentDataType.SHORT);
                INTEGER = new NBTType<>(ItemTagType.INTEGER, PersistentDataType.INTEGER);
                LONG = new NBTType<>(ItemTagType.LONG, PersistentDataType.LONG);
                FLOAT = new NBTType<>(ItemTagType.FLOAT, PersistentDataType.FLOAT);
                DOUBLE = new NBTType<>(ItemTagType.DOUBLE, PersistentDataType.DOUBLE);
                STRING = new NBTType<>(ItemTagType.STRING, PersistentDataType.STRING);
                BYTE_ARRAY = new NBTType<>(ItemTagType.BYTE_ARRAY, PersistentDataType.BYTE_ARRAY);
                INTEGER_ARRAY = new NBTType<>(ItemTagType.INTEGER_ARRAY, PersistentDataType.INTEGER_ARRAY);
                LONG_ARRAY = new NBTType<>(ItemTagType.LONG_ARRAY, PersistentDataType.LONG_ARRAY);
                TAG_COMPOUND = new NBTType<>(ItemTagType.TAG_CONTAINER, PersistentDataType.TAG_CONTAINER);
            } else {
                BYTE = new NBTType<>(ItemTagType.BYTE);
                BOOLEAN = new NBTType<>(ItemTagType.BYTE);
                SHORT = new NBTType<>(ItemTagType.SHORT);
                INTEGER = new NBTType<>(ItemTagType.INTEGER);
                LONG = new NBTType<>(ItemTagType.LONG);
                FLOAT = new NBTType<>(ItemTagType.FLOAT);
                DOUBLE = new NBTType<>(ItemTagType.DOUBLE);
                STRING = new NBTType<>(ItemTagType.STRING);
                BYTE_ARRAY = new NBTType<>(ItemTagType.BYTE_ARRAY);
                INTEGER_ARRAY = new NBTType<>(ItemTagType.INTEGER_ARRAY);
                LONG_ARRAY = new NBTType<>(ItemTagType.LONG_ARRAY);
                TAG_COMPOUND = new NBTType<>(ItemTagType.TAG_CONTAINER);
            }
        } else {
            BYTE = new NBTType<>();
            BOOLEAN = new NBTType<>();
            SHORT = new NBTType<>();
            INTEGER = new NBTType<>();
            LONG = new NBTType<>();
            FLOAT = new NBTType<>();
            DOUBLE = new NBTType<>();
            STRING = new NBTType<>();
            BYTE_ARRAY = new NBTType<>();
            INTEGER_ARRAY = new NBTType<>();
            LONG_ARRAY = new NBTType<>();
            TAG_COMPOUND = new NBTType<>();
        }
    }

    private final PersistentDataType<T, T> persistentDataType;
    private final ItemTagType<T, T> itemTagType;

    @SuppressWarnings("unchecked")
    private NBTType(ItemTagType<?, ?> itemTagType, PersistentDataType<?, ?> persistentDataType) {
        this.itemTagType = (ItemTagType<T, T>) itemTagType;
        this.persistentDataType = (PersistentDataType<T, T>) persistentDataType;
    }

    private NBTType(ItemTagType<?, ?> itemTagType) {
        this(itemTagType, null);
    }

    private NBTType() {
        this(null);
    }

    public ItemTagType<T, T> getItemTagType() {
        return itemTagType;
    }

    public PersistentDataType<T, T> getPersistentDataType() {
        return persistentDataType;
    }
}