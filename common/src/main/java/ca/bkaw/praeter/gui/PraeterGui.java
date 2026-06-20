package ca.bkaw.praeter.gui;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public final class PraeterGui {

    private static @Nullable PraeterGui instance;
    private final Platform platform;
    private final PlatformEvents events;
    private @Nullable PraeterGuiAssets assets;

    public PraeterGui() {
        this.platform = bootstrap();
        this.events = new PlatformEvents(this);
        this.reload();
    }

    public static PraeterGui instance() {
        if (instance == null) {
            instance = new PraeterGui();
        }
        return instance;
    }

    private static Platform bootstrap() {
        if (classExists("net.fabricmc.loader.api.FabricLoader")) {
            return instantiate("ca.bkaw.praeter.gui.fabric.FabricPlatform");
        } else if (classExists("org.bukkit.Bukkit")) {
            return instantiate("ca.bkaw.praeter.gui.paper.PaperPlatform");
        } else {
            throw new IllegalStateException(
                "No supported PraeterGui platform implementation was found on the classpath. "
                    + "Add praeter-gui-paper, praeter-gui-fabric, or another implementation."
            );
        }
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, PraeterGui.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static Platform instantiate(String className) {
        try {
            return (Platform) Class.forName(className, false, PraeterGui.class.getClassLoader()).getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate platform class: " + className, e);
        }
    }

    private void reload() {
        try {
            this.assets = PraeterGuiAssets.createPack(this);
        } catch (IOException e) {
            throw new RuntimeException("Failed to set up praeter-gui", e);
        }
    }

    /**
     * Get the {@link Platform} instance.
     *
     * @return The platform.
     */
    public Platform getPlatform() {
        return this.platform;
    }

    public PraeterGuiAssets getAssets() {
        if (this.assets == null) {
            throw new IllegalStateException();
        }
        return this.assets;
    }

    public PlatformEvents getPlatformEvents() {
        return this.events;
    }
}
