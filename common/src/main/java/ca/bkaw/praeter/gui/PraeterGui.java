package ca.bkaw.praeter.gui;

import ca.bkaw.praeter.gui.gui.CustomGuiRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public final class PraeterGui {
    /**
     * The namespace to use for generated textures.
     */
    public static final String GENERATED_NAMESPACE = "generated";

    /**
     * The default path to store generated resource pack and vanilla assets if no
     * plugin or mod was detected.
     */
    public static final String DEFAULT_STORAGE_PATH = ".praeter_gui";

    private static @Nullable PraeterGui instance;
    private final Platform platform;
    private final PlatformEvents events;
    private @Nullable PraeterGuiAssets assets;
    private @Nullable Path storagePath;
    private final CustomGuiRegistry registry = new CustomGuiRegistry();

    public PraeterGui() {
        this.platform = bootstrap();
        this.events = new PlatformEvents(this);
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

    public CustomGuiRegistry getRegistry() {
        return this.registry;
    }

    /**
     * Check whether the assets have been set up.
     *
     * @return True if the assets have been set up, false otherwise.
     */
    public boolean hasAssets() {
        return this.assets != null;
    }

    /**
     * Setup generated resource pack and vanilla assets.
     */
    @ApiStatus.Internal
    public void setupAssets() {
        if (this.assets != null) {
            throw new IllegalStateException("Assets have already been set up.");
        }
        if (this.storagePath == null) {
            this.storagePath = this.platform.getStoragePath();
        }
        if (this.storagePath == null) {
            storagePath = Path.of(DEFAULT_STORAGE_PATH);
        }
        try {
            this.assets = PraeterGuiAssets.setup(this, this.storagePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to set up praeter-gui assets", e);
        }
        this.platform.includeAssetsFromOwners();
    }

    /**
     * Reload all assets and guis.
     * <p>
     * This may be used if guis are created dynamically or when using hot reloading
     * during development.
     */
    public void reload() {
        this.setupAssets();
        // TODO re-create all gui types
    }
}
