package ca.bkaw.praeter.gui.pack;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

/**
 * A {@link Pack} that contains assets for the client like models and textures.
 */
public class ResourcePack extends Pack {

    protected ResourcePack(Path root, @Nullable FileSystem zipFileSystem) {
        super(root, zipFileSystem);
    }

    /**
     * Load a resource pack from a directory and use that directory as the root of the
     * pack.
     * <p>
     * There must not be a resource pack in the folder as it can later be created
     * using {@link Pack#create(String, int, int)}.
     * <p>
     * The folder will be created if it does not exist.
     *
     * @param directory The directory to load from
     * @return The loaded resource pack.
     * @throws IllegalArgumentException If the specified path already exists but is not a directory.
     * @throws IOException If an I/O error occurs.
     */
    public static ResourcePack loadDirectory(Path directory) throws IOException {
        return new ResourcePack(validateDirectoryPath(directory), null);
    }

    /**
     * Load a resource pack from a zip file and use the root of the zip as the root of
     * the resource pack.
     * <p>
     * The zip file will be created if it does not exist.
     *
     * @param zipFile The path of the zip file to read.
     * @return The loaded or created resource pack.
     * @throws IOException If an I/O error occurs.
     */
    public static ResourcePack loadZip(Path zipFile) throws IOException {
        FileSystem fileSystem = openZip(zipFile);
        return new ResourcePack(fileSystem.getPath(".").normalize(), fileSystem);
    }

    /**
     * Get the string path to a texture resource in a resource pack.
     * <p>
     * The key is relative to the "textures" folder, but "item", "block", etc. folders
     * must be provided in the key. The file extension (.png) may optionally be
     * provided in the key.
     *
     * @param identifier The identifier of the texture to get the string path.
     * @return The string path of the texture.
     */
    public static String getTextureStringPath(String identifier) {
        String namespace = "minecraft";
        String key = identifier;
        if (identifier.contains(":")) {
            String[] parts = identifier.split(":", 2);
            namespace = parts[0];
            key = parts[1];
        }
        String dotExtension = ".png";
        if (!key.endsWith(dotExtension)) {
            key += dotExtension;
        }
        return "assets/" + namespace + "/textures/" + key;
    }

    /**
     * Get the path to a texture resource in this resource pack.
     * <p>
     * The key is relative to the "textures" folder, but "item", "block", etc. folders
     * must be provided in the key. The file extension (.png) may optionally be
     * provided in the key.
     *
     * @param identifier The identifier of the texture to get the string path.
     * @return The path of the texture.
     */
    public Path getTexturePath(String identifier) {
        return this.getPath(getTextureStringPath(identifier));
    }

}
