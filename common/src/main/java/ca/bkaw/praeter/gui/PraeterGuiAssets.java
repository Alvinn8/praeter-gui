package ca.bkaw.praeter.gui;

import ca.bkaw.praeter.gui.pack.ResourcePack;
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
    public static final UUID PACK_UUID = UUID.fromString("2ff44814-84c0-4dcb-9443-045667eed902");
    public static final String DESCRIPTION = "Praeter";
    public static final int MIN_PACK_FORMAT = 55;
    public static final int MAX_PACK_FORMAT = 123; // Arbitrary

    private final PraeterGui praeterGui;
    private @Nullable ResourcePack resourcePack;
    private final Path resourcePackPath;
    private @Nullable String sha1;
    private final ResourcePackSender sender;

    private PraeterGuiAssets(PraeterGui praeterGui, ResourcePack resourcePack, Path resourcePackPath) {
        this.praeterGui = praeterGui;
        this.resourcePack = resourcePack;
        this.resourcePackPath = resourcePackPath;
        try {
            this.sender = new BuiltInTcpResourcePackSender(praeterGui);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to set up built-in TCP resource pack sender.", e);
        }
    }

    public static PraeterGuiAssets createPack(PraeterGui praeterGui) throws IOException {
        // Create resource pack zip
        // TODO dedicated folder
        Path resourcePackPath = Path.of("praeter_gui_resource_pack.zip");
        Files.deleteIfExists(resourcePackPath);
        ResourcePack resourcePack = ResourcePack.loadZip(resourcePackPath);
        resourcePack.create(DESCRIPTION, MIN_PACK_FORMAT, MAX_PACK_FORMAT);

        return new PraeterGuiAssets(praeterGui, resourcePack, resourcePackPath);
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
