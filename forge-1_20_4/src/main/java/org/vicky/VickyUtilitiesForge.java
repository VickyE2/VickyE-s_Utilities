package org.vicky;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.vicky.forgeplatform.*;
import org.vicky.utilities.ANSIColor;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.ForgeModConfig;
import org.vicky.platform.*;
import org.vicky.forgeplatform.useables.ForgePlatformPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(VickyUtilitiesForge.MODID)
public class VickyUtilitiesForge implements PlatformPlugin {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "v_utils";
    public static MinecraftServer server;
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ContextLogger CONTEXT_LOGGER =
            new ContextLogger(ContextLogger.ContextType.SYSTEM, "V-UTLS");
    // Create a Deferred Register to hold Blocks which will all be registered under the "v_utils" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "v_utils" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "v_utils" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new Block with the id "v_utils:example_block", combining the namespace and path
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    // Creates a new BlockItem with the id "v_utils:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    // Creates a new food item with the id "v_utils:example_id", nutrition 1 and saturation 2
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEat().nutrition(1).saturationMod(2f).build())));

    // Creates a creative tab with the id "v_utils:example_tab" for the example item, that is placed after the combat tab
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
            output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());

    public VickyUtilitiesForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ForgeModConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        CONTEXT_LOGGER.print(ANSIColor.colorizeMixed("""
					gradient-10deg-right-#AA0000-#DDDD00[
					 _  _  __  ___  __ _  _  _  _ ____    _  _  ____  __  __    __  ____  __  ____  ____
					/ )( \\(  )/ __)(  / )( \\/ )(// ___)  / )( \\(_  _)(  )(  )  (  )(_  _)(  )(  __)/ ___)
					\\ \\/ / )(( (__  )  (  )  /   \\___ \\  ) \\/ (  )(   )( / (_/\\ )(   )(   )(  ) _) \\___ \\
					 \\__/ (__)\\___)(__\\_)(__/    (____/  \\____/ (__) (__)\\____/(__) (__) (__)(____)(____/]
					                                                                         dark_gray[0.0.1-BETA]"""));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(EXAMPLE_BLOCK_ITEM);
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
        server = event.getServer();
    }

    @Override
    public PlatformLogger getPlatformLogger() {
        return new ForgeLogger();
    }

    @Override
    public PlatformScheduler getPlatformScheduler() {
        return new ForgePlatformScheduler();
    }

    @Override
    public PlatformRankService getRankService() {
        return null;
    }

    @Override
    public PlatformParticleProvider getParticleProvider() {
        return new ForgeParticleProvider();
    }

    @Override
    public PlatformChatFormatter getChatFormatter() {
        return new ForgeChatFormatter();
    }

    @Override
    public PlatformConfig getPlatformConfig() {
        return ForgePlatformConfig.getInstance();
    }

    @Override
    public PlatformBossBarFactory getPlatformBossBarFactory() {
        return new ForgeBossBarFactory();
    }

    @Override
    public PlatformEntityFactory getPlatformEntityFactory() {
        return new ForgePlatformEntityfactory();
    }

    @Override
    public PlatformLocationAdapter getPlatformLocationAdapter() {
        return new ForgePlatformLocationAdapter();
    }

    @Override
    public File getPlatformDataFolder() {
        Path dataFolderPath = FMLPaths.CONFIGDIR.get().resolve("my_mod_id");
        try {
            Files.createDirectory(dataFolderPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Optional<PlatformPlayer> getPlatformPlayer(UUID uuid) {
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        return player != null ? Optional.of(new ForgePlatformPlayer(player)) : Optional.empty();
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
