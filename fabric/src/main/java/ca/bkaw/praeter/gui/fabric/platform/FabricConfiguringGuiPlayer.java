package ca.bkaw.praeter.gui.fabric.platform;

import ca.bkaw.praeter.gui.fabric.mixin.ServerCommonPacketListenerImplAccessor;
import ca.bkaw.praeter.gui.platform.GuiPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

/**
 * A {@link GuiPlayer} implementation for a player that is still in the
 * configuration state, in the form of a {@link ServerConfigurationPacketListenerImpl}.
 */
public final class FabricConfiguringGuiPlayer implements GuiPlayer {
    private final ServerConfigurationPacketListenerImpl listener;

    public FabricConfiguringGuiPlayer(ServerConfigurationPacketListenerImpl listener) {
        this.listener = listener;
    }

    @Override
    public @Nullable InetAddress getAddress() {
        Connection connection = ((ServerCommonPacketListenerImplAccessor) this.listener).praeter_gui$getConnection();
        if (!(connection.getRemoteAddress() instanceof InetSocketAddress address)) {
            return null;
        }
        return address.getAddress();
    }

    @Override
    public void sendResourcePack(UUID uuid, String url, String sha1Hash, boolean required, @Nullable String prompt) {
        this.listener.send(new ClientboundResourcePackPushPacket(
            uuid, url, sha1Hash, required,
            Optional.ofNullable(prompt).map(Component::literal)
        ));
    }
}
