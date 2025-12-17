package org.vicky.forge.entity;

import de.pauleff.api.ICompoundTag;
import de.pauleff.core.*;
import net.minecraft.nbt.*;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.forgeplatform.ForgePlatformLocationAdapter;
import org.vicky.forge.forgeplatform.useables.ForgePlatformPlayer;
import org.vicky.forge.forgeplatform.useables.ForgePlatformWorldAdapter;
import org.vicky.forge.forgeplatform.useables.ForgeVec3;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.defaults.AABB;
import org.vicky.platform.entity.PlatformEntity;
import org.vicky.platform.utils.Direction;
import org.vicky.platform.utils.ResourceLocation;
import org.vicky.platform.utils.Vec3;
import org.vicky.platform.world.PlatformLocation;
import org.vicky.platform.world.PlatformWorld;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.vicky.forge.forgeplatform.useables.ForgeHacks.toVicky;

public class ForgePlatformEntity implements PlatformEntity {

    public final Entity ordinal;

    protected ForgePlatformEntity(Entity ordinal) {
        this.ordinal = ordinal;
    }

    public static ForgePlatformEntity from(Entity e) {
        return new ForgePlatformEntity(e);
    }

    @Override
    public @NotNull UUID getUuid() {
        return ordinal.getUUID();
    }

    @Override
    public @NotNull ResourceLocation getTypeId() {
        return toVicky(EntityType.getKey(ordinal.getType()));
    }

    @Override
    public @NotNull PlatformWorld<?, ?> getWorld() {
        return new ForgePlatformWorldAdapter(ordinal.level());
    }

    @Override
    public @NotNull PlatformLocation getLocation() {
        return ForgePlatformLocationAdapter.fromNativeS(
                new ForgeVec3(
                        ordinal.level(),
                        ordinal.blockPosition(),
                        ordinal.xRotO,
                        ordinal.yRotO
                )
        );
    }

    @Override
    public void teleport(@NotNull PlatformLocation platformLocation) {
        if (platformLocation instanceof ForgeVec3 i)
            if (i.getForgeWorld() instanceof ServerLevel l)
                ordinal.teleportTo(l, i.x, i.y, i.z
                        , Set.of(), ordinal.yRotO, ordinal.xRotO);
    }

    @Override
    public void remove() {
        ordinal.remove(Entity.RemovalReason.DISCARDED);
    }

    @Override
    public boolean isDead() {
        return !ordinal.isAlive();
    }

    @Override
    public @Nullable Vec3 getVelocity() {
        return Vec3.of(
                ordinal.getDeltaMovement().x,
                ordinal.getDeltaMovement().y,
                ordinal.getDeltaMovement().z
        );
    }

    @Override
    public void setVelocity(@Nullable Vec3 vec3) {
        if (vec3 != null)
            ordinal.setDeltaMovement(vec3.x, vec3.y, vec3.z);
    }

    @Override
    public void setRotation(float v, float v1) {
        ordinal.setXRot(v);
        ordinal.setYRot(v1);
    }

    @Override
    public float getYaw() {
        return ordinal.getXRot();
    }

    @Override
    public float getPitch() {
        return ordinal.getYRot();
    }

    // Make this a float
    @Override
    public float getEyeHeight() {
        return ordinal.getEyeHeight();
    }

    // Make use direction instead U, D, N, S, E, W
    @Override
    public @NotNull Direction getLookDirection() {
        return Direction.valueOf(ordinal.getDirection().getName());
    }

    @Override
    public @NotNull Vec3 getEyeLocation() {
        return Vec3.of(
                ordinal.getEyePosition().x,
                ordinal.getEyePosition().y,
                ordinal.getEyePosition().z
        );
    }

    @Override
    public void setGravity(boolean b) {
        ordinal.setNoGravity(!b);
    }

    @Override
    public void setInvisible(boolean b) {
        ordinal.setInvisible(b);
    }

    @Override
    public void setInvulnerable(boolean b) {
        ordinal.setInvulnerable(b);
    }

    @Override
    public void setCustomName(@NotNull String s) {
        ordinal.setCustomName(Component.literal(s));
    }

    @Override
    public @NotNull Optional<String> getCustomName() {
        if (ordinal.getCustomName() != null)
            return Optional.of(ordinal.getCustomName().getString());
        return Optional.empty();
    }

    // Make use de.pauleff.core.Tag<?>
    @Override
    public <T> void setPersistentData(@NotNull String s, @NotNull de.pauleff.core.Tag<T> o) {
        if (o.getData() instanceof String)
            ordinal.getPersistentData().putString(s, (String) o.getData());
        else if (o.getData() instanceof Integer)
            ordinal.getPersistentData().putInt(s, (Integer) o.getData());
        else if (o.getData() instanceof Float)
            ordinal.getPersistentData().putFloat(s, (Float) o.getData());
        else if (o.getData() instanceof Byte)
            ordinal.getPersistentData().putBoolean(s, (Boolean) o.getData());
        else if (o.getData() instanceof UUID)
            ordinal.getPersistentData().putUUID(s, (UUID) o.getData());
    }

    // Make use de.pauleff.core.Tag<?>
    @Override
    public @Nullable de.pauleff.core.Tag<?> getPersistentData(@NotNull String s) {
        if (ordinal.getPersistentData().get(s) != null) {
            if (ordinal.getPersistentData().get(s).getType() instanceof StringTag)
                return new Tag_String(s, ordinal.getPersistentData().getString(s));
            else if (ordinal.getPersistentData().get(s).getType() instanceof IntTag)
                return new Tag_Int(s, ordinal.getPersistentData().getInt(s));
            else if (ordinal.getPersistentData().get(s).getType() instanceof FloatTag)
                return new Tag_Float(s, ordinal.getPersistentData().getFloat(s));
            else if (ordinal.getPersistentData().get(s).getType() instanceof ByteTag)
                return new Tag_Byte(s, ordinal.getPersistentData().getByte(s));
            else if (ordinal.getPersistentData().get(s).getType() instanceof IntArrayTag)
                return new Tag_Int_Array(s, ordinal.getPersistentData().getIntArray(s));
        }
        return null;
    }

    // Should pass PlatformPlayer
    @Override
    public void interact(@NotNull PlatformPlayer player) {
        if (player instanceof ForgePlatformPlayer p)
            ordinal.interact(p.getHandle(), InteractionHand.MAIN_HAND);
    }

    @Override
    public @NotNull AABB getBoundingBox() {
        return new AABB(
            ordinal.getBoundingBox().minX,
            ordinal.getBoundingBox().minY,
            ordinal.getBoundingBox().minZ,
            ordinal.getBoundingBox().maxX,
            ordinal.getBoundingBox().maxY,
            ordinal.getBoundingBox().maxZ
        );
    }

    @Override
    public void setBoundingBox(@NotNull AABB aabb) {
        ordinal.setBoundingBox(net.minecraft.world.phys.AABB.ofSize(
                new net.minecraft.world.phys.Vec3(
                        aabb.getCenter().x,
                        aabb.getCenter().y,
                        aabb.getCenter().z
                ),
                aabb.getWidth(),
                aabb.getHeight(),
                aabb.getDepth()
        ));
    }

    @Override
    public @NotNull Entity getHandle() {
        return ordinal;
    }

    @Override
    public boolean isPlayer() {
        return ordinal instanceof Player;
    }
}
