val paperApiVersion: String by project
val nettyVersion: String by project

dependencies {
    api(project(":common"))
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    compileOnly("io.netty:netty-buffer:${nettyVersion}")
    compileOnly("io.netty:netty-transport:${nettyVersion}")
}
