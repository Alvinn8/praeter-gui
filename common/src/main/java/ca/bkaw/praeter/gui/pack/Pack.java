package ca.bkaw.praeter.gui.pack;

import ca.bkaw.praeter.gui.pack.collision.ResourceCollisionException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A pack that contains resources, assets and data.
 * <p>
 * This class can be used to access and modify files, merge packs, and perform
 * other operations.
 *
 * @see ResourcePack
 */
public abstract class Pack {
    private static final Gson GSON = new Gson();

    private final @Nullable FileSystem zipFileSystem;
    private final Path root;

    protected Pack(Path root, @Nullable FileSystem zipFileSystem) {
        this.root = root;
        this.zipFileSystem = zipFileSystem;
    }

    protected static Path validateDirectoryPath(Path directory) throws IOException {
        if (Files.exists(directory) && !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("The specified path exists but is not a directory.");
        }
        Files.createDirectories(directory);
        return directory;
    }

    protected static FileSystem openZip(Path zipFile) throws IOException {
        URI uri = URI.create("jar:" + zipFile.toUri());
        return FileSystems.newFileSystem(uri, Map.of("create", true));
    }

    /**
     * Get the root path of this pack.
     *
     * @return The root path.
     */
    public Path getRoot() {
        return this.root;
    }

    /**
     * Get a path within the pack.
     * <p>
     * Example of getting the pack.mcmeta file from the pack:
     * <br>
     * {@code pack.getPath("pack.mcmeta");}
     * <p>
     * Note that the returned path may belong to a file system that
     * has a root that isn't the resource pack root, so it is not safe
     * to call {@code resolve} with a string starting with a slash.
     *
     * @param path The path to get. Leading slashes will be removed.
     * @return The path within the pack.
     */
    public Path getPath(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return this.root.resolve(path);
    }

    /**
     * Create the pack by creating the {@code pack.mcmeta} file.
     *
     * @param description A description of the pack.
     * @param minFormat The minimum pack format version supported by this pack.
     * @param maxFormat The maximum pack format version supported by this pack.
     * @throws IllegalStateException If the pack already exists.
     * @throws IOException If an I/O error occurs.
     */
    public void create(String description, int minFormat, int maxFormat) throws IOException {
        Path path = this.getPath("pack.mcmeta");

        if (Files.exists(path)) {
            throw new IllegalArgumentException("Tried to create a pack but one " +
                "already existed. The pack already has a pack.mcmeta file.");
        }

        JsonObject root = new JsonObject();
        JsonObject pack = new JsonObject();
        root.add("pack", pack);
        pack.addProperty("pack_format", minFormat);
        pack.addProperty("min_format", minFormat);
        pack.addProperty("max_format", maxFormat);
        JsonObject ver = new JsonObject();
        ver.addProperty("min_inclusive", minFormat);
        ver.addProperty("max_inclusive", maxFormat);
        pack.add("supported_formats", ver);
        pack.addProperty("description", description);
        Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
    }

    /**
     * Include all resources from the other pack in to this pack.
     * <p>
     * This is the same as calling {@code pack.include(other, CollisionHandlerImpl.INSTANCE, null)}.
     *
     * @param other The pack to include resources from.
     * @throws ResourceCollisionException If a collision that could not be resolved occurred.
     * @throws IOException If something goes wrong while copying the files.
     * @see #include(Pack, Predicate)
     */
    public void include(Pack other) throws ResourceCollisionException, IOException {
        this.include(other, null);
    }

    /**
     * Include all resources from the other pack in to this pack.
     *
     * @param other The pack to include resources from.
     * @param filter Determines which resources should be included and not.
     * @throws ResourceCollisionException If a collision that could not be resolved occurred.
     * @throws IOException If something goes wrong while copying the files.
     */
    public void include(Pack other, @Nullable Predicate<String> filter) throws ResourceCollisionException, IOException {
        Path thisRoot = this.getRoot();
        Path otherRoot = other.getRoot();
        try {
            Files.walkFileTree(otherRoot, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    String relative = otherRoot.relativize(dir).toString();
                    if (filter != null && !filter.test(relative)) return FileVisitResult.CONTINUE;

                    Files.createDirectories(thisRoot.resolve(relative));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path otherFile, BasicFileAttributes attrs) throws IOException {
                    String relative = otherRoot.relativize(otherFile).toString();
                    if (filter != null && !filter.test(relative)) return FileVisitResult.CONTINUE;

                    Path thisFile = thisRoot.resolve(relative);
                    if (Files.exists(thisFile)) {
                        // The file already exists, we have a collision
                        try {
                            handleCollision(Pack.this, other, thisFile, otherFile);
                        } catch (ResourceCollisionException e) {
                            // The file visitor signature only permits IOException, so
                            // wrap the collision and unwrap it once walkFileTree returns.
                            throw new WrappedCollisionException(e);
                        }
                    } else {
                        Files.copy(otherFile, thisFile);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (WrappedCollisionException e) {
            throw e.getCause();
        }
    }

    public void close() throws IOException {
        if (this.zipFileSystem != null) {
            this.zipFileSystem.close();
        }
    }

    /**
     * Wraps a {@link ResourceCollisionException} so that it can be thrown from a
     * file visitor, whose methods may only throw {@link IOException}.
     */
    private static class WrappedCollisionException extends IOException {
        WrappedCollisionException(ResourceCollisionException cause) {
            super(cause);
        }

        @Override
        public ResourceCollisionException getCause() {
            return (ResourceCollisionException) super.getCause();
        }
    }

    private void handleCollision(Pack packA, Pack packB, Path pathA, Path pathB) throws ResourceCollisionException {
        // For now, just throw an exception. In the future, we may want to add
        // options for automatically resolving collisions.
        String relative = packA.getRoot().relativize(pathA).toString();
        throw new ResourceCollisionException("Pack collision: " + relative);
    }
}