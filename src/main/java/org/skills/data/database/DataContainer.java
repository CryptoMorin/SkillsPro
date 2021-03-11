package org.skills.data.database;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

public abstract class DataContainer {
    private transient String saveMeta;

    protected static String compressUUID(UUID id) {
        return id == null ? "" : Long.toString(id.getLeastSignificantBits()) + id.getMostSignificantBits();
    }

    protected static String compressBoolean(boolean bool) {
        return bool ? "1" : "";
    }

    protected static <T> String compressCollecton(Collection<T> collection, Function<T, Object> function) {
        StringBuilder builder = new StringBuilder(collection.size() * collection.size());
        for (T element : collection) builder.append(function.apply(element));
        return builder.toString();
    }

    public abstract @NonNull String getKey();

    public String getSaveMeta() {
        return this.saveMeta;
    }

    public void setSaveMeta() {
        this.saveMeta = this.getCompressedData();
    }

    public boolean shouldSave() {
        String compressedData = getCompressedData();
        if (!compressedData.equals(this.saveMeta)) {
            this.saveMeta = compressedData;
            return true;
        }
        return false;
    }

    public abstract @NonNull String getCompressedData();

    public abstract void setIdentifier(@NonNull String identifier);
}