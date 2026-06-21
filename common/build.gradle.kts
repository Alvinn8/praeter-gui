val adventureVersion: String by project
val jetbrainsAnnotationsVersion: String by project
val gsonVersion: String by project
val slf4jVersion: String by project
val nettyVersion: String by project

dependencies {
    compileOnly("net.kyori:adventure-api:$adventureVersion")
    compileOnly("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    compileOnly("com.google.code.gson:gson:$gsonVersion")
    compileOnly("org.slf4j:slf4j-api:$slf4jVersion")
    compileOnly("io.netty:netty-buffer:${nettyVersion}")
    compileOnly("io.netty:netty-transport:${nettyVersion}")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    test {
        useJUnitPlatform()

        maxHeapSize = "1G"

        testLogging {
            events("passed")
        }
    }
}