package ca.bkaw.praeter.gui.fabric;

import ca.bkaw.praeter.gui.Platform;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import io.netty.channel.ChannelHandler;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.nio.file.Path;

/**
 * Fabric-backed {@link Platform}.
 * */
public final class FabricPlatform implements Platform {

    @Override
    public String name() {
        return "Fabric";
    }

    @Override
    public int getServerPort() {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public void injectChannelHandler(ChannelHandler channelHandler, String handlerKey) throws ReflectiveOperationException {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public void uninjectChannelHandler(String handlerKey) throws ReflectiveOperationException {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public @Nullable InetAddress getPlayerAddress(Audience player) {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public void guessOwner(Class<?> clazz) {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public @Nullable Path getStoragePath() {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public void includeAssetsFromOwners() {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public CustomGui createGui(CustomGuiType type) {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }

    @Override
    public void sendResourcePackToOnlinePlayers() {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }
}
