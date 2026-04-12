package org.vicky.platform.utils.defaultproperties;

import org.vicky.platform.utils.Property;

import java.util.Arrays;
import java.util.Collection;

public final class BooleanProperty implements Property<Boolean> {

    private static final Collection<Boolean> BOOLEANS =
            Arrays.asList(true, false);
    private final String name;

    public BooleanProperty(String name) {
        this.name = name;
    }

    @Override public Collection<Boolean> values() {
        return BOOLEANS;
    }

    @Override public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public String getIdentifier() {
        return name;
    }
}
