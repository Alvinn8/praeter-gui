package ca.bkaw.praeter.gui.click;

import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.player.GuiPlayer;

/**
 * Information about a click passed to a click handler registered with
 * {@link ca.bkaw.praeter.gui.render.RenderContext#onClick}.
 * <p>
 * Platforms provide implementations that expose more native information, for
 * example the underlying event on Paper.
 */
public interface ClickContext {

    /**
     * Get the gui instance that was clicked.
     *
     * @return The gui instance.
     */
    CustomGui getGui();

    /**
     * Get the player that clicked.
     *
     * @return The player.
     */
    GuiPlayer getPlayer();

    /**
     * Get the raw slot index that was clicked.
     *
     * @return The raw slot index.
     */
    int getRawSlot();

    /**
     * Play the standard button click sound to the player that clicked.
     */
    void playClickSound();
}
