plugins {
    kotlin("jvm")
}

group = "org.vicky.vicky_utils"
version = "1.23.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}