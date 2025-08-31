package org.vicky.forge.forgeplatform.useables;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import org.vicky.platform.world.PlatformBlockState;
import org.vicky.platform.world.PlatformMaterial;

import java.util.Map;
import java.util.stream.Collectors;

public record ForgePlatformBlockStateAdapter(BlockState state) implements PlatformBlockState<BlockState> {

    @Override
    public String getId() {
        return ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
    }

    @Override
    public PlatformMaterial getMaterial() {
        return new ForgePlatformMaterial(state.getBlock());
    }

    @Override
    public BlockState getNative() {
        return state;
    }

    @Override
    public Map<String, String> getProperties() {
        return state.getProperties().stream()
                .collect(Collectors.toMap(
                        Property::getName,
                        property -> state.getValue(property).toString()
                ));
    }

    @Override
    public <P> P getProperty(String name) {
        return (P) state.getProperties().stream()
                .filter(property -> property.getName().equals(name))
                .limit(1)
                .map(property -> state.getValue(property));
    }
}
