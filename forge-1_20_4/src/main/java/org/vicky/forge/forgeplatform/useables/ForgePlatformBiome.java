package org.vicky.forge.forgeplatform.useables;

import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import org.vicky.platform.utils.ComponentUtil;
import org.vicky.platform.utils.ResourceLocation;
import org.vicky.platform.world.PlatformBiome;

public record ForgePlatformBiome(Biome delegate) implements PlatformBiome {
    @Override
    public ResourceLocation id() {
        return ForgeHacks.toVicky(ForgeRegistries.BIOMES.getKey(delegate));
    }

    @Override
    public Component displayName() {
        return ComponentUtil.createTranslated(Util.makeDescriptionId("biome",
                ForgeRegistries.BIOMES.getKey(delegate)));
    }

    @Override
    public float temperature() {
        return delegate.getBaseTemperature();
    }

    @Override
    public float downfall() {
        return delegate.hasPrecipitation() ? 1 : 0;
    }

    @Override
    public boolean hasPrecipitation() {
        return delegate.hasPrecipitation();
    }

    @Override
    public int fogColor() {
        return delegate.getFogColor();
    }

    @Override
    public int waterColor() {
        return delegate.getWaterColor();
    }

    @Override
    public int waterFogColor() {
        return delegate.getWaterFogColor();
    }

    @Override
    public int skyColor() {
        return delegate.getSkyColor();
    }
}
