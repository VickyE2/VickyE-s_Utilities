package org.vicky.forge.forgeplatform.useables;

import net.minecraft.nbt.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.platform.tags.CompoundData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ForgeCompoundData(CompoundTag delegate) implements CompoundData {

    @Override
    public @NonNull String toString() {
        return delegate.toString();
    }

    @Override
    public boolean contains(String key) {
        return delegate.contains(key);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public Set<String> keys() {
        return delegate.getAllKeys();
    }

    @Override
    public void remove(String key) {
        delegate.remove(key);
    }

    @Override
    public void clear() {
        for (var key : delegate.getAllKeys()) {
            delegate.remove(key);
        }
    }

    @Override
    public CompoundData copy() {
        return new ForgeCompoundData(delegate.copy());
    }

    @Override
    public void applyRaw(Map<String, Object> map) {
        for (var entry : map.entrySet()) {
            delegate.put(entry.getKey(), ForgeHacks.toNBT(entry.getValue()));
        }
    }

    @Override
    public void putString(String key, String value) {
        delegate.putString(key, value);
    }

    @Override
    public String getString(String key) {
        return delegate.getString(key);
    }

    @Override
    public String getString(String key, String fallback) {
        var entry = delegate.get(key);
        return entry == null ? fallback : entry.getAsString();
    }

    @Override
    public void putInt(String key, int value) {
        delegate.putInt(key, value);
    }

    @Override
    public int getInt(String key) {
        return delegate.getInt(key);
    }

    @Override
    public int getInt(String key, int fallback) {
        var entry = delegate.get(key);
        return entry == null ? fallback : ((IntTag) entry).getAsInt();
    }

    @Override
    public void putBoolean(String key, boolean value) {
        delegate.putBoolean(key, value);
    }

    @Override
    public boolean getBoolean(String key) {
        return delegate.getBoolean(key);
    }

    @Override
    public boolean getBoolean(String key, boolean fallback) {
        var entry = delegate.get(key);
        return entry == null ? fallback : ((ByteTag) entry).getAsByte() != 0;
    }

    @Override
    public void putLong(String key, long value) {
        delegate.putLong(key, value);
    }

    @Override
    public long getLong(String key) {
        return delegate.getLong(key);
    }

    @Override
    public long getLong(String key, long fallback) {
        var entry = delegate.get(key);
        return entry == null ? fallback : ((LongTag) entry).getAsLong();
    }

    @Override
    public void putFloat(String key, float value) {
        delegate.putFloat(key, value);
    }

    @Override
    public float getFloat(String key) {
        return delegate.getFloat(key);
    }

    @Override
    public float getFloat(String key, float fallback) {
        var entry = delegate.get(key);
        return entry == null ? fallback : ((FloatTag) entry).getAsFloat();
    }

    @Override
    public void putDouble(String key, double value) {
        delegate.putDouble(key, value);
    }

    @Override
    public double getDouble(String key) {
        return delegate.getDouble(key);
    }

    @Override
    public double getDouble(String key, double fallback) {
        var entry = delegate.get(key);
        return entry == null ? fallback : ((DoubleTag) entry).getAsDouble();
    }

    @Override
    public void putByteArray(String key, byte[] value) {
        delegate.putByteArray(key, value);
    }

    @Override
    public byte[] getByteArray(String key) {
        return delegate.getByteArray(key);
    }

    @Override
    public void putIntArray(String key, int[] value) {
        delegate.putIntArray(key, value);
    }

    @Override
    public int[] getIntArray(String key) {
        return delegate.getIntArray(key);
    }

    @Override
    public void putLongArray(String key, long[] value) {
        delegate.putLongArray(key, value);
    }

    @Override
    public long[] getLongArray(String key) {
        return delegate.getLongArray(key);
    }

    @Override
    public void putStringList(String key, List<String> value) {
        var tag = new ListTag();
        for (String s : value) {
            tag.add(StringTag.valueOf(s));
        }
        delegate.put(key, tag);
    }

    @Override
    public List<String> getStringList(String key) {
        var tag = delegate.getList(key, Tag.TAG_STRING);
        return new ArrayList<>(tag.stream().map(Tag::getAsString).toList());
    }

    @Override
    public void putIntList(String key, List<Integer> value) {
        var tag = new ListTag();
        for (Integer s : value) {
            tag.add(IntTag.valueOf(s));
        }
        delegate.put(key, tag);
    }

    @Override
    public List<Integer> getIntList(String key) {
        var tag = delegate.getList(key, Tag.TAG_INT_ARRAY);
        return new ArrayList<>(tag.stream().map((data -> (IntTag) data)).map(IntTag::getAsInt).toList());
    }

    @Override
    public void putCompound(String key, CompoundData value) {
        if (value instanceof ForgeCompoundData) {
            delegate.put(key, ((ForgeCompoundData) value).delegate);
        }
    }

    @Override
    public @Nullable CompoundData findCompound(String key) {
        var entry = delegate.get(key);
        return entry == null ? null : entry instanceof CompoundTag ? new ForgeCompoundData((CompoundTag) entry) : null;
    }

    @Override
    public CompoundData getCompound(String key) {
        return new ForgeCompoundData(delegate.getCompound(key));
    }

    @Override
    public CompoundData getOrCreateCompound(String key) {
        if (delegate.contains(key, Tag.TAG_COMPOUND)) {
            return new ForgeCompoundData(delegate.getCompound(key));
        }
        delegate.put(key, new CompoundTag());
        return new ForgeCompoundData(delegate.getCompound(key));
    }

    @Override
    public String asString() {
        return delegate.toString();
    }
}
