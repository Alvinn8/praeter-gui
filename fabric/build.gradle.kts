plugins {
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
}

val minecraftVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val adventureVersion: String by project

dependencies {
    api(project(":common"))
    include(project(":common"))

    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    compileOnly("net.kyori:adventure-api:$adventureVersion")
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}
