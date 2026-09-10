package ca.bkaw.praeter.gui.imagegen;

import ca.bkaw.praeter.gui.CommonHooks;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.pack.font.FontSequence;
import ca.bkaw.praeter.gui.platform.GuiPlayer;
import ca.bkaw.praeter.gui.platform.Platform;
import ca.bkaw.praeter.gui.render.RenderContext;
import ca.bkaw.praeter.gui.render.RenderDispatcher;
import ca.bkaw.praeter.gui.render.RenderStep;
import ca.bkaw.praeter.gui.slot.SlotPos;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class ImageGenPlatform implements Platform {
    private final Path storagePath;

    public ImageGenPlatform(Path storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public int getServerPort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void injectChannelHandler(io.netty.channel.ChannelHandler channelHandler, String handlerKey) {}

    @Override
    public void uninjectChannelHandler(String handlerKey) {}

    @Override
    public void guessOwner(Class<?> clazz) {}

    @Override
    public @Nullable Path getStoragePath() {
        return this.storagePath;
    }

    @Override
    public void includeAssetsFromOwners() {}

    @Override
    public CustomGui createGui(CustomGuiType type) {
        return new ImageGenCustomGui(type);
    }

    @Override
    public List<GuiPlayer> getOnlinePlayers() {
        return List.of();
    }

    @Override
    public void plainTextHoverText(RenderContext r, SlotPos pos, String[] text) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void plainTextTitle(RenderContext r, Function<CustomGui, String> titleFunction) {
        CommonHooks.onCreated(r, gui -> ((ImageGenCustomGui) gui).setTitleFunction(titleFunction));
    }

    @Override
    public void plainTextTitle(CustomGui gui, String title) {
        throw new UnsupportedOperationException();
    }

}
