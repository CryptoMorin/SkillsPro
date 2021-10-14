package org.skills.utils.nbt;

public interface NBTTagCompound {
    <T> void set(String key, NBTType<T> type, T value);

    <T> T get(String key, NBTType<T> type);

    <T> boolean has(String key, NBTType<T> type);

    Object getContainer();
}