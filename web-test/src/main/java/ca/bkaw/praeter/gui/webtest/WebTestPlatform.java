package ca.bkaw.praeter.gui.webtest;

import ca.bkaw.praeter.gui.Platform;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import io.netty.channel.ChannelHandler;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.nio.file.Path;

public class WebTestPlatform implements Platform {
    private final Path storagePath;

    public WebTestPlatform(Path storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public String name() {
        return "WebTest";
    }

    @Override
    public int getServerPort() {
        throw new UnsupportedOperationException("No game server in web mode");
    }

    @Override
    public @Nullable InetAddress getPlayerAddress(Audience player) {
        throw new UnsupportedOperationException("No players in web mode");
    }

    @Override
    public void injectChannelHandler(ChannelHandler channelHandler, String handlerKey) {
        // No-op: no Netty pipeline in web mode
    }

    @Override
    public void uninjectChannelHandler(String handlerKey) {
        // No-op
    }

    @Override
    public void guessOwner(Class<?> clazz) {
        // No-op: no plugin/mod ownership concept in web mode
    }

    @Override
    public Path getStoragePath() {
        return this.storagePath;
    }

    @Override
    public void includeAssetsFromOwners() {
        // No-op: no plugin JARs to scan in web mode
    }

    @Override
    public CustomGui createGui(CustomGuiType type) {
        return new CustomGui(type);
    }

    @Override
    public void sendResourcePackToOnlinePlayers() {
        // No-op
    }
}
