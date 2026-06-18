val paperApiVersion: String by project

dependencies {
    api(project(":common"))
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
}
