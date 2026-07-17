package ca.bkaw.praeter.gui.fabric.platform;

import ca.bkaw.praeter.gui.platform.GuiPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

/**
 * A {@link GuiPlayer} implementation for a connected player in the form of
 * a {@link Player}. The player may be client side or server side.
 */
public final class FabricGuiPlayer implements GuiPlayer {
    private final Player player;

    public FabricGuiPlayer(Player player) {
        this.player = player;
    }

    @Override
    public @Nullable InetAddress getAddress() {
        if (!(this.player instanceof ServerPlayer serverPlayer)) {
            return null;
        }
        if (!(serverPlayer.connection.getRemoteAddress() instanceof InetSocketAddress address)) {
            return null;
        }
        return address.getAddress();
    }

    @Override
    public void sendResourcePack(UUID uuid, String url, String sha1Hash, boolean required, @Nullable String prompt) {
        if (!(this.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        serverPlayer.connection.send(new ClientboundResourcePackPushPacket(
            uuid, url, sha1Hash, required,
            Optional.ofNullable(prompt).map(Component::literal)
        ));
    }
}