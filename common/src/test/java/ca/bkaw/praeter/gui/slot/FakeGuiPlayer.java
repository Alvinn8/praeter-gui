package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.player.GuiPlayer;

/**
 * A fake player for testing the slot interaction handler without a platform.
 *
 * @param name The name of the player.
 */
public record FakeGuiPlayer(String name) implements GuiPlayer {
    @Override
    public String getName() {
        return this.name;
    }
}
