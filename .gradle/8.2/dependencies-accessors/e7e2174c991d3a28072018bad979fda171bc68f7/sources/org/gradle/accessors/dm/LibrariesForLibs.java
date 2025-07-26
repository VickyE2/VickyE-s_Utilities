package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the `libs` extension.
 */
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final ComLibraryAccessors laccForComLibraryAccessors = new ComLibraryAccessors(owner);
    private final DevLibraryAccessors laccForDevLibraryAccessors = new DevLibraryAccessors(owner);
    private final IoLibraryAccessors laccForIoLibraryAccessors = new IoLibraryAccessors(owner);
    private final KotlinLibraryAccessors laccForKotlinLibraryAccessors = new KotlinLibraryAccessors(owner);
    private final MeLibraryAccessors laccForMeLibraryAccessors = new MeLibraryAccessors(owner);
    private final NetLibraryAccessors laccForNetLibraryAccessors = new NetLibraryAccessors(owner);
    private final OrgLibraryAccessors laccForOrgLibraryAccessors = new OrgLibraryAccessors(owner);
    private final PaperLibraryAccessors laccForPaperLibraryAccessors = new PaperLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

    /**
     * Returns the group of libraries at com
     */
    public ComLibraryAccessors getCom() {
        return laccForComLibraryAccessors;
    }

    /**
     * Returns the group of libraries at dev
     */
    public DevLibraryAccessors getDev() {
        return laccForDevLibraryAccessors;
    }

    /**
     * Returns the group of libraries at io
     */
    public IoLibraryAccessors getIo() {
        return laccForIoLibraryAccessors;
    }

    /**
     * Returns the group of libraries at kotlin
     */
    public KotlinLibraryAccessors getKotlin() {
        return laccForKotlinLibraryAccessors;
    }

    /**
     * Returns the group of libraries at me
     */
    public MeLibraryAccessors getMe() {
        return laccForMeLibraryAccessors;
    }

    /**
     * Returns the group of libraries at net
     */
    public NetLibraryAccessors getNet() {
        return laccForNetLibraryAccessors;
    }

    /**
     * Returns the group of libraries at org
     */
    public OrgLibraryAccessors getOrg() {
        return laccForOrgLibraryAccessors;
    }

    /**
     * Returns the group of libraries at paper
     */
    public PaperLibraryAccessors getPaper() {
        return laccForPaperLibraryAccessors;
    }

    /**
     * Returns the group of versions at versions
     */
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    /**
     * Returns the group of bundles at bundles
     */
    public BundleAccessors getBundles() {
        return baccForBundleAccessors;
    }

    /**
     * Returns the group of plugins at plugins
     */
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    public static class ComLibraryAccessors extends SubDependencyFactory {
        private final ComComphenixLibraryAccessors laccForComComphenixLibraryAccessors = new ComComphenixLibraryAccessors(owner);
        private final ComDfsekLibraryAccessors laccForComDfsekLibraryAccessors = new ComDfsekLibraryAccessors(owner);
        private final ComGithubLibraryAccessors laccForComGithubLibraryAccessors = new ComGithubLibraryAccessors(owner);
        private final ComOnarandomboxLibraryAccessors laccForComOnarandomboxLibraryAccessors = new ComOnarandomboxLibraryAccessors(owner);
        private final ComSk89qLibraryAccessors laccForComSk89qLibraryAccessors = new ComSk89qLibraryAccessors(owner);

        public ComLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.comphenix
         */
        public ComComphenixLibraryAccessors getComphenix() {
            return laccForComComphenixLibraryAccessors;
        }

        /**
         * Returns the group of libraries at com.dfsek
         */
        public ComDfsekLibraryAccessors getDfsek() {
            return laccForComDfsekLibraryAccessors;
        }

        /**
         * Returns the group of libraries at com.github
         */
        public ComGithubLibraryAccessors getGithub() {
            return laccForComGithubLibraryAccessors;
        }

        /**
         * Returns the group of libraries at com.onarandombox
         */
        public ComOnarandomboxLibraryAccessors getOnarandombox() {
            return laccForComOnarandomboxLibraryAccessors;
        }

        /**
         * Returns the group of libraries at com.sk89q
         */
        public ComSk89qLibraryAccessors getSk89q() {
            return laccForComSk89qLibraryAccessors;
        }

    }

    public static class ComComphenixLibraryAccessors extends SubDependencyFactory {
        private final ComComphenixProtocolLibraryAccessors laccForComComphenixProtocolLibraryAccessors = new ComComphenixProtocolLibraryAccessors(owner);

        public ComComphenixLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.comphenix.protocol
         */
        public ComComphenixProtocolLibraryAccessors getProtocol() {
            return laccForComComphenixProtocolLibraryAccessors;
        }

    }

    public static class ComComphenixProtocolLibraryAccessors extends SubDependencyFactory {

        public ComComphenixProtocolLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for protocollib (com.comphenix.protocol:ProtocolLib)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getProtocollib() {
                return create("com.comphenix.protocol.protocollib");
        }

    }

    public static class ComDfsekLibraryAccessors extends SubDependencyFactory {
        private final ComDfsekTerraLibraryAccessors laccForComDfsekTerraLibraryAccessors = new ComDfsekTerraLibraryAccessors(owner);

        public ComDfsekLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.dfsek.terra
         */
        public ComDfsekTerraLibraryAccessors getTerra() {
            return laccForComDfsekTerraLibraryAccessors;
        }

    }

    public static class ComDfsekTerraLibraryAccessors extends SubDependencyFactory {
        private final ComDfsekTerraManifestLibraryAccessors laccForComDfsekTerraManifestLibraryAccessors = new ComDfsekTerraManifestLibraryAccessors(owner);

        public ComDfsekTerraLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for api (com.dfsek.terra:api)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getApi() {
                return create("com.dfsek.terra.api");
        }

            /**
             * Creates a dependency provider for bukkit (com.dfsek.terra:bukkit)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getBukkit() {
                return create("com.dfsek.terra.bukkit");
        }

        /**
         * Returns the group of libraries at com.dfsek.terra.manifest
         */
        public ComDfsekTerraManifestLibraryAccessors getManifest() {
            return laccForComDfsekTerraManifestLibraryAccessors;
        }

    }

    public static class ComDfsekTerraManifestLibraryAccessors extends SubDependencyFactory {
        private final ComDfsekTerraManifestAddonLibraryAccessors laccForComDfsekTerraManifestAddonLibraryAccessors = new ComDfsekTerraManifestAddonLibraryAccessors(owner);

        public ComDfsekTerraManifestLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.dfsek.terra.manifest.addon
         */
        public ComDfsekTerraManifestAddonLibraryAccessors getAddon() {
            return laccForComDfsekTerraManifestAddonLibraryAccessors;
        }

    }

    public static class ComDfsekTerraManifestAddonLibraryAccessors extends SubDependencyFactory {

        public ComDfsekTerraManifestAddonLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for loader (com.dfsek.terra:manifest-addon-loader)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getLoader() {
                return create("com.dfsek.terra.manifest.addon.loader");
        }

    }

    public static class ComGithubLibraryAccessors extends SubDependencyFactory {
        private final ComGithubLonedev6LibraryAccessors laccForComGithubLonedev6LibraryAccessors = new ComGithubLonedev6LibraryAccessors(owner);
        private final ComGithubZockeraxelLibraryAccessors laccForComGithubZockeraxelLibraryAccessors = new ComGithubZockeraxelLibraryAccessors(owner);

        public ComGithubLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.github.lonedev6
         */
        public ComGithubLonedev6LibraryAccessors getLonedev6() {
            return laccForComGithubLonedev6LibraryAccessors;
        }

        /**
         * Returns the group of libraries at com.github.zockeraxel
         */
        public ComGithubZockeraxelLibraryAccessors getZockeraxel() {
            return laccForComGithubZockeraxelLibraryAccessors;
        }

    }

    public static class ComGithubLonedev6LibraryAccessors extends SubDependencyFactory {
        private final ComGithubLonedev6ApiLibraryAccessors laccForComGithubLonedev6ApiLibraryAccessors = new ComGithubLonedev6ApiLibraryAccessors(owner);

        public ComGithubLonedev6LibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.github.lonedev6.api
         */
        public ComGithubLonedev6ApiLibraryAccessors getApi() {
            return laccForComGithubLonedev6ApiLibraryAccessors;
        }

    }

    public static class ComGithubLonedev6ApiLibraryAccessors extends SubDependencyFactory {

        public ComGithubLonedev6ApiLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for itemsadder (com.github.LoneDev6:api-itemsadder)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getItemsadder() {
                return create("com.github.lonedev6.api.itemsadder");
        }

    }

    public static class ComGithubZockeraxelLibraryAccessors extends SubDependencyFactory {

        public ComGithubZockeraxelLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for crazyadvancementsapi (com.github.ZockerAxel:CrazyAdvancementsAPI)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getCrazyadvancementsapi() {
                return create("com.github.zockeraxel.crazyadvancementsapi");
        }

    }

    public static class ComOnarandomboxLibraryAccessors extends SubDependencyFactory {
        private final ComOnarandomboxMultiversecoreLibraryAccessors laccForComOnarandomboxMultiversecoreLibraryAccessors = new ComOnarandomboxMultiversecoreLibraryAccessors(owner);
        private final ComOnarandomboxMultiverseinventoriesLibraryAccessors laccForComOnarandomboxMultiverseinventoriesLibraryAccessors = new ComOnarandomboxMultiverseinventoriesLibraryAccessors(owner);
        private final ComOnarandomboxMultiverseportalsLibraryAccessors laccForComOnarandomboxMultiverseportalsLibraryAccessors = new ComOnarandomboxMultiverseportalsLibraryAccessors(owner);

        public ComOnarandomboxLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.onarandombox.multiversecore
         */
        public ComOnarandomboxMultiversecoreLibraryAccessors getMultiversecore() {
            return laccForComOnarandomboxMultiversecoreLibraryAccessors;
        }

        /**
         * Returns the group of libraries at com.onarandombox.multiverseinventories
         */
        public ComOnarandomboxMultiverseinventoriesLibraryAccessors getMultiverseinventories() {
            return laccForComOnarandomboxMultiverseinventoriesLibraryAccessors;
        }

        /**
         * Returns the group of libraries at com.onarandombox.multiverseportals
         */
        public ComOnarandomboxMultiverseportalsLibraryAccessors getMultiverseportals() {
            return laccForComOnarandomboxMultiverseportalsLibraryAccessors;
        }

    }

    public static class ComOnarandomboxMultiversecoreLibraryAccessors extends SubDependencyFactory {
        private final ComOnarandomboxMultiversecoreMultiverseLibraryAccessors laccForComOnarandomboxMultiversecoreMultiverseLibraryAccessors = new ComOnarandomboxMultiversecoreMultiverseLibraryAccessors(owner);

        public ComOnarandomboxMultiversecoreLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.onarandombox.multiversecore.multiverse
         */
        public ComOnarandomboxMultiversecoreMultiverseLibraryAccessors getMultiverse() {
            return laccForComOnarandomboxMultiversecoreMultiverseLibraryAccessors;
        }

    }

    public static class ComOnarandomboxMultiversecoreMultiverseLibraryAccessors extends SubDependencyFactory {

        public ComOnarandomboxMultiversecoreMultiverseLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for core (com.onarandombox.multiversecore:Multiverse-Core)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getCore() {
                return create("com.onarandombox.multiversecore.multiverse.core");
        }

    }

    public static class ComOnarandomboxMultiverseinventoriesLibraryAccessors extends SubDependencyFactory {
        private final ComOnarandomboxMultiverseinventoriesMultiverseLibraryAccessors laccForComOnarandomboxMultiverseinventoriesMultiverseLibraryAccessors = new ComOnarandomboxMultiverseinventoriesMultiverseLibraryAccessors(owner);

        public ComOnarandomboxMultiverseinventoriesLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.onarandombox.multiverseinventories.multiverse
         */
        public ComOnarandomboxMultiverseinventoriesMultiverseLibraryAccessors getMultiverse() {
            return laccForComOnarandomboxMultiverseinventoriesMultiverseLibraryAccessors;
        }

    }

    public static class ComOnarandomboxMultiverseinventoriesMultiverseLibraryAccessors extends SubDependencyFactory {

        public ComOnarandomboxMultiverseinventoriesMultiverseLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for inventories (com.onarandombox.multiverseinventories:Multiverse-Inventories)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getInventories() {
                return create("com.onarandombox.multiverseinventories.multiverse.inventories");
        }

    }

    public static class ComOnarandomboxMultiverseportalsLibraryAccessors extends SubDependencyFactory {
        private final ComOnarandomboxMultiverseportalsMultiverseLibraryAccessors laccForComOnarandomboxMultiverseportalsMultiverseLibraryAccessors = new ComOnarandomboxMultiverseportalsMultiverseLibraryAccessors(owner);

        public ComOnarandomboxMultiverseportalsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.onarandombox.multiverseportals.multiverse
         */
        public ComOnarandomboxMultiverseportalsMultiverseLibraryAccessors getMultiverse() {
            return laccForComOnarandomboxMultiverseportalsMultiverseLibraryAccessors;
        }

    }

    public static class ComOnarandomboxMultiverseportalsMultiverseLibraryAccessors extends SubDependencyFactory {

        public ComOnarandomboxMultiverseportalsMultiverseLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for portals (com.onarandombox.multiverseportals:Multiverse-Portals)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getPortals() {
                return create("com.onarandombox.multiverseportals.multiverse.portals");
        }

    }

    public static class ComSk89qLibraryAccessors extends SubDependencyFactory {
        private final ComSk89qWorldeditLibraryAccessors laccForComSk89qWorldeditLibraryAccessors = new ComSk89qWorldeditLibraryAccessors(owner);
        private final ComSk89qWorldguardLibraryAccessors laccForComSk89qWorldguardLibraryAccessors = new ComSk89qWorldguardLibraryAccessors(owner);

        public ComSk89qLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.sk89q.worldedit
         */
        public ComSk89qWorldeditLibraryAccessors getWorldedit() {
            return laccForComSk89qWorldeditLibraryAccessors;
        }

        /**
         * Returns the group of libraries at com.sk89q.worldguard
         */
        public ComSk89qWorldguardLibraryAccessors getWorldguard() {
            return laccForComSk89qWorldguardLibraryAccessors;
        }

    }

    public static class ComSk89qWorldeditLibraryAccessors extends SubDependencyFactory {
        private final ComSk89qWorldeditWorldeditLibraryAccessors laccForComSk89qWorldeditWorldeditLibraryAccessors = new ComSk89qWorldeditWorldeditLibraryAccessors(owner);

        public ComSk89qWorldeditLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.sk89q.worldedit.worldedit
         */
        public ComSk89qWorldeditWorldeditLibraryAccessors getWorldedit() {
            return laccForComSk89qWorldeditWorldeditLibraryAccessors;
        }

    }

    public static class ComSk89qWorldeditWorldeditLibraryAccessors extends SubDependencyFactory {

        public ComSk89qWorldeditWorldeditLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for bukkit (com.sk89q.worldedit:worldedit-bukkit)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getBukkit() {
                return create("com.sk89q.worldedit.worldedit.bukkit");
        }

    }

    public static class ComSk89qWorldguardLibraryAccessors extends SubDependencyFactory {
        private final ComSk89qWorldguardWorldguardLibraryAccessors laccForComSk89qWorldguardWorldguardLibraryAccessors = new ComSk89qWorldguardWorldguardLibraryAccessors(owner);

        public ComSk89qWorldguardLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at com.sk89q.worldguard.worldguard
         */
        public ComSk89qWorldguardWorldguardLibraryAccessors getWorldguard() {
            return laccForComSk89qWorldguardWorldguardLibraryAccessors;
        }

    }

    public static class ComSk89qWorldguardWorldguardLibraryAccessors extends SubDependencyFactory {

        public ComSk89qWorldguardWorldguardLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for bukkit (com.sk89q.worldguard:worldguard-bukkit)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getBukkit() {
                return create("com.sk89q.worldguard.worldguard.bukkit");
        }

    }

    public static class DevLibraryAccessors extends SubDependencyFactory {
        private final DevJorelLibraryAccessors laccForDevJorelLibraryAccessors = new DevJorelLibraryAccessors(owner);
        private final DevLoneLibraryAccessors laccForDevLoneLibraryAccessors = new DevLoneLibraryAccessors(owner);

        public DevLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at dev.jorel
         */
        public DevJorelLibraryAccessors getJorel() {
            return laccForDevJorelLibraryAccessors;
        }

        /**
         * Returns the group of libraries at dev.lone
         */
        public DevLoneLibraryAccessors getLone() {
            return laccForDevLoneLibraryAccessors;
        }

    }

    public static class DevJorelLibraryAccessors extends SubDependencyFactory {
        private final DevJorelCommandapiLibraryAccessors laccForDevJorelCommandapiLibraryAccessors = new DevJorelCommandapiLibraryAccessors(owner);

        public DevJorelLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at dev.jorel.commandapi
         */
        public DevJorelCommandapiLibraryAccessors getCommandapi() {
            return laccForDevJorelCommandapiLibraryAccessors;
        }

    }

    public static class DevJorelCommandapiLibraryAccessors extends SubDependencyFactory {
        private final DevJorelCommandapiBukkitLibraryAccessors laccForDevJorelCommandapiBukkitLibraryAccessors = new DevJorelCommandapiBukkitLibraryAccessors(owner);

        public DevJorelCommandapiLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at dev.jorel.commandapi.bukkit
         */
        public DevJorelCommandapiBukkitLibraryAccessors getBukkit() {
            return laccForDevJorelCommandapiBukkitLibraryAccessors;
        }

    }

    public static class DevJorelCommandapiBukkitLibraryAccessors extends SubDependencyFactory {

        public DevJorelCommandapiBukkitLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for core (dev.jorel:commandapi-bukkit-core)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getCore() {
                return create("dev.jorel.commandapi.bukkit.core");
        }

    }

    public static class DevLoneLibraryAccessors extends SubDependencyFactory {

        public DevLoneLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for lonelibs (dev.lone:LoneLibs)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getLonelibs() {
                return create("dev.lone.lonelibs");
        }

    }

    public static class IoLibraryAccessors extends SubDependencyFactory {
        private final IoLumineLibraryAccessors laccForIoLumineLibraryAccessors = new IoLumineLibraryAccessors(owner);

        public IoLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at io.lumine
         */
        public IoLumineLibraryAccessors getLumine() {
            return laccForIoLumineLibraryAccessors;
        }

    }

    public static class IoLumineLibraryAccessors extends SubDependencyFactory {
        private final IoLumineMythicLibraryAccessors laccForIoLumineMythicLibraryAccessors = new IoLumineMythicLibraryAccessors(owner);

        public IoLumineLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at io.lumine.mythic
         */
        public IoLumineMythicLibraryAccessors getMythic() {
            return laccForIoLumineMythicLibraryAccessors;
        }

    }

    public static class IoLumineMythicLibraryAccessors extends SubDependencyFactory {

        public IoLumineMythicLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for dist (io.lumine:Mythic-Dist)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getDist() {
                return create("io.lumine.mythic.dist");
        }

    }

    public static class KotlinLibraryAccessors extends SubDependencyFactory {

        public KotlinLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for stdlib (org.jetbrains.kotlin:kotlin-stdlib-jdk8)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getStdlib() {
                return create("kotlin.stdlib");
        }

    }

    public static class MeLibraryAccessors extends SubDependencyFactory {
        private final MeClipLibraryAccessors laccForMeClipLibraryAccessors = new MeClipLibraryAccessors(owner);

        public MeLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at me.clip
         */
        public MeClipLibraryAccessors getClip() {
            return laccForMeClipLibraryAccessors;
        }

    }

    public static class MeClipLibraryAccessors extends SubDependencyFactory {

        public MeClipLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for placeholderapi (me.clip:placeholderapi)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getPlaceholderapi() {
                return create("me.clip.placeholderapi");
        }

    }

    public static class NetLibraryAccessors extends SubDependencyFactory {
        private final NetSfLibraryAccessors laccForNetSfLibraryAccessors = new NetSfLibraryAccessors(owner);

        public NetLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at net.sf
         */
        public NetSfLibraryAccessors getSf() {
            return laccForNetSfLibraryAccessors;
        }

    }

    public static class NetSfLibraryAccessors extends SubDependencyFactory {
        private final NetSfTrove4jLibraryAccessors laccForNetSfTrove4jLibraryAccessors = new NetSfTrove4jLibraryAccessors(owner);

        public NetSfLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at net.sf.trove4j
         */
        public NetSfTrove4jLibraryAccessors getTrove4j() {
            return laccForNetSfTrove4jLibraryAccessors;
        }

    }

    public static class NetSfTrove4jLibraryAccessors extends SubDependencyFactory {

        public NetSfTrove4jLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for core (net.sf.trove4j:core)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getCore() {
                return create("net.sf.trove4j.core");
        }

    }

    public static class OrgLibraryAccessors extends SubDependencyFactory {
        private final OrgBetonquestLibraryAccessors laccForOrgBetonquestLibraryAccessors = new OrgBetonquestLibraryAccessors(owner);
        private final OrgJetbrainsLibraryAccessors laccForOrgJetbrainsLibraryAccessors = new OrgJetbrainsLibraryAccessors(owner);
        private final OrgJunitLibraryAccessors laccForOrgJunitLibraryAccessors = new OrgJunitLibraryAccessors(owner);
        private final OrgReflectionsLibraryAccessors laccForOrgReflectionsLibraryAccessors = new OrgReflectionsLibraryAccessors(owner);
        private final OrgVickyLibraryAccessors laccForOrgVickyLibraryAccessors = new OrgVickyLibraryAccessors(owner);

        public OrgLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at org.betonquest
         */
        public OrgBetonquestLibraryAccessors getBetonquest() {
            return laccForOrgBetonquestLibraryAccessors;
        }

        /**
         * Returns the group of libraries at org.jetbrains
         */
        public OrgJetbrainsLibraryAccessors getJetbrains() {
            return laccForOrgJetbrainsLibraryAccessors;
        }

        /**
         * Returns the group of libraries at org.junit
         */
        public OrgJunitLibraryAccessors getJunit() {
            return laccForOrgJunitLibraryAccessors;
        }

        /**
         * Returns the group of libraries at org.reflections
         */
        public OrgReflectionsLibraryAccessors getReflections() {
            return laccForOrgReflectionsLibraryAccessors;
        }

        /**
         * Returns the group of libraries at org.vicky
         */
        public OrgVickyLibraryAccessors getVicky() {
            return laccForOrgVickyLibraryAccessors;
        }

    }

    public static class OrgBetonquestLibraryAccessors extends SubDependencyFactory {

        public OrgBetonquestLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for betonquest (org.betonquest:betonquest)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getBetonquest() {
                return create("org.betonquest.betonquest");
        }

    }

    public static class OrgJetbrainsLibraryAccessors extends SubDependencyFactory {
        private final OrgJetbrainsKotlinLibraryAccessors laccForOrgJetbrainsKotlinLibraryAccessors = new OrgJetbrainsKotlinLibraryAccessors(owner);
        private final OrgJetbrainsKotlinxLibraryAccessors laccForOrgJetbrainsKotlinxLibraryAccessors = new OrgJetbrainsKotlinxLibraryAccessors(owner);

        public OrgJetbrainsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at org.jetbrains.kotlin
         */
        public OrgJetbrainsKotlinLibraryAccessors getKotlin() {
            return laccForOrgJetbrainsKotlinLibraryAccessors;
        }

        /**
         * Returns the group of libraries at org.jetbrains.kotlinx
         */
        public OrgJetbrainsKotlinxLibraryAccessors getKotlinx() {
            return laccForOrgJetbrainsKotlinxLibraryAccessors;
        }

    }

    public static class OrgJetbrainsKotlinLibraryAccessors extends SubDependencyFactory {
        private final OrgJetbrainsKotlinKotlinLibraryAccessors laccForOrgJetbrainsKotlinKotlinLibraryAccessors = new OrgJetbrainsKotlinKotlinLibraryAccessors(owner);

        public OrgJetbrainsKotlinLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at org.jetbrains.kotlin.kotlin
         */
        public OrgJetbrainsKotlinKotlinLibraryAccessors getKotlin() {
            return laccForOrgJetbrainsKotlinKotlinLibraryAccessors;
        }

    }

    public static class OrgJetbrainsKotlinKotlinLibraryAccessors extends SubDependencyFactory {

        public OrgJetbrainsKotlinKotlinLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for test (org.jetbrains.kotlin:kotlin-test)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getTest() {
                return create("org.jetbrains.kotlin.kotlin.test");
        }

    }

    public static class OrgJetbrainsKotlinxLibraryAccessors extends SubDependencyFactory {
        private final OrgJetbrainsKotlinxKotlinxLibraryAccessors laccForOrgJetbrainsKotlinxKotlinxLibraryAccessors = new OrgJetbrainsKotlinxKotlinxLibraryAccessors(owner);

        public OrgJetbrainsKotlinxLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at org.jetbrains.kotlinx.kotlinx
         */
        public OrgJetbrainsKotlinxKotlinxLibraryAccessors getKotlinx() {
            return laccForOrgJetbrainsKotlinxKotlinxLibraryAccessors;
        }

    }

    public static class OrgJetbrainsKotlinxKotlinxLibraryAccessors extends SubDependencyFactory {
        private final OrgJetbrainsKotlinxKotlinxSerializationLibraryAccessors laccForOrgJetbrainsKotlinxKotlinxSerializationLibraryAccessors = new OrgJetbrainsKotlinxKotlinxSerializationLibraryAccessors(owner);

        public OrgJetbrainsKotlinxKotlinxLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at org.jetbrains.kotlinx.kotlinx.serialization
         */
        public OrgJetbrainsKotlinxKotlinxSerializationLibraryAccessors getSerialization() {
            return laccForOrgJetbrainsKotlinxKotlinxSerializationLibraryAccessors;
        }

    }

    public static class OrgJetbrainsKotlinxKotlinxSerializationLibraryAccessors extends SubDependencyFactory {

        public OrgJetbrainsKotlinxKotlinxSerializationLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for json (org.jetbrains.kotlinx:kotlinx-serialization-json)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJson() {
                return create("org.jetbrains.kotlinx.kotlinx.serialization.json");
        }

    }

    public static class OrgJunitLibraryAccessors extends SubDependencyFactory {
        private final OrgJunitJupiterLibraryAccessors laccForOrgJunitJupiterLibraryAccessors = new OrgJunitJupiterLibraryAccessors(owner);

        public OrgJunitLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at org.junit.jupiter
         */
        public OrgJunitJupiterLibraryAccessors getJupiter() {
            return laccForOrgJunitJupiterLibraryAccessors;
        }

    }

    public static class OrgJunitJupiterLibraryAccessors extends SubDependencyFactory {
        private final OrgJunitJupiterJunitLibraryAccessors laccForOrgJunitJupiterJunitLibraryAccessors = new OrgJunitJupiterJunitLibraryAccessors(owner);

        public OrgJunitJupiterLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at org.junit.jupiter.junit
         */
        public OrgJunitJupiterJunitLibraryAccessors getJunit() {
            return laccForOrgJunitJupiterJunitLibraryAccessors;
        }

    }

    public static class OrgJunitJupiterJunitLibraryAccessors extends SubDependencyFactory {
        private final OrgJunitJupiterJunitJupiterLibraryAccessors laccForOrgJunitJupiterJunitJupiterLibraryAccessors = new OrgJunitJupiterJunitJupiterLibraryAccessors(owner);

        public OrgJunitJupiterJunitLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at org.junit.jupiter.junit.jupiter
         */
        public OrgJunitJupiterJunitJupiterLibraryAccessors getJupiter() {
            return laccForOrgJunitJupiterJunitJupiterLibraryAccessors;
        }

    }

    public static class OrgJunitJupiterJunitJupiterLibraryAccessors extends SubDependencyFactory {

        public OrgJunitJupiterJunitJupiterLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for api (org.junit.jupiter:junit-jupiter-api)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getApi() {
                return create("org.junit.jupiter.junit.jupiter.api");
        }

            /**
             * Creates a dependency provider for params (org.junit.jupiter:junit-jupiter-params)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getParams() {
                return create("org.junit.jupiter.junit.jupiter.params");
        }

    }

    public static class OrgReflectionsLibraryAccessors extends SubDependencyFactory {

        public OrgReflectionsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for reflections (org.reflections:reflections)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getReflections() {
                return create("org.reflections.reflections");
        }

    }

    public static class OrgVickyLibraryAccessors extends SubDependencyFactory {
        private final OrgVickyVickyLibraryAccessors laccForOrgVickyVickyLibraryAccessors = new OrgVickyVickyLibraryAccessors(owner);

        public OrgVickyLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at org.vicky.vicky
         */
        public OrgVickyVickyLibraryAccessors getVicky() {
            return laccForOrgVickyVickyLibraryAccessors;
        }

    }

    public static class OrgVickyVickyLibraryAccessors extends SubDependencyFactory {
        private final OrgVickyVickyUtilsLibraryAccessors laccForOrgVickyVickyUtilsLibraryAccessors = new OrgVickyVickyUtilsLibraryAccessors(owner);

        public OrgVickyVickyLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at org.vicky.vicky.utils
         */
        public OrgVickyVickyUtilsLibraryAccessors getUtils() {
            return laccForOrgVickyVickyUtilsLibraryAccessors;
        }

    }

    public static class OrgVickyVickyUtilsLibraryAccessors extends SubDependencyFactory {
        private final OrgVickyVickyUtilsVLibraryAccessors laccForOrgVickyVickyUtilsVLibraryAccessors = new OrgVickyVickyUtilsVLibraryAccessors(owner);

        public OrgVickyVickyUtilsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at org.vicky.vicky.utils.v
         */
        public OrgVickyVickyUtilsVLibraryAccessors getV() {
            return laccForOrgVickyVickyUtilsVLibraryAccessors;
        }

    }

    public static class OrgVickyVickyUtilsVLibraryAccessors extends SubDependencyFactory {

        public OrgVickyVickyUtilsVLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for utls (org.vicky.vicky_utils:v-utls)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getUtls() {
                return create("org.vicky.vicky.utils.v.utls");
        }

    }

    public static class PaperLibraryAccessors extends SubDependencyFactory {

        public PaperLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for api (io.papermc.paper:paper-api)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getApi() {
                return create("paper.api");
        }

    }

    public static class VersionAccessors extends VersionFactory  {

        private final ComVersionAccessors vaccForComVersionAccessors = new ComVersionAccessors(providers, config);
        private final DevVersionAccessors vaccForDevVersionAccessors = new DevVersionAccessors(providers, config);
        private final IoVersionAccessors vaccForIoVersionAccessors = new IoVersionAccessors(providers, config);
        private final MeVersionAccessors vaccForMeVersionAccessors = new MeVersionAccessors(providers, config);
        private final NetVersionAccessors vaccForNetVersionAccessors = new NetVersionAccessors(providers, config);
        private final OrgVersionAccessors vaccForOrgVersionAccessors = new OrgVersionAccessors(providers, config);
        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: kotlin (1.9.22)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getKotlin() { return getVersion("kotlin"); }

            /**
             * Returns the version associated to this alias: paper (1.20.4-R0.1-SNAPSHOT)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getPaper() { return getVersion("paper"); }

        /**
         * Returns the group of versions at versions.com
         */
        public ComVersionAccessors getCom() {
            return vaccForComVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.dev
         */
        public DevVersionAccessors getDev() {
            return vaccForDevVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.io
         */
        public IoVersionAccessors getIo() {
            return vaccForIoVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.me
         */
        public MeVersionAccessors getMe() {
            return vaccForMeVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.net
         */
        public NetVersionAccessors getNet() {
            return vaccForNetVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.org
         */
        public OrgVersionAccessors getOrg() {
            return vaccForOrgVersionAccessors;
        }

    }

    public static class ComVersionAccessors extends VersionFactory  {

        private final ComComphenixVersionAccessors vaccForComComphenixVersionAccessors = new ComComphenixVersionAccessors(providers, config);
        private final ComDfsekVersionAccessors vaccForComDfsekVersionAccessors = new ComDfsekVersionAccessors(providers, config);
        private final ComGithubVersionAccessors vaccForComGithubVersionAccessors = new ComGithubVersionAccessors(providers, config);
        private final ComOnarandomboxVersionAccessors vaccForComOnarandomboxVersionAccessors = new ComOnarandomboxVersionAccessors(providers, config);
        private final ComSk89qVersionAccessors vaccForComSk89qVersionAccessors = new ComSk89qVersionAccessors(providers, config);
        public ComVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.comphenix
         */
        public ComComphenixVersionAccessors getComphenix() {
            return vaccForComComphenixVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.com.dfsek
         */
        public ComDfsekVersionAccessors getDfsek() {
            return vaccForComDfsekVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.com.github
         */
        public ComGithubVersionAccessors getGithub() {
            return vaccForComGithubVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.com.onarandombox
         */
        public ComOnarandomboxVersionAccessors getOnarandombox() {
            return vaccForComOnarandomboxVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.com.sk89q
         */
        public ComSk89qVersionAccessors getSk89q() {
            return vaccForComSk89qVersionAccessors;
        }

    }

    public static class ComComphenixVersionAccessors extends VersionFactory  {

        private final ComComphenixProtocolVersionAccessors vaccForComComphenixProtocolVersionAccessors = new ComComphenixProtocolVersionAccessors(providers, config);
        public ComComphenixVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.comphenix.protocol
         */
        public ComComphenixProtocolVersionAccessors getProtocol() {
            return vaccForComComphenixProtocolVersionAccessors;
        }

    }

    public static class ComComphenixProtocolVersionAccessors extends VersionFactory  {

        public ComComphenixProtocolVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: com.comphenix.protocol.protocollib (5.1.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getProtocollib() { return getVersion("com.comphenix.protocol.protocollib"); }

    }

    public static class ComDfsekVersionAccessors extends VersionFactory  {

        private final ComDfsekTerraVersionAccessors vaccForComDfsekTerraVersionAccessors = new ComDfsekTerraVersionAccessors(providers, config);
        public ComDfsekVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.dfsek.terra
         */
        public ComDfsekTerraVersionAccessors getTerra() {
            return vaccForComDfsekTerraVersionAccessors;
        }

    }

    public static class ComDfsekTerraVersionAccessors extends VersionFactory  {

        private final ComDfsekTerraManifestVersionAccessors vaccForComDfsekTerraManifestVersionAccessors = new ComDfsekTerraManifestVersionAccessors(providers, config);
        public ComDfsekTerraVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: com.dfsek.terra.api (6.5.0-BETA+060cbfd0c)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getApi() { return getVersion("com.dfsek.terra.api"); }

            /**
             * Returns the version associated to this alias: com.dfsek.terra.bukkit (6.5.0-BETA+060cbfd0c)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getBukkit() { return getVersion("com.dfsek.terra.bukkit"); }

        /**
         * Returns the group of versions at versions.com.dfsek.terra.manifest
         */
        public ComDfsekTerraManifestVersionAccessors getManifest() {
            return vaccForComDfsekTerraManifestVersionAccessors;
        }

    }

    public static class ComDfsekTerraManifestVersionAccessors extends VersionFactory  {

        private final ComDfsekTerraManifestAddonVersionAccessors vaccForComDfsekTerraManifestAddonVersionAccessors = new ComDfsekTerraManifestAddonVersionAccessors(providers, config);
        public ComDfsekTerraManifestVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.dfsek.terra.manifest.addon
         */
        public ComDfsekTerraManifestAddonVersionAccessors getAddon() {
            return vaccForComDfsekTerraManifestAddonVersionAccessors;
        }

    }

    public static class ComDfsekTerraManifestAddonVersionAccessors extends VersionFactory  {

        public ComDfsekTerraManifestAddonVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: com.dfsek.terra.manifest.addon.loader (1.0.0-BETA+fd6decc70)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getLoader() { return getVersion("com.dfsek.terra.manifest.addon.loader"); }

    }

    public static class ComGithubVersionAccessors extends VersionFactory  {

        private final ComGithubLonedev6VersionAccessors vaccForComGithubLonedev6VersionAccessors = new ComGithubLonedev6VersionAccessors(providers, config);
        private final ComGithubZockeraxelVersionAccessors vaccForComGithubZockeraxelVersionAccessors = new ComGithubZockeraxelVersionAccessors(providers, config);
        public ComGithubVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.github.lonedev6
         */
        public ComGithubLonedev6VersionAccessors getLonedev6() {
            return vaccForComGithubLonedev6VersionAccessors;
        }

        /**
         * Returns the group of versions at versions.com.github.zockeraxel
         */
        public ComGithubZockeraxelVersionAccessors getZockeraxel() {
            return vaccForComGithubZockeraxelVersionAccessors;
        }

    }

    public static class ComGithubLonedev6VersionAccessors extends VersionFactory  {

        private final ComGithubLonedev6ApiVersionAccessors vaccForComGithubLonedev6ApiVersionAccessors = new ComGithubLonedev6ApiVersionAccessors(providers, config);
        public ComGithubLonedev6VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.github.lonedev6.api
         */
        public ComGithubLonedev6ApiVersionAccessors getApi() {
            return vaccForComGithubLonedev6ApiVersionAccessors;
        }

    }

    public static class ComGithubLonedev6ApiVersionAccessors extends VersionFactory  {

        public ComGithubLonedev6ApiVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: com.github.lonedev6.api.itemsadder (3.6.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getItemsadder() { return getVersion("com.github.lonedev6.api.itemsadder"); }

    }

    public static class ComGithubZockeraxelVersionAccessors extends VersionFactory  {

        public ComGithubZockeraxelVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: com.github.zockeraxel.crazyadvancementsapi (v2.1.17a)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getCrazyadvancementsapi() { return getVersion("com.github.zockeraxel.crazyadvancementsapi"); }

    }

    public static class ComOnarandomboxVersionAccessors extends VersionFactory  {

        private final ComOnarandomboxMultiversecoreVersionAccessors vaccForComOnarandomboxMultiversecoreVersionAccessors = new ComOnarandomboxMultiversecoreVersionAccessors(providers, config);
        private final ComOnarandomboxMultiverseinventoriesVersionAccessors vaccForComOnarandomboxMultiverseinventoriesVersionAccessors = new ComOnarandomboxMultiverseinventoriesVersionAccessors(providers, config);
        private final ComOnarandomboxMultiverseportalsVersionAccessors vaccForComOnarandomboxMultiverseportalsVersionAccessors = new ComOnarandomboxMultiverseportalsVersionAccessors(providers, config);
        public ComOnarandomboxVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.onarandombox.multiversecore
         */
        public ComOnarandomboxMultiversecoreVersionAccessors getMultiversecore() {
            return vaccForComOnarandomboxMultiversecoreVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.com.onarandombox.multiverseinventories
         */
        public ComOnarandomboxMultiverseinventoriesVersionAccessors getMultiverseinventories() {
            return vaccForComOnarandomboxMultiverseinventoriesVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.com.onarandombox.multiverseportals
         */
        public ComOnarandomboxMultiverseportalsVersionAccessors getMultiverseportals() {
            return vaccForComOnarandomboxMultiverseportalsVersionAccessors;
        }

    }

    public static class ComOnarandomboxMultiversecoreVersionAccessors extends VersionFactory  {

        private final ComOnarandomboxMultiversecoreMultiverseVersionAccessors vaccForComOnarandomboxMultiversecoreMultiverseVersionAccessors = new ComOnarandomboxMultiversecoreMultiverseVersionAccessors(providers, config);
        public ComOnarandomboxMultiversecoreVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.onarandombox.multiversecore.multiverse
         */
        public ComOnarandomboxMultiversecoreMultiverseVersionAccessors getMultiverse() {
            return vaccForComOnarandomboxMultiversecoreMultiverseVersionAccessors;
        }

    }

    public static class ComOnarandomboxMultiversecoreMultiverseVersionAccessors extends VersionFactory  {

        public ComOnarandomboxMultiversecoreMultiverseVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: com.onarandombox.multiversecore.multiverse.core (4.3.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getCore() { return getVersion("com.onarandombox.multiversecore.multiverse.core"); }

    }

    public static class ComOnarandomboxMultiverseinventoriesVersionAccessors extends VersionFactory  {

        private final ComOnarandomboxMultiverseinventoriesMultiverseVersionAccessors vaccForComOnarandomboxMultiverseinventoriesMultiverseVersionAccessors = new ComOnarandomboxMultiverseinventoriesMultiverseVersionAccessors(providers, config);
        public ComOnarandomboxMultiverseinventoriesVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.onarandombox.multiverseinventories.multiverse
         */
        public ComOnarandomboxMultiverseinventoriesMultiverseVersionAccessors getMultiverse() {
            return vaccForComOnarandomboxMultiverseinventoriesMultiverseVersionAccessors;
        }

    }

    public static class ComOnarandomboxMultiverseinventoriesMultiverseVersionAccessors extends VersionFactory  {

        public ComOnarandomboxMultiverseinventoriesMultiverseVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: com.onarandombox.multiverseinventories.multiverse.inventories (4.2.3)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getInventories() { return getVersion("com.onarandombox.multiverseinventories.multiverse.inventories"); }

    }

    public static class ComOnarandomboxMultiverseportalsVersionAccessors extends VersionFactory  {

        private final ComOnarandomboxMultiverseportalsMultiverseVersionAccessors vaccForComOnarandomboxMultiverseportalsMultiverseVersionAccessors = new ComOnarandomboxMultiverseportalsMultiverseVersionAccessors(providers, config);
        public ComOnarandomboxMultiverseportalsVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.onarandombox.multiverseportals.multiverse
         */
        public ComOnarandomboxMultiverseportalsMultiverseVersionAccessors getMultiverse() {
            return vaccForComOnarandomboxMultiverseportalsMultiverseVersionAccessors;
        }

    }

    public static class ComOnarandomboxMultiverseportalsMultiverseVersionAccessors extends VersionFactory  {

        public ComOnarandomboxMultiverseportalsMultiverseVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: com.onarandombox.multiverseportals.multiverse.portals (4.2.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getPortals() { return getVersion("com.onarandombox.multiverseportals.multiverse.portals"); }

    }

    public static class ComSk89qVersionAccessors extends VersionFactory  {

        private final ComSk89qWorldeditVersionAccessors vaccForComSk89qWorldeditVersionAccessors = new ComSk89qWorldeditVersionAccessors(providers, config);
        private final ComSk89qWorldguardVersionAccessors vaccForComSk89qWorldguardVersionAccessors = new ComSk89qWorldguardVersionAccessors(providers, config);
        public ComSk89qVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.sk89q.worldedit
         */
        public ComSk89qWorldeditVersionAccessors getWorldedit() {
            return vaccForComSk89qWorldeditVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.com.sk89q.worldguard
         */
        public ComSk89qWorldguardVersionAccessors getWorldguard() {
            return vaccForComSk89qWorldguardVersionAccessors;
        }

    }

    public static class ComSk89qWorldeditVersionAccessors extends VersionFactory  {

        private final ComSk89qWorldeditWorldeditVersionAccessors vaccForComSk89qWorldeditWorldeditVersionAccessors = new ComSk89qWorldeditWorldeditVersionAccessors(providers, config);
        public ComSk89qWorldeditVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.sk89q.worldedit.worldedit
         */
        public ComSk89qWorldeditWorldeditVersionAccessors getWorldedit() {
            return vaccForComSk89qWorldeditWorldeditVersionAccessors;
        }

    }

    public static class ComSk89qWorldeditWorldeditVersionAccessors extends VersionFactory  {

        public ComSk89qWorldeditWorldeditVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: com.sk89q.worldedit.worldedit.bukkit (7.2.9)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getBukkit() { return getVersion("com.sk89q.worldedit.worldedit.bukkit"); }

    }

    public static class ComSk89qWorldguardVersionAccessors extends VersionFactory  {

        private final ComSk89qWorldguardWorldguardVersionAccessors vaccForComSk89qWorldguardWorldguardVersionAccessors = new ComSk89qWorldguardWorldguardVersionAccessors(providers, config);
        public ComSk89qWorldguardVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.com.sk89q.worldguard.worldguard
         */
        public ComSk89qWorldguardWorldguardVersionAccessors getWorldguard() {
            return vaccForComSk89qWorldguardWorldguardVersionAccessors;
        }

    }

    public static class ComSk89qWorldguardWorldguardVersionAccessors extends VersionFactory  {

        public ComSk89qWorldguardWorldguardVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: com.sk89q.worldguard.worldguard.bukkit (7.0.12)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getBukkit() { return getVersion("com.sk89q.worldguard.worldguard.bukkit"); }

    }

    public static class DevVersionAccessors extends VersionFactory  {

        private final DevJorelVersionAccessors vaccForDevJorelVersionAccessors = new DevJorelVersionAccessors(providers, config);
        private final DevLoneVersionAccessors vaccForDevLoneVersionAccessors = new DevLoneVersionAccessors(providers, config);
        public DevVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.dev.jorel
         */
        public DevJorelVersionAccessors getJorel() {
            return vaccForDevJorelVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.dev.lone
         */
        public DevLoneVersionAccessors getLone() {
            return vaccForDevLoneVersionAccessors;
        }

    }

    public static class DevJorelVersionAccessors extends VersionFactory  {

        private final DevJorelCommandapiVersionAccessors vaccForDevJorelCommandapiVersionAccessors = new DevJorelCommandapiVersionAccessors(providers, config);
        public DevJorelVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.dev.jorel.commandapi
         */
        public DevJorelCommandapiVersionAccessors getCommandapi() {
            return vaccForDevJorelCommandapiVersionAccessors;
        }

    }

    public static class DevJorelCommandapiVersionAccessors extends VersionFactory  {

        private final DevJorelCommandapiBukkitVersionAccessors vaccForDevJorelCommandapiBukkitVersionAccessors = new DevJorelCommandapiBukkitVersionAccessors(providers, config);
        public DevJorelCommandapiVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.dev.jorel.commandapi.bukkit
         */
        public DevJorelCommandapiBukkitVersionAccessors getBukkit() {
            return vaccForDevJorelCommandapiBukkitVersionAccessors;
        }

    }

    public static class DevJorelCommandapiBukkitVersionAccessors extends VersionFactory  {

        public DevJorelCommandapiBukkitVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: dev.jorel.commandapi.bukkit.core (9.7.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getCore() { return getVersion("dev.jorel.commandapi.bukkit.core"); }

    }

    public static class DevLoneVersionAccessors extends VersionFactory  {

        public DevLoneVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: dev.lone.lonelibs (1.0.58)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getLonelibs() { return getVersion("dev.lone.lonelibs"); }

    }

    public static class IoVersionAccessors extends VersionFactory  {

        private final IoLumineVersionAccessors vaccForIoLumineVersionAccessors = new IoLumineVersionAccessors(providers, config);
        public IoVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.io.lumine
         */
        public IoLumineVersionAccessors getLumine() {
            return vaccForIoLumineVersionAccessors;
        }

    }

    public static class IoLumineVersionAccessors extends VersionFactory  {

        private final IoLumineMythicVersionAccessors vaccForIoLumineMythicVersionAccessors = new IoLumineMythicVersionAccessors(providers, config);
        public IoLumineVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.io.lumine.mythic
         */
        public IoLumineMythicVersionAccessors getMythic() {
            return vaccForIoLumineMythicVersionAccessors;
        }

    }

    public static class IoLumineMythicVersionAccessors extends VersionFactory  {

        public IoLumineMythicVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: io.lumine.mythic.dist (5.6.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getDist() { return getVersion("io.lumine.mythic.dist"); }

    }

    public static class MeVersionAccessors extends VersionFactory  {

        private final MeClipVersionAccessors vaccForMeClipVersionAccessors = new MeClipVersionAccessors(providers, config);
        public MeVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.me.clip
         */
        public MeClipVersionAccessors getClip() {
            return vaccForMeClipVersionAccessors;
        }

    }

    public static class MeClipVersionAccessors extends VersionFactory  {

        public MeClipVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: me.clip.placeholderapi (2.10.10)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getPlaceholderapi() { return getVersion("me.clip.placeholderapi"); }

    }

    public static class NetVersionAccessors extends VersionFactory  {

        private final NetSfVersionAccessors vaccForNetSfVersionAccessors = new NetSfVersionAccessors(providers, config);
        public NetVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.net.sf
         */
        public NetSfVersionAccessors getSf() {
            return vaccForNetSfVersionAccessors;
        }

    }

    public static class NetSfVersionAccessors extends VersionFactory  {

        private final NetSfTrove4jVersionAccessors vaccForNetSfTrove4jVersionAccessors = new NetSfTrove4jVersionAccessors(providers, config);
        public NetSfVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.net.sf.trove4j
         */
        public NetSfTrove4jVersionAccessors getTrove4j() {
            return vaccForNetSfTrove4jVersionAccessors;
        }

    }

    public static class NetSfTrove4jVersionAccessors extends VersionFactory  {

        public NetSfTrove4jVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: net.sf.trove4j.core (3.1.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getCore() { return getVersion("net.sf.trove4j.core"); }

    }

    public static class OrgVersionAccessors extends VersionFactory  {

        private final OrgBetonquestVersionAccessors vaccForOrgBetonquestVersionAccessors = new OrgBetonquestVersionAccessors(providers, config);
        private final OrgJetbrainsVersionAccessors vaccForOrgJetbrainsVersionAccessors = new OrgJetbrainsVersionAccessors(providers, config);
        private final OrgJunitVersionAccessors vaccForOrgJunitVersionAccessors = new OrgJunitVersionAccessors(providers, config);
        private final OrgReflectionsVersionAccessors vaccForOrgReflectionsVersionAccessors = new OrgReflectionsVersionAccessors(providers, config);
        private final OrgVickyVersionAccessors vaccForOrgVickyVersionAccessors = new OrgVickyVersionAccessors(providers, config);
        public OrgVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.org.betonquest
         */
        public OrgBetonquestVersionAccessors getBetonquest() {
            return vaccForOrgBetonquestVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.org.jetbrains
         */
        public OrgJetbrainsVersionAccessors getJetbrains() {
            return vaccForOrgJetbrainsVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.org.junit
         */
        public OrgJunitVersionAccessors getJunit() {
            return vaccForOrgJunitVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.org.reflections
         */
        public OrgReflectionsVersionAccessors getReflections() {
            return vaccForOrgReflectionsVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.org.vicky
         */
        public OrgVickyVersionAccessors getVicky() {
            return vaccForOrgVickyVersionAccessors;
        }

    }

    public static class OrgBetonquestVersionAccessors extends VersionFactory  {

        public OrgBetonquestVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: org.betonquest.betonquest (2.2.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getBetonquest() { return getVersion("org.betonquest.betonquest"); }

    }

    public static class OrgJetbrainsVersionAccessors extends VersionFactory  {

        private final OrgJetbrainsKotlinVersionAccessors vaccForOrgJetbrainsKotlinVersionAccessors = new OrgJetbrainsKotlinVersionAccessors(providers, config);
        private final OrgJetbrainsKotlinxVersionAccessors vaccForOrgJetbrainsKotlinxVersionAccessors = new OrgJetbrainsKotlinxVersionAccessors(providers, config);
        public OrgJetbrainsVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.org.jetbrains.kotlin
         */
        public OrgJetbrainsKotlinVersionAccessors getKotlin() {
            return vaccForOrgJetbrainsKotlinVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.org.jetbrains.kotlinx
         */
        public OrgJetbrainsKotlinxVersionAccessors getKotlinx() {
            return vaccForOrgJetbrainsKotlinxVersionAccessors;
        }

    }

    public static class OrgJetbrainsKotlinVersionAccessors extends VersionFactory  {

        private final OrgJetbrainsKotlinKotlinVersionAccessors vaccForOrgJetbrainsKotlinKotlinVersionAccessors = new OrgJetbrainsKotlinKotlinVersionAccessors(providers, config);
        public OrgJetbrainsKotlinVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.org.jetbrains.kotlin.kotlin
         */
        public OrgJetbrainsKotlinKotlinVersionAccessors getKotlin() {
            return vaccForOrgJetbrainsKotlinKotlinVersionAccessors;
        }

    }

    public static class OrgJetbrainsKotlinKotlinVersionAccessors extends VersionFactory  {

        public OrgJetbrainsKotlinKotlinVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: org.jetbrains.kotlin.kotlin.test (1.9.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getTest() { return getVersion("org.jetbrains.kotlin.kotlin.test"); }

    }

    public static class OrgJetbrainsKotlinxVersionAccessors extends VersionFactory  {

        private final OrgJetbrainsKotlinxKotlinxVersionAccessors vaccForOrgJetbrainsKotlinxKotlinxVersionAccessors = new OrgJetbrainsKotlinxKotlinxVersionAccessors(providers, config);
        public OrgJetbrainsKotlinxVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.org.jetbrains.kotlinx.kotlinx
         */
        public OrgJetbrainsKotlinxKotlinxVersionAccessors getKotlinx() {
            return vaccForOrgJetbrainsKotlinxKotlinxVersionAccessors;
        }

    }

    public static class OrgJetbrainsKotlinxKotlinxVersionAccessors extends VersionFactory  {

        private final OrgJetbrainsKotlinxKotlinxSerializationVersionAccessors vaccForOrgJetbrainsKotlinxKotlinxSerializationVersionAccessors = new OrgJetbrainsKotlinxKotlinxSerializationVersionAccessors(providers, config);
        public OrgJetbrainsKotlinxKotlinxVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.org.jetbrains.kotlinx.kotlinx.serialization
         */
        public OrgJetbrainsKotlinxKotlinxSerializationVersionAccessors getSerialization() {
            return vaccForOrgJetbrainsKotlinxKotlinxSerializationVersionAccessors;
        }

    }

    public static class OrgJetbrainsKotlinxKotlinxSerializationVersionAccessors extends VersionFactory  {

        public OrgJetbrainsKotlinxKotlinxSerializationVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: org.jetbrains.kotlinx.kotlinx.serialization.json (1.6.3)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getJson() { return getVersion("org.jetbrains.kotlinx.kotlinx.serialization.json"); }

    }

    public static class OrgJunitVersionAccessors extends VersionFactory  {

        private final OrgJunitJupiterVersionAccessors vaccForOrgJunitJupiterVersionAccessors = new OrgJunitJupiterVersionAccessors(providers, config);
        public OrgJunitVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.org.junit.jupiter
         */
        public OrgJunitJupiterVersionAccessors getJupiter() {
            return vaccForOrgJunitJupiterVersionAccessors;
        }

    }

    public static class OrgJunitJupiterVersionAccessors extends VersionFactory  {

        private final OrgJunitJupiterJunitVersionAccessors vaccForOrgJunitJupiterJunitVersionAccessors = new OrgJunitJupiterJunitVersionAccessors(providers, config);
        public OrgJunitJupiterVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.org.junit.jupiter.junit
         */
        public OrgJunitJupiterJunitVersionAccessors getJunit() {
            return vaccForOrgJunitJupiterJunitVersionAccessors;
        }

    }

    public static class OrgJunitJupiterJunitVersionAccessors extends VersionFactory  {

        private final OrgJunitJupiterJunitJupiterVersionAccessors vaccForOrgJunitJupiterJunitJupiterVersionAccessors = new OrgJunitJupiterJunitJupiterVersionAccessors(providers, config);
        public OrgJunitJupiterJunitVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.org.junit.jupiter.junit.jupiter
         */
        public OrgJunitJupiterJunitJupiterVersionAccessors getJupiter() {
            return vaccForOrgJunitJupiterJunitJupiterVersionAccessors;
        }

    }

    public static class OrgJunitJupiterJunitJupiterVersionAccessors extends VersionFactory  {

        public OrgJunitJupiterJunitJupiterVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: org.junit.jupiter.junit.jupiter.api (5.11.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getApi() { return getVersion("org.junit.jupiter.junit.jupiter.api"); }

            /**
             * Returns the version associated to this alias: org.junit.jupiter.junit.jupiter.params (5.11.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getParams() { return getVersion("org.junit.jupiter.junit.jupiter.params"); }

    }

    public static class OrgReflectionsVersionAccessors extends VersionFactory  {

        public OrgReflectionsVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: org.reflections.reflections (0.10.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getReflections() { return getVersion("org.reflections.reflections"); }

    }

    public static class OrgVickyVersionAccessors extends VersionFactory  {

        private final OrgVickyVickyVersionAccessors vaccForOrgVickyVickyVersionAccessors = new OrgVickyVickyVersionAccessors(providers, config);
        public OrgVickyVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.org.vicky.vicky
         */
        public OrgVickyVickyVersionAccessors getVicky() {
            return vaccForOrgVickyVickyVersionAccessors;
        }

    }

    public static class OrgVickyVickyVersionAccessors extends VersionFactory  {

        private final OrgVickyVickyUtilsVersionAccessors vaccForOrgVickyVickyUtilsVersionAccessors = new OrgVickyVickyUtilsVersionAccessors(providers, config);
        public OrgVickyVickyVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.org.vicky.vicky.utils
         */
        public OrgVickyVickyUtilsVersionAccessors getUtils() {
            return vaccForOrgVickyVickyUtilsVersionAccessors;
        }

    }

    public static class OrgVickyVickyUtilsVersionAccessors extends VersionFactory  {

        private final OrgVickyVickyUtilsVVersionAccessors vaccForOrgVickyVickyUtilsVVersionAccessors = new OrgVickyVickyUtilsVVersionAccessors(providers, config);
        public OrgVickyVickyUtilsVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of versions at versions.org.vicky.vicky.utils.v
         */
        public OrgVickyVickyUtilsVVersionAccessors getV() {
            return vaccForOrgVickyVickyUtilsVVersionAccessors;
        }

    }

    public static class OrgVickyVickyUtilsVVersionAccessors extends VersionFactory  {

        public OrgVickyVickyUtilsVVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: org.vicky.vicky.utils.v.utls (0.0.1-BETA)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getUtls() { return getVersion("org.vicky.vicky.utils.v.utls"); }

    }

    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

    }

    public static class PluginAccessors extends PluginFactory {
        private final KotlinPluginAccessors paccForKotlinPluginAccessors = new KotlinPluginAccessors(providers, config);

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the group of plugins at plugins.kotlin
         */
        public KotlinPluginAccessors getKotlin() {
            return paccForKotlinPluginAccessors;
        }

    }

    public static class KotlinPluginAccessors extends PluginFactory {

        public KotlinPluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a plugin provider for kotlin.jvm to the plugin id 'org.jetbrains.kotlin.jvm'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getJvm() { return createPlugin("kotlin.jvm"); }

    }

}
