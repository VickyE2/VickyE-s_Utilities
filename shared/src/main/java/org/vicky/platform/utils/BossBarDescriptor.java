package org.vicky.platform.utils;

import net.kyori.adventure.text.Component;
import org.vicky.platform.IColor;
import org.vicky.platform.defaults.BossBarOverlay;

import java.util.HashMap;
import java.util.Map;

public class BossBarDescriptor implements Cloneable {
    public Component title;
    public Component subTitle;
    public float progress;
    public IColor color;
    public BossBarOverlay overlay;
    public String context;
    private Map<String, Object> information = new HashMap<>();

    // ✅ New full constructor
    public BossBarDescriptor(
            Component title,
            Component subTitle,
            float progress,
            IColor color,
            BossBarOverlay overlay,
            String context,
            Map<String, Object> data
    ) {
        this.title = title;
        this.subTitle = subTitle;
        this.progress = progress;
        this.color = color;
        this.overlay = overlay;
        this.context = context;
        if (data != null) this.information.putAll(data);
    }

    // ✅ New full constructor
    public BossBarDescriptor(
            Component title,
            Component subTitle,
            float progress,
            IColor color,
            BossBarOverlay overlay,
            String context
    ) {
        this.title = title;
        this.subTitle = subTitle;
        this.progress = progress;
        this.color = color;
        this.overlay = overlay;
        this.context = context;
    }

    // ✅ Minimal constructor fallback for legacy
    public BossBarDescriptor(Component title) {
        this(title, null, 1.0f, null, null, "default", new HashMap<>());
    }

    // 🔁 Fluent API still supported
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

    // ✅ Getters
    public Component getTitle() {
        return title;
    }

    public Component getSubTitle() {
        return subTitle;
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

    public BossBarDescriptor(BossBarDescriptor other) {
        this.title = other.title;
        this.subTitle = other.subTitle;
        this.progress = other.progress;
        this.color = other.color;
        this.overlay = other.overlay;
        this.context = other.context;
        // shallow copy of map keys; DO NOT deep-clone arbitrary objects
        this.information = new HashMap<>(other.information);
    }

    public Map<String, Object> getInformation() {
        return information;
    }

    /**
     * Convenient explicit copy method — preferred over clone()
     */
    public BossBarDescriptor copy() {
        return new BossBarDescriptor(this);
    }

    // If you *must* keep clone() for compatibility:
    @Override
    public BossBarDescriptor clone() {
        return copy();
    }
}
