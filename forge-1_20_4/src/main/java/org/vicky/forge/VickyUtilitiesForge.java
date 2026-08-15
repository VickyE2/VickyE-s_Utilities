/* Licensed under Apache-2.0 2024. */
package org.vicky.forge;

import com.eliotlash.mclib.math.Variable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vicky.forge.annotationssystem.AnnotationRegisterEvent;
import org.vicky.forge.annotationssystem.PostAnnotationScanEvent;
import org.vicky.forge.annotationssystem.SimpleEventBus;
import org.vicky.forge.client.audio.MidiSynthManager;
import org.vicky.forge.entity.ForgePlatformEntityFactory;
import org.vicky.forge.entity.PlatformBasedLivingEntityRenderer;
import org.vicky.forge.entity.bridge.CreativeTabBootstrap;
import org.vicky.forge.entity.bridge.EffectBootstrap;
import org.vicky.forge.entity.bridge.EntityFactoryBootstrap;
import org.vicky.forge.entity.bridge.ItemsFactoryBootstrap;
import org.vicky.forge.entity.effects.ForgePlatformEffectBridge;
import org.vicky.forge.forgeplatform.*;
import org.vicky.forge.forgeplatform.player.ForgePlatformPlayer;
import org.vicky.forge.forgeplatform.useables.ForgeVec3;
import org.vicky.forge.network.PacketHandler;
import org.vicky.forge.utilities.ForgeModConfig;
import org.vicky.forge.weather.ForgeWeatherChangeTracker;
import org.vicky.forge.weather.SimpleLevelWeatherAccess;
import org.vicky.music.MusicRegistry;
import org.vicky.music.utils.MusicBuilder;
import org.vicky.music.utils.MusicPiece;
import org.vicky.music.utils.Sound;
import org.vicky.musicPlayer.PlatformSoundBackend;
import org.vicky.platform.*;
import org.vicky.platform.entity.MobEntityDescriptor;
import org.vicky.platform.entity.PlatformEffectBridge;
import org.vicky.platform.entity.PlatformEntityFactory;
import org.vicky.platform.events.PlatformEventDispatcher;
import org.vicky.platform.events.PlatformEventRegistry;
import org.vicky.platform.items.PlatformCreativeTabRegistry;
import org.vicky.platform.items.PlatformItemFactory;
import org.vicky.platform.player.PlatformPlayer;
import org.vicky.platform.server.PlatformServer;
import org.vicky.platform.world.PlatformBlockStateFactory;
import org.vicky.utilities.ANSIColor;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseManager.SQLManager;
import org.vicky.utilities.DatabaseManager.SQLManagerBuilder;
import org.vicky.utilities.DatabaseManager.templates.*;
import org.vicky.utilities.DatabaseManager.utils.Hbm2DdlAutoType;
import org.vicky.utilities.DatabaseTemplate;
import software.bernie.geckolib.core.molang.MolangParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.vicky.utilities.DatabaseManager.SQLManager.generator;


@Mod(VickyUtilitiesForge.MODID)
@SuppressWarnings({"deprecation", "removal"})
public class VickyUtilitiesForge implements PlatformPlugin {
	public static final String MODID = "v_utls";
	public static final Logger LOGGER = LoggerFactory.getLogger("vutls-platform");
	public static final SimpleEventBus ANNOTATION_BUS = new SimpleEventBus();
	public static ContextLogger CONTEXT_LOGGER;
	private static final List<Class<?>> mappingClasses = new ArrayList<>();
	public static MinecraftServer server;
	public static SQLManager sqlManager;
	public static ForgePlatformItemFactory FACTORY;
	public static ForgePlatformCreativeTabs CREATIVE_TABS;

