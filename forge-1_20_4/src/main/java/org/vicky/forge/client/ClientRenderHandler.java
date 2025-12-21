/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.client;

import static org.vicky.forge.client.ClientIncomingPacketHandler.activeBars;

import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientRenderHandler {
	@SubscribeEvent
	public static void onRenderOverlay(RenderGuiEvent.Pre event) {
		if (activeBars.isEmpty())
			return;
		Minecraft mc = Minecraft.getInstance();
		int sw = mc.getWindow().getGuiScaledWidth();
		int sh = mc.getWindow().getGuiScaledHeight();

		// draw each bar at a fixed area — for example bottom center offset
		int x = sw / 2;
		AtomicInteger y = new AtomicInteger(sh - 40); // 40 px above bottom

		activeBars.values().forEach(bar -> {
			try {
				bar.render(event.getGuiGraphics(), x, y.get(), event.getPartialTick());
			} catch (Throwable t) {
				t.printStackTrace();
			}
			y.addAndGet(-12); // move next bar up 12 px
		});
	}
}