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
        options.release.set(javaVersion.toInt())
    }

    // Every module publishes as "praeter-gui-<module>" (e.g. praeter-gui-paper).
    extensions.configure<PublishingExtension> {
        publications.create<MavenPublication>("maven") {
            artifactId = "${rootProject.name}-${project.name}"
            from(components["java"])
        }

        repositories {
            maven {
                val isSnapshot = version.toString().endsWith("-SNAPSHOT")
                url = uri(
                    if (isSnapshot) {
                        "https://maven.bkaw.ca/repository/maven-snapshots/"
                    } else {
                        "https://maven.bkaw.ca/repository/maven-releases/"
                    }
                )
                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }
            }
        }
    }
}
