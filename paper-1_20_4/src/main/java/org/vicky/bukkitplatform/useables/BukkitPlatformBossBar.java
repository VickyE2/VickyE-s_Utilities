/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import static org.vicky.bukkitplatform.useables.BukkitBossBarDescriptor.mapToBarStyle;

import org.bukkit.boss.BossBar;
import org.vicky.bukkitplatform.BukkitBossBarFactory;
import org.vicky.platform.IColor;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.defaults.BossBarOverlay;
import org.vicky.platform.utils.BossBarDescriptor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class BukkitPlatformBossBar implements PlatformBossBar {
	private final BossBar bar;
    private BossBarDescriptor descriptor;

    public BukkitPlatformBossBar(BossBar bar, BossBarDescriptor descriptor) {
		this.bar = bar;
        this.descriptor = descriptor;
	}

	@Override
	public void setProgress(Float progress) {
        bar.setProgress(progress);
	}

	@Override
	public void setVisible(Boolean visible, PlatformPlayer player) {
		if (visible) {
            bar.addPlayer(((BukkitPlatformPlayer) player).getBukkitPlayer());
		} else {
            bar.removePlayer(((BukkitPlatformPlayer) player).getBukkitPlayer());
		}
	}

	@Override
    public boolean isVisible(PlatformPlayer platformPlayer) {
        return bar.isVisible();
    }

    @Override
    public void setColor(IColor iColor) {
        bar.setColor(BukkitBossBarFactory.adaptColor(iColor));
	}

	@Override
	public void setOverlay(BossBarOverlay overlay) {
        bar.setStyle(BukkitBossBarFactory.adaptOverlay(overlay));
	}

	@Override
	public void setTitle(Component title) {
        bar.setTitle(PlainTextComponentSerializer.plainText().serialize(title));
	}

	@Override
	public void addViewer(PlatformPlayer player) {
        bar.addPlayer(((BukkitPlatformPlayer) player).getBukkitPlayer());
	}

	@Override
	public void removeViewer(PlatformPlayer player) {
        bar.removePlayer(((BukkitPlatformPlayer) player).getBukkitPlayer());
	}

	@Override
	public void hideAll() {
        bar.removeAll();
    }

    @Override
    public BossBarDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void setDescriptor(BossBarDescriptor bossBarDescriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public void updateFromDescriptor() {
        bar.setColor(BukkitBossBarFactory.adaptColor(descriptor.color));
        bar.setProgress(descriptor.progress);
        bar.setStyle(mapToBarStyle(descriptor.getOverlay()));
        bar.setTitle(PlainTextComponentSerializer.plainText().serialize(descriptor.getTitle()));
        bar.setVisible(true);
	}

	public BossBar getBossBar() {
		return bar;
	}
}
