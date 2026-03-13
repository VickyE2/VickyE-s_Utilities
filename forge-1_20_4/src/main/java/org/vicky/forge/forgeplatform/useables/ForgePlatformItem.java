/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform.useables;

import net.kyori.adventure.text.Component;
import net.minecraft.nbt.*;
import org.vicky.forge.forgeplatform.adventure.AdventureComponentConverter;
import org.vicky.platform.PlatformItemStack;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.vicky.platform.utils.ResourceLocation;
import org.vicky.utilities.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ForgePlatformItem(ItemStack item) implements PlatformItemStack {

	@Override
	public Component getName() {
		return AdventureComponentConverter.fromNative(item.getHoverName());
	}

	@Override
	public void setName(Component name) {
		item.setHoverName(AdventureComponentConverter.toNative(name));
	}

	@Override
	public ResourceLocation getDescriptorId() {
		return ResourceLocation.from(item.getDescriptionId());
	}

	@Override
	public void setCount(int count) {
		item.setCount(count);
	}

	@Override
	public int getCount() {
		return item.getCount();
	}

	@Override
	public void applyNbt(Map<String, ?> nbt) {
		CompoundTag tag = item.getOrCreateTag();
		for (Map.Entry<String, ?> entry : nbt.entrySet()) {
			Tag converted = toNbt(entry.getValue());
			if (converted != null) {
				tag.put(entry.getKey(), converted);
			}
		}
	}

	@Override
	public void applyNbt(Pair<String, ?> nbt) {
		CompoundTag tag = item.getOrCreateTag();
		Tag converted = toNbt(nbt.getValue());
		if (converted != null) {
			tag.put(nbt.getKey(), converted);
		}
	}

	@Override
	public boolean hasNbt(String key) {
		CompoundTag tag = item.getTag();
		return tag != null && tag.contains(key);
	}

	@Override
	public Object getNbt(String key) {
		CompoundTag tag = item.getTag();
		if (tag == null || !tag.contains(key)) return null;
		return fromNbt(tag.get(key));
	}

	private Tag toNbt(Object value) {
		if (value instanceof String s) return StringTag.valueOf(s);
		if (value instanceof Integer i) return IntTag.valueOf(i);
		if (value instanceof Boolean b) return ByteTag.valueOf(b);
		if (value instanceof Double d) return DoubleTag.valueOf(d);
		if (value instanceof Float f) return FloatTag.valueOf(f);
		if (value instanceof Long l) return LongTag.valueOf(l);
		if (value instanceof Byte b) return ByteTag.valueOf(b);
		if (value instanceof Short s) return ShortTag.valueOf(s);
		if (value instanceof Map m) {
			CompoundTag tag = new CompoundTag();
			for (Object k : m.keySet()) {
				if (k instanceof String s) {
					Tag t = toNbt(m.get(k));
					if (t != null) tag.put(s, t);
				}
			}
			return tag;
		}
		if (value instanceof List l) {
			ListTag list = new ListTag();
			for (Object o : l) {
				Tag t = toNbt(o);
				if (t != null) list.add(t);
			}
			return list;
		}
		return null;
	}

	private Object fromNbt(Tag tag) {
		if (tag instanceof StringTag s) return s.getAsString();
		if (tag instanceof IntTag i) return i.getAsInt();
		if (tag instanceof ByteTag b) return b.getAsByte();
		if (tag instanceof DoubleTag d) return d.getAsDouble();
		if (tag instanceof FloatTag f) return f.getAsFloat();
		if (tag instanceof LongTag l) return l.getAsLong();
		if (tag instanceof ShortTag s) return s.getAsShort();
		if (tag instanceof CompoundTag c) {
			Map<String, Object> map = new HashMap<>();
			for (String key : c.getAllKeys()) {
				map.put(key, fromNbt(c.get(key)));
			}
			return map;
		}
		if (tag instanceof ListTag l) {
			List<Object> list = new ArrayList<>();
			for (Tag t : l) {
				list.add(fromNbt(t));
			}
			return list;
		}
		return null;
	}

	@Override
	public PlatformItemStack copy() {
		return new ForgePlatformItem(item.copy());
	}

	@Override
	public String getIdentifier() {
		return ForgeRegistries.ITEMS.getKey(item.getItem()).toString();
	}
}
