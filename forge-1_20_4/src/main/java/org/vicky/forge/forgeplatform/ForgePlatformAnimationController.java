/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform;

import com.eliotlash.mclib.math.Variable;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.network.PacketHandler;
import org.vicky.forge.network.registeredpackets.PauseAnimationPacket;
import org.vicky.forge.network.registeredpackets.PlayAnimationPacket;
import org.vicky.forge.network.registeredpackets.ResumeAnimationPacket;
import org.vicky.forge.network.registeredpackets.StopAnimationPacket;
import org.vicky.platform.entity.PlatformAnimationController;
import org.vicky.platform.entity.VariableProvider;
import org.vicky.platform.items.Animation;
import software.bernie.geckolib.core.molang.MolangParser;

public class ForgePlatformAnimationController implements PlatformAnimationController {
	private final Entity ordinal;
	private ForgePlatformAnimationController(Entity e) {
		this.ordinal = e;
	}

	public static ForgePlatformAnimationController from(Entity e) {
		return new ForgePlatformAnimationController(e);
	}

	@Override
	public void play(@NotNull Animation animation) {
		var pkt = new PlayAnimationPacket(ordinal.getId(), animation);
		PacketHandler.MAIN_CHNNEL.send(pkt, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(ordinal));
	}

	@Override
	public @Nullable String getCurrentAnimation() {
		return "";
	}

	@Override
	public @NotNull org.vicky.platform.entity.VariableProvider getProvider() {
		return new VariableProvider() {
			@Override
			public void register(@NotNull String s) {
				MolangParser.INSTANCE.register(new Variable(s, 0));
			}

			@Override
			public void registerOrSet(@NotNull String s, double o) {
				if (MolangParser.INSTANCE.getVariable(s) == null) {
					MolangParser.INSTANCE.register(new Variable(s, 0));
				}

				MolangParser.INSTANCE.setValue(s, () -> o);
			}

			@Override
			public @Nullable Double get(@NotNull String s) {
				if (MolangParser.INSTANCE.getVariable(s) == null)
					return null;

				return MolangParser.INSTANCE.getVariable(s).get();
			}
		};
	}

	@Override
	public void stop(@Nullable String animation) {
        var pkt = new StopAnimationPacket(ordinal.getId(), animation);
        PacketHandler.MAIN_CHNNEL.send(pkt, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(ordinal));
	}

	@Override
	public void pause() {
        var pkt = new PauseAnimationPacket(ordinal.getId());
        PacketHandler.MAIN_CHNNEL.send(pkt, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(ordinal));
	}

	@Override
	public void resume() {
        var pkt = new ResumeAnimationPacket(ordinal.getId());
        PacketHandler.MAIN_CHNNEL.send(pkt, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(ordinal));
	}

	@Override
	public boolean isPlaying(@Nullable String s) {
		return false;
	}
}
