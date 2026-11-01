plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.24"
}

group = "com.loademon"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    // Pinned to the server's exact build: the fake player uses server internals.
    paperweight.paperDevBundle("26.3.build.230-stable")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("paper-plugin.yml") {
        expand("version" to project.version)
    }
}
