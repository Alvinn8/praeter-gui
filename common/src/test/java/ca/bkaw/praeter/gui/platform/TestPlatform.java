package ca.bkaw.praeter.gui.platform;

import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.render.RenderContext;
import io.netty.channel.ChannelHandler;

import java.nio.file.Path;
import java.util.List;

/**
 * The platform used for tests.
 */
public final class TestPlatform implements Platform {
    private final Path storagePath;

    public TestPlatform(Path storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public int getServerPort() {
        throw new IllegalStateException();
    }

    @Override
    public void injectChannelHandler(ChannelHandler channelHandler, String handlerKey) {
    }

    @Override
    public void uninjectChannelHandler(String handlerKey) {
    }

    @Override
    public void guessOwner(Class<?> clazz) {
    }

    @Override
    public Path getStoragePath() {
        return this.storagePath;
    }

    @Override
    public void includeAssetsFromOwners() {
    }

    @Override
    public CustomGui createGui(CustomGuiType type) {
        return new CustomGui(type);
    }

    @Override
    public List<GuiPlayer> getOnlinePlayers() {
        return List.of();
    }

    @Override
    public void plainTextHoverText(RenderContext r, SlotPos pos, String[] text) {
    }
}
