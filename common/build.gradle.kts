val jetbrainsAnnotationsVersion: String by project
val gsonVersion: String by project

dependencies {
    compileOnly("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    compileOnly("com.google.code.gson:gson:$gsonVersion")
}
