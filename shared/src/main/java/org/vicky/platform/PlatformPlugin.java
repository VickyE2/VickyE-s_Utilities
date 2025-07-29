package org.vicky.platform;

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
        Holder.INSTANCE = instance;
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

    static PlatformLocationAdapter locationAdapter() {
        return get().getPlatformLocationAdapter();
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

    static ClassLoader classLoader() {
        return get().getClass().getClassLoader();
    }

    static Optional<PlatformPlayer> getPlayer(UUID uuid) {
        return get().getPlatformPlayer(uuid);
    }

    PlatformLogger getPlatformLogger();
    PlatformScheduler getPlatformScheduler();
    PlatformRankService getRankService();
    PlatformParticleProvider getParticleProvider();
    PlatformChatFormatter getChatFormatter();
    PlatformConfig getPlatformConfig();
    PlatformBossBarFactory getPlatformBossBarFactory();
    PlatformEntityFactory getPlatformEntityFactory();
    PlatformLocationAdapter getPlatformLocationAdapter();
    File getPlatformDataFolder();
    Optional<PlatformPlayer> getPlatformPlayer(UUID uuid);

    void onEnable();
    void onDisable();
    class Holder {
        private static final Map<String, String> pendingDBTemplates = new HashMap<>();
        private static final Map<String, String> pendingDBTemplatesUtils = new HashMap<>();
        private static volatile PlatformPlugin INSTANCE;
    }
}