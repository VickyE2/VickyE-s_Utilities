package org.vicky.platform.world;

import org.jetbrains.annotations.NotNull;
import org.vicky.platform.utils.Property;

import java.util.Map;
import java.util.function.Consumer;

public interface PlatformBlockState<N> {
    /**
     * Returns the string identifier of the block type.
     * For example: "minecraft:stone", "minecraft:oak_log"
     */
    @NotNull
    String getId();

    @NotNull
    PlatformMaterial getMaterial();

    @NotNull
    N getNative();

    default String getAsString() {
        return this.getAsString(true);
    }

    String getAsString(boolean var1);

    boolean matches(PlatformBlockState<N> var1);

    <T extends Comparable<T>> boolean has(Property<T> property);
    <T extends Comparable<T>> T get(Property<T> property);
    <T extends Comparable<T>> PlatformBlockState<N> set(Property<T> property, T value);
    default <T extends Comparable<T>> PlatformBlockState<N> setIfPresent(Property<T> property, T value) {
        return has(property) ? set(property, value) : this;
    }

    default <T extends Comparable<T>> PlatformBlockState<N> ifProperty(
            Property<T> property,
            java.util.function.Function<PlatformBlockState<N>, PlatformBlockState<N>> action
    ) {
        return has(property) ? action.apply(this) : this;
    }
}
