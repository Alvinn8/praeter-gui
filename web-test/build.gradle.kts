val adventureVersion: String by project
val jetbrainsAnnotationsVersion: String by project
val gsonVersion: String by project
val slf4jVersion: String by project
val nettyVersion: String by project

dependencies {
    implementation(project(":common"))
    // All of common's compileOnly deps must be implementation here so they end up in
    // the fat JAR that CheerpJ loads in the browser.
    implementation("net.kyori:adventure-api:$adventureVersion")
    implementation("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("io.netty:netty-buffer:$nettyVersion")
    implementation("io.netty:netty-transport:$nettyVersion")
}

// The project is compiled targeting Java 21 (class file version 65), which CheerpJ 3
// supports. If javaVersion in gradle.properties is raised above 21 in future, verify
// that CheerpJ has been updated to support the new class file version before doing so.

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Builds a fat JAR containing all runtime dependencies for use with CheerpJ."
    archiveClassifier.set("fat")
    dependsOn(configurations.runtimeClasspath)
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "ca.bkaw.praeter.gui.webtest.WebTestMain"
    }
}

// Copy the fat JAR next to index.html after building, so the HTML page and JAR
// are in the same directory for serving.
tasks.register<Copy>("copyFatJar") {
    group = "build"
    description = "Copies the fat JAR to html/ so serve.sh can find everything in one place."
    dependsOn("fatJar")
    from(tasks.named<Jar>("fatJar").map { it.archiveFile })
    into(layout.projectDirectory.dir("html"))
    rename { "praeter-web-test.jar" }
}
