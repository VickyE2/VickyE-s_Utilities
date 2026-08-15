package org.vicky.forge.entity.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.jetbrains.annotations.NotNull;
import org.vicky.forge.entity.ForgePlatformEntity;
import org.vicky.forge.forgeplatform.useables.ForgeHacks;
import org.vicky.platform.entity.*;
import org.vicky.platform.utils.Vec3;

public record ForgeMobControls(Mob mob) implements PlatformMobControls {

    @Override
    public @NotNull PlatformLookControl getLookControl() {
        return new ForgeLookControl(mob.getLookControl());
    }

    @Override
    public @NotNull PlatformMoveControl getMoveControl() {
        return new ForgeMoveControl(mob.getMoveControl());
    }

    @Override
    public @NotNull PlatformJumpControl getJumpControl() {
        return new ForgeJumpControl(mob.getJumpControl());
    }

    public record ForgeJumpControl(JumpControl jumpControl) implements PlatformJumpControl {
        @Override
        public void jump() {
            jumpControl.jump();
        }

        @Override
        public boolean isJumping() {
            return false;
        }
    }

    public record ForgeMoveControl(MoveControl moveControl) implements PlatformMoveControl {

        @Override
        public void setWantedPosition(double v, double v1, double v2, double v3) {
            moveControl.setWantedPosition(v, v1, v2, v3);
        }

        @Override
        public void strafe(float v, float v1) {
            moveControl.strafe(v, v1);
        }

        @Override
        public boolean hasWanted() {
            return moveControl.hasWanted();
        }

        @Override
        public double getSpeedModifier() {
            return moveControl.getSpeedModifier();
        }

        @Override
        public float getStrafeForwards() {
            return 0;
        }

        @Override
        public float getStrafeRight() {
            return 0;
        }
    }

    public record ForgeLookControl(LookControl control) implements PlatformLookControl {

        @Override
        public boolean isLookingAtTarget() {
            return control.isLookingAtTarget();
        }

        @Override
        public void setLookAt(double v, double v1, double v2) {
            control.setLookAt(v, v1, v2);
        }

        @Override
        public void setLookAt(@NotNull Vec3 vec3) {
            control.setLookAt(ForgeHacks.fromVicky(vec3));
        }

        @Override
        public void setLookAt(@NotNull PlatformEntity platformEntity) {
            if (platformEntity instanceof ForgePlatformEntity entity)
                control.setLookAt(entity.ordinal);
        }

        @Override
        public void setLookAt(double v, double v1, double v2, float v3, float v4) {
            control.setLookAt(v, v1, v2, v3, v4);
        }

        @Override
        public void setLookAt(@NotNull PlatformEntity platformEntity, float v, float v1) {
            if (platformEntity instanceof ForgePlatformEntity entity)
                control.setLookAt(entity.ordinal, v, v1);
        }

        @Override
        public float getYRotD() {
            return 0;
        }

        @Override
        public float getXRotD() {
            return 0;
        }
    }
}
