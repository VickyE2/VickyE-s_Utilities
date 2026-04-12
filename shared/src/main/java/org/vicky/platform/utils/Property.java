package org.vicky.platform.utils;

import org.vicky.utilities.Identifiable;

import java.util.Collection;

public interface Property<T> extends Identifiable {
    Collection<T> values();
    Class<T> getType();
    default boolean isValid(T value) {
        return values().contains(value);
    }
}
