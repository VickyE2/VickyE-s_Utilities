package org.vicky.platform;

import net.kyori.adventure.text.Component;
import org.vicky.platform.utils.ResourceLocation;
import org.vicky.utilities.Identifiable;
import org.vicky.utilities.Pair;

import java.util.Map;

public interface PlatformItemStack extends Identifiable {
    Component getName();
    void setName(Component name);
    ResourceLocation getDescriptorId();
    void setCount(int count);
    int getCount();
    void applyNbt(Map<String, ?> nbt);
    void applyNbt(Pair<String, ?> nbt);
    boolean hasNbt(String key);
    Object getNbt(String key);
    PlatformItemStack copy();
}