	public VickyUtilitiesForge() {
		PlatformPlugin.set(this);
		CONTEXT_LOGGER = new ContextLogger(ContextLogger.ContextType.SYSTEM, "V-UTLS");
		FACTORY = new ForgePlatformItemFactory();
		CREATIVE_TABS = new ForgePlatformCreativeTabs();
		MolangParser.INSTANCE.register(new Variable("core.hand_is_left", 0));
		if (!FMLLoader.getLaunchHandler().isData()) {
			new MusicRegistry();
			IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

			modEventBus.addListener(this::onConstruct);
			modEventBus.addListener(this::commonSetup);
			modEventBus.addListener(this::clientSetup);

			MinecraftForge.EVENT_BUS.register(this);
			ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ForgeModConfig.SPEC);
			PacketHandler.register();
			org.vicky.musicPlayer.MusicPlayer.INSTANCE.toggleLogging();

			ANNOTATION_BUS.addListener(AnnotationRegisterEvent.class,
					this::onRegisterAnnotations);
			ANNOTATION_BUS.addListener(PostAnnotationScanEvent.class,
					this::onPostAnnotationScan);
		}
	}

	private void onPostAnnotationScan(PostAnnotationScanEvent event) {
		EntityFactoryBootstrap.discoverAndRegisterAll(this, event);
		CreativeTabBootstrap.discoverAndRegisterAll(this, event);
		ItemsFactoryBootstrap.discoverAndRegisterAll(this, event);
		EffectBootstrap.discoverAndRegisterAll(event);

		ForgePlatformEffectBridge.EFFECTS.forEach((ignored, effect) ->
				effect.register(FMLJavaModLoadingContext.get().getModEventBus()));
		ForgePlatformEntityFactory.ENTITIES.forEach((ignored, entity) ->
				entity.register(FMLJavaModLoadingContext.get().getModEventBus()));
		ForgePlatformEntityFactory.INSTANCE.attachListeners(FMLJavaModLoadingContext.get().getModEventBus());

		FACTORY.attachToEventBus(FMLJavaModLoadingContext.get().getModEventBus());
		CREATIVE_TABS.attachToEventBus(FMLJavaModLoadingContext.get().getModEventBus());
	}
	private void onRegisterAnnotations(AnnotationRegisterEvent event) {
		EntityFactoryBootstrap.registerTo(event);
		CreativeTabBootstrap.registerTo(event);
		ItemsFactoryBootstrap.registerTo(event);
		EffectBootstrap.registerTo(event);
	}

	private void initializeAnnotations() {
		AnnotationRegisterEvent registerEvent =
				new AnnotationRegisterEvent();

		ANNOTATION_BUS.post(registerEvent);

		org.vicky.forge.entity.bridge.AnnotationScanner.scanAll(
				FMLLoader.backgroundScanHandler,
				registerEvent.getAnnotations()
		);

		ANNOTATION_BUS.post(
				new PostAnnotationScanEvent(
						org.vicky.forge.entity.bridge.AnnotationScanner.getResults()
				)
		);
	}

	private void onConstruct(FMLConstructModEvent event) {
		event.enqueueWork(this::initializeAnnotations);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		CONTEXT_LOGGER.print(ANSIColor.colorizeMixed("""
				gradient-10deg-right-#AA0000-#DDDD00[
				 _  _  __  ___  __ _  _  _  _ ____    _  _  ____  __  __    __  ____  __  ____  ____
				/ )( \\(  )/ __)(  / )( \\/ )(// ___)  / )( \\(_  _)(  )(  )  (  )(_  _)(  )(  __)/ ___)
				\\ \\/ / )(( (__  )  (  )  /   \\___ \\  ) \\/ (  )(   )( / (_/\\ )(   )(   )(  ) _) \\___ \\
				 \\__/ (__)\\___)(__\\_)(__/    (____/  \\____/ (__) (__)\\____/(__) (__) (__)(____)(____/]
				                                                                         dark_gray[0.0.1-BETA]"""));

		ForgeWeatherChangeTracker.setWeatherAccess(new SimpleLevelWeatherAccess());
		ForgeWeatherChangeTracker.register(MinecraftForge.EVENT_BUS);
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

	private void clientSetup(final FMLClientSetupEvent event) {
		try {
			MidiSynthManager.createInstance(Minecraft.getInstance().getResourceManager());
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				MidiSynthManager.getInstance().close();
			}));
			for (var entityType : ForgePlatformEntityFactory.INSTANCE.getRegisteredEntities().values())
				EntityRenderers.register(entityType.get(), PlatformBasedLivingEntityRenderer::new);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SubscribeEvent
	public void onServerStarting(ServerAboutToStartEvent event) {
		LOGGER.info("AHA... There is a server after all...");
		server = event.getServer();
		RegistryAccess access = event.getServer().registryAccess();
		ForgePlatformBlockStateFactory.setLookupProvider(access);
	}

	@SubscribeEvent
	public void onWorldGettingDestroyedStarting(LevelEvent.Unload event) {
		HibernateUtil.close();
		HibernateUtil.shutdown();
	}

	@SubscribeEvent
	public void onWorldGettingCreatedStarting(LevelEvent.Load event) {
		if (!(event.getLevel() instanceof ServerLevel serverLevel))
			return;
		if (!serverLevel.dimension().equals(Level.OVERWORLD))
			return;

		Path utlsWorldDir = serverLevel.getServer().getWorldPath(new LevelResource("v_utls"));
		LOGGER.info("Database path: {}", utlsWorldDir);

		sqlManager = new SQLManagerBuilder()
				.addMappingClass(OwnedPiece.class)
				.addMappingClass(MusicPlayer.class)
				.addMappingClass(OwnedPieceId.class)
				.addMappingClass(MusicPlaylist.class)
				.addMappingClass(DatabasePlayer.class)
				.addMappingClass(ExtendedPlayerBase.class)
				.addMappingClass(org.vicky.utilities.DatabaseManager.templates.MusicPiece.class)
				.addMappingClasses(mappingClasses)
				.setUsername(generator.generate(20, true, true, true, false))
				.setPassword(generator.generatePassword(30)).setShowSql(false).setFormatSql(false)
				.setDialect("org.hibernate.community.dialect.SQLiteDialect")
				.setAbsoluteDatabaseFolder(utlsWorldDir.toAbsolutePath().toString())
				.setDdlAuto(Hbm2DdlAutoType.UPDATE).build();

		sqlManager.configureSessionFactory();
		sqlManager.startDatabase();
		registerMusicBuiltins();
	}

	private void registerMusicBuiltins() {
		var registry = MusicRegistry.getInstance(MusicRegistry.class);
        var symphony1Builder = new MusicBuilder();

        List<MusicPiece> pieces = new ArrayList<>(List.of(new MusicPiece("vicky_utils_symphony1", "Symphony 1", List.of(
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
                        (236 * 9), 0.7f),
                symphony1Builder.ofScore(Sound.VIOLIN,
                        "C+->@cello1,B+->@cello2,A+->@cello3,G+->@cello4,.->@instrujoin,G,C+,G,D+,G,F+,.->@dinstru,G,A,G,C+,G,D,.->@cinstru,A,B,C,.->@ginstru,B,C,D",
                        (12 * 9), 0.7f),
                symphony1Builder.ofScore(Sound.BRASS, ".->@instrujoin,C-->@dinstru,D-->@cinstru,G--->@ginstru",
                        (236 * 9), 0.7f)),
                new String[]{"VickyE2"}, "BLUES", 0xBB004D)));

		for (var piece : pieces)
			registry.register(piece);
	}

	@Override
	public PlatformLogger getPlatformLogger() {
		return ForgeLogger.getInstance();
	}

	@Override
	public PlatformServer getPlatformServer() {
		return ForgePlatformServer.getInstance();
	}

	@Override
	public PlatformRankService getRankService() {
		return null;
	}

	@Override
	public PlatformParticleProvider getParticleProvider() {
		return ForgeParticleProvider.getInstance();
	}

	@Override
	public PlatformChatFormatter getChatFormatter() {
		return ForgeChatFormatter.getInstance();
	}

	@Override
	public PlatformConfig getPlatformConfig() {
		return ForgePlatformConfig.getInstance();
	}

	@Override
	public PlatformBossBarFactory getPlatformBossBarFactory() {
		return ForgeBossBarFactory.getInstance();
	}

	@Override
	public PlatformBlockStateFactory getPlatformBlockStateFactory() {
		return ForgePlatformBlockStateFactory.getInstance();
	}

	@Override
	public PlatformItemFactory getPlatformItemFactory() {
		return FACTORY;
	}

	@Override
	public PlatformCreativeTabRegistry getPlatformCreativeTabRegistry() {
		return CREATIVE_TABS;
	}

	@Override
	public PlatformEventRegistry getEventRegistry() {
		return ForgeEventFactory.INSTANCE;
	}

	@Override
	public PlatformEventDispatcher getEventDispatch() {
		return ForgeEventFactory.INSTANCE;
	}

	@Override
	public PlatformSoundBackend getSoundBackend() {
		return ForgeSynthSoundBackend.INSTANCE;
	}

	@Override
	public PlatformLocationAdapter<ForgeVec3> getPlatformLocationAdapter() {
		return ForgePlatformLocationAdapter.getInstance();
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

	@Override
	public int getLogLevel() {
		System.out.println("Requested log level...");
		return -100;
	}

	@Override
	public String getPlatformIdentifier() {
		return MODID;
	}

	@Override
	public PlatformEffectBridge<?> getPlatformEffectBridge() {
		return ForgePlatformEffectBridge.INSTANCE;
	}

	@Override
	public PlatformClassProvider getClassProvider() {
		return null;
	}

	@Override
	public PlatformEntityFactory getPlatformEntityFactory() {
		return ForgePlatformEntityFactory.INSTANCE;
	}

	@Override
	public void registerMobEntityDescriptor(MobEntityDescriptor mobEntityDescriptor) {
		ForgePlatformEntityFactory.INSTANCE.register(mobEntityDescriptor);
	}

	@Override
	public void registerMobEntityDescriptor(MobEntityDescriptor... descriptors) {
		for (var descriptor : descriptors) {
			ForgePlatformEntityFactory.INSTANCE.register(descriptor);
		}
	}

	@Override
	public void registerMobEntityDescriptor(Collection<MobEntityDescriptor> descriptors) {
		for (var descriptor : descriptors) {
			ForgePlatformEntityFactory.INSTANCE.register(descriptor);
		}
	}

	// You can use EventBusSubscriber to automatically register all static methods
	// in the class annotated with @SubscribeEvent
	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			LOGGER.info("So what do we need to do on client again?");
		}
	}
}
