package org.vicky.platform.utils.defaultproperties;

import org.vicky.platform.utils.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class IntegerProperty implements Property<Integer> {

    private final String name;
    private final int min;
    private final int max;

    public IntegerProperty(String name, int min, int max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }

    @Override public Collection<Integer> values() {
        List<Integer> vals = new ArrayList<>();
        for (int i = min; i <= max; i++) vals.add(i);
        return vals;
    }

    @Override public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public String getIdentifier() {
        return name;
    }
}
