package org.vicky.forge.entity.bridge;

import cpw.mods.jarhandling.SecureJar;
import io.github.classgraph.AnnotationParameterValue;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.BackgroundScanHandler;
import net.minecraftforge.forgespi.locating.IModFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vicky.platform.items.Items;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Lightweight, re-usable annotation scanner using BackgroundScanHandler -> ModFile list
 */
public final class AnnotationScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationScanner.class);

    private AnnotationScanner() {}

    public enum MemberKind { CLASS, FIELD, METHOD }

    public static final class ScanResult {
        public final String annotationClassName;
        public final String ownerClassName;
        public final MemberKind kind;
        public final String memberName; // for field/method
        public final Map<String,AnnotationParameterValue> annotationValues;
        public final Path jarPath;
        public final ModFile modFile;
        public final SecureJar secureJar;

        public ScanResult(String annotationClassName,
                          String ownerClassName,
                          MemberKind kind,
                          String memberName,
                          Map<String,AnnotationParameterValue> annotationValues,
                          Path jarPath,
                          ModFile modFile,
                          SecureJar secureJar) {
            this.annotationClassName = annotationClassName;
            this.ownerClassName = ownerClassName;
            this.kind = kind;
            this.memberName = memberName;
            this.annotationValues = Collections.unmodifiableMap(new LinkedHashMap<>(annotationValues));
            this.jarPath = jarPath;
            this.modFile = modFile;
            this.secureJar = secureJar;
        }

        @Override
        public String toString() {
            return "ScanResult{" + annotationClassName + " on " + ownerClassName +
                    (memberName == null ? "" : ("#" + memberName)) +
                    " in " + (jarPath == null ? "unknown-jar" : jarPath.getFileName()) + "}";
        }
    }

    /**
     * Main entry — scan all ModFile entries provided by BackgroundScanHandler
     */
    public static List<ScanResult> scanFor(BackgroundScanHandler handler, Class<? extends Annotation> annotationClass) {
        Objects.requireNonNull(handler);
        Objects.requireNonNull(annotationClass);

        final String targetAnnName = annotationClass.getName();
        final List<ScanResult> results = new ArrayList<>();
        LOGGER.info("Scanning for annotation: {}", targetAnnName);

        List<ModFile> modFiles;
        try {
            modFiles = handler.getModFiles(); // BackgroundScanHandler.getModFiles() or .modFiles() depending on version
        } catch (Throwable t) {
            // fallback: try reflection field
            modFiles = tryGetModFilesReflectively(handler);
        }
        if (modFiles == null || modFiles.isEmpty()) {
            LOGGER.warn("AnnotationScanner: no mod files returned by BackgroundScanHandler");
            return results;
        }

        for (ModFile mf : modFiles) {
            if (mf.getType() != IModFile.Type.MOD) continue;
            if (mf.getFileName().contains("forge-")) continue;
            LOGGER.info("Deep Scanning {} - {}", mf.getType().name().toLowerCase(), mf.getFileName());
            Path jarPath = tryResolveModFilePath(mf);
            SecureJar secureJar = tryGetSecureJarFromModFile(mf);

            results.addAll(scanLibraryJarFor(jarPath, targetAnnName, mf, secureJar));
        }

        return results;
    }

    /* ---------------- helpers ---------------- */

    private static List<ModFile> tryGetModFilesReflectively(BackgroundScanHandler handler) {
        try {
            Method m = handler.getClass().getMethod("getModFiles");
            Object o = m.invoke(handler);
            if (o instanceof List) return (List<ModFile>) o;
        } catch (Throwable ignored) {}
        try { // try field 'modFiles'
            var f = handler.getClass().getDeclaredField("modFiles");
            f.setAccessible(true);
            Object o = f.get(handler);
            if (o instanceof List) return (List<ModFile>) o;
        } catch (Throwable ignored) {}
        return Collections.emptyList();
    }

    private static List<ScanResult> scanLibraryJarFor(
            Path jarPath,
            String targetAnnName,
            ModFile mf,
            SecureJar sj) {

        List<ScanResult> out = new ArrayList<>();
        if (mf == null) {
            LOGGER.warn("scanLibraryJarFor: ModFile is null for path {}", jarPath);
            return out;
        }

        Path outerJar = mf.getFilePath();
        if (outerJar == null) {
            LOGGER.warn("scanLibraryJarFor: outer jar path null for ModFile {}", mf.getFileName());
            return out;
        }

        String outerName = outerJar.getFileName() == null ? outerJar.toString() : outerJar.getFileName().toString();
        // fast skip for known large / irrelevant libs
        if (outerName.startsWith("classgraph") || outerName.startsWith("kotlin") || outerName.startsWith("jackson")) {
            LOGGER.debug("Skipping heavy/irrelevant jar: {}", outerName);
            return out;
        }

        LOGGER.info("  Path: {}", outerJar.toUri());

        // gather classpath entries: outer jar + the target jarPath + any extracted nested jars
        List<String> classpathEntries = new ArrayList<>();
        classpathEntries.add(outerJar.toUri().toString());

        // IMPORTANT: include the requested jarPath (the library we want to scan)
        if (jarPath != null) {
            try {
                if (Files.isDirectory(jarPath)) {
                    // if a folder was passed, add all jars in that folder
                    try (DirectoryStream<Path> ds = Files.newDirectoryStream(jarPath, "*.jar")) {
                        for (Path p : ds) {
                            classpathEntries.add(p.toUri().toString());
                            LOGGER.debug("Adding jar from dir to classpath entries: {}", p);
                        }
                    } catch (IOException e) {
                        LOGGER.debug("Failed to list jars in {}: {}", jarPath, e.toString());
                    }
                } else {
                    classpathEntries.add(jarPath.toUri().toString());
                }
            } catch (Throwable t) {
                LOGGER.warn("Failed to include jarPath {} in classpath entries: {}", jarPath, t.toString());
            }
        }

        // keep temporary files so we can delete them later
        List<Path> tempJars = new ArrayList<>();

        // Extract nested jars from common locations (META-INF/jarjar/, BOOT-INF/lib/, lib/)
        try (JarFile jarFile = new JarFile(outerJar.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                // Only extract nested jars from known locations.
                if ((name.startsWith("META-INF/jarjar/") || name.startsWith("BOOT-INF/lib/") || name.startsWith("lib/"))
                        && name.endsWith(".jar")) {

                    LOGGER.debug("Found nested jar entry: {} in {}", name, outerName);

                    try (InputStream in = jarFile.getInputStream(entry)) {
                        Path tempJar = Files.createTempFile("cg-nested-", ".jar");
                        Files.copy(in, tempJar, StandardCopyOption.REPLACE_EXISTING);
                        tempJars.add(tempJar);
                        classpathEntries.add(tempJar.toUri().toString());
                        LOGGER.debug("Extracted nested jar to {}", tempJar);
                    } catch (Throwable t) {
                        LOGGER.warn("Failed to extract nested entry {} from {}: {}", name, outerName, t.toString());
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.warn("Failed to read outer jar {} for nested jars: {}", outerJar, t.toString());
            // Fall through: we'll still attempt to scan the outer jar below.
        }

        LOGGER.debug("Classpath entries for ClassGraph scan: {}", classpathEntries);

        // Run a single ClassGraph scan over outer + nested temp jars + target jarPath
        try (io.github.classgraph.ScanResult sr = new io.github.classgraph.ClassGraph()
                .overrideClasspath(classpathEntries)
                .enableClassInfo()
                .enableFieldInfo()
                .enableAnnotationInfo()
                .ignoreClassVisibility()
                .ignoreFieldVisibility()
                .removeTemporaryFilesAfterScan()
                .verbose()
                .scan()) {

            for (io.github.classgraph.ClassInfo ci : sr.getAllClasses()) {
                if (Objects.equals(ci.getName(), Items.class.getName())) {
                    LOGGER.info("Found Items class");
                    LOGGER.info("annotationsA: {}", ci.getAnnotationInfo());
                    LOGGER.info("annotationsB: {}", ci.getAnnotations());
                }
            }

            sr.getClassesWithAnnotation(targetAnnName).forEach(ci -> {
                String className = ci.getName();
                LOGGER.info("Found Class ClassInfo for {}: {}", className, ci);
                Map<String, AnnotationParameterValue> attrs = Collections.emptyMap();
                try {
                    io.github.classgraph.AnnotationInfo ainfo = ci.getAnnotationInfo(targetAnnName);
                    if (ainfo != null) {
                        attrs = ainfo.getParameterValues().asMap();
                    }
                } catch (Throwable t) {
                    LOGGER.info("Failed to read annotation info for {}: {}", className, t.toString());
                }
                out.add(new ScanResult(targetAnnName, className, MemberKind.CLASS, null, attrs, jarPath, mf, sj));
                LOGGER.info("Found class-level annotation {} in {}", targetAnnName, className);
            });

            // Field-level annotations (use ClassGraph metadata to inspect fields)
            sr.getClassesWithFieldAnnotation(targetAnnName).forEach(ci -> {
                String className = ci.getName();
                LOGGER.info("Found Field ClassInfo for {}: {}", className, ci);
                try {
                    for (io.github.classgraph.FieldInfo fInfo : ci.getDeclaredFieldInfo()) {
                        io.github.classgraph.AnnotationInfo afi = fInfo.getAnnotationInfo(targetAnnName);
                        if (afi == null) continue;
                        Map<String, AnnotationParameterValue> attrs = afi.getParameterValues().asMap();
                        out.add(new ScanResult(targetAnnName, className, MemberKind.FIELD, fInfo.getName(), attrs, jarPath, mf, sj));
                        LOGGER.info("Found field-level annotation {} on {}#{}", targetAnnName, className, fInfo.getName());
                    }
                } catch (Throwable t) {
                    LOGGER.info("Failed to inspect fields metadata of {}: {}", className, t.toString());
                }
            });

        } catch (Throwable t) {
            LOGGER.info("ClassGraph fallback scan failed for {}: {}", outerName, t.toString());
        } finally {
            // cleanup temporary nested jars
            for (Path p : tempJars) {
                try {
                    Files.deleteIfExists(p);
                    LOGGER.debug("Deleted temp nested jar {}", p);
                } catch (Throwable ignored) {}
            }
        }

        return out;
    }

    private static Map<String,Object> extractAnnotationValuesSafe(Annotation ann) {
        if (ann == null) return Collections.emptyMap();
        try {
            return extractAnnotationValues(ann);
        } catch (Throwable t) {
            return Collections.emptyMap();
        }
    }

    private static Map<String,Object> extractAnnotationValues(Annotation ann) throws Exception {
        Map<String,Object> map = new LinkedHashMap<>();
        Class<? extends Annotation> at = ann.annotationType();
        for (Method m : at.getDeclaredMethods()) {
            Object val = m.invoke(ann);
            map.put(m.getName(), val);
        }
        return map;
    }

    private static Path tryResolveModFilePath(ModFile mf) {
        if (mf == null) return null;
        else return mf.getSecureJar().getRootPath();
    }

    private static SecureJar tryGetSecureJarFromModFile(ModFile mf) {
        if (mf == null) return null;
        try {
            Method m = mf.getClass().getMethod("getSecureJar");
            Object o = m.invoke(mf);
            if (o instanceof SecureJar s) return s;
        } catch (Throwable ignored) {}
        return null;
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
            } catch (Throwable ignored) {}
        }
        if (sr.secureJar != null) {
            try {
                Method m = sr.secureJar.getClass().getMethod("getClassLoader");
                Object clo = m.invoke(sr.secureJar);
                if (clo instanceof ClassLoader cl) loaders.add(cl);
            } catch (Throwable ignored) {}
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