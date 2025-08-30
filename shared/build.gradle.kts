
plugins {
    kotlin("jvm")
}

version = "all-0.0.1-BETA"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.spongepowered:configurate-yaml:4.1.2")
    compileOnly("org.spongepowered:configurate-jackson:4.1.2")
    compileOnly("org.spongepowered:configurate-xml:4.1.2")
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("com.google.code.gson:gson:2.13.1")
    add("downgrade", files("libs/jNBT-1.6.0-downgraded-17.jar"))
    testImplementation(kotlin("test"))
    api("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    api("jakarta.ws.rs:jakarta.ws.rs-api:4.0.0")
    api("org.hibernate.orm:hibernate-core:6.4.1.Final")
    api("org.hibernate.orm:hibernate-community-dialects:6.3.1.Final")
    api("org.xerial:sqlite-jdbc:3.48.0.0")
    api("net.kyori:adventure-api:4.14.0")
    api("net.kyori:adventure-text-serializer-gson:4.14.0")
    api("net.kyori:adventure-text-serializer-plain:4.14.0")
}


tasks.test {
    useJUnitPlatform()
}

tasks.register<xyz.wagyourtail.jvmdg.gradle.task.DowngradeJar>("downgrade") {
    inputFile = file("libs/jNBT-1.6.0.jar")
    downgradeTo = JavaVersion.VERSION_17
    archiveClassifier = "downgraded-17"
}