package ca.bkaw.praeter.gui;

import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import io.netty.channel.ChannelHandler;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.Objects;

public final class TestPlatform implements Platform {
    private final Path storagePath;

    public TestPlatform(Path storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public String name() {
        return "Test";
    }

    @Override
    public int getServerPort() {
        throw new IllegalStateException();
    }

    @Override
    public @Nullable InetAddress getPlayerAddress(Audience player) {
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
        throw new IllegalStateException();
    }

    @Override
    public void sendResourcePackToOnlinePlayers() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TestPlatform) obj;
        return Objects.equals(this.storagePath, that.storagePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storagePath);
    }

    @Override
    public String toString() {
        return "TestPlatform[" +
            "storagePath=" + storagePath + ']';
    }

}
