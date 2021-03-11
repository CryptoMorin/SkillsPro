package org.skills.utils;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.ForkJoinPool;

public final class CacheHandler {
    private static final ForkJoinPool POOL = new ForkJoinPool();

    public static Caffeine<Object, Object> newBuilder() {
        return Caffeine.newBuilder().executor(POOL);
    }

    public static ForkJoinPool getPool() {
        return POOL;
    }
}
