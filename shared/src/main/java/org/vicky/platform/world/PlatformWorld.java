package org.vicky.platform.world;

public interface PlatformWorld {
    String getName(); // optional identifier
    Object getNative(); // underlying platform world object
}