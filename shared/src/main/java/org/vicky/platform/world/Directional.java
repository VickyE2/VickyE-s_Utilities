package org.vicky.platform.world;

import org.vicky.platform.utils.Direction;

public interface Directional {
    Direction getFacing();

    void setFacing(Direction direction);
}
