package ca.bkaw.praeter.gui.fabric.platform;

import ca.bkaw.praeter.gui.platform.GuiPlayer;
import ca.bkaw.praeter.gui.platform.Platform;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import io.netty.channel.ChannelHandler;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

/**
 * Fabric-backed {@link Platform}.
 * */
public final class FabricPlatform implements Platform {

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
    public List<GuiPlayer> getOnlinePlayers() {
        throw new UnsupportedOperationException("Not yet implemented for Fabric");
    }
}
