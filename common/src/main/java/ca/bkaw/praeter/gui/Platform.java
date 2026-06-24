package ca.bkaw.praeter.gui;

import io.netty.channel.ChannelHandler;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.nio.file.Path;

/**
 * An abstraction over the platform that the game runs on.
 */
public interface Platform {

    /**
     * A human-readable name for this platform, e.g. {@code "Paper"}.
     */
    String name();

    /**
     * Get the port that the server is running on.
     *
     * @return The port.
     */
    int getServerPort();

    /**
     * Inject a channel handler into the server networking pipeline.
     *
     * @param channelHandler The channel handler to inject.
     * @param handlerKey The key of the channel.
     * @throws ReflectiveOperationException If something goes wrong.
     */
    void injectChannelHandler(ChannelHandler channelHandler, String handlerKey) throws ReflectiveOperationException;

    /**
     * Uninject a channel handler from the server networking pipeline.
     *
     * @param handlerKey The key of the channel to uninject.
     * @throws ReflectiveOperationException If something goes wrong.
     */
    void uninjectChannelHandler(String handlerKey) throws ReflectiveOperationException;

    /**
     * Get the IP address of a player.
     * <p>
     * Will return null if the audience is not a player or if the IP address
     * cannot be determined.
     *
     * @param player The audience, typically a player.
     * @return The IP address of the player, or null.
     */
    @Nullable
    InetAddress getPlayerAddress(Audience player);

    /**
     * Guess the owning plugin or mod of a class by looking at the class loader.
     * <p>
     * If a plugin or mod is identified, all assets from that plugin or mod will be
     * included in the resource pack. The storage path for generated assets and vanilla
     * assets will also be set to an appropriate location for that plugin or mod.
     *
     * @param clazz The class that belongs to the code of the plugin or mod.
     */
    void guessOwner(Class<?> clazz);

    /**
     * Get the preferred storage path for generated assets and vanilla assets.
     * <p>
     * This path is determined by the platform and the owning plugin or mod, if any.
     *
     * @return The preferred storage path, or null.
     */
    @Nullable Path getStoragePath();

    /**
     * Include all assets from the owning plugin or mod in to the resource pack.
     */
    void includeAssetsFromOwners();
}
