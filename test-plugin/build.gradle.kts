plugins {
    id("com.gradleup.shadow") version "9.4.2"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

val paperApiVersion: String by project
val minecraftVersion: String by project

dependencies {
    implementation(project(":paper"))
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("")
    }
    runServer {
        minecraftVersion(minecraftVersion)
    }
}
