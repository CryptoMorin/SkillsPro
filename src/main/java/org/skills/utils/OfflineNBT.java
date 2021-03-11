package org.skills.utils;

import com.cryptomorin.xseries.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.MessageHandler;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class OfflineNBT {
    private static final MethodHandle GET_COMPOUND;
    private static final MethodHandle SET_FLOAT;
    private static final MethodHandle GET_FLOAT;
    private static final MethodHandle SET_BOOLEAN;
    private static final MethodHandle GET_BOOLEAN;
    private static final MethodHandle SAVE;
    private static final MethodHandle LOAD;
    private static final boolean EMERGENCY;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> compressor = ReflectionUtils.getNMSClass("NBTCompressedStreamTools");
        Class<?> nbtTagCompound = ReflectionUtils.getNMSClass("NBTTagCompound");
        MethodHandle getCompound = null;
        MethodHandle setFloat = null;
        MethodHandle getFloat = null;
        MethodHandle setBoolean = null;
        MethodHandle getBoolean = null;
        MethodHandle save = null;
        MethodHandle load = null;
        boolean emergency = false;

        try {
            getCompound = lookup.findVirtual(nbtTagCompound, "getCompound", MethodType.methodType(nbtTagCompound, String.class));
            setFloat = lookup.findVirtual(nbtTagCompound, "setFloat", MethodType.methodType(void.class, String.class, float.class));
            getFloat = lookup.findVirtual(nbtTagCompound, "getFloat", MethodType.methodType(float.class, String.class));

            setBoolean = lookup.findVirtual(nbtTagCompound, "setBoolean", MethodType.methodType(void.class, String.class, boolean.class));
            getBoolean = lookup.findVirtual(nbtTagCompound, "getBoolean", MethodType.methodType(boolean.class, String.class));

            save = lookup.findStatic(compressor, "a", MethodType.methodType(void.class, nbtTagCompound, OutputStream.class));
            load = lookup.findStatic(compressor, "a", MethodType.methodType(nbtTagCompound, InputStream.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            emergency = true;
        }

        GET_COMPOUND = getCompound;
        SET_FLOAT = setFloat;
        GET_FLOAT = getFloat;
        SET_BOOLEAN = setBoolean;
        GET_BOOLEAN = getBoolean;
        SAVE = save;
        LOAD = load;
        EMERGENCY = emergency;
    }

    private UUID player;
    private File file;
    private Object compound;
    private Object abilities;

    public OfflineNBT(UUID id) {
        try {
            this.player = id;
            this.file = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "playerdata" + File.separator + this.player + ".dat");
            this.abilities = GET_COMPOUND.invoke(this.compound, "abilities");
            if (this.file.exists()) {
                this.compound = LOAD.invoke(new FileInputStream(this.file));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void perform() {
        if (EMERGENCY) {
            MessageHandler.sendConsolePluginMessage("&cThere has been an error while getting ready to modify players data.");
            MessageHandler.sendConsolePluginMessage("&cPlease find the error from the logs above and report it.");
            return;
        }
        if (SkillsConfig.ARMOR_WEIGHTS_ENABLED.getBoolean()) {
            MessageHandler.sendConsolePluginMessage("&4Cannot reset players speed when armor weights option is on.");
            MessageHandler.sendConsolePluginMessage("&4You do not need to reset players data with this option on, the speeds");
            MessageHandler.sendConsolePluginMessage("&4will be automatically adjusted.");
            return;
        }
        float amount = (float) SkillsConfig.ARMOR_WEIGHTS_RESET_SPEEDS_AMOUNT.getDouble();
        MessageHandler.sendConsolePluginMessage("&2Starting to reset players speed to " + amount + "...");

        CompletableFuture.runAsync(() -> {
            for (World world : Bukkit.getWorlds()) {
                File[] files = new File(world.getWorldFolder(), "playerdata").listFiles();

                for (File file : files) {
                    String name = file.getName();
                    int index = name.lastIndexOf('.');
                    if (index != -1) name = name.substring(0, index);
                    MessageHandler.sendConsolePluginMessage("&2Reseting player speed in &e" + world.getName() + "&8: &e" + name);
                    UUID id = FastUUID.fromString(name);

                    OfflineNBT nbt = new OfflineNBT(id);
                    nbt.setWalkSpeed(amount);
                    nbt.savePlayerData();
                }
            }
        }).exceptionally(t -> {
            t.printStackTrace();
            return null;
        });
    }

    public void savePlayerData() {
        try {
            SAVE.invoke(this.compound, new FileOutputStream(this.file));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public float getWalkSpeed() {
        try {
            return (float) GET_FLOAT.invoke(abilities, "walkSpeed");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return 0;
        }
    }

    public void setWalkSpeed(float speed) {
        try {
            SET_FLOAT.invoke(abilities, "walkSpeed", speed);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public float getFlySpeed() {
        try {
            return (float) GET_FLOAT.invoke(abilities, "flySpeed");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return 0;
        }
    }

    public void setFlySpeed(float speed) {
        try {
            SET_FLOAT.invoke(abilities, "flySpeed", speed);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public boolean getAllowFlight() {
        try {
            return (boolean) GET_BOOLEAN.invoke(abilities, "mayFly");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }
    }

    public void setAllowFlight(boolean allow) {
        try {
            SET_BOOLEAN.invoke(abilities, "mayFly", allow);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public boolean isFlying() {
        try {
            return (boolean) GET_BOOLEAN.invoke(abilities, "flying");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }
    }

    public void setFlying(boolean flying) {
        try {
            SET_BOOLEAN.invoke(abilities, "flying", flying);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}