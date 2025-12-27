plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
}

group = "org.vicky.blockbench-dissolver"
version = "0.0.1-HANA"

gradlePlugin {
    plugins {
        create("blockbenchConverter") {
            id = "org.vicky.blockbench-dissolver"
            implementationClass = "org.vicky.gradle.BlockbenchDissolverPlugin"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenBBD") {
            groupId = "org.vicky"
            artifactId = "blockbench-dissolver"
            version = "0.0.1-HANA"
            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
        maven {
            name = "localRepo"
            url = layout.buildDirectory.dir("local-repo").get().asFile.toURI()
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}
