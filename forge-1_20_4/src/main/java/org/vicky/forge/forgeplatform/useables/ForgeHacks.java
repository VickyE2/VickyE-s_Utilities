/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform.useables;

import de.pauleff.core.*;
import de.pauleff.core.Tag;
import de.pauleff.util.NBTTags;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.vicky.platform.entity.ModifierOperation;
import org.vicky.platform.entity.PlatformEntityAttribute;
import org.vicky.platform.events.EventPriority;
import org.vicky.platform.item.InteractionHand;
import org.vicky.platform.items.CreativeTabMenu;
import org.vicky.platform.items.EventResult;
import org.vicky.platform.items.ItemRenderPerspective;
import org.vicky.platform.utils.IntVec3;
import org.vicky.platform.world.PlatformLocation;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class ForgeHacks {
	public static @NotNull ResourceLocation fromVicky(org.vicky.platform.utils.ResourceLocation resourceLocation) {
		return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), resourceLocation.getPath());
	}
	public static @NotNull org.vicky.platform.utils.ResourceLocation toVicky(ResourceLocation resourceLocation) {
		return org.vicky.platform.utils.ResourceLocation.from(resourceLocation.getNamespace(),
				resourceLocation.getPath());
	}
	public static @NotNull IntVec3 toVicky(BlockPos pos) {
		return IntVec3.of(pos.getX(), pos.getY(), pos.getZ());
	}
	public static @NotNull BlockPos fromVicky(IntVec3 pos) {
		return BlockPos.of(BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ()));
	}
	public static @NotNull org.vicky.platform.utils.Vec3 toVicky(Vec3 vec3) {
		return org.vicky.platform.utils.Vec3.of(vec3.x, vec3.y, vec3.z);
	}
	public static @NotNull PlatformLocation toVicky(Vec3 vec3, Level level) {
		return new ForgeVec3(level, vec3.x, vec3.y, vec3.z, 0.0f, 0.0f);
	}

	public static @NotNull net.minecraftforge.eventbus.api.EventPriority fromVicky(EventPriority priority) {
		return switch (priority) {
            case LOWEST -> net.minecraftforge.eventbus.api.EventPriority.LOWEST;
            case LOW -> net.minecraftforge.eventbus.api.EventPriority.LOW;
            case HIGHEST -> net.minecraftforge.eventbus.api.EventPriority.HIGHEST;
            case NORMAL -> net.minecraftforge.eventbus.api.EventPriority.NORMAL;
			case HIGH -> net.minecraftforge.eventbus.api.EventPriority.HIGH;
			// case HIGHEST -> net.minecraftforge.eventbus.api.EventPriority.HIGHEST;
			// case LOWEST -> net.minecraftforge.eventbus.api.EventPriority.LOWEST;
		};
	}

    public static @NotNull Rarity fromVicky(org.vicky.platform.items.Rarity rarity) {
        return switch (rarity) {
            case UNCOMMON -> Rarity.UNCOMMON;
			case RARE -> Rarity.RARE;
			case EPIC -> Rarity.EPIC;
			default -> Rarity.COMMON;
		};
    }

    public static @NotNull InteractionHand toVicky(net.minecraft.world.InteractionHand hand) {
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

    public static @NotNull Event.Result fromVicky(EventResult eventResult) {
        return switch (eventResult) {
			case ALLOW -> Event.Result.ALLOW;
			case DENY -> Event.Result.DENY;
			case DEFAULT -> Event.Result.DEFAULT;
		};
    }

	public static @NotNull Tag_Compound toVicky(String name, CompoundTag tag) {
		Tag_Compound result = new Tag_Compound(name);

		for (String key : tag.getAllKeys()) {
			net.minecraft.nbt.Tag value = tag.get(key);

			if (value instanceof StringTag) {
				result.addString(key, value.getAsString());

			} else if (value instanceof IntTag) {
				result.addInt(key, ((IntTag) value).getAsInt());

			} else if (value instanceof DoubleTag) {
				result.addDouble(key, ((DoubleTag) value).getAsDouble());

			} else if (value instanceof FloatTag) {
				result.addFloat(key, ((FloatTag) value).getAsFloat());

			} else if (value instanceof ByteTag) {
				result.addByte(key, ((ByteTag) value).getAsByte());

			} else if (value instanceof ShortTag) {
				result.addShort(key, ((ShortTag) value).getAsShort());

			} else if (value instanceof LongTag) {
				result.addLong(key, ((LongTag) value).getAsLong());

			} else if (value instanceof CompoundTag) {
				result.addTag(toVicky(key, (CompoundTag) value));

			} else if (value instanceof ListTag mcList) {
				Tag_List list = new Tag_List(key, NBTTags.Tag_Compound.getId());

                for (net.minecraft.nbt.Tag item : mcList) {
                    list.addTag(toVicky(item));
                }

				result.addTag(list);
			}
		}

		return result;
	}
	public static @NotNull Tag<?> toVicky(net.minecraft.nbt.Tag tag) {
		if (tag instanceof StringTag) {
			return new Tag_String("", tag.getAsString());

		} else if (tag instanceof IntTag) {
			return new Tag_Int("", ((IntTag) tag).getAsInt());

		} else if (tag instanceof DoubleTag) {
			return new Tag_Double("", ((DoubleTag) tag).getAsDouble());

		} else if (tag instanceof FloatTag) {
			return new Tag_Float("", ((FloatTag) tag).getAsFloat());

		} else if (tag instanceof ByteTag) {
			return new Tag_Byte("", ((ByteTag) tag).getAsByte());

		} else if (tag instanceof ShortTag) {
			return new Tag_Short("", ((ShortTag) tag).getAsShort());

		} else if (tag instanceof LongTag) {
			return new Tag_Long("", ((LongTag) tag).getAsLong());

		} else if (tag instanceof CompoundTag) {
			return toVicky("", (CompoundTag) tag);

		} else if (tag instanceof ListTag mcList) {
			Tag_List list = new Tag_List("", NBTTags.Tag_Compound.getId());

            for (net.minecraft.nbt.Tag value : mcList) {
                list.addTag(toVicky(value));
            }

			return list;
		}

		return new Tag_String("", tag.toString()); // fallback
	}
	public static @NotNull net.minecraft.nbt.Tag fromVicky(Tag<?> tag) {
		if (tag instanceof Tag_String value) {
			return StringTag.valueOf(value.getData());

		} else if (tag instanceof Tag_Int value) {
			return IntTag.valueOf(value.getData());

		} else if (tag instanceof Tag_Double value) {
			return DoubleTag.valueOf(value.getData());

		} else if (tag instanceof Tag_Float value) {
			return FloatTag.valueOf(value.getData());

		} else if (tag instanceof Tag_Byte value) {
			return ByteTag.valueOf(value.getData());

		} else if (tag instanceof Tag_Short value) {
			return ShortTag.valueOf(value.getData());

		} else if (tag instanceof Tag_Long value) {
			return LongTag.valueOf(value.getData());

		} else if (tag instanceof Tag_Compound value) {
			return fromVicky(value);

		} else if (tag instanceof Tag_List mcList) {
			return convertListFromVicky(mcList);
		}

		return StringTag.valueOf(tag.toString()); // fallback
	}
	public static @NotNull Tag<?> toVicky(String key, net.minecraft.nbt.Tag tag) {
		if (tag instanceof StringTag) {
			return new Tag_String(key, tag.getAsString());

		} else if (tag instanceof IntTag) {
			return new Tag_Int(key, ((IntTag) tag).getAsInt());

		} else if (tag instanceof DoubleTag) {
			return new Tag_Double(key, ((DoubleTag) tag).getAsDouble());

		} else if (tag instanceof FloatTag) {
			return new Tag_Float(key, ((FloatTag) tag).getAsFloat());

		} else if (tag instanceof ByteTag) {
			return new Tag_Byte(key, ((ByteTag) tag).getAsByte());

		} else if (tag instanceof ShortTag) {
			return new Tag_Short(key, ((ShortTag) tag).getAsShort());

		} else if (tag instanceof LongTag) {
			return new Tag_Long(key, ((LongTag) tag).getAsLong());

		} else if (tag instanceof CompoundTag) {
			return toVicky(key, (CompoundTag) tag);

		} else if (tag instanceof ListTag mcList) {
			Tag_List list = new Tag_List(key, NBTTags.Tag_Compound.getId());

            for (net.minecraft.nbt.Tag value : mcList) {
                list.addTag(toVicky(value));
            }

			return list;
		}

		return new Tag_String(key, tag.toString()); // fallback
	}

	public static @NotNull CompoundTag fromVicky(Tag_Compound tag) {
		CompoundTag result = new CompoundTag();

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
	private static ListTag convertListFromVicky(Tag_List list) {
		ListTag result = new ListTag();

		ArrayList<Tag<?>> data = list.getData();

		for (Tag<?> entry : data) {
			result.add(convertListElementFromVicky(entry));
		}

		return result;
	}
	private static net.minecraft.nbt.Tag convertListElementFromVicky(Tag<?> tag) {
		if (tag instanceof Tag_String) {
			return StringTag.valueOf((String) tag.getData());

		} else if (tag instanceof Tag_Int) {
			return IntTag.valueOf((Integer) tag.getData());

		} else if (tag instanceof Tag_Double) {
			return DoubleTag.valueOf((Double) tag.getData());

		} else if (tag instanceof Tag_Float) {
			return FloatTag.valueOf((Float) tag.getData());

		} else if (tag instanceof Tag_Byte) {
			return ByteTag.valueOf((Byte) tag.getData());

		} else if (tag instanceof Tag_Short) {
			return ShortTag.valueOf((Short) tag.getData());

		} else if (tag instanceof Tag_Long) {
			return LongTag.valueOf((Long) tag.getData());

		} else if (tag instanceof Tag_Compound) {
			return fromVicky((Tag_Compound) tag);

		} else if (tag instanceof Tag_List) {
			return convertListFromVicky((Tag_List) tag);
		}

		return StringTag.valueOf(tag.getData().toString()); // fallback
	}

	public static @NotNull net.minecraft.nbt.Tag toNBT(Object value) {
		if (value instanceof Integer i) {
			return IntTag.valueOf(i);

		}
		else if (value instanceof Double d) {
			return DoubleTag.valueOf(d);

		}
		else if (value instanceof Long d) {
			return LongTag.valueOf(d);

		}
		else if (value instanceof Float d) {
			return FloatTag.valueOf(d);

		}
		else if (value instanceof String s) {
			return StringTag.valueOf(s);

		}
		else if (value instanceof Boolean b) {
			return ByteTag.valueOf((byte) (b ? 1 : 0));

		}
		else if (value instanceof Byte b) {
			return ByteTag.valueOf(b);

		}
		else if (value instanceof Short b) {
			return ShortTag.valueOf(b);

		}
		else if (value instanceof Map<?, ?> map) {
			CompoundTag compound = new CompoundTag();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				if (entry.getKey() instanceof String key) {
					compound.put(key, toNBT(entry.getValue()));
				}
			}
			return compound;

		}
		else if (value instanceof List<?> list) {
			ListTag listTag = new ListTag();
			for (Object o : list) {
				listTag.add(toNBT(o));
			}
			return listTag;
		}
		else if (value instanceof Tag<?> tag) {
			return toNBT(tag.getData());
		}
		else if (value == null) return StringTag.valueOf("null");

		// LAST RESORT
		return StringTag.valueOf(value.toString());
	}
	public static @NotNull Object fromNBT(net.minecraft.nbt.Tag tag) {

		if (tag instanceof IntTag t) {
			return t.getAsInt();

		}
		else if (tag instanceof DoubleTag t) {
			return t.getAsDouble();

		}
		else if (tag instanceof LongTag t) {
			return t.getAsLong();

		}
		else if (tag instanceof FloatTag t) {
			return t.getAsFloat();

		}
		else if (tag instanceof StringTag t) {
			return t.getAsString();

		}
		else if (tag instanceof ByteTag t) {
			byte b = t.getAsByte();
			// Optional: interpret as boolean if it's 0/1
			if (b == 0 || b == 1) {
				return b == 1;
			}
			return b;

		}
		else if (tag instanceof ShortTag t) {
			return t.getAsShort();

		}
		else if (tag instanceof CompoundTag compound) {
			Map<String, Object> map = new HashMap<>();

			for (String key : compound.getAllKeys()) {
				map.put(key, fromNBT(compound.get(key)));
			}

			return map;

		}
		else if (tag instanceof ListTag list) {
			List<Object> result = new ArrayList<>();

			for (int i = 0; i < list.size(); i++) {
				result.add(fromNBT(list.get(i)));
			}

			return result;
		}

		// fallback
		return tag.toString();
	}

	public static @NotNull <T extends Comparable<T>> Property<T> fromVicky(
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

    public static MutableComponent fromVicky(net.kyori.adventure.text.Component component) {
		String jsonString = GsonComponentSerializer.gson().serialize(component);
		MutableComponent forgeComponent = MutableComponent.Serializer.fromJson(jsonString);
		return forgeComponent != null ? forgeComponent : Component.empty();
    }

	public static boolean isNativeAttribute(PlatformEntityAttribute type) {
		if (type instanceof PlatformEntityAttribute.Inbuilt inbuilt)
			return switch (inbuilt.getType()) {
				case MAX_HEALTH,
					 ATTACK_DAMAGE,
					 ATTACK_SPEED,
					 ARMOR,
					 ARMOR_TOUGHNESS,
					 KNOCKBACK_RESISTANCE,
					 LUCK,
					 MOVEMENT_SPEED,
					 FLYING_SPEED,
					 FOLLOW_RANGE,
					 ATTACK_KNOCKBACK,
					 SPAWN_REINFORCEMENTS_CHANCE,
					 JUMP_STRENGTH-> true;
				default -> false;
			};

		return false;
	}

    public static Attribute fromVicky(PlatformEntityAttribute attribute) {
		if (attribute instanceof PlatformEntityAttribute.Inbuilt inbuilt) {
			return switch (inbuilt.getType()) {
                case MAX_HEALTH -> Attributes.MAX_HEALTH;
                case ATTACK_DAMAGE -> Attributes.ATTACK_DAMAGE;
                case ATTACK_SPEED -> Attributes.ATTACK_SPEED;
                case ARMOR -> Attributes.ARMOR;
                case ARMOR_TOUGHNESS -> Attributes.ARMOR_TOUGHNESS;
                case KNOCKBACK_RESISTANCE -> Attributes.KNOCKBACK_RESISTANCE;
                case LUCK -> Attributes.LUCK;
                case MOVEMENT_SPEED -> Attributes.MOVEMENT_SPEED;
                case FLYING_SPEED -> Attributes.FLYING_SPEED;
                case FOLLOW_RANGE -> Attributes.FOLLOW_RANGE;
                case ATTACK_KNOCKBACK -> Attributes.ATTACK_KNOCKBACK;
                case SPAWN_REINFORCEMENTS_CHANCE -> Attributes.SPAWN_REINFORCEMENTS_CHANCE;
                case JUMP_STRENGTH -> Attributes.JUMP_STRENGTH;
				default -> null;
            };
		}
		if (attribute instanceof PlatformEntityAttribute.Custom custom)
			return ForgeRegistries.ATTRIBUTES.getValue(fromVicky(custom.getId()));

		// should never reach here
		return null;
    }

	public static UUID getFromString(String determiner) {
		return UUID.nameUUIDFromBytes(
				determiner.getBytes(StandardCharsets.UTF_8)
		);
	}

    public static AttributeModifier.Operation fromVicky(ModifierOperation operation) {
        return switch (operation) {
            case ADD_VALUE -> AttributeModifier.Operation.ADDITION;
            case ADD_MULTIPLY_TOTAL -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            case ADD_MULTIPLY_BASE -> AttributeModifier.Operation.MULTIPLY_BASE;
        };
    }

	public static ResourceKey<CreativeModeTab> fromVicky(
            CreativeTabMenu.Inbuilt tab
    ) {
		if (tab instanceof CreativeTabMenu.Inbuilt.BuildingBlocks) {
			return CreativeModeTabs.BUILDING_BLOCKS;
		}

		if (tab instanceof CreativeTabMenu.Inbuilt.ColoredBlocks) {
			return CreativeModeTabs.COLORED_BLOCKS;
		}

		if (tab instanceof CreativeTabMenu.Inbuilt.NaturalBlocks) {
			return CreativeModeTabs.NATURAL_BLOCKS;
		}

		if (tab instanceof CreativeTabMenu.Inbuilt.FunctionalBlocks) {
			return CreativeModeTabs.FUNCTIONAL_BLOCKS;
		}

		if (tab instanceof CreativeTabMenu.Inbuilt.RedstoneBlocks) {
			return CreativeModeTabs.REDSTONE_BLOCKS;
		}

		if (tab instanceof CreativeTabMenu.Inbuilt.Tools) {
			return CreativeModeTabs.TOOLS_AND_UTILITIES;
		}

		if (tab instanceof CreativeTabMenu.Inbuilt.Combat) {
			return CreativeModeTabs.COMBAT;
		}

		if (tab instanceof CreativeTabMenu.Inbuilt.FoodAndDrinks) {
			return CreativeModeTabs.FOOD_AND_DRINKS;
		}

		if (tab instanceof CreativeTabMenu.Inbuilt.Ingredients) {
			return CreativeModeTabs.INGREDIENTS;
		}

		if (tab instanceof CreativeTabMenu.Inbuilt.SpawnEggs) {
			return CreativeModeTabs.SPAWN_EGGS;
		}

		if (tab instanceof CreativeTabMenu.Inbuilt.Operator) {
			return CreativeModeTabs.OP_BLOCKS;
		}

		return null;
	}

    public static InteractionHand interactionHand(EquipmentSlot data) {
        return switch (data) {
            case MAINHAND -> InteractionHand.MAIN_HAND;
            case OFFHAND -> InteractionHand.OFF_HAND;
            case FEET, LEGS, CHEST, HEAD -> null;
        };
    }

    public static double doubles(EquipmentSlot data) {
        return switch (data) {
            case MAINHAND -> 0.0;
            case OFFHAND -> 1.0;
            case FEET, LEGS, CHEST, HEAD -> 0.0;
        };
    }

    public static ItemRenderPerspective toVicky(ItemDisplayContext data) {
        return switch (data) {
            case NONE -> ItemRenderPerspective.NONE;
            case THIRD_PERSON_LEFT_HAND -> ItemRenderPerspective.THIRD_PERSON_LEFT_HAND;
            case THIRD_PERSON_RIGHT_HAND -> ItemRenderPerspective.THIRD_PERSON_RIGHT_HAND;
            case FIRST_PERSON_LEFT_HAND -> ItemRenderPerspective.FIRST_PERSON_LEFT_HAND;
            case FIRST_PERSON_RIGHT_HAND -> ItemRenderPerspective.FIRST_PERSON_RIGHT_HAND;
            case HEAD -> ItemRenderPerspective.HEAD;
            case GUI -> ItemRenderPerspective.GUI;
            case GROUND -> ItemRenderPerspective.GROUND;
            case FIXED -> ItemRenderPerspective.FIXED;
        };
    }
}
