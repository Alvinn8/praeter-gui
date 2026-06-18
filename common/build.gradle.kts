val jetbrainsAnnotationsVersion: String by project
val gsonVersion: String by project
val slf4jVersion: String by project

dependencies {
    compileOnly("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    compileOnly("com.google.code.gson:gson:$gsonVersion")
    compileOnly("org.slf4j:slf4j-api:$slf4jVersion")
}
