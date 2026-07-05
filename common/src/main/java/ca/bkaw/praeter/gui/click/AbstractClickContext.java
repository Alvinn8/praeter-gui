package ca.bkaw.praeter.gui.click;

import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.player.GuiPlayer;

/**
 * A base implementation of {@link ClickContext} that platforms extend to add
 * platform-specific information and to implement {@link #playClickSound()}.
 */
public abstract class AbstractClickContext implements ClickContext {
    private final CustomGui gui;
    private final GuiPlayer player;
    private final int rawSlot;

    protected AbstractClickContext(CustomGui gui, GuiPlayer player, int rawSlot) {
        this.gui = gui;
        this.player = player;
        this.rawSlot = rawSlot;
    }

    @Override
    public CustomGui getGui() {
        return this.gui;
    }

    @Override
    public GuiPlayer getPlayer() {
        return this.player;
    }

    @Override
    public int getRawSlot() {
        return this.rawSlot;
    }
}
