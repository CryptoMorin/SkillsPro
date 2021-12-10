package org.skills.utils.nbt;

import com.cryptomorin.xseries.ReflectionUtils;
import com.cryptomorin.xseries.XMaterial;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public final class NBTWrappers {
    private NBTWrappers() {}

    private static Class<?> getNBTClass(String clazz) {
        return ReflectionUtils.getNMSClass("nbt", clazz);
    }

    private static Field getDeclaredField(Class<?> clazz, String... names) {
        int i = 0;
        for (String name : names) {
            i++;
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                if (i == names.length) e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * A base class for the essential methods
     */
    public abstract static class NBTBase<T> {
        protected final T value;

        public NBTBase(T value) {
            this.value = value;
        }

        /**
         * @param nbtObject The NBT object
         *
         * @return The correct {@link NBTBase} or null if the tag is not supported
         */
        public static NBTBase<?> fromNBT(Object nbtObject) {
            switch (nbtObject.getClass().getSimpleName()) {
                case "NBTTagCompound":
                    return NBTTagCompound.fromNBT(nbtObject);
                case "NBTTagString":
                    return NBTTagString.fromNBT(nbtObject);
                case "NBTTagByte":
                    return NBTTagByte.fromNBT(nbtObject);
                case "NBTTagShort":
                    return NBTTagShort.fromNBT(nbtObject);
                case "NBTTagInt":
                    return NBTTagInt.fromNBT(nbtObject);
                case "NBTTagLong":
                    return NBTTagLong.fromNBT(nbtObject);
                case "NBTTagFloat":
                    return NBTTagFloat.fromNBT(nbtObject);
                case "NBTTagDouble":
                    return NBTTagDouble.fromNBT(nbtObject);
                case "NBTTagByteArray":
                    return NBTTagByteArray.fromNBT(nbtObject);
                case "NBTTagIntArray":
                    return NBTTagIntArray.fromNBT(nbtObject);
                case "NBTTagLongArray":
                    return NBTTagLongArray.fromNBT(nbtObject);
                case "NBTTagList":
                    return NBTTagList.fromNBT(nbtObject);
                case "NBTTagEnd":
                    return NBTTagEnd.fromNBT(nbtObject);
                default:
                    throw new UnsupportedOperationException("Unknown NBT type: " + nbtObject.getClass().getSimpleName());
            }
        }

        public final T getValue() {
            return value;
        }

        public abstract Object toNBT();
    }

    public static final class NBTTagEnd extends NBTBase<Void> {
        private static final MethodHandle NBT_CONSTRUCTOR;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> stringClass = getNBTClass("NBTTagEnd");
            MethodHandle handler = null;

            try {
                lookup.findConstructor(stringClass, MethodType.methodType(void.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            NBT_CONSTRUCTOR = handler;
        }

        public NBTTagEnd() {
            super(null);
        }

        @SuppressWarnings("unused")
        public static NBTBase<Void> fromNBT(Object nbtObject) {
            return null;
        }

        @Override
        public Object toNBT() {
            try {
                return NBT_CONSTRUCTOR.invoke();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return "NBTTagEnd";
        }
    }

    public static final class NBTTagString extends NBTBase<String> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> clazz = getNBTClass("NBTTagString");
            MethodHandle handler = null, data = null;

            try {
                if (XMaterial.supports(15)) handler = lookup.findStatic(clazz, "a", MethodType.methodType(clazz, String.class));
                else handler = lookup.findConstructor(clazz, MethodType.methodType(void.class, String.class));

                Field field = getDeclaredField(clazz, "A", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }

        public NBTTagString(String value) {
            super(value);
        }

        public static NBTTagString fromNBT(Object nbtObject) {
            try {
                return new NBTTagString((String) NBT_DATA.invoke(nbtObject));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        public NBTType<String> getNBTType() {
            return NBTType.STRING;
        }

        @Override
        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(value == null ? "" : value);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return "NBTTagString{" + value + '}';
        }
    }

    public static final class NBTTagCompound extends NBTBase<Map<String, NBTBase<?>>> {
        private static final MethodHandle NBT_TAG_COMPOUND_CONSTRUCTOR;
        private static final MethodHandle GET_COMPOUND_MAP, SET_COMPOUND_MAP;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> nbtCompound = getNBTClass("NBTTagCompound");
            MethodHandle handler = null,
                    getMap = null,
                    setMap = null;

            try {
                Field field = getDeclaredField(nbtCompound, "x", "map");
                field.setAccessible(true);
                getMap = lookup.unreflectGetter(field);

                if (XMaterial.supports(15)) {
                    Constructor<?> ctor = nbtCompound.getDeclaredConstructor(Map.class);
                    ctor.setAccessible(true);
                    handler = lookup.unreflectConstructor(ctor);
                } else {
                    handler = lookup.findConstructor(nbtCompound, MethodType.methodType(void.class));
                    setMap = lookup.unreflectSetter(field);
                }
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            NBT_TAG_COMPOUND_CONSTRUCTOR = handler;
            GET_COMPOUND_MAP = getMap;
            SET_COMPOUND_MAP = setMap;
        }

        public NBTTagCompound(Map<String, NBTBase<?>> value) {
            super(value);
        }

        public NBTTagCompound(int capacity) {
            this(new HashMap<>(capacity));
        }

        public NBTTagCompound() {
            this(new HashMap<>());
        }

        @SuppressWarnings("unchecked")
        public static Map<String, Object> getRawMap(Object nbtObject) {
            try {
                return (Map<String, Object>) GET_COMPOUND_MAP.invoke(nbtObject);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        public static NBTTagCompound fromNBT(Object nbtObject) {
            try {
                Map<String, Object> baseMap = getRawMap(nbtObject);
                NBTTagCompound compound = new NBTTagCompound(baseMap.size());
                for (Map.Entry<String, Object> base : baseMap.entrySet()) {
                    NBTBase<?> nbtBase = NBTBase.fromNBT(base.getValue());
                    if (nbtBase != null) compound.set(base.getKey(), nbtBase);
                }
                return compound;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        public <T> void set(String key, NBTType<T> type, T value) {
            NBTBase<?> base = null;

            if (type == NBTType.STRING) base = new NBTWrappers.NBTTagString((String) value);
            else if (type == NBTType.BYTE) base = new NBTWrappers.NBTTagByte((byte) value);
            else if (type == NBTType.BOOLEAN) base = new NBTWrappers.NBTTagByte((byte) ((boolean) value ? 1 : 0));
            else if (type == NBTType.SHORT) base = new NBTWrappers.NBTTagShort((short) value);
            else if (type == NBTType.INTEGER) base = new NBTWrappers.NBTTagInt((int) value);
            else if (type == NBTType.LONG) base = new NBTWrappers.NBTTagLong((long) value);
            else if (type == NBTType.FLOAT) base = new NBTWrappers.NBTTagFloat((float) value);
            else if (type == NBTType.DOUBLE) base = new NBTWrappers.NBTTagDouble((double) value);
            else if (type == NBTType.BYTE_ARRAY) base = new NBTWrappers.NBTTagByteArray((byte[]) value);
            else if (type == NBTType.INTEGER_ARRAY) base = new NBTWrappers.NBTTagIntArray((int[]) value);
            else if (type == NBTType.LONG_ARRAY) base = new NBTWrappers.NBTTagLong((long) value);

            this.value.put(key, base);
        }

        @SuppressWarnings("unchecked")
        public <T> T get(String key, NBTType<T> type) {
            NBTBase<T> base = (NBTBase<T>) value.get(key);
            return base == null ? null : base.value;
        }

        public <T> boolean has(String key, NBTType<T> type) {
            return has(key);
        }

        public boolean has(String key) {
            return this.value.containsKey(key);
        }

        public Object getContainer() {
            return this;
        }

        public void set(String key, NBTBase<?> nbt) {
            this.value.put(key, nbt);
        }

        @SuppressWarnings("unchecked")
        public <T extends NBTBase<?>> T remove(String key) {
            return (T) value.remove(key);
        }

        public NBTBase<?> removeUnchecked(String key) {
            return value.remove(key);
        }

        public void setByte(String key, byte value) {
            getValue().put(key, new NBTTagByte(value));
        }

        public void setShort(String key, short value) {
            getValue().put(key, new NBTTagShort(value));
        }

        public void setInt(String key, int value) {
            getValue().put(key, new NBTTagInt(value));
        }

        public void setLong(String key, long value) {
            getValue().put(key, new NBTTagLong(value));
        }

        public void setFloat(String key, float value) {
            getValue().put(key, new NBTTagFloat(value));
        }

        public void setDouble(String key, double value) {
            getValue().put(key, new NBTTagDouble(value));
        }

        public void setString(String key, String value) {
            getValue().put(key, new NBTTagString(value));
        }

        public void setCompound(String key, NBTTagCompound compound) {
            this.value.put(key, compound);
        }

        public void setByteArray(String key, byte[] value) {
            getValue().put(key, new NBTTagByteArray(value));
        }

        public void setIntArray(String key, int[] value) {
            getValue().put(key, new NBTTagIntArray(value));
        }

        public void setBoolean(String key, boolean value) {
            setByte(key, (byte) (value ? 1 : 0));
        }

        public NBTBase<?> get(String key) {
            return value.get(key);
        }

        public byte getByte(String key) {
            NBTBase<?> nbt = get(key);
            if (!(nbt instanceof NBTTagByte)) return 0;
            return ((NBTTagByte) nbt).getAsByte();
        }

        public short getShort(String key) {
            NBTBase<?> nbt = get(key);
            if (!(nbt instanceof NBTTagShort)) return 0;
            return ((NBTTagShort) nbt).getAsShort();
        }

        public int getInt(String key) {
            NBTBase<?> nbt = get(key);
            if (!(nbt instanceof NBTTagInt)) return 0;
            return ((NBTTagInt) nbt).getAsShort();
        }

        public long getLong(String key) {
            NBTBase<?> nbt = get(key);
            if (!(nbt instanceof NBTTagLong)) return 0;
            return ((NBTTagLong) nbt).getAsLong();
        }

        public NBTTagCompound getCompound(String key) {
            NBTBase<?> value = get(key);
            if (!(value instanceof NBTTagCompound)) return null;
            return (NBTTagCompound) value;
        }

        public float getFloat(String key) {
            NBTBase<?> nbt = get(key);
            if (!(nbt instanceof NBTTagFloat)) return 0;
            return ((NBTTagFloat) nbt).getAsFloat();
        }

        public double getDouble(String key) {
            NBTBase<?> nbt = get(key);
            if (!(nbt instanceof NBTTagDouble)) return 0;
            return ((NBTTagDouble) nbt).getAsDouble();
        }

        public String getString(String key) {
            NBTBase<?> nbt = get(key);
            if (!(nbt instanceof NBTTagString)) return null;
            return ((NBTTagString) nbt).getValue();
        }

        public byte[] getByteArray(String key) {
            NBTBase<?> nbt = get(key);
            if (!(nbt instanceof NBTTagByteArray)) return null;
            return ((NBTTagByteArray) nbt).getValue();
        }

        public int[] getIntArray(String key) {
            NBTBase<?> nbt = get(key);
            if (!(nbt instanceof NBTTagIntArray)) return null;
            return ((NBTTagIntArray) nbt).getValue();
        }

        public boolean getBoolean(String key) {
            return getByte(key) != 0;
        }

        @Override
        public Object toNBT() {
            try {
                Map<String, Object> map = new HashMap<>(value.size());
                for (Map.Entry<String, NBTBase<?>> entry : value.entrySet()) {
                    map.put(entry.getKey(), entry.getValue().toNBT());
                }

                Object compound;
                if (XMaterial.supports(15)) compound = NBT_TAG_COMPOUND_CONSTRUCTOR.invoke(map);
                else {
                    compound = NBT_TAG_COMPOUND_CONSTRUCTOR.invoke();
                    SET_COMPOUND_MAP.invoke(compound, map);
                }
                return compound;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(10 + (value.size() * 50));
            builder.append("NBTTagCompound{");

            for (Map.Entry<String, NBTBase<?>> entry : value.entrySet()) {
                builder.append('\n').append("  ").append(entry.getKey()).append(": ").append(entry.getValue());
            }

            return builder.append('\n').append('}').toString();
        }
    }

    public static final class NBTTagLongArray extends NBTArray<long[]> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        static {
            Class<?> clazz = getNBTClass("NBTTagLongArray");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null, data = null;

            try {
                handler = lookup.findConstructor(clazz, MethodType.methodType(void.class, long[].class));

                Field field = getDeclaredField(clazz, "c", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }

        public NBTTagLongArray(long[] value) {
            super(value);
        }

        public static NBTTagLongArray fromNBT(Object nbtObject) {
            try {
                return new NBTTagLongArray((long[]) NBT_DATA.invoke(nbtObject));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(value);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return "NBTTagLongArray{" + Arrays.toString(value) + '}';
        }
    }


    public static final class NBTTagIntArray extends NBTArray<int[]> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        static {
            Class<?> clazz = getNBTClass("NBTTagIntArray");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null, data = null;

            try {
                handler = lookup.findConstructor(clazz, MethodType.methodType(void.class, int[].class));

                Field field = getDeclaredField(clazz, "c", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }

        public NBTTagIntArray(int[] value) {
            super(value);
        }

        public static NBTTagIntArray fromNBT(Object nbtObject) {
            try {
                return new NBTTagIntArray((int[]) NBT_DATA.invoke(nbtObject));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(value);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return "NBTTagIntArray{" + Arrays.toString(value) + '}';
        }
    }

    public static final class NBTTagList<T> extends NBTBase<List<NBTBase<T>>> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle GET_DATA, SET_DATA;
        private static final MethodHandle GET_TYPE_ID;

        static {
            Class<?> clazz = getNBTClass("NBTTagList");
            Class<?> nbtBase = getNBTClass("NBTBase");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null, getData = null, setData = null, getTypeId = null;

            try {
                Field field = getDeclaredField(clazz, "c", "list");
                field.setAccessible(true);
                getData = lookup.unreflectGetter(field);

                if (XMaterial.supports(15)) {
                    Constructor<?> ctor = clazz.getDeclaredConstructor(List.class, byte.class);
                    ctor.setAccessible(true);
                    handler = lookup.unreflectConstructor(ctor);
                } else {
                    handler = lookup.findConstructor(clazz, MethodType.methodType(void.class));
                    setData = lookup.unreflectSetter(field);
                }

                getTypeId = lookup.findVirtual(nbtBase,
                        ReflectionUtils.supports(18) ? "a" : "getTypeId",
                        MethodType.methodType(byte.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            CONSTRUCTOR = handler;
            GET_DATA = getData;
            SET_DATA = setData;
            GET_TYPE_ID = getTypeId;
        }

        public NBTTagList(List<NBTBase<T>> value) {
            super(value);
        }

        public NBTTagList() {
            super(new ArrayList<>());
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public static NBTTagList<?> fromNBT(Object nbtObject) {
            List<?> nbtList;
            try {
                nbtList = (List<?>) GET_DATA.invoke(nbtObject);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return new NBTTagList<>();
            }

            List<NBTBase<?>> list = new ArrayList<>(nbtList.size());
            for (Object entry : nbtList) list.add(NBTBase.fromNBT(entry));
            return new NBTTagList(list);
        }

        /**
         * Adds the {@link NBTBase}, if the type of the list is correct or the list is empty
         *
         * @param base The {@link NBTBase} to add
         *
         * @return True if it was added.
         */
        public boolean add(NBTBase<T> base) {
            value.add(base);
            return true;
        }

        /**
         * @param type The type to check for
         *
         * @return True if the list is empty or this type
         */
        public boolean isType(NBTBase<?> type) {
            return value.isEmpty() || value.get(0).getClass().isInstance(type);
        }

        @Override
        public Object toNBT() {
            try {
                List<Object> array = new ArrayList<>(value.size());
                for (NBTBase<T> base : value) array.add(base.toNBT());

                if (XMaterial.supports(15)) {
                    byte typeId = array.isEmpty() ? 0 : (byte) GET_TYPE_ID.invoke(array.get(0));
                    return CONSTRUCTOR.invoke(array, typeId);
                } else {
                    Object nbtList = CONSTRUCTOR.invoke();
                    SET_DATA.invoke(nbtList, array);
                    return nbtList;
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return "NBTTagList{" + Arrays.toString(value.toArray()) + '}';
        }
    }

    public abstract static class NBTArray<T> extends NBTBase<T> {
        public NBTArray(T value) {
            super(value);
        }
    }

    public abstract static class NBTNumber<T extends Number> extends NBTBase<T> {
        public NBTNumber(T value) {
            super(value);
        }

        public int getAsInt() {
            return (int) Math.floor(getAsDouble());
        }

        public long getAsLong() {
            return (long) Math.floor(getAsDouble());
        }

        public abstract double getAsDouble();

        public float getAsFloat() {
            return (float) getAsDouble();
        }

        public byte getAsByte() {
            return (byte) (getAsInt() & 255);
        }

        public short getAsShort() {
            return (short) (getAsInt() & '\uffff');
        }
    }

    public static final class NBTTagDouble extends NBTNumber<Double> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        static {
            Class<?> clazz = getNBTClass("NBTTagDouble");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null, data = null;

            try {
                if (XMaterial.supports(15)) handler = lookup.findStatic(clazz, "a", MethodType.methodType(clazz, double.class));
                else handler = lookup.findConstructor(clazz, MethodType.methodType(void.class, double.class));

                Field field = getDeclaredField(clazz, "w", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }

        public NBTTagDouble(double value) {
            super(value);
        }

        public static NBTTagDouble fromNBT(Object nbtObject) {
            try {
                return new NBTTagDouble((double) NBT_DATA.invoke(nbtObject));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(getAsDouble());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return "NBTTagDouble{" + value + '}';
        }

        @Override
        public double getAsDouble() {
            return value;
        }
    }

    public static final class NBTTagInt extends NBTNumber<Integer> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        static {
            Class<?> clazz = getNBTClass("NBTTagInt");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null, data = null;

            try {
                if (XMaterial.supports(15)) {
                    handler = lookup.findStatic(clazz, "a", MethodType.methodType(clazz, int.class));
                } else {
                    handler = lookup.findConstructor(clazz, MethodType.methodType(void.class, int.class));
                }

                Field field = getDeclaredField(clazz, "c", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }

        public NBTTagInt(int value) {
            super(value);
        }

        public static NBTTagInt fromNBT(Object nbtObject) {
            try {
                return new NBTTagInt((int) NBT_DATA.invoke(nbtObject));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public double getAsDouble() {
            return value;
        }

        @Override
        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(value);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return "NBTTagInt{" + value + '}';
        }
    }

    public static final class NBTTagByte extends NBTNumber<Byte> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        static {
            Class<?> clazz = getNBTClass("NBTTagByte");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null, data = null;

            try {
                if (XMaterial.supports(15)) {
                    handler = lookup.findStatic(clazz, "a", MethodType.methodType(clazz, byte.class));
                } else {
                    handler = lookup.findConstructor(clazz, MethodType.methodType(void.class, byte.class));
                }

                Field field = getDeclaredField(clazz, "x", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }

        public NBTTagByte(byte value) {
            super(value);
        }

        public static NBTBase<Byte> fromNBT(Object nbtObject) {
            try {
                return new NBTTagByte((byte) NBT_DATA.invoke(nbtObject));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public double getAsDouble() {
            return value;
        }

        @Override
        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(getAsByte());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return "NBTTagByte{" + value + '}';
        }
    }

    public static final class NBTTagByteArray extends NBTArray<byte[]> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        static {
            Class<?> clazz = getNBTClass("NBTTagByteArray");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null, data = null;

            try {
                handler = lookup.findConstructor(clazz, MethodType.methodType(void.class, byte[].class));

                Field field = getDeclaredField(clazz, "c", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }

        public NBTTagByteArray(byte[] value) {
            super(value);
        }

        public static NBTTagByteArray fromNBT(Object nbtObject) {
            try {
                return nbtObject == null ?
                        new NBTTagByteArray(new byte[0]) :
                        new NBTTagByteArray((byte[]) NBT_DATA.invoke(nbtObject));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(value);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return "NBTTagByteArray{" + Arrays.toString(value) + '}';
        }
    }

    public static final class NBTTagShort extends NBTNumber<Short> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> clazz = getNBTClass("NBTTagShort");
            MethodHandle handler = null, data = null;

            try {
                if (XMaterial.supports(15)) {
                    handler = lookup.findStatic(clazz, "a", MethodType.methodType(clazz, short.class));
                } else {
                    handler = lookup.findConstructor(clazz, MethodType.methodType(void.class, short.class));
                }

                Field field = getDeclaredField(clazz, "c", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }

        public NBTTagShort(short value) {
            super(value);
        }

        public static NBTTagShort fromNBT(Object nbtObject) {
            try {
                return new NBTTagShort((short) NBT_DATA.invoke(nbtObject));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public short getAsShort() {
            return value;
        }

        @Override
        public double getAsDouble() {
            return value;
        }

        @Override
        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(getAsShort());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return "NBTTagShort{" + value + '}';
        }
    }

    public static final class NBTTagLong extends NBTNumber<Long> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> clazz = getNBTClass("NBTTagLong");
            MethodHandle handler = null, data = null;

            try {
                if (XMaterial.supports(15)) handler = lookup.findStatic(clazz, "a", MethodType.methodType(clazz, long.class));
                else handler = lookup.findConstructor(clazz, MethodType.methodType(void.class, long.class));

                Field field = getDeclaredField(clazz, "c", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }

        public NBTTagLong(long value) {
            super(value);
        }

        public static NBTTagLong fromNBT(Object nbtObject) {
            try {
                return new NBTTagLong((long) NBT_DATA.invoke(nbtObject));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public double getAsDouble() {
            return value;
        }

        @Override
        public long getAsLong() {
            return value;
        }

        @Override
        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(getAsLong());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return "NBTTagLong{" + value + '}';
        }
    }

    public static final class NBTTagFloat extends NBTNumber<Float> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        static {
            Class<?> clazz = getNBTClass("NBTTagFloat");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null, data = null;

            try {
                if (XMaterial.supports(15)) handler = lookup.findStatic(clazz, "a", MethodType.methodType(clazz, float.class));
                else handler = lookup.findConstructor(clazz, MethodType.methodType(void.class, float.class));

                Field field = getDeclaredField(clazz, "w", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }

        public NBTTagFloat(float value) {
            super(value);
        }

        public static NBTTagFloat fromNBT(Object nbtObject) {
            try {
                return new NBTTagFloat((float) NBT_DATA.invoke(nbtObject));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public double getAsDouble() {
            return value;
        }

        @Override
        public float getAsFloat() {
            return value;
        }

        @Override
        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(getAsFloat());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        @Override
        public String toString() {
            return "NBTTagFloat{" + value + '}';
        }
    }
}
