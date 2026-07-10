# praeter-gui

## Usage

// TODO repo

### Paper

```kotlin
dependencies {
    implementation("ca.bkaw.praeter:praeter-gui-paper:0.2-SNAPSHOT")
}
```

// TODO shading

### Fabric

```kotlin
dependencies {
    include(modImplementation("ca.bkaw.praeter:praeter-gui-fabric:0.2-SNAPSHOT"))
}
```

### Platform-independent

If you want to support Paper and Fabric, compile against `praeter-gui-common`. In your platform-specific modules, include the platform implementation as above.
