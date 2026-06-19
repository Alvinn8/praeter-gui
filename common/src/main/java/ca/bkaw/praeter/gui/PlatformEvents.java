package ca.bkaw.praeter.gui;

import net.kyori.adventure.audience.Audience;

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
}
