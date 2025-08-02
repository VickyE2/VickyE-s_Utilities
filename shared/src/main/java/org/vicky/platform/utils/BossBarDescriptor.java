package org.vicky.platform.utils;

import net.kyori.adventure.text.Component;
import org.vicky.platform.IColor;
import org.vicky.platform.defaults.BossBarOverlay;

public class BossBarDescriptor {
    private Component title;
    private float progress;
    private IColor color;
    private BossBarOverlay overlay;
    private String context;

    public BossBarDescriptor(Component title) {
        this.title = title;
    }

    public BossBarDescriptor progress(float progress) {
        this.progress = progress;
        return this;
    }

    public BossBarDescriptor color(IColor color) {
        this.color = color;
        return this;
    }

    public BossBarDescriptor overlay(BossBarOverlay overlay) {
        this.overlay = overlay;
        return this;
    }

    public BossBarDescriptor context(String context) {
        this.context = context;
        return this;
    }

    // Getters
    public Component getTitle() {
        return title;
    }

    public float getProgress() {
        return progress;
    }

    public IColor getColor() {
        return color;
    }

    public BossBarOverlay getOverlay() {
        return overlay;
    }

    public String getContext() {
        return context;
    }
}