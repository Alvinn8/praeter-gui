package ca.bkaw.praeter.gui.paper.platform;

import ca.bkaw.praeter.gui.platform.GuiPlayer;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.URI;
import java.util.UUID;

/**
 * A {@link GuiPlayer} implementation for a player in the configuring state, in the
 * form of a {@link PlayerConfigurationConnection}.
 */
public class PaperConfiguringGuiPlayer implements GuiPlayer {
    private final PlayerConfigurationConnection connection;

    public PaperConfiguringGuiPlayer(PlayerConfigurationConnection connection) {
        this.connection = connection;
    }

    @Override
    public @Nullable InetAddress getAddress() {
        return this.connection.getClientAddress().getAddress();
    }

    @Override
    public void sendResourcePack(UUID uuid, String url, String sha1Hash, boolean required, @Nullable String prompt) {
        this.connection.getAudience().sendResourcePacks(ResourcePackRequest.resourcePackRequest()
            .packs(
                ResourcePackInfo.resourcePackInfo(uuid, URI.create(url), sha1Hash)
            )
            .required(required)
            .prompt(prompt != null ? Component.text(prompt) : null)
            .build()
        );
    }
}
