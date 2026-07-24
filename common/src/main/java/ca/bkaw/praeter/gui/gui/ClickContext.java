package ca.bkaw.praeter.gui.gui;

import ca.bkaw.praeter.gui.slot.SlotInteraction;
import ca.bkaw.praeter.gui.slot.SlotPos;
import org.jetbrains.annotations.Nullable;

/**
 * The context of a click on a {@link CustomGui}.
 * <p>
 * This class is platform-agnostic and can be used in any platform. Platform-specific
 * implementations of this class exist in the platform modules.
 */
public abstract class ClickContext {
    private final CustomGui gui;
    private final SlotInteraction slotInteraction;
    private final @Nullable SlotPos slotPos;

    public ClickContext(CustomGui gui, SlotInteraction slotInteraction) {
        this.gui = gui;
        this.slotInteraction = slotInteraction;
        this.slotPos = slotInteraction.getSlotPos();
    }

    /**
     * Get the {@link CustomGui} that was clicked.
     *
     * @return The gui.
     */
    public CustomGui getGui() {
        return this.gui;
    }

    /**
     * Get the {@link SlotInteraction} that represents the type of click.
     *
     * @return The slot interaction.
     */
    public SlotInteraction getSlotInteraction() {
        return this.slotInteraction;
    }

    /**
     * Get the {@link SlotPos} of the slot that was clicked, or null if the click was
     * outside the gui.
     *
     * @return The slot position, or null.
     */
    public @Nullable SlotPos getSlotPos() {
        return this.slotPos;
    }

    /**
     * Play a UI click sound for the player that clicked.
     */
    public abstract void playClickSound();

    /**
     * Cancel the item movement behavior of the click.
     */
    public abstract void cancel();

    /**
     * Check if the click has been canceled.
     *
     * @return True if the click has been canceled, false otherwise.
     */
    public abstract boolean isCancelled();
}
