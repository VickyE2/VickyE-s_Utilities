/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.tags;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CompoundData {

	boolean contains(String key);

	boolean isEmpty();

	Set<String> keys();

	void remove(String key);

	void clear();

	CompoundData copy();

	void applyRaw(Map<String, Object> map);

	void putString(String key, String value);

	String getString(String key);

	String getString(String key, String fallback);

	void putInt(String key, int value);

	int getInt(String key);

	int getInt(String key, int fallback);

	void putBoolean(String key, boolean value);

	boolean getBoolean(String key);

	boolean getBoolean(String key, boolean fallback);

	void putLong(String key, long value);

	long getLong(String key);

	long getLong(String key, long fallback);

	void putFloat(String key, float value);

	float getFloat(String key);

	float getFloat(String key, float fallback);

	void putDouble(String key, double value);

	double getDouble(String key);

	double getDouble(String key, double fallback);

	void putByteArray(String key, byte[] value);

	byte[] getByteArray(String key);

	void putIntArray(String key, int[] value);

	int[] getIntArray(String key);

	void putLongArray(String key, long[] value);

	long[] getLongArray(String key);

	void putStringList(String key, List<String> value);

	List<String> getStringList(String key);

	void putIntList(String key, List<Integer> value);

	List<Integer> getIntList(String key);

	void putCompound(String key, CompoundData value);

	@Nullable
	CompoundData findCompound(String key);

	CompoundData getCompound(String key);

	CompoundData getOrCreateCompound(String key);

	String asString();
}