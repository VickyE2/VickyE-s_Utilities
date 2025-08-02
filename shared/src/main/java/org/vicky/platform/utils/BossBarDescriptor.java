package org.vicky.platform.utils;

import net.kyori.adventure.text.Component;
import org.vicky.platform.IColor;
import org.vicky.platform.defaults.BossBarOverlay;

import java.util.HashMap;
import java.util.Map;

public class BossBarDescriptor implements Cloneable {
    private Component title;
    private Component subTitle;
    private float progress;
    private IColor color;
    private BossBarOverlay overlay;
    private String context;
    private Map<String, Object> information = new HashMap<>();

    public BossBarDescriptor(Component title) {
        this.title = title;
    }

    public BossBarDescriptor title(Component title) {
        this.title = title;
        return this;
    }

    public BossBarDescriptor progress(float progress) {
        this.progress = progress;
        return this;
    }

    public BossBarDescriptor subTitle(Component subTitle) {
        this.subTitle = subTitle;
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

    public BossBarDescriptor addData(String key, Object value) {
        this.information.put(key, value);
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

    public Map<String, Object> getInformation() {
        return new HashMap<>(information);
    }

    @Override
    public BossBarDescriptor clone() {
        try {
            BossBarDescriptor clone = (BossBarDescriptor) super.clone();
            clone.information = new HashMap<>(this.information);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}