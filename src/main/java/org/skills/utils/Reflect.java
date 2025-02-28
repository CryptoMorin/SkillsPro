package org.skills.utils;

import java.lang.reflect.Field;
import java.util.Arrays;

public final class Reflect {
    public static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Field getDeclaredField(Class<?> clazz, String... names) throws NoSuchFieldException {
        NoSuchFieldException error = null;

        for (String name : names) {
            try {
                /* 1.20.6-36 The fuck??????
                 * Caused by: java.lang.NullPointerException: Cannot invoke "java.lang.Class.name()" because "clazz" is null
                 *         at io.papermc.paper.pluginremap.reflect.PaperReflection.mapDeclaredFieldName(PaperReflection.java:77) ~[paper-1.20.6.jar:git-Paper-36]
                 *         at io.papermc.reflectionrewriter.runtime.AbstractDefaultRulesReflectionProxy.getDeclaredField(AbstractDefaultRulesReflectionProxy.java:90) ~[reflection-rewriter-runtime-0.0.1.jar:?]
                 *         at io.papermc.paper.pluginremap.reflect.PaperReflectionHolder.getDeclaredField(Unknown Source) ~[paper-1.20.6.jar:git-Paper-36]
                 *         at KingdomsX-1.16.20.5.jar/org.kingdoms.utils.internal.reflection.Reflect.getDeclaredField(Reflect.java:31) ~[KingdomsX-1.16.20.5.jar:?]
                 */
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException ex) {
                if (error == null)
                    error = new NoSuchFieldException("Couldn't find any of the fields " + Arrays.toString(names) + " in class: " + clazz);
                error.addSuppressed(ex);
            }
        }

        throw error;
    }
}
