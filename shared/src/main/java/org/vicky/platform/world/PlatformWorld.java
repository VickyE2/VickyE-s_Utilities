package org.vicky.platform.world;

public interface PlatformWorld {
    String getName(); // optional identifier
    Object getNative(); // underlying platform world object

    int getHighestBlockYAt(double x, double z);

    PlatformBlock getBlockAt(double x, double y, double z);
}