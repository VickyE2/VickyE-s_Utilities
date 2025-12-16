plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    // Compile-time only for Forge/Configurate/etc.
    compileOnly("org.spongepowered:configurate-yaml:4.1.2")
    compileOnly("org.spongepowered:configurate-jackson:4.1.2")
    compileOnly("org.spongepowered:configurate-xml:4.1.2")
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("com.google.code.gson:gson:2.13.1")

    // Hibernate as internal implementation
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.hibernate.orm:hibernate-core:6.4.1.Final")
    implementation("org.hibernate.orm:hibernate-community-dialects:6.3.1.Final")
    implementation("org.jboss.logging:jboss-logging:3.5.3.Final")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:4.0.0")

    // Other runtime dependencies
    compileOnly("org.xerial:sqlite-jdbc:3.48.0.0")
    compileOnly("net.kyori:adventure-api:4.14.0")
    compileOnly("net.kyori:adventure-text-serializer-gson:4.14.0")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.14.0")
    compileOnly("com.google.code.gson:gson:2.13.1")
    add("downgrade", files("libs/jNBT-1.6.0-downgraded-17.jar"))

    testImplementation(kotlin("test"))
}

// Standard jar task just includes your code (no shading)
tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Specification-Title" to project.name,
            "Specification-Version" to project.version
        )
    }
}

// Shadow jar for shading ANTLR inside Hibernate
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("all")

    configurations = listOf(project.configurations.getByName("runtimeClasspath"))

    // Relocate ANTLR inside Hibernate to avoid conflicts with Forge
    relocate("org.antlr.v4", "org.vicky.shaded.antlr.v4")
    relocate("jakarta", "org.vicky.shaded.jakarta")
    relocate("javassist", "org.vicky.shaded.javassist")
    relocate("org.jboss.logging", "org.vicky.shaded.jboss.logging") {
        exclude("META-INF/services/**")
    }
    relocate("org.hibernate", "org.vicky.shaded.hibernate") {
        exclude("org/hibernate/community/**")
    }

    // Exclude dependencies you want to keep external
    dependencies {
        exclude("org/eclipse/**")
        // exclude("module-info.class")
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
        exclude(dependency("org.jetbrains.kotlinx:kotlinx-serialization-json"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
        exclude(dependency("org.spongepowered:.*"))
        exclude(dependency("org.jetbrains:annotations"))
        exclude(dependency("com.google.code.gson:.*"))
        exclude(dependency("net.kyori:.*"))
        // exclude(dependency("jakarta.*:.*"))
        exclude(dependency("org.xerial:sqlite-jdbc:.*"))
    }

    // Optional: list runtime dependencies in Class-Path
    mergeServiceFiles()
    manifest {
        attributes(
            "Class-Path" to project.configurations.getByName("runtimeClasspath")
                .files.joinToString(" ") { "libs/${it.name}" }
        )
    }
}

// Ensure tests use JVM classpath (no modules)
tasks.test {
    useJUnitPlatform()
}

// Optional: downgrade task if you need
tasks.register<xyz.wagyourtail.jvmdg.gradle.task.DowngradeJar>("downgrade") {
    inputFile = file("libs/jNBT-1.6.0.jar")

    archiveFileName.set("jNBT-1.6.0-downgraded-17.jar")
    destinationDirectory.set(file("libs"))
    archiveClassifier.set("downgraded-17")

    downgradeTo = JavaVersion.VERSION_17
    doFirst {
        println("Input: ${inputFile.asFile.get().path}")
        println("Output: ${archiveFile.get().asFile.path}")
    }
}
