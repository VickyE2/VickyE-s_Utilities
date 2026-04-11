/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform.useables;

import de.pauleff.core.*;
import de.pauleff.util.NBTTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;
import org.vicky.platform.items.EventResult;
import org.vicky.platform.items.InteractionHand;
import org.vicky.platform.utils.IntVec3;
import org.vicky.platform.world.PlatformLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class ForgeHacks {
	public static ResourceLocation fromVicky(org.vicky.platform.utils.ResourceLocation resourceLocation) {
		return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), resourceLocation.getPath());
	}
	public static org.vicky.platform.utils.ResourceLocation toVicky(ResourceLocation resourceLocation) {
		return org.vicky.platform.utils.ResourceLocation.from(resourceLocation.getNamespace(),
				resourceLocation.getPath());
	}
	public static IntVec3 toVicky(BlockPos pos) {
		return IntVec3.of(pos.getX(), pos.getY(), pos.getZ());
	}
	public static BlockPos fromVicky(IntVec3 pos) {
		return BlockPos.of(BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ()));
	}
	public static org.vicky.platform.utils.Vec3 toVicky(Vec3 vec3) {
		return org.vicky.platform.utils.Vec3.of(vec3.x, vec3.y, vec3.z);
	}
	public static PlatformLocation toVicky(Vec3 vec3, Level level) {
		return new ForgeVec3(level, vec3.x, vec3.y, vec3.z, 0.0f, 0.0f);
	}

    public static Rarity fromVicky(org.vicky.platform.items.Rarity rarity) {
        return switch (rarity) {
            case UNCOMMON -> Rarity.UNCOMMON;
			case RARE -> Rarity.RARE;
			case EPIC -> Rarity.EPIC;
			default -> Rarity.COMMON;
		};
    }

    public static InteractionHand toVicky(net.minecraft.world.InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> InteractionHand.MAIN_HAND;
            case OFF_HAND -> InteractionHand.OFF_HAND;
        };
    }

    public static @NotNull InteractionResult fromVicky(org.vicky.platform.items.InteractionResult interactionResult) {
        return switch (interactionResult) {
			case SUCCESS -> InteractionResult.SUCCESS;
			case CONSUME -> InteractionResult.CONSUME;
			case CONSUME_PARTIAL -> InteractionResult.CONSUME_PARTIAL;
			case PASS -> InteractionResult.PASS;
			case FAIL -> InteractionResult.FAIL;
		};
    }

    public static Event.Result fromVicky(EventResult eventResult) {
        return switch (eventResult) {
			case ALLOW -> Event.Result.ALLOW;
			case DENY -> Event.Result.DENY;
			case DEFAULT -> Event.Result.DEFAULT;
		};
    }

	public static Tag_Compound toVicky(String name, net.minecraft.nbt.CompoundTag tag) {
		Tag_Compound result = new Tag_Compound(name);

		for (String key : tag.getAllKeys()) {
			net.minecraft.nbt.Tag value = tag.get(key);

			if (value instanceof net.minecraft.nbt.StringTag) {
				result.addString(key, value.getAsString());

			} else if (value instanceof net.minecraft.nbt.IntTag) {
				result.addInt(key, ((net.minecraft.nbt.IntTag) value).getAsInt());

			} else if (value instanceof net.minecraft.nbt.DoubleTag) {
				result.addDouble(key, ((net.minecraft.nbt.DoubleTag) value).getAsDouble());

			} else if (value instanceof net.minecraft.nbt.FloatTag) {
				result.addFloat(key, ((net.minecraft.nbt.FloatTag) value).getAsFloat());

			} else if (value instanceof net.minecraft.nbt.ByteTag) {
				result.addByte(key, ((net.minecraft.nbt.ByteTag) value).getAsByte());

			} else if (value instanceof net.minecraft.nbt.ShortTag) {
				result.addShort(key, ((net.minecraft.nbt.ShortTag) value).getAsShort());

			} else if (value instanceof net.minecraft.nbt.LongTag) {
				result.addLong(key, ((net.minecraft.nbt.LongTag) value).getAsLong());

			} else if (value instanceof net.minecraft.nbt.CompoundTag) {
				result.addTag(toVicky(key, (net.minecraft.nbt.CompoundTag) value));

			} else if (value instanceof net.minecraft.nbt.ListTag mcList) {
				Tag_List list = new Tag_List(key, NBTTags.Tag_Compound.getId());

                for (net.minecraft.nbt.Tag item : mcList) {
                    list.addTag(toVicky(item));
                }

				result.addTag(list);
			}
		}

		return result;
	}
	public static Tag<?> toVicky(net.minecraft.nbt.Tag tag) {
		if (tag instanceof net.minecraft.nbt.StringTag) {
			return new Tag_String("", tag.getAsString());

		} else if (tag instanceof net.minecraft.nbt.IntTag) {
			return new Tag_Int("", ((net.minecraft.nbt.IntTag) tag).getAsInt());

		} else if (tag instanceof net.minecraft.nbt.DoubleTag) {
			return new Tag_Double("", ((net.minecraft.nbt.DoubleTag) tag).getAsDouble());

		} else if (tag instanceof net.minecraft.nbt.FloatTag) {
			return new Tag_Float("", ((net.minecraft.nbt.FloatTag) tag).getAsFloat());

		} else if (tag instanceof net.minecraft.nbt.ByteTag) {
			return new Tag_Byte("", ((net.minecraft.nbt.ByteTag) tag).getAsByte());

		} else if (tag instanceof net.minecraft.nbt.ShortTag) {
			return new Tag_Short("", ((net.minecraft.nbt.ShortTag) tag).getAsShort());

		} else if (tag instanceof net.minecraft.nbt.LongTag) {
			return new Tag_Long("", ((net.minecraft.nbt.LongTag) tag).getAsLong());

		} else if (tag instanceof net.minecraft.nbt.CompoundTag) {
			return toVicky("", (net.minecraft.nbt.CompoundTag) tag);

		} else if (tag instanceof net.minecraft.nbt.ListTag mcList) {
			Tag_List list = new Tag_List("", NBTTags.Tag_Compound.getId());

            for (net.minecraft.nbt.Tag value : mcList) {
                list.addTag(toVicky(value));
            }

			return list;
		}

		return new Tag_String("", tag.toString()); // fallback
	}
	public static Tag<?> toVicky(String key, net.minecraft.nbt.Tag tag) {
		if (tag instanceof net.minecraft.nbt.StringTag) {
			return new Tag_String(key, tag.getAsString());

		} else if (tag instanceof net.minecraft.nbt.IntTag) {
			return new Tag_Int(key, ((net.minecraft.nbt.IntTag) tag).getAsInt());

		} else if (tag instanceof net.minecraft.nbt.DoubleTag) {
			return new Tag_Double(key, ((net.minecraft.nbt.DoubleTag) tag).getAsDouble());

		} else if (tag instanceof net.minecraft.nbt.FloatTag) {
			return new Tag_Float(key, ((net.minecraft.nbt.FloatTag) tag).getAsFloat());

		} else if (tag instanceof net.minecraft.nbt.ByteTag) {
			return new Tag_Byte(key, ((net.minecraft.nbt.ByteTag) tag).getAsByte());

		} else if (tag instanceof net.minecraft.nbt.ShortTag) {
			return new Tag_Short(key, ((net.minecraft.nbt.ShortTag) tag).getAsShort());

		} else if (tag instanceof net.minecraft.nbt.LongTag) {
			return new Tag_Long(key, ((net.minecraft.nbt.LongTag) tag).getAsLong());

		} else if (tag instanceof net.minecraft.nbt.CompoundTag) {
			return toVicky(key, (net.minecraft.nbt.CompoundTag) tag);

		} else if (tag instanceof net.minecraft.nbt.ListTag mcList) {
			Tag_List list = new Tag_List(key, NBTTags.Tag_Compound.getId());

            for (net.minecraft.nbt.Tag value : mcList) {
                list.addTag(toVicky(value));
            }

			return list;
		}

		return new Tag_String(key, tag.toString()); // fallback
	}

	public static net.minecraft.nbt.CompoundTag fromVicky(Tag_Compound tag) {
		net.minecraft.nbt.CompoundTag result = new net.minecraft.nbt.CompoundTag();

		ArrayList<Tag<?>> data = tag.getData();

		for (Tag<?> entry : data) {
			String key = entry.getName();

			if (entry instanceof Tag_String) {
				result.putString(key, (String) entry.getData());

			} else if (entry instanceof Tag_Int) {
				result.putInt(key, (Integer) entry.getData());

			} else if (entry instanceof Tag_Double) {
				result.putDouble(key, (Double) entry.getData());

			} else if (entry instanceof Tag_Float) {
				result.putFloat(key, (Float) entry.getData());

			} else if (entry instanceof Tag_Byte) {
				result.putByte(key, (Byte) entry.getData());

			} else if (entry instanceof Tag_Short) {
				result.putShort(key, (Short) entry.getData());

			} else if (entry instanceof Tag_Long) {
				result.putLong(key, (Long) entry.getData());

			} else if (entry instanceof Tag_Compound) {
				result.put(key, fromVicky((Tag_Compound) entry));

			} else if (entry instanceof Tag_List) {
				result.put(key, convertListFromVicky((Tag_List) entry));
			}
		}

		return result;
	}
	private static net.minecraft.nbt.ListTag convertListFromVicky(Tag_List list) {
		net.minecraft.nbt.ListTag result = new net.minecraft.nbt.ListTag();

		ArrayList<Tag<?>> data = list.getData();

		for (Tag<?> entry : data) {
			result.add(convertListElementFromVicky(entry));
		}

		return result;
	}
	private static net.minecraft.nbt.Tag convertListElementFromVicky(Tag<?> tag) {
		if (tag instanceof Tag_String) {
			return net.minecraft.nbt.StringTag.valueOf((String) tag.getData());

		} else if (tag instanceof Tag_Int) {
			return net.minecraft.nbt.IntTag.valueOf((Integer) tag.getData());

		} else if (tag instanceof Tag_Double) {
			return net.minecraft.nbt.DoubleTag.valueOf((Double) tag.getData());

		} else if (tag instanceof Tag_Float) {
			return net.minecraft.nbt.FloatTag.valueOf((Float) tag.getData());

		} else if (tag instanceof Tag_Byte) {
			return net.minecraft.nbt.ByteTag.valueOf((Byte) tag.getData());

		} else if (tag instanceof Tag_Short) {
			return net.minecraft.nbt.ShortTag.valueOf((Short) tag.getData());

		} else if (tag instanceof Tag_Long) {
			return net.minecraft.nbt.LongTag.valueOf((Long) tag.getData());

		} else if (tag instanceof Tag_Compound) {
			return fromVicky((Tag_Compound) tag);

		} else if (tag instanceof Tag_List) {
			return convertListFromVicky((Tag_List) tag);
		}

		return net.minecraft.nbt.StringTag.valueOf(tag.getData().toString()); // fallback
	}

	public static net.minecraft.nbt.Tag toNBT(Object value) {
		if (value instanceof Integer i) {
			return net.minecraft.nbt.IntTag.valueOf(i);

		}
		else if (value instanceof Double d) {
			return net.minecraft.nbt.DoubleTag.valueOf(d);

		}
		else if (value instanceof Long d) {
			return net.minecraft.nbt.LongTag.valueOf(d);

		}
		else if (value instanceof Float d) {
			return net.minecraft.nbt.FloatTag.valueOf(d);

		}
		else if (value instanceof String s) {
			return net.minecraft.nbt.StringTag.valueOf(s);

		}
		else if (value instanceof Boolean b) {
			return net.minecraft.nbt.ByteTag.valueOf((byte) (b ? 1 : 0));

		}
		else if (value instanceof Byte b) {
			return net.minecraft.nbt.ByteTag.valueOf(b);

		}
		else if (value instanceof Short b) {
			return net.minecraft.nbt.ShortTag.valueOf(b);

		}
		else if (value instanceof Map<?, ?> map) {
			net.minecraft.nbt.CompoundTag compound = new net.minecraft.nbt.CompoundTag();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				if (entry.getKey() instanceof String key) {
					compound.put(key, toNBT(entry.getValue()));
				}
			}
			return compound;

		}
		else if (value instanceof List<?> list) {
			net.minecraft.nbt.ListTag listTag = new net.minecraft.nbt.ListTag();
			for (Object o : list) {
				listTag.add(toNBT(o));
			}
			return listTag;
		}
		else if (value instanceof Tag<?> tag) {
			return toNBT(tag.getData());
		}
		else if (value == null) return null;

		// LAST RESORT
		return net.minecraft.nbt.StringTag.valueOf(value.toString());
	}
	public static Object fromNBT(net.minecraft.nbt.Tag tag) {

		if (tag instanceof net.minecraft.nbt.IntTag t) {
			return t.getAsInt();

		} else if (tag instanceof net.minecraft.nbt.DoubleTag t) {
			return t.getAsDouble();

		} else if (tag instanceof net.minecraft.nbt.LongTag t) {
			return t.getAsLong();

		} else if (tag instanceof net.minecraft.nbt.FloatTag t) {
			return t.getAsFloat();

		} else if (tag instanceof net.minecraft.nbt.StringTag t) {
			return t.getAsString();

		} else if (tag instanceof net.minecraft.nbt.ByteTag t) {
			byte b = t.getAsByte();
			// Optional: interpret as boolean if it's 0/1
			if (b == 0 || b == 1) {
				return b == 1;
			}
			return b;

		} else if (tag instanceof net.minecraft.nbt.ShortTag t) {
			return t.getAsShort();

		} else if (tag instanceof net.minecraft.nbt.CompoundTag compound) {
			Map<String, Object> map = new HashMap<>();

			for (String key : compound.getAllKeys()) {
				map.put(key, fromNBT(compound.get(key)));
			}

			return map;

		} else if (tag instanceof net.minecraft.nbt.ListTag list) {
			List<Object> result = new ArrayList<>();

			for (int i = 0; i < list.size(); i++) {
				result.add(fromNBT(list.get(i)));
			}

			return result;
		}

		// fallback
		return tag.toString();
	}

	public static <T extends Comparable<T>> Property<T> fromVicky(
			org.vicky.platform.utils.Property<T> property
	) {
		if (property instanceof org.vicky.platform.utils.defaultproperties.IntegerProperty intP) {

			var values = intP.values();
			if (values.isEmpty()) {
				throw new IllegalArgumentException("IntegerProperty has no values: " + property.getIdentifier());
			}

			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;

			for (int v : values) {
				if (v < min) min = v;
				if (v > max) max = v;
			}

			return cast(IntegerProperty.create(property.getIdentifier(), min, max));
		}

		if (property instanceof org.vicky.platform.utils.defaultproperties.BooleanProperty) {
			return cast(BooleanProperty.create(property.getIdentifier()));
		}

		if (property instanceof org.vicky.platform.utils.defaultproperties.EnumProperty<?> enumP) {
			return cast(EnumProperty.create(property.getIdentifier(), cast(enumP.getType())));
		}

		throw new IllegalArgumentException("Unsupported property type: " + property.getClass());
	}

	@SuppressWarnings("unchecked")
	private static <T> T cast(Object obj) {
		return (T) obj;
	}
}
