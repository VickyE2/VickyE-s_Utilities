package org.vicky;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.level.LevelEvent;
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
import org.vicky.forge.forgeplatform.forgeplatform.*;
import org.vicky.forge.forgeplatform.forgeplatform.useables.ForgePlatformPlayer;
import org.vicky.forge.forgeplatform.forgeplatform.useables.ForgeVec3;
import org.vicky.forge.network.PacketHandler;
import org.vicky.forge.utilities.ForgeModConfig;
import org.vicky.music.MusicRegistry;
import org.vicky.music.utils.MusicBuilder;
import org.vicky.music.utils.MusicPiece;
import org.vicky.music.utils.Sound;
import org.vicky.platform.*;
import org.vicky.platform.events.PlatformEventFactory;
import org.vicky.platform.world.PlatformBlockStateFactory;
import org.vicky.utilities.ANSIColor;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.DatabaseManager.HibernateDatabaseManager;
import org.vicky.utilities.DatabaseManager.SQLManager;
import org.vicky.utilities.DatabaseManager.SQLManagerBuilder;
import org.vicky.utilities.DatabaseManager.templates.DatabasePlayer;
import org.vicky.utilities.DatabaseManager.templates.ExtendedPlayerBase;
import org.vicky.utilities.DatabaseManager.templates.MusicPlayer;
import org.vicky.utilities.DatabaseManager.templates.MusicPlaylist;
import org.vicky.utilities.DatabaseManager.utils.Hbm2DdlAutoType;
import org.vicky.utilities.DatabaseTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.vicky.utilities.DatabaseManager.SQLManager.generator;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(VickyUtilitiesForge.MODID)
public class VickyUtilitiesForge implements PlatformPlugin {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "v_utls";
    public static MinecraftServer server;
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ContextLogger CONTEXT_LOGGER =
            new ContextLogger(ContextLogger.ContextType.SYSTEM, "V-UTLS");
    // Create a Deferred Register to hold Blocks which will all be registered under the "v_utls" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Creates a new Block with the id "v_utls:example_block", combining the namespace and path
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    // Create a Deferred Register to hold Items which will all be registered under the "v_utls" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Creates a new BlockItem with the id "v_utls:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));
    // Creates a new food item with the id "v_utls:example_id", nutrition 1 and saturation 2
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEat().nutrition(1).saturationMod(2f).build())));
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "v_utls" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    // Creates a creative tab with the id "v_utls:example_tab" for the example item, that is placed after the combat tab
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
            output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());

    public static RegistryAccess access;
    public static final HibernateDatabaseManager databaseManager = new HibernateDatabaseManager();
    private static final List<Class<?>> mappingClasses = new ArrayList<>();
    public static SQLManager sqlManager;

    public VickyUtilitiesForge() {
        PlatformPlugin.set(this);
        new MusicRegistry();
        sqlManager = new SQLManagerBuilder()
                .addMappingClass(DatabasePlayer.class)
                .addMappingClass(MusicPlaylist.class)
                .addMappingClass(MusicPlayer.class)
                .addMappingClass(org.vicky.utilities.DatabaseManager.templates.MusicPiece.class)
                .addMappingClass(ExtendedPlayerBase.class)
                .addMappingClasses(mappingClasses)
                .setUsername(generator.generate(20, true, true, true, false))
                .setPassword(generator.generatePassword(30)).setShowSql(false).setFormatSql(false)
                .setDialect("org.hibernate.community.dialect.SQLiteDialect")
                .setDdlAuto(Hbm2DdlAutoType.UPDATE).build();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ForgeModConfig.SPEC);
        PacketHandler.register();
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

    public static void addTemplateClass(Class<? extends DatabaseTemplate> clazz) {
        if (sqlManager == null) {
            mappingClasses.add(clazz);
        } else {
            sqlManager.addMappingClass(clazz);
        }
    }

    @SafeVarargs
    public static void addTemplateClasses(Class<? extends DatabaseTemplate>... clazzez) {
        if (sqlManager == null) {
            mappingClasses.addAll(List.of(clazzez));
        } else {
            sqlManager.addMappingClasses(List.of(clazzez));
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("AHA... There is a server after all...");
        server = event.getServer();
        access = server.registryAccess();
    }

    @SubscribeEvent
    public void onWorldGettingCreatedStarting(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            Path worldDir = serverLevel.getServer().getWorldPath(LevelResource.ROOT);
            registerMusicBuiltins();
            sqlManager.configureSessionFactory();
            sqlManager.startDatabase();
        }
    }

    private void registerMusicBuiltins() {
        var registry = MusicRegistry.getInstance(MusicRegistry.class);
        List<MusicPiece> pieces = new ArrayList<>();
        var symphony1Builder = new MusicBuilder();

        pieces.addAll(List.of(
                new MusicPiece("vicky_utils_symphony1", "Symphony 1", List.of(
                        symphony1Builder.ofScore(Sound.PIANO, "C+,D+,E+,C+,D+,E+,C+,D+,E+,C++—C+,C++—D+,C++—E+,"
                                        + "@[cello1][B],D+,E+,B,D+,E+,B,D+,E+,B++—B,B++—D+,B++—E+,"
                                        + "@[cello2][A],D+,E+,A,D+,E+,A,D+,E+,A++—A,A++—D+,A++—E+,"
                                        + "@[cello3][G],D+,E+,G,D+,E+,G,D+,E+,G++—C++,G++—G+,G++—E+,@[cello4][G++—C+],"
                                        + "D+—G,E+—C,C+,D+—G,E+—C,C+,D+—G,E+—C,C+,D+—G,E+—C,"
                                        + "B,G—D+,B-—E+,B,G—D+,B-—E+,B,G—D+,B-—E+,B,G—D+,B-—E+,"
                                        + "A,E—D+,A-—E+,A,E—D+,A-—E+,A,E—D+,A-—E+,A,E—D+,A-—E+,"
                                        + "G,G-—D+,G—E+,G,G-—D+,G—E+,G,G-—D+,G—E+,C++,G+,E+,"
                                        + "@[instrujoin][C+—C-],G-—G++,C—C++,C-—C++,G-—G++,C—C++,C-—C++,G-—G++,C—C++,C-—C++,G-—G++,C—C++,"
                                        + "@[dinstru][D-—B++],G-—G++,B-—D++,D-—B++,G-—G++,B-—D++,D-—B++,G-—G++,B-—D++,D-—B++,G-—G++,B-—D++,"
                                        + "@[cinstru][G--—A++],D-—D++,G-—G+,G--—A++,D-—D++,G-—G+,G--—A++,D-—D++,G-—G+,G--—A++,D-—D++,G-—G+,"
                                        + "@[ginstru][G-—G++],B-—G+,D—B+,G-—G++,B-—G+,D—B+,G-—G++,B-—G+,D—B+,"
                                        + "G,B,D+,C+,D+—G,E+—C,D+—G,E+—C,D+—G,E+—C,C+—G,"
                                        + "C+,B,C+,D+,B-—E+,G—D+,B-—E+,G—D+,B-—E+,G—D+,B-—E+,B-—C+,"
                                        + "B,A,B,C+,E—D+,A-—E+,E—D+,A-—E+,E—D+,A-—E+,E—D+,A-—G,"
                                        + "A,G,A,B,G-—D+,G—E+,G-—D+,G—E+,G-—D+,G—E+,G-—D+,G—E+,"
                                        + "G,F,E,G,F--—A+,G-—G+,F--—A+,G-—G+,F--—A+,G-—G+,F--—A+,G-—F+,"
                                        + "G+,F+,E+,G+,G--—F+,G+,G--—F+,G+,G--—F+,G+,G--—D+,"
                                        + "C++,B+,A+,B+,C++—F--,B+—F-,F--—C++,F-—B+,F--—C++,F-—B+,F--—C++,F-—A+,"
                                        + "D++,C++,B+,C++,G--—D++,G-—G+,G--—D++,G-—G+,G--—D++,G-—G+,G--—D++,G-—G+,C++—E++—G++",
                                (236 * 9), 1),
                        symphony1Builder.ofScore(Sound.VIOLIN,
                                "C+->@cello1,B+->@cello2,A+->@cello3,G+->@cello4,.->@instrujoin,[G,C+,G,D+,G,F+]*2,.->@dinstru,[G,A,G,C+,G,D]*2,.->@cinstru,A,B,C,.->@ginstru,B,C,D",
                                (12 * 9), 0.8f),
                        symphony1Builder.ofScore(Sound.BRASS, ".->@instrujoin,C-->@dinstru,D-->@cinstru,G--->@ginstru",
                                (236 * 9), 0.9f)),
                        new String[]{"VickyE2"}, "BLUES", 0xBB004D)
        ));

        for (var piece : pieces)
            registry.register(piece);
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
    public PlatformBlockStateFactory getPlatformBlockStateFactory() {
        return new ForgePlatformBlockStateFactory();
    }

    @Override
    public PlatformItemFactory getPlatformItemFactory() {
        return new ForgePlatformItemFactory();
    }

    @Override
    public PlatformEntityFactory getPlatformEntityFactory() {
        return new ForgePlatformEntityfactory();
    }

    @Override
    public PlatformEventFactory getEventFactory() {
        return new ForgeEventFactory();
    }

    @Override
    public PlatformLocationAdapter<ForgeVec3> getPlatformLocationAdapter() {
        return new ForgePlatformLocationAdapter();
    }

    @Override
    public File getPlatformDataFolder() {
        Path dataFolderPath = FMLPaths.GAMEDIR.get().resolve(MODID);
        try {
            Files.createDirectories(dataFolderPath); // safe: only creates if missing
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data folder for mod: " + MODID, e);
        }
        return dataFolderPath.toFile();
    }

    @Override
    public Optional<PlatformPlayer> getPlatformPlayer(UUID uuid) {
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        return player != null ? Optional.of(new ForgePlatformPlayer(player)) : Optional.empty();
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
