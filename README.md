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

## Fabric

```kotlin
dependencies {
    include(modImplementation("ca.bkaw.praeter:praeter-gui-fabric:0.2-SNAPSHOT"))
}
```

### Platform-independent

Compile against `praeter-gui-common`, then ship the implementations matching
the platforms you target.
