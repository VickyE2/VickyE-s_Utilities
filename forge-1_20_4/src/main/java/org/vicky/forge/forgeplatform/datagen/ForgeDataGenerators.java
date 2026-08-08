package org.vicky.forge.forgeplatform.datagen;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static org.vicky.forge.VickyUtilitiesForge.MODID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ForgeDataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
                event.includeClient(),
                new DescriptorItemModelProvider(
                        event.getGenerator().getPackOutput(),
                        MODID,
                        event.getExistingFileHelper()
                )
        );
    }
}