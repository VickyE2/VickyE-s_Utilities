package org.vicky.platform.entity;

public interface PlatformParticle {
    String getId(); // used for mapping to native particle
    boolean supportsColor();
    boolean supportsTransition();
    // maybe more depending on feature set
}
