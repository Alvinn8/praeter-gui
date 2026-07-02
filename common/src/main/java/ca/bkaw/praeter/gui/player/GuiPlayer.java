package ca.bkaw.praeter.gui.player;

/**
 * A platform-agnostic handle to a player interacting with a gui.
 * <p>
 * Platforms implement this interface by wrapping their native player type.
 * Platform-specific code can unwrap the native player from the platform's
 * implementation.
 */
public interface GuiPlayer {

    /**
     * Get the name of the player.
     *
     * @return The name.
     */
    String getName();
}
