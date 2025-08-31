package org.vicky.forge.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static org.vicky.forge.client.ClientIncomingPacketHandler.activeBars;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientRenderHandler {
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Pre event) {
        activeBars.values().forEach(bar -> bar.render(event.getGuiGraphics(), 0, 0, event.getPartialTick()));
    }
}