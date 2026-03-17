package org.vicky.forge.entity.navigation;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.vicky.platform.entity.MovementMode;

import net.minecraft.world.entity.Mob;

public final class MovementModeResolver {

    private MovementModeResolver() {}

    public static @NotNull MovementMode resolve(
            @NotNull Mob mob,
            @NotNull Set<MovementMode> allowed
    ) {

        // 🌊 WATER LOGIC
        if (mob.isInWater() || mob.isInLava()) {
            if (allowed.contains(MovementMode.SWIMMING)) {
                return MovementMode.SWIMMING;
            }
            if (allowed.contains(MovementMode.AMPHIBIOUS)) {
                return MovementMode.AMPHIBIOUS;
            }
        }

        // 🧗 CLIMBING LOGIC
        if (mob.horizontalCollision && !mob.onGround()) {
            if (allowed.contains(MovementMode.CLIMBING)) {
                return MovementMode.CLIMBING;
            }
        }

        // 🪽 AIR / FALLING LOGIC
        if (!mob.onGround() && mob.getDeltaMovement().y < -0.1) {
            if (allowed.contains(MovementMode.FLYING)) {
                return MovementMode.FLYING;
            }
        }

        // 🐾 DEFAULT GROUND
        if (allowed.contains(MovementMode.GROUND)) {
            return MovementMode.GROUND;
        }

        // 🧠 LAST RESORT
        return allowed.iterator().next();
    }
}