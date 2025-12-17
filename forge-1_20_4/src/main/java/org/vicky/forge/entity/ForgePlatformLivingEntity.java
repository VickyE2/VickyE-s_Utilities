package org.vicky.forge.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.entity.navigation.ForgePlatformNavigator;
import org.vicky.forge.forgeplatform.useables.ForgePlatformItem;
import org.vicky.forge.forgeplatform.useables.ForgePlatformPlayer;
import org.vicky.platform.PlatformItem;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.entity.*;

import java.util.HashMap;
import java.util.Map;

public class ForgePlatformLivingEntity extends ForgePlatformEntity implements PlatformLivingEntity {

    public final LivingEntity ordinal;

    protected ForgePlatformLivingEntity(LivingEntity ordinal) {
        super(ordinal);
        this.ordinal = ordinal;
    }

    public static ForgePlatformLivingEntity from(LivingEntity e) {
        return new ForgePlatformLivingEntity(e);
    }

    @Override
    public float getHealth() {
        return ordinal.getHealth();
    }

    @Override
    public void setHealth(float v) {
        ordinal.setHealth(v);
    }

    @Override
    public float getAbsorption() {
        return ordinal.getAbsorptionAmount();
    }

    @Override
    public void setAbsorption(float v) {
        ordinal.setAbsorptionAmount(v);
    }

    @Override
    public float getMaxHealth() {
        return ordinal.getMaxHealth();
    }

    @Override
    public float getMaxAbsorption() {
        return ordinal.getMaxAbsorption();
    }

    // Make double
    @Override
    public double getLookDistance() {
        if (ordinal instanceof Mob mob)
            return mob.getAttribute(Attributes.FOLLOW_RANGE) != null ?
                    mob.getAttribute(Attributes.FOLLOW_RANGE).getValue() : 16f;
        return 16f;
    }

    @Override
    public void hurt(float v, @NotNull AntagonisticDamageSource antagonisticDamageSource) {
        ordinal.hurt(ForgeDamageSource.from(ordinal, antagonisticDamageSource), v);
    }

    @Override
    public void die(@NotNull AntagonisticDamageSource antagonisticDamageSource) {
        ordinal.die(ForgeDamageSource.from(ordinal, antagonisticDamageSource));
    }

    @Override
    public void heal(float v) {
        ordinal.heal(v);
    }

    @Override
    public @Nullable Double getAttributeBaseValue(@NotNull String s) {
        Attribute pos = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.parse(s));
        if (pos != null)
            if (ordinal.getAttribute(pos) != null)
                return ordinal.getAttribute(pos).getBaseValue();

