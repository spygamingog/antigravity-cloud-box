plugins {
    java
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" 
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "9.0.0"
}

group = "com.spygamingog.dynamicblocks"
version = "1.0.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21)) 
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // 🟢 CHANGE THIS LINE FOR 1.21.11
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    
    implementation("org.joml:joml:1.10.5") 
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("org.joml", "${project.group}.shaded.joml")
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}
