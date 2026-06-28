package ca.bkaw.praeter.gui;

import ca.bkaw.praeter.gui.pack.ResourcePack;
import ca.bkaw.praeter.gui.pack.VanillaAssets;
import ca.bkaw.praeter.gui.pack.collision.ResourceCollisionException;
import ca.bkaw.praeter.gui.pack.send.BuiltInTcpResourcePackSender;
import ca.bkaw.praeter.gui.pack.send.ResourcePackSender;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

public class PraeterGuiAssets {
    /**
     * The UUID to use for the resource pack.
     */
    public static final UUID PACK_UUID = UUID.fromString("2ff44814-84c0-4dcb-9443-045667eed902");

    /**
     * The game version to fetch vanilla assets for.
     * <p>
     * This is hard-coded as supposed to using the server game version because assets
     * may change between game versions which could suddenly break components that rely
     * on specific assets, for example, button textures.
     */
    // TODO make this configurable?
    public static final String VANILLA_ASSETS_VERSION = "26.2";

    public static final String DESCRIPTION = "Praeter";
    public static final int MIN_PACK_FORMAT = 55;
    public static final int MAX_PACK_FORMAT = 123; // Arbitrary

    private final PraeterGui praeterGui;
    private @Nullable ResourcePack resourcePack;
    private final Path resourcePackPath;
    private @Nullable ResourcePack vanillaAssets;
    private @Nullable String sha1;
    private final ResourcePackSender sender;

    private PraeterGuiAssets(PraeterGui praeterGui, ResourcePack resourcePack, Path resourcePackPath, ResourcePack vanillaAssets) {
        this.praeterGui = praeterGui;
        this.resourcePack = resourcePack;
        this.resourcePackPath = resourcePackPath;
        this.vanillaAssets = vanillaAssets;
        if (praeterGui.isSkipSender()) {
            this.sender = ResourcePackSender.noOp();
        } else {
            try {
                this.sender = new BuiltInTcpResourcePackSender(praeterGui);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to set up built-in TCP resource pack sender.", e);
            }
        }
    }

    public static PraeterGuiAssets setup(PraeterGui praeterGui, Path storagePath) throws IOException {
        // Create resource pack zip
        Files.createDirectories(storagePath);
        Path resourcePackPath = storagePath.resolve("resource_pack.zip");
        Files.deleteIfExists(resourcePackPath);
        ResourcePack resourcePack = ResourcePack.loadZip(resourcePackPath);
        resourcePack.create(DESCRIPTION, MIN_PACK_FORMAT, MAX_PACK_FORMAT);

        Path vanillaAssetsPath = storagePath.resolve("vanilla_assets.zip");
        ResourcePack vanillaAssets = VanillaAssets.readOrExtract(vanillaAssetsPath, VANILLA_ASSETS_VERSION);

        return new PraeterGuiAssets(praeterGui, resourcePack, resourcePackPath, vanillaAssets);
    }

    public static ResourcePack getJarResources(Class<?> clazz) throws IOException {
        // Read the jar file to get the resources from it.
        Path jarPath;
        try {
            jarPath = Path.of(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to find jar file.", e);
        }
        return ResourcePack.loadZip(jarPath);
    }

    /**
     * Include the assets folder from the specified resource pack.
     *
     * @param assets The resource pack to include assets from.
     * @throws IOException If an I/O error occurs.
     * @throws ResourceCollisionException If there is a resource collision with the
     *                                    existing assets in this pack.
     */
    public void includeAssets(ResourcePack assets) throws IOException, ResourceCollisionException {
        if (this.resourcePack == null) {
            throw new IllegalStateException("Already saved");
        }
        this.resourcePack.include(assets, path -> path.startsWith("assets"));
    }

    public void save() throws IOException {
        if (this.resourcePack == null) {
            throw new IllegalStateException("Already saved");
        }
        // Close resource pack
        this.resourcePack.close();
        this.resourcePack = null;

        // Get the file SHA-1 hash of resource pack
        try {
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(Files.readAllBytes(this.resourcePackPath));
            this.sha1 = HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // Should never happen. JVM must implement SHA-1.
        }

        // Also close vanilla assets
        if (this.vanillaAssets != null) {
            this.vanillaAssets.close();
            this.vanillaAssets = null;
        }
    }

    public @Nullable ResourcePack getVanillaAssets() {
        return this.vanillaAssets;
    }

    public @Nullable ResourcePack getResourcePack() {
        return this.resourcePack;
    }

    public Path getResourcePackPath() {
        return this.resourcePackPath;
    }

    public @Nullable String getSha1Hash() {
        return this.sha1;
    }

    public ResourcePackSender getSender() {
        return this.sender;
    }

}