        return null;
    }

    @Override
    public @Nullable Double getAttributeValue(@NotNull String s) {
        return 0.0;
    }

    @Override
    public void setAttributeBaseValue(@NotNull String s, double v) {
        Attribute pos = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.parse(s));
        if (pos != null)
            if (ordinal.getAttribute(pos) != null)
                ordinal.getAttribute(pos).setBaseValue(v);
    }

    @Override
    public void setLookDistance(double v) {

    }

    @Override
    public @NotNull Map<String, Double> getAttributes() {
        return new HashMap<>();
    }

    @Override
    public void increaseAirSupply(int v) {
        ordinal.setAirSupply(ordinal.getAirSupply() + v);
    }

    @Override
    public void decreaseAirSupply(int v) {
        ordinal.setAirSupply(ordinal.getAirSupply() - v);
    }

    @Override
    public int getAirSupply() {
        return ordinal.getAirSupply();
    }

    @Override
    public void setAirSupply(int i) {
        ordinal.setAirSupply(i);
    }

    @Override
    public void setSpeed(float v) {
        ordinal.setSpeed(v);
    }

    @Override
    public float getSpeed() {
        return ordinal.getSpeed();
    }

    @Override
    public boolean isOnGround() {
        return ordinal.onGround();
    }

    @Override
    public boolean isInWater() {
        return ordinal.isInWater();
    }

    @Override
    public @Nullable PathNavigator getNavigator() {
        if (ordinal instanceof PathfinderMob p)
            return ForgePlatformNavigator.from(p.getNavigation());
        return null;
    }

    @Override
    public @Nullable PlatformLivingEntity getLastAttacker() {
        return ForgePlatformLivingEntity.from(ordinal.getLastAttacker());
    }

    @Override
    public @Nullable PlatformLivingEntity getLastHurtByMob() {
        return ForgePlatformLivingEntity.from(ordinal.getLastHurtByMob());
    }

    @Override
    public void setLastHurtByMob(@Nullable PlatformLivingEntity platformLivingEntity) {
        if (platformLivingEntity instanceof ForgePlatformLivingEntity l)
            ordinal.setLastHurtByMob(l.ordinal);
    }

    @Override
    public void setLastHurtByPlayer(@Nullable PlatformPlayer platformPlayer) {
        if (platformPlayer instanceof ForgePlatformPlayer p)
            ordinal.setLastHurtByPlayer(p.getHandle());
    }

    @Override
    public @Nullable PlatformLivingEntity getLastHurtMob() {
        return ForgePlatformLivingEntity.from(ordinal.getLastHurtMob());
    }

    @Override
    public void setLastHurtMob(@Nullable PlatformLivingEntity platformLivingEntity) {
        if (platformLivingEntity instanceof ForgePlatformLivingEntity l)
            ordinal.setLastHurtMob(l.ordinal);
    }

    @Override
    public @Nullable PlatformItem getOffhandItem() {
        return new ForgePlatformItem(ordinal.getOffhandItem());
    }

    @Override
    public @Nullable PlatformItem getMainHandItem() {
        return new ForgePlatformItem(ordinal.getMainHandItem());
    }

    @Override
    public void setItemSlot(@NotNull EquipmentSlot equipmentSlot, @NotNull PlatformItem platformItem) {
        if (platformItem instanceof ForgePlatformItem i)
            ordinal.setItemSlot(net.minecraft.world.entity.EquipmentSlot.valueOf(equipmentSlot.name()), i.item());
    }

    @Override
    public @Nullable PlatformItem getItemBySlot(@NotNull EquipmentSlot equipmentSlot) {
        return new ForgePlatformItem(ordinal.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.valueOf(equipmentSlot.name())));
    }

    @Override
    public boolean hasItemInSlot(@NotNull EquipmentSlot equipmentSlot) {
        return ordinal.hasItemInSlot(net.minecraft.world.entity.EquipmentSlot.valueOf(equipmentSlot.name()));
    }

    @Override
    public boolean isHolding(@NotNull PlatformItem platformItem) {
        if (platformItem instanceof ForgePlatformItem i)
            return ordinal.isHolding(i.item().getItem());
        return false;
    }

    @Override
    public boolean isAffectedByPotions() {
        return ordinal.isAffectedByPotions();
    }

    @Override
    public boolean hasLineOfSight(@NotNull PlatformEntity platformEntity) {
        if (platformEntity instanceof ForgePlatformEntity e)
            return ordinal.hasLineOfSight(e.ordinal);
        return false;
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return ordinal.isCurrentlyGlowing();
    }

    @Override
    public boolean isPushable() {
        return ordinal.isPushable();
    }

    @Override
    public boolean isPickable() {
        return ordinal.isPickable();
    }

    @Override
    public boolean isSensitiveToWater() {
        return ordinal.isSensitiveToWater();
    }

    @Override
    public boolean isInvertedHealAndHarm() {
        return ordinal.isInvertedHealAndHarm();
    }

    @Override
    public boolean isBaby() {
        return ordinal.isBaby();
    }

    @Override
    public boolean isOnFire() {
        return ordinal.isOnFire();
    }

    @Override
    public boolean isSprinting() {
        return ordinal.isSprinting();
    }

    @Override
    public boolean isSneaking() {
        return ordinal.isCrouching();
    }

    @Override
    public boolean getShouldDropExperience() {
        return ordinal.shouldDropExperience();
    }

    @Override
    public boolean getCanBreatheUnderwater() {
        return ordinal.canBreatheUnderwater();
    }

    @Override
    public boolean getCanDisableShield() {
        return ordinal.canDisableShield();
    }

    @Override
    public boolean getCanChangeDimensions() {
        return ordinal.canChangeDimensions();
    }

    @Override
    public boolean getCanBeSeenByAnyone() {
        return ordinal.canBeSeenByAnyone();
    }

    @Override
    public boolean getCanFreeze() {
        return ordinal.canFreeze();
    }
}
