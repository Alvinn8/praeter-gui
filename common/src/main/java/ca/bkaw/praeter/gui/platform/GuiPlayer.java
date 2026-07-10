package ca.bkaw.praeter.gui.platform;

import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.UUID;

/**
 * A handle to a player that might still be connecting to the server in the
 * configuration state, or might be fully connected.
 */
public interface GuiPlayer {
    /**
     * Get the IP address of the player.
     * <p>
     * Will return null if the IP address cannot be determined.
     *
     * @return The IP address of the player, or null.
     */
    @Nullable
    InetAddress getAddress();

    /**
     * Send a resource pack to the player.
     *
     * @param uuid The UUID of the resource pack.
     * @param url The URL of the resource pack.
     * @param sha1Hash The SHA-1 hash of the resource pack.
     * @param required Whether the resource pack is required.
     * @param prompt An optional prompt to show to the player.
     */
    void sendResourcePack(UUID uuid, String url, String sha1Hash, boolean required, @Nullable String prompt);

}
