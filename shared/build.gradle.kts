plugins {
    kotlin("jvm")
}

group = "org.vicky.vicky_utils"
version = "1.23.0"

repositories {
    mavenCentral()
}

dependencies {
    api("org.spongepowered:configurate-yaml:4.1.2")
    api("org.spongepowered:configurate-jackson:4.1.2")
    api("org.spongepowered:configurate-xml:4.1.2")
    implementation("com.google.code.gson:gson:2.13.1")
    testImplementation(kotlin("test"))
    implementation("org.jetbrains:annotations:24.0.1")
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