package ca.bkaw.praeter.gui;

import org.jetbrains.annotations.Nullable;

public final class PraeterGui {

    private static @Nullable PraeterGui instance;
    private final Platform platform;
    private PraeterGuiAssets assets;

    public PraeterGui() {
        this.platform = bootstrap();
    }

    public static PraeterGui instance() {
        if (instance == null) {
            instance = new PraeterGui();
        }
        return instance;
    }

    private static Platform bootstrap() {
        if (classExists("net.fabricmc.loader.api.FabricLoader")) {
            return instantiate("ca.bkaw.praeter.fabric.FabricPlatform");
        } else if (classExists("org.bukkit.Bukkit")) {
            return instantiate("ca.bkaw.praeter.paper.PaperPlatform");
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

    /**
     * Get the {@link Platform} instance.
     *
     * @return The platform.
     */
    public Platform platform() {
        return this.platform;
    }

    public PraeterGuiAssets getAssets() {
        return this.assets;
    }
}
