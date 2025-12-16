package org.vicky.platform.utils;

public enum Direction {
    NORTH(Vec3.of(0, 0, 1)),
    SOUTH(Vec3.of(0, 0, -1)),
    EAST(Vec3.of(1, 0, 0)),
    WEST(Vec3.of(-1, 0, 0)),
    UP(Vec3.of(0, 1, 0)),
    DOWN(Vec3.of(0, -1, 0));

    public final Vec3 dir;

    Direction(Vec3 dir) {
        this.dir = dir;
    }
}