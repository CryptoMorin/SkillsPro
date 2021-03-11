package org.skills.services.mobs;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.LivingEntity;
import org.skills.utils.Pair;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@SuppressWarnings("JavaLangInvokeHandleSignature")
public class ServiceMythicMobs {
    private static final MethodHandle GET_LEVEL;

    static {
        MethodHandle getLevel = null;

        try {
            getLevel = MethodHandles.lookup().findVirtual(ActiveMob.class, "getLevel", MethodType.methodType(int.class));
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
        }

        GET_LEVEL = getLevel;
    }

    private static double getLevel(ActiveMob mob) {
        try {
            return GET_LEVEL == null ? mob.getLevel() : (double) GET_LEVEL.invoke(mob);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return 0;
        }
    }

    public static Pair<String, Number> getMobProperties(LivingEntity entity) {
        ActiveMob mob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity);
        return mob == null ? null : Pair.of(mob.getType().getInternalName(), getLevel(mob));
    }
}
