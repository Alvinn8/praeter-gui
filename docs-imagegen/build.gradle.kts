plugins {
    application
}

val jetbrainsAnnotationsVersion: String by project
val gsonVersion: String by project
val slf4jVersion: String by project
val nettyVersion: String by project

application {
    mainClass.set("ca.bkaw.praeter.gui.imagegen.ImageGen")
}

dependencies {
    implementation(project(":common"))

    implementation("org.jetbrains:annotations:${jetbrainsAnnotationsVersion}")
    implementation("com.google.code.gson:gson:${gsonVersion}")
    implementation("org.slf4j:slf4j-api:${slf4jVersion}")
    implementation("org.slf4j:slf4j-simple:${slf4jVersion}")
    implementation("io.netty:netty-buffer:${nettyVersion}")
    implementation("io.netty:netty-transport:${nettyVersion}")
}
