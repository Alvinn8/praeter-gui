package ca.bkaw.praeter.gui;

import io.netty.channel.ChannelHandler;

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
     */
    void injectChannelHandler(ChannelHandler channelHandler, String handlerKey);
}
