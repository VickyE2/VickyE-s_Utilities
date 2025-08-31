package org.vicky.forge.utilities;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.vicky.VickyUtilitiesForge;
import org.vicky.forge.forgeplatform.ForgePlatformConfig;

@Mod.EventBusSubscriber(modid = VickyUtilitiesForge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeModConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.BooleanValue DEBUG_MODE;

    static {
        DEBUG_MODE = BUILDER
                .comment("Enable debug mode")
                .define("general.debug_mode", false);

        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        ForgePlatformConfig.getInstance().syncFromForgeConfig();
    }
}