package org.skills.utils.nbt;

import com.cryptomorin.xseries.ReflectionUtils;
import com.cryptomorin.xseries.XMaterial;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Provides wrapper objects to abstract the NBT versions. Probably way too complicated...
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class NBTWrappers {
    private static Object findNBTData(Object nbt) {
        try {
            Field field = nbt.getClass().getDeclaredField("data");
            field.setAccessible(true);
            return field.get(nbt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
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
                case "NBTTagList":
                    return NBTTagList.fromNBT(nbtObject);
                default:
                    return null;
            }
        }

        public T getValue() {
            return value;
        }

        abstract Object toNBT();
    }

    public static class NBTTagString extends NBTBase<String> {
        private static final MethodHandle NBT_TAG_STRING_CONSTRUCTOR;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> stringClass = ReflectionUtils.getNMSClass("NBTTagString");
            MethodHandle handler = null;

            try {
                if (XMaterial.supports(15)) handler = lookup.findStatic(stringClass, "a", MethodType.methodType(stringClass, String.class));
                else handler = lookup.findConstructor(stringClass, MethodType.methodType(void.class, String.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            NBT_TAG_STRING_CONSTRUCTOR = handler;
        }

        public NBTTagString(String value) {
            super(value);
        }

        public static NBTBase<String> fromNBT(Object nbtObject) {
            return new NBTTagString((String) findNBTData(nbtObject));
        }

        @Override
        public Object toNBT() {
            try {
                return NBT_TAG_STRING_CONSTRUCTOR.invoke(getValue());
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

    public static class NBTTagCompound extends NBTBase<Map<String, NBTBase<?>>> {
        private static final MethodHandle NBT_TAG_COMPOUND_CONSTRUCTOR;
        private static final MethodHandle SET_TAG_METHOD;
        private static final MethodHandle GET_COMPOUND_MAP;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> nbtCompound = ReflectionUtils.getNMSClass("NBTTagCompound");
            Class<?> nbtBase = ReflectionUtils.getNMSClass("NBTBase");
            MethodHandle handler = null;
            MethodHandle handlerSet = null;
            MethodHandle compoundMap = null;

            try {
                handler = lookup.findConstructor(nbtCompound, MethodType.methodType(void.class));
                if (XMaterial.supports(14)) handlerSet = lookup.findVirtual(nbtCompound, "set", MethodType.methodType(nbtBase, String.class, nbtBase));
                else handlerSet = lookup.findVirtual(nbtCompound, "set", MethodType.methodType(void.class, String.class, nbtBase));

                Field field = nbtCompound.getDeclaredField("map");
                field.setAccessible(true);
                compoundMap = lookup.unreflectGetter(field);
            } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }

            NBT_TAG_COMPOUND_CONSTRUCTOR = handler;
            SET_TAG_METHOD = handlerSet;
            GET_COMPOUND_MAP = compoundMap;
        }

        public NBTTagCompound(Map<String, NBTBase<?>> value) {
            super(value);
        }

        public NBTTagCompound() {
            this(new HashMap<>());
        }

        @SuppressWarnings("unchecked")
        public static NBTTagCompound fromNBT(Object nbtObject) {
            Map<String, Object> baseMap = null;
            try {
                baseMap = (Map<String, Object>) GET_COMPOUND_MAP.invoke(nbtObject);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            NBTTagCompound compound = new NBTTagCompound();
            for (Map.Entry<String, Object> base : baseMap.entrySet()) {
                NBTBase<?> nbtBase = NBTBase.fromNBT(base.getValue());
                if (nbtBase != null) compound.set(base.getKey(), nbtBase);
            }

            return compound;
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
            else if (type == NBTType.TAG_CONTAINER) base = (NBTTagCompound) value;

            getValue().put(key, base);
        }

        @SuppressWarnings("unchecked")
        public <T> T get(String key, NBTType<T> type) {
            NBTBase<T> base = (NBTBase<T>) getValue().get(key);
            if (base == null) return null;
            return base.value;
        }

        public void set(String key, NBTBase<?> value) {
            getValue().put(key, value);
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

        public void setCompound(String key, NBTTagCompound value) {
            getValue().put(key, value);
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
            Object compound = null;
            try {
                compound = NBT_TAG_COMPOUND_CONSTRUCTOR.invoke();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            for (Map.Entry<String, NBTBase<?>> entry : getValue().entrySet()) {
                try {
                    SET_TAG_METHOD.invoke(compound, entry.getKey(), entry.getValue().toNBT());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            return compound;
        }

        @Override
        public String toString() {
            return "NBTTagCompound{" + value + '}';
        }
    }

    public static class NBTTagList<T> extends NBTBase<List<NBTBase<T>>> {
        private static final MethodHandle NBT_TAG_LIST_CONSTRUCTOR;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null;

            try {
                handler = lookup.findConstructor(ReflectionUtils.getNMSClass("NBTTagList"),
                        MethodType.methodType(void.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            NBT_TAG_LIST_CONSTRUCTOR = handler;
        }

        public NBTTagList(List<NBTBase<T>> value) {
            super(value);
        }

        public NBTTagList() {
            super(new ArrayList<>());
        }


        public static NBTTagList<?> fromNBT(Object nbtObject) {
            NBTTagList<?> list = new NBTTagList<>();
            List<?> nbtList = null;

            try {
                Field field = nbtObject.getClass().getDeclaredField("list");
                field.setAccessible(true);
                nbtList = (List<?>) field.get(nbtObject);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            if (nbtList == null) return list;
            for (Object entry : nbtList) list.add(NBTBase.fromNBT(entry));
            return list;
        }

        /**
         * Adds the {@link NBTBase}, if the type of the list is correct or the list is empty
         * @param base The {@link NBTBase} to add
         * @return True if it was added.
         */
        @SuppressWarnings({"rawtypes", "unchecked"})
        public boolean add(NBTBase base) {
            return isType(base.getClass()) && getValue().add(base);
        }

        /**
         * @param type The type to check for
         * @return True if the list is empty or this type
         */
        @SuppressWarnings("rawtypes")
        public boolean isType(Class<? extends NBTBase> type) {
            return getValue().isEmpty() || getValue().get(0).getClass() == type;
        }

        @Override
        public Object toNBT() {
            Object nbtList = null;
            try {
                nbtList = NBT_TAG_LIST_CONSTRUCTOR.invoke();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            Method add = null;
            try {
                if (XMaterial.supports(14)) add = nbtList.getClass().getMethod("add", int.class, ReflectionUtils.getNMSClass("NBTBase"));
                else add = nbtList.getClass().getMethod("add", ReflectionUtils.getNMSClass("NBTBase"));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            int i = 0;
            for (NBTBase<T> NBTBase : getValue()) {
                try {
                    if (XMaterial.supports(14)) add.invoke(nbtList, i, NBTBase.toNBT());
                    else add.invoke(nbtList, NBTBase.toNBT());
                    i++;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            return nbtList;
        }

        @Override
        public String toString() {
            return "NBTTagList{" + Arrays.toString(value.toArray()) + '}';
        }
    }

    public abstract static class INBTNumber<T extends Number> extends NBTBase<T> {
        public INBTNumber(T value) {
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

    /**
     * A NBTTagDouble
     */
    public static class NBTTagDouble extends INBTNumber<Double> {
        private static final MethodHandle NBT_TAG_DOUBLE_CONSTRUCTOR;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> doubleNBT = ReflectionUtils.getNMSClass("NBTTagDouble");
            MethodHandle handler = null;

            try {
                if (XMaterial.supports(15)) handler = lookup.findStatic(doubleNBT, "a", MethodType.methodType(doubleNBT, double.class));
                else handler = lookup.findConstructor(doubleNBT, MethodType.methodType(void.class, double.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            NBT_TAG_DOUBLE_CONSTRUCTOR = handler;
        }

        public NBTTagDouble(double value) {
            super(value);
        }

        public static NBTTagDouble fromNBT(Object nbtObject) {
            Double value;
            value = (Double) findNBTData(nbtObject);
            //value = (Double) findNBTNumberGetMethod(ReflectionUtils.getNMSClass("NBTTagDouble"), double.class).invoke(nbtObject);
            return value == null ? new NBTTagDouble(-1) : new NBTTagDouble(value);
        }

        @Override
        public Object toNBT() {
            try {
                return NBT_TAG_DOUBLE_CONSTRUCTOR.invoke(getAsDouble());
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
            return getValue();
        }
    }

    public static class NBTTagInt extends INBTNumber<Integer> {
        private static final MethodHandle NBT_TAG_INT_CONSTRUCTOR;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> nbtInt = ReflectionUtils.getNMSClass("NBTTagInt");
            MethodHandle handler = null;

            try {
                if (XMaterial.supports(15)) {
                    handler = lookup.findStatic(nbtInt, "a", MethodType.methodType(nbtInt, int.class));
                } else {
                    handler = lookup.findConstructor(nbtInt, MethodType.methodType(void.class, int.class));
                }
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            NBT_TAG_INT_CONSTRUCTOR = handler;
        }

        public NBTTagInt(int value) {
            super(value);
        }

        public static NBTTagInt fromNBT(Object nbtObject) {
            Integer value = (Integer) findNBTData(nbtObject);
            return new NBTTagInt(value == null ? 0 : value);
        }

        @Override
        public double getAsDouble() {
            return getValue();
        }

        @Override
        public Object toNBT() {
            try {
                return NBT_TAG_INT_CONSTRUCTOR.invoke(getAsInt());
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

    public static class NBTTagIntArray extends NBTBase<int[]> {
        private static final MethodHandle NBT_TAG_INT_ARRAY_CONSTRUCTOR;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null;

            try {
                handler = lookup.findConstructor(ReflectionUtils.getNMSClass("NBTTagIntArray"),
                        MethodType.methodType(void.class, int[].class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            NBT_TAG_INT_ARRAY_CONSTRUCTOR = handler;
        }

        public NBTTagIntArray(int[] value) {
            super(value);
        }

        public static NBTTagIntArray fromNBT(Object nbtObject) {
            int[] data = null;
            for (Method method : nbtObject.getClass().getMethods()) {
                if (method.getReturnType() == int[].class) {
                    try {
                        data = (int[]) method.invoke(nbtObject);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            return new NBTTagIntArray(data);
        }

        @Override
        public Object toNBT() {
            try {
                return NBT_TAG_INT_ARRAY_CONSTRUCTOR.invoke(getValue());
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

    public static class NBTTagByte extends INBTNumber<Byte> {
        private static final MethodHandle NBT_TAG_BYTE_CONSTRUCTOR;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null;
            Class<?> nbtTagByte = ReflectionUtils.getNMSClass("NBTTagByte");

            try {
                if (XMaterial.supports(15)) {
                    handler = lookup.findStatic(nbtTagByte, "a", MethodType.methodType(nbtTagByte, byte.class));
                } else {
                    handler = lookup.findConstructor(nbtTagByte, MethodType.methodType(void.class, byte.class));
                }
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            NBT_TAG_BYTE_CONSTRUCTOR = handler;
        }

        public NBTTagByte(byte value) {
            super(value);
        }

        public static NBTBase<Byte> fromNBT(Object nbtObject) {
            Byte value = (Byte) findNBTData(nbtObject);
            return new NBTTagByte(value == null ? 0 : value);
        }

        @Override
        public double getAsDouble() {
            return getValue();
        }

        @Override
        public Object toNBT() {
            try {
                return NBT_TAG_BYTE_CONSTRUCTOR.invoke(getAsByte());
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

    public static class NBTTagByteArray extends NBTBase<byte[]> {
        private static final MethodHandle NBT_TAG_BYTE_ARRAY_CONSTRUCTOR;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null;

            try {
                handler = lookup.findConstructor(ReflectionUtils.getNMSClass("NBTTagByteArray"),
                        MethodType.methodType(void.class, byte[].class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }
            NBT_TAG_BYTE_ARRAY_CONSTRUCTOR = handler;
        }

        public NBTTagByteArray(byte[] value) {
            super(value);
        }

        public static NBTTagByteArray fromNBT(Object nbtObject) {
            byte[] data = null;
            for (Method method : nbtObject.getClass().getMethods()) {
                if (method.getReturnType() == byte[].class) {
                    try {
                        data = (byte[]) method.invoke(nbtObject);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            return new NBTTagByteArray(data);
        }

        @Override
        public Object toNBT() {
            try {
                return NBT_TAG_BYTE_ARRAY_CONSTRUCTOR.invoke(getValue());
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

    public static class NBTTagShort extends INBTNumber<Short> {
        private static final MethodHandle NBT_TAG_SHORT_CONSTRUCTOR;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> shortNbt = ReflectionUtils.getNMSClass("NBTTagShort");
            MethodHandle handler = null;

            try {
                if (XMaterial.supports(15)) {
                    handler = lookup.findStatic(shortNbt, "a", MethodType.methodType(shortNbt, short.class));
                } else {
                    handler = lookup.findConstructor(shortNbt, MethodType.methodType(void.class, short.class));
                }
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            NBT_TAG_SHORT_CONSTRUCTOR = handler;
        }

        public NBTTagShort(short value) {
            super(value);
        }

        public static NBTTagShort fromNBT(Object nbtObject) {
            Short value = (Short) findNBTData(nbtObject);
            return new NBTTagShort(value == null ? 0 : value);
        }

        @Override
        public double getAsDouble() {
            return getValue();
        }

        @Override
        public Object toNBT() {
            try {
                return NBT_TAG_SHORT_CONSTRUCTOR.invoke(getAsShort());
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

    public static class NBTTagLong extends INBTNumber<Long> {
        private static final MethodHandle NBT_TAG_LONG_CONSTRUCTOR;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> nbtTagLong = ReflectionUtils.getNMSClass("NBTTagLong");
            MethodHandle handler = null;

            try {
                if (XMaterial.supports(15)) handler = lookup.findStatic(nbtTagLong, "a",
                        MethodType.methodType(nbtTagLong, long.class));
                else handler = lookup.findConstructor(nbtTagLong,
                        MethodType.methodType(void.class, long.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            NBT_TAG_LONG_CONSTRUCTOR = handler;
        }

        public NBTTagLong(long value) {
            super(value);
        }

        public static NBTTagLong fromNBT(Object nbtObject) {
            Long value = (Long) findNBTData(nbtObject);
            return new NBTTagLong(value == null ? 0 : value);
        }

        @Override
        public double getAsDouble() {
            return getValue();
        }

        @Override
        public Object toNBT() {
            try {
                return NBT_TAG_LONG_CONSTRUCTOR.invoke(getAsLong());
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

    public static class NBTTagFloat extends INBTNumber<Float> {
        private static final MethodHandle NBT_TAG_LONG_CONSTRUCTOR;

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> nbtFloat = ReflectionUtils.getNMSClass("NBTTagFloat");
            MethodHandle handler = null;

            try {
                if (XMaterial.supports(15)) handler = lookup.findStatic(nbtFloat, "a", MethodType.methodType(nbtFloat, float.class));
                else handler = lookup.findConstructor(nbtFloat, MethodType.methodType(void.class, float.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }
            NBT_TAG_LONG_CONSTRUCTOR = handler;
        }

        public NBTTagFloat(float value) {
            super(value);
        }

        public static NBTTagFloat fromNBT(Object nbtObject) {
            Float value = (Float) findNBTData(nbtObject);
            return new NBTTagFloat(value == null ? 0 : value);
        }

        @Override
        public double getAsDouble() {
            return getValue();
        }

        @Override
        public Object toNBT() {
            try {
                return NBT_TAG_LONG_CONSTRUCTOR.invoke(getAsFloat());
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
