plugins {
    `java-library`
    `maven-publish`
}

val javaVersion: String by project

allprojects {
    group = property("group") as String
    version = property("version") as String
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") { name = "PaperMC" }
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        // Target Java 8 bytecode (class file version 52) so the fat JAR loads
        // in CheerpJ 3's Java 8 runtime. The JDK toolchain stays at javaVersion (21).
        options.release.set(8)
    }

    // Keep compile-classpath dependency resolution at javaVersion so that compileOnly
    // deps requiring Java 21 (e.g. adventure-api 5.x) still resolve even though we
    // output Java 8 bytecode.
    configurations.configureEach {
        if (name == "compileClasspath" || name.endsWith("CompileClasspath")) {
            attributes.attribute(
                org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE,
                javaVersion.toInt()
            )
        }
    }

    // Every module publishes as "praeter-gui-<module>" (e.g. praeter-gui-paper).
    extensions.configure<PublishingExtension> {
        publications.create<MavenPublication>("maven") {
            artifactId = "${rootProject.name}-${project.name}"
            from(components["java"])
        }
    }
}
