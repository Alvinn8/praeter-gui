package ca.bkaw.praeter.gui;

import net.kyori.adventure.audience.Audience;

import java.io.IOException;

/**
 * Methods called when certain events happen in the game.
 * <p>
 * These methods are called by the platform-specific implementation.
 */
public class PlatformEvents {
    private final PraeterGui praeterGui;

    public PlatformEvents(PraeterGui praeterGui) {
        this.praeterGui = praeterGui;
    }

    public void onPlayerConfigure(Audience player) {
        this.praeterGui.getAssets().getSender().send(player, true, null);
    }

    public void onServerStarted() {
        try {
            this.praeterGui.getAssets().save();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save assets.", e);
        }
    }
}
