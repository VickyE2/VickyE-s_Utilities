package org.vicky.platform.utils.defaultproperties;

import org.vicky.platform.utils.Property;

import java.util.Collection;
import java.util.List;

public final class EnumProperty<T extends Enum<T> & Comparable<T>> implements Property<T> {

    private final String name;
    private final Class<T> type;
    private final List<T> values;

    public EnumProperty(String name, Class<T> type) {
        this.name = name;
        this.type = type;
        this.values = List.of(type.getEnumConstants());
    }

    @Override public Collection<T> values() { return values; }

    @Override public Class<T> getType() { return type; }

    @Override
    public String getIdentifier() {
        return name;
    }
}
