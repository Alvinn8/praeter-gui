package ca.bkaw.praeter.gui;

import io.netty.channel.ChannelHandler;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;

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
}
