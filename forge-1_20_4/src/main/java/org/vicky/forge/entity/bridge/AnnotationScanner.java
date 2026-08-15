package org.vicky.forge.entity.bridge;

import cpw.mods.jarhandling.SecureJar;
import io.github.classgraph.*;
import net.minecraftforge.fml.loading.moddiscovery.BackgroundScanHandler;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.locating.IModFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * Annotation scanner for Forge mod files.
 *
 * <p>
 * This scanner intentionally avoids assuming that every Forge Path is backed
 * by the default filesystem. Forge can expose paths using filesystem
 * providers such as union:, jar:, etc.
 * </p>
 *
 * <p>
 * ClassGraph is used strictly for metadata discovery. Classes are not loaded
 * or initialized while scanning.
 * </p>
 */
public final class AnnotationScanner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AnnotationScanner.class);

    private AnnotationScanner() {
    }

    public enum MemberKind {CLASS, FIELD, METHOD}

    public static final class ScanResult {
        public final String annotationClassName;
        public final String ownerClassName;
        public final MemberKind kind;
        public final String memberName; // for field/method
        public final Map<String, AnnotationParameterValue> annotationValues;
        public final ModFile modFile;
        public final SecureJar secureJar;

        public ScanResult(String annotationClassName,
                          String ownerClassName,
                          MemberKind kind,
                          String memberName,
                          Map<String, AnnotationParameterValue> annotationValues,
                          ModFile modFile,
                          SecureJar secureJar) {
            this.annotationClassName = annotationClassName;
            this.ownerClassName = ownerClassName;
            this.kind = kind;
            this.memberName = memberName;
            this.annotationValues = Collections.unmodifiableMap(new LinkedHashMap<>(annotationValues));
            this.modFile = modFile;
            this.secureJar = secureJar;
        }

        @Override
        public String toString() {
            return "ScanResult{" + annotationClassName + " on " + ownerClassName +
                    (memberName == null ? "" : ("#" + memberName));
        }
    }

    private static final List<ScanResult> SCANNED_CACHE = new ArrayList<>();

    public static void scanAll(
            BackgroundScanHandler handler, List<Class<? extends Annotation>> annotations) {
        SCANNED_CACHE.addAll(scanFor(handler, annotations));
    }

    public static List<ScanResult> getResults() {
        return SCANNED_CACHE;
    }

    /**
     * Scan all relevant Forge mod files for a particular annotation.
     *
     * @param handler         Forge background scan handler
     * @param annotationClass annotation to search for
     * @return discovered annotation usages
     */
    public static List<ScanResult> scanFor(
            BackgroundScanHandler handler,
            Class<? extends Annotation> annotationClass
    ) {
        return scanFor(handler, List.of(annotationClass));
    }

    /**
     * Scan all relevant Forge mod files for a particular annotation.
     *
     * @param handler           Forge background scan handler
     * @param annotationClasses annotations to search for
     * @return discovered annotation usages
     */
    public static List<ScanResult> scanFor(
            BackgroundScanHandler handler,
            List<Class<? extends Annotation>> annotationClasses
    ) {
        Objects.requireNonNull(handler, "handler");
        Objects.requireNonNull(annotationClasses, "annotationClass");

        LOGGER.info(
                "Scanning Forge mod files for annotations: {}",
                String.join(", ", annotationClasses.stream().map(Class::getSimpleName).map(it -> "@" + it).toList())
        );

        List<ModFile> modFiles = getModFiles(handler);

        if (modFiles.isEmpty()) {
            LOGGER.warn("AnnotationScanner: no mod files available");
            return Collections.emptyList();
        }

        List<ScanResult> results =
                new ArrayList<>();

        for (ModFile modFile : modFiles) {
            if (modFile == null) {
                continue;
            }

            /*
             * Only scan actual mods.
             *
             * Forge also exposes things such as libraries and Forge itself
             * through its discovery infrastructure.
             */
            if (modFile.getType() != IModFile.Type.MOD &&
                    modFile.getType() != IModFile.Type.GAMELIBRARY) {
                continue;
            }

            if (!shouldScan(modFile)) {
                continue;
            }

            LOGGER.info(
                    "Scanning {}: {}",
                    modFile.getType(),
                    modFile.getFileName()
            );

            SecureJar secureJar = getSecureJar(modFile);

            if (secureJar == null) {
                LOGGER.warn(
                        "Unable to obtain SecureJar for {}",
                        modFile.getFileName()
                );
                continue;
            }


            scanMod(
                    modFile,
                    secureJar,
                    annotationClasses,
                    results
            );
        }

        /*
         * ------------------------------------------------------------
         * 2. Development/runtime classpath
         * ------------------------------------------------------------
         *
         * This is important for ForgeGradle's exploded `main` output and
         * dependencies available directly to the IDE.
         */
        if (isDevelopmentEnvironment()) {
            scanDevelopmentClasspath(
                    annotationClasses,
                    results
            );
        }

        LOGGER.info(
                "Annotation scan complete. Found {} result(s) for {}",
                results.size(),
                String.join(", ", annotationClasses.stream().map(Class::getSimpleName).map(it -> "@" + it).toList())
        );

        return results;
    }

    private static void scanDevelopmentClasspath(
            List<Class<? extends Annotation>> annotations,
            List<ScanResult> results
    ) {
        LOGGER.info("Scanning development/runtime classpath for {}",
                String.join(", ", annotations.stream().map(Class::getSimpleName).map(it -> "@" + it).toList()));

        String classPath = System.getProperty("java.class.path");

        if (classPath == null || classPath.isBlank()) {
            LOGGER.debug("java.class.path is empty");
            return;
        }

        List<String> entries = new ArrayList<>();

        for (String entry : classPath.split(
                java.util.regex.Pattern.quote(
                        File.pathSeparator
                )
        )) {
            if (entry.isBlank()) {
                continue;
            }

            Path path;

            try {
                path = Paths.get(entry);
            } catch (Exception e) {
                LOGGER.debug("Unable to parse classpath entry {}", entry);
                continue;
            }

            if (!Files.exists(path)) {
                continue;
            }

            /*
             * Don't scan Forge itself or obvious runtime internals.
             */
            String lower = path.getFileName() == null
                    ? path.toString().toLowerCase(Locale.ROOT)
                    : path.getFileName().toString().toLowerCase(Locale.ROOT);

            if (lower.contains("forge")
                    || lower.contains("fmlcore")
                    || lower.contains("javafmllanguage")
                    || lower.contains("mclanguage")
                    || lower.contains("lowcodelanguage")) {
                continue;
            }

            entries.add(path.toString());

            LOGGER.debug(
                    "Development classpath entry: {}",
                    path
            );
        }

        if (entries.isEmpty()) {
            return;
        }

        try (io.github.classgraph.ScanResult scan =
                     new ClassGraph()
                             .overrideClasspath(entries)
                             .enableClassInfo()
                             .enableFieldInfo()
                             .enableAnnotationInfo()
                             .ignoreClassVisibility()
                             .ignoreFieldVisibility()
                             .scan()) {

            /*
             * Development classes.
             *
             * These don't belong to a ModFile, so the ScanResult intentionally
             * uses null for modFile/SecureJar.
             */
            scanClasses(
                    scan,
                    annotations,
                    null,
                    null,
                    results
            );

            scanFields(
                    scan,
                    annotations,
                    null,
                    null,
                    results
            );

        } catch (Throwable t) {
            LOGGER.error(
                    "Failed to scan development classpath",
                    t
            );
        }
    }

    // -------------------------------------------------------------------------
    // Mod discovery
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static List<ModFile> getModFiles(
            BackgroundScanHandler handler
    ) {
        /*
         * Prefer the public method if available.
         */
        try {
            Method method =
                    handler.getClass().getMethod("getModFiles");

            Object value = method.invoke(handler);

            if (value instanceof List<?> list) {
                return (List<ModFile>) list;
            }
        } catch (Throwable ignored) {
        }

        /*
         * Compatibility fallback for Forge versions where the field is
         * available but the method isn't.
         */
        try {
            var field =
                    handler.getClass().getDeclaredField("modFiles");

            field.setAccessible(true);

            Object value = field.get(handler);

            if (value instanceof List<?> list) {
                return (List<ModFile>) list;
            }
        } catch (Throwable ignored) {
        }

        LOGGER.warn(
                "Unable to obtain ModFile list from BackgroundScanHandler"
        );

        return Collections.emptyList();
    }

    private static boolean shouldScan(ModFile modFile) {
        String name = modFile.getFileName();

        if (name == null) {
            return true;
        }

        /*
         * Do NOT use startsWith("forge-") as the primary mechanism for
         * determining what is a mod. Forge's ModFile type has already done
         * that job.
         *
         * This is only a safety exclusion for Forge's own distribution.
         */
        String lower = name.toLowerCase(Locale.ROOT);

        if (lower.startsWith("forge-")) {
            LOGGER.debug(
                    "Skipping Forge distribution: {}",
                    name
            );

            return false;
        }

        return true;
    }

    private static SecureJar getSecureJar(ModFile modFile) {
        try {
            return modFile.getSecureJar();
        } catch (Throwable t) {
            LOGGER.warn(
                    "Failed to obtain SecureJar from {}",
                    modFile.getFileName(),
                    t
            );

            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Scanning
    // -------------------------------------------------------------------------

    private static void scanMod(
            ModFile mf,
            SecureJar secureJar,
            List<Class<? extends Annotation>> targetAnns,
            List<ScanResult> out
    ) {

        Path root = secureJar.getRootPath();

        if (root == null) {
            LOGGER.warn(
                    "SecureJar for {} has no root path",
                    mf.getFileName()
            );
            return;
        }

        List<String> classpath = new ArrayList<>();

        addForgePath(classpath, root);

        if (classpath.isEmpty()) {
            LOGGER.warn(
                    "No usable ClassGraph roots for {}",
                    mf.getFileName()
            );
            return;
        }

        try (io.github.classgraph.ScanResult sr =
                     new io.github.classgraph.ClassGraph()
                             .overrideClasspath(classpath)
                             .enableClassInfo()
                             .enableFieldInfo()
                             .enableAnnotationInfo()
                             .ignoreClassVisibility()
                             .ignoreFieldVisibility()
                             .scan()) {

            scanClasses(sr, targetAnns, mf, secureJar, out);
            scanFields(sr, targetAnns, mf, secureJar, out);

        } catch (Throwable t) {
            LOGGER.error(
                    "Failed to scan {}",
                    mf.getFileName(),
                    t
            );
        }
    }

    private static void addForgePath(
            List<String> classpath,
            Path path
    ) {
        if (path == null) {
            return;
        }

        try {
            FileSystem fs = path.getFileSystem();

            String scheme =
                    fs.provider().getScheme();

            if ("file".equalsIgnoreCase(scheme)) {
                classpath.add(path.toAbsolutePath().toString());
            } else {
                /*
                 * Important:
                 *
                 * DO NOT call path.toFile() here.
                 *
                 * Forge may give us union:/, jar:/, etc.
                 */
                classpath.add(path.toUri().toString());
            }

        } catch (Throwable t) {
            LOGGER.debug(
                    "Could not convert Forge root to ClassGraph classpath: {}",
                    path,
                    t
            );
        }
    }

    // -------------------------------------------------------------------------
    // Class annotations
    // -------------------------------------------------------------------------

    private static void scanClasses(
            io.github.classgraph.ScanResult scan,
            List<Class<? extends Annotation>> annotations,
            ModFile modFile,
            SecureJar secureJar,
            List<ScanResult> results
    ) {
        for (var ann : annotations)
            for (ClassInfo classInfo :
                    scan.getClassesWithAnnotation(ann.getName())) {

                String className = classInfo.getName();

                Map<String, AnnotationParameterValue> attributes =
                        readAnnotationValues(
                                classInfo.getAnnotationInfo(ann.getName())
                        );

                results.add(
                        new ScanResult(
                                ann.getName(),
                                className,
                                MemberKind.CLASS,
                                null,
                                attributes,
                                modFile,
                                secureJar
                        )
                );

                LOGGER.info(
                        "Found @{} on class {} in {}",
                        ann.getName(),
                        className,
                        modFile != null ? modFile.getFileName() : "null"
                );
            }
    }

    // -------------------------------------------------------------------------
    // Field annotations
    // -------------------------------------------------------------------------

    private static void scanFields(
            io.github.classgraph.ScanResult scan,
            List<Class<? extends Annotation>> annotations,
            ModFile modFile,
            SecureJar secureJar,
            List<ScanResult> results
    ) {
        for (var ann : annotations)
            for (ClassInfo classInfo :
                    scan.getClassesWithFieldAnnotation(ann.getName())) {

                String className = classInfo.getName();

                try {
                    for (FieldInfo field :
                            classInfo.getDeclaredFieldInfo()) {

                        AnnotationInfo annotation =
                                field.getAnnotationInfo(ann.getName());

                        if (annotation == null) {
                            continue;
                        }

                        Map<String, AnnotationParameterValue> attributes =
                                readAnnotationValues(annotation);

                        results.add(
                                new ScanResult(
                                        ann.getName(),
                                        className,
                                        MemberKind.FIELD,
                                        field.getName(),
                                        attributes,
                                        modFile,
                                        secureJar
                                )
                        );

                        LOGGER.info(
                                "Found @{} on {}#{} in {}",
                                ann.getName(),
                                className,
                                field.getName(),
                                modFile != null ? modFile.getFileName() : "null"
                        );
                    }
                } catch (Throwable t) {
                    LOGGER.debug(
                            "Failed to inspect fields of {}",
                            className,
                            t
                    );
                }
            }
    }

    private static boolean isDevelopmentEnvironment() {
        /*
         * ForgeGradle normally exposes this property when running a
         * development client/server.
         */
        return System.getProperty("forge.enableGameTest") != null
                || System.getProperty("fml.earlyprogresswindow") != null
                || isLikelyDevelopmentClasspath();
    }

    private static boolean isLikelyDevelopmentClasspath() {
        String cp = System.getProperty("java.class.path");

        if (cp == null) {
            return false;
        }

        return cp.contains("build")
                || cp.contains("classes")
                || cp.contains("resources")
                || cp.contains("run");
    }

    // -------------------------------------------------------------------------
    // Annotation metadata
    // -------------------------------------------------------------------------

    private static Map<String, AnnotationParameterValue> readAnnotationValues(
            AnnotationInfo annotation
    ) {
        if (annotation == null) {
            return Collections.emptyMap();
        }

        try {
            return annotation
                    .getParameterValues()
                    .asMap();
        } catch (Throwable t) {
            LOGGER.debug(
                    "Failed to read annotation parameters",
                    t
            );

            return Collections.emptyMap();
        }
    }

    // -------------------------------------------------------------------------
    // Internal ClassLoader adapter
    // -------------------------------------------------------------------------

    /**
     * ClassLoader which exposes a Forge root to ClassGraph without forcing
     * the root through Path.toFile().
     * <p>
     * This is mainly useful for ForgeGradle's exploded development paths.
     */
    private static final class ForgePathClassLoader
            extends ClassLoader {

        private final Path root;

        private ForgePathClassLoader(Path root) {
            super(AnnotationScanner.class.getClassLoader());
            this.root = root;
        }

        @Override
        protected Class<?> findClass(String name)
                throws ClassNotFoundException {

            /*
             * We intentionally don't load classes ourselves.
             *
             * ClassGraph only needs this loader to expose the root to its
             * classpath discovery machinery.
             */
            throw new ClassNotFoundException(name);
        }

        @Override
        public java.net.URL getResource(String name) {

            try {
                Path resource = root.resolve(name);

                if (!Files.exists(resource)) {
                    return null;
                }

                return resource.toUri().toURL();
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    /**
     * Try to load the runtime value of a field described by a ScanResult (if it's a field).
     * This WILL attempt several classloaders and can run initializers; use only when you accept that.
     */
    public static Optional<Object> resolveFieldValue(ScanResult sr, Class<?>... preferLoaders) {
        if (sr.kind != MemberKind.FIELD || sr.memberName == null) return Optional.empty();

        List<ClassLoader> loaders = new ArrayList<>();
        if (preferLoaders != null) {
            for (Class<?> c : preferLoaders) if (c != null) loaders.add(c.getClassLoader());
        }

        if (sr.modFile != null) {
            try {
                Method m = sr.modFile.getClass().getMethod("getClassLoader");
                Object clo = m.invoke(sr.modFile);
                if (clo instanceof ClassLoader cl) loaders.add(cl);
            } catch (Throwable ignored) {
            }
        }
        if (sr.secureJar != null) {
            try {
                Method m = sr.secureJar.getClass().getMethod("getClassLoader");
                Object clo = m.invoke(sr.secureJar);
                if (clo instanceof ClassLoader cl) loaders.add(cl);
            } catch (Throwable ignored) {
            }
        }

        ClassLoader ctx = Thread.currentThread().getContextClassLoader();
        if (ctx != null) loaders.add(ctx);
        ClassLoader sys = ClassLoader.getSystemClassLoader();
        if (sys != null) loaders.add(sys);

        // distinct
        List<ClassLoader> tryLoaders = loaders.stream().filter(Objects::nonNull).distinct().toList();

        for (ClassLoader loader : tryLoaders) {
            try {
                Class<?> owner = Class.forName(sr.ownerClassName, false, loader);
                Field f = owner.getDeclaredField(sr.memberName);
                f.setAccessible(true);
                Object inst = null;
                if (!Modifier.isStatic(f.getModifiers())) {
                    try {
                        Field instField = owner.getDeclaredField("INSTANCE");
                        instField.setAccessible(true);
                        inst = instField.get(null);
                    } catch (NoSuchFieldException nsf) {
                        inst = owner.getDeclaredConstructor().newInstance();
                    }
                }
                return Optional.ofNullable(f.get(inst));
            } catch (Throwable t) {
                // try next loader
            }
        }
        return Optional.empty();
    }
}