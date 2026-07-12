package ca.bkaw.praeter.gui.platform;

import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.UUID;

/**
 * A mocked player.
 */
public class MockGuiPlayer implements GuiPlayer {
    @Override
    public @Nullable InetAddress getAddress() {
        return null;
    }

    @Override
    public void sendResourcePack(UUID uuid, String url, String sha1Hash, boolean required, @Nullable String prompt) {}
}
