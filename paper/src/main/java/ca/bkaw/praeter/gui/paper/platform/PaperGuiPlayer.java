package ca.bkaw.praeter.gui.paper.platform;

import ca.bkaw.praeter.gui.platform.GuiPlayer;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.UUID;

/**
 * A {@link GuiPlayer} implementation for a connected player in the form of
 * a {@link HumanEntity}.
 */
public class PaperGuiPlayer implements GuiPlayer {
    private final HumanEntity player;

    public PaperGuiPlayer(HumanEntity player) {
        this.player = player;
    }

    @Override
    public @Nullable InetAddress getAddress() {
        if (!(this.player instanceof Player serverPlayer)) {
            return null;
        }
        InetSocketAddress socketAddress = serverPlayer.getAddress();
        if (socketAddress == null) {
            return null;
        }
        return socketAddress.getAddress();
    }

    @Override
    public void sendResourcePack(UUID uuid, String url, String sha1Hash, boolean required, @Nullable String prompt) {
        this.player.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
                .packs(
                    ResourcePackInfo.resourcePackInfo(uuid, URI.create(url), sha1Hash)
                )
                .required(required)
                .prompt(prompt != null ? Component.text(prompt) : null)
            .build()
        );
    }
}
