package org.vicky.platform;

import org.vicky.coreregistry.CoreEntityRegistry;
import org.vicky.musicPlayer.PlatformSoundBackend;
import org.vicky.platform.entity.DefaultEntities;
import org.vicky.platform.entity.MobEntityDescriptor;
import org.vicky.platform.entity.PlatformEffectBridge;
import org.vicky.platform.entity.PlatformEntityFactory;
import org.vicky.platform.events.PlatformEventFactory;
import org.vicky.platform.world.PlatformBlockStateFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface PlatformPlugin {

    static PlatformPlugin get() {
        if (Holder.INSTANCE == null) {
            throw new IllegalStateException("PlatformPlugin has not been initialized!");
        }
        return Holder.INSTANCE;
    }

    static void set(PlatformPlugin instance) {
        if (Holder.INSTANCE == null) {
            Holder.INSTANCE = instance;
            DefaultEntities.INSTANCE.register();
            CoreEntityRegistry.installInto(instance);
        } else {
            throw new IllegalStateException("Cannot set PlatformPlugin after its already been set.");
        }
    }

    static PlatformBlockStateFactory stateFactory() {
        return get().getPlatformBlockStateFactory();
    }

    static PlatformLogger logger() {
        return get().getPlatformLogger();
    }

    static PlatformScheduler scheduler() {
        return get().getPlatformScheduler();
    }

    static PlatformRankService rankService() {
        return get().getRankService();
    }

    static PlatformParticleProvider particleProvider() {
        return get().getParticleProvider();
    }

    static PlatformBossBarFactory bossBarFactory() {
        return get().getPlatformBossBarFactory();
    }

    static File dataFolder() {
        return get().getPlatformDataFolder();
    }

    static PlatformChatFormatter chatFormatter() {
        return get().getChatFormatter();
    }

    static PlatformConfig config() {
        return get().getPlatformConfig();
    }

    static PlatformEntityFactory entityFactory() {
        return get().getPlatformEntityFactory();
    }

    static PlatformLocationAdapter<?> locationAdapter() {
        return get().getPlatformLocationAdapter();
    }

    static PlatformItemFactory itemFactory() {
        return get().getPlatformItemFactory();
    }

    static PlatformEventFactory eventFactory() {
        return get().getEventFactory();
    }

    static PlatformSoundBackend soundBackend() {
        return get().getSoundBackend();
    }

    static PlatformEffectBridge<?> effectBridge() {
        return get().getPlatformEffectBridge();
    }

    static void registerTemplateUtilityPackage(String jarName, String packageName) {
        Holder.pendingDBTemplatesUtils.put(packageName, jarName);
    }

    static void registerTemplatePackage(String jarName, String packageName) {
        Holder.pendingDBTemplates.put(packageName, jarName);
    }

    static Map<String, String> getPendingDBTemplates() {
        return Holder.pendingDBTemplates;
    }

    static Map<String, String> getPendingDBTemplatesUtils() {
        return Holder.pendingDBTemplatesUtils;
    }

    /**
     * Only the instance that was registered can unregister itself.
     */
    default void unregister() {
        if (Holder.INSTANCE == this) {
            Holder.INSTANCE = null;
        } else {
            throw new IllegalStateException("Only the registered instance can unregister itself!");
        }
    }

    static ClassLoader classLoader() {
        return get().getClass().getClassLoader();
    }

    static Optional<PlatformPlayer> getPlayer(UUID uuid) {
        return get().getPlatformPlayer(uuid);
    }

    static int logLevel() {
        return get().getLogLevel();
    }

    static String id() {
        return get().getPlatformIdentifier();
    }

    void registerMobEntityDescriptor(MobEntityDescriptor descriptor);

    /**
     * Optional: platform may implement to immediately process any pending descriptors.
     * For example, call processPendingEntities() or processEntityGenerators();
     */
    default void processPendingEntities() {
        // default no-op; implementations may trigger PlatformDimensionManager.processDimensions()
    }

    PlatformLogger getPlatformLogger();
    PlatformScheduler getPlatformScheduler();
    PlatformRankService getRankService();
    PlatformParticleProvider getParticleProvider();
    PlatformChatFormatter getChatFormatter();
    PlatformConfig getPlatformConfig();
    PlatformBossBarFactory getPlatformBossBarFactory();

    PlatformBlockStateFactory getPlatformBlockStateFactory();
    PlatformItemFactory getPlatformItemFactory();
    PlatformEntityFactory getPlatformEntityFactory();

    PlatformEventFactory getEventFactory();

    PlatformSoundBackend getSoundBackend();

    PlatformEffectBridge<?> getPlatformEffectBridge();

    PlatformLocationAdapter<?> getPlatformLocationAdapter();
    File getPlatformDataFolder();
    Optional<PlatformPlayer> getPlatformPlayer(UUID uuid);

    int getLogLevel();
    String getPlatformIdentifier();

    class Holder {
        private static final Map<String, String> pendingDBTemplates = new HashMap<>();
        private static final Map<String, String> pendingDBTemplatesUtils = new HashMap<>();
        private static volatile PlatformPlugin INSTANCE;
    }
}