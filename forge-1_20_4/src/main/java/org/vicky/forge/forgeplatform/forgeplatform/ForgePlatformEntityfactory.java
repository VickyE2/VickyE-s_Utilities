package org.vicky.forge.forgeplatform.forgeplatform;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import org.vicky.forge.forgeplatform.forgeplatform.useables.ForgeVec3;
import org.vicky.platform.PlatformEntityFactory;
import org.vicky.platform.entity.PlatformArrow;
import org.vicky.platform.utils.Vec3;
import org.vicky.platform.world.PlatformLocation;

public class ForgePlatformEntityfactory implements PlatformEntityFactory {
    @Override
    public PlatformArrow spawnArrowAt(PlatformLocation loc) {
        if (!(loc instanceof ForgeVec3 location)) throw new IllegalArgumentException("Expected ForgeVec3 got abstract");
        Level world = location.getForgeWorld();
        Arrow arrow = EntityType.ARROW.create(world);
        if (arrow != null) {
            arrow.setPos(loc.x, loc.y, loc.z);
            return new ForgePlatformArrow(arrow);
        }
        return null;
    }

    static class ForgePlatformArrow implements PlatformArrow {
        private final Arrow arrow;

        public ForgePlatformArrow(Arrow arrow) {
            this.arrow = arrow;
        }

        @Override
        public void setGravity(boolean gravity) {
            arrow.setNoGravity(!gravity);
        }

        @Override
        public void remove() {
            arrow.remove(Entity.RemovalReason.KILLED);
        }

        @Override
        public void setCustomName(String name) {
            arrow.setCustomName(Component.literal(name));
        }

        @Override
        public void teleport(double x, double y, double z) {
            arrow.setPos(x, y, z);
        }

        @Override
        public boolean isValid() {
            return arrow.isAlive();
        }

        @Override
        public Object getHandle() {
            return arrow;
        }

        @Override
        public float getYaw() {
            return arrow.yRotO;
        }

        @Override
        public float getPitch() {
            return arrow.xRotO;
        }

        @Override
        public boolean isDead() {
            return arrow.isAlive();
        }

        @Override
        public PlatformLocation getLocation() {
            return new ForgeVec3(arrow.level(), arrow.xo, arrow.yo, arrow.zo, arrow.yRotO, arrow.xRotO);
        }

        @Override
        public Vec3 getVelocity() {
            return new Vec3(arrow.getForward().x, arrow.getForward().y, arrow.getForward().z);
        }
    }
}
