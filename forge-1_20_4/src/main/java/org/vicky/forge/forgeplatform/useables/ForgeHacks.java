/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform.useables;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;
import org.vicky.platform.items.EventResult;
import org.vicky.platform.items.InteractionHand;
import org.vicky.platform.utils.IntVec3;
import org.vicky.platform.world.PlatformLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
}
