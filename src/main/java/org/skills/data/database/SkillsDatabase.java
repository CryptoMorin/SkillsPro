package org.skills.data.database;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface SkillsDatabase<T extends DataContainer> {
    @Nullable
    T load(String key);

    void save(@Nullable T data);

    void delete(@Nullable String key);

    boolean hasData(@Nullable String key);

    @NonNull
    String[] getAllKeys();
}