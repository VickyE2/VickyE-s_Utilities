/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.utilities;

import java.util.concurrent.CompletableFuture;

import org.vicky.VickyUtilitiesForge;
import org.vicky.forge.forgeplatform.ForgePlatformBlockStateFactory;

import net.minecraft.core.HolderLookup;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VickyUtilitiesForge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeModDataGen {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		CompletableFuture<HolderLookup.Provider> providerFuture = event.getLookupProvider();
		providerFuture.thenAccept(ForgePlatformBlockStateFactory::setLookupProvider);
	}
}
