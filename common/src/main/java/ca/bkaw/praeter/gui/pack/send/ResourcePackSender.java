package ca.bkaw.praeter.gui.pack.send;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * A sender responsible for sending resource packs to players.
 * <p>
 * The server can only send resource packs to clients using an HTTP url. Resource
 * pack senders are responsible for crafting a URL to send to clients.
 */
public interface ResourcePackSender {
    Logger LOGGER = LoggerFactory.getLogger(ResourcePackSender.class);

    /**
     * Send the resource pack to a player.
     *
     * @param player The player to send the resource pack to.
     * @param required Whether the resource pack is required.
     * @param prompt An optional prompt to show to the player.
     */
    void send(Audience player, boolean required, @Nullable Component prompt);

    /**
     * Called when the resource pack sender is being removed. Can be used to clean up.
     */
    void remove() throws ReflectiveOperationException;

    /**
     * Utilities and shared code for {@link ResourcePackSender} implementations.
     */
    final class Utils {
        private static final String CHECK_IP_URL = "https://checkip.amazonaws.com/";

        private static @Nullable String localHostname;
        private static @Nullable String remoteHostname;

        /**
         * Get the hostname that the player can use to connect to this server.
         * <p>
         * This will be the server's public ip unless the player connected from a local
         * address.
         *
         * @param playerAddress The player IP address, used to determine if the player is
         *                      local or remote.
         * @return The hostname the player can use to connect to this server.
         */
        @NotNull
        public static String getHostnameFor(@Nullable InetAddress playerAddress) {
            // Check if the player joined from a local address
            // and in that case return local
            if (playerAddress != null && (playerAddress.isSiteLocalAddress() || playerAddress.isLoopbackAddress())) {
                return getLocalhost();
            }

            // Otherwise, it's a normal remote player, return the public ip
            return getRemoteHostname();
        }

        /**
         * Return the local ip that can be used to connect to the server from the same
         * network.
         *
         * @return The local ip.
         */
        @NotNull
        private static String getLocalhost() {
            if (localHostname == null) {
                try {
                    localHostname = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    LOGGER.warn("Failed to get local ip for getting the url " +
                        "to send to players when sending resource packs.", e);
                    localHostname = "localhost";
                }
            }
            return localHostname;
        }

        /**
         * Get the server's public ip.
         *
         * @return The remote hostname.
         */
        @NotNull
        private static String getRemoteHostname() {
            if (remoteHostname != null) {
                return remoteHostname;
            }
            String configHostname = null; //  TODO configurable?
            if (configHostname != null) {
                remoteHostname = configHostname;
            } else {
                try (InputStream stream = URI.create(CHECK_IP_URL).toURL().openStream()) {
                    remoteHostname = new String(stream.readAllBytes()).trim();
                } catch (IOException e) {
                    LOGGER.error("Failed to get public ip for getting the " +
                        "url to send to players when sending praeter resources.", e);
                    remoteHostname = getLocalhost();
                }
            }
            return remoteHostname;
        }
    }
}