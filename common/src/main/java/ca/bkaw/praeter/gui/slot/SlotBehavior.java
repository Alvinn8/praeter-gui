package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.item.GuiItem;
import ca.bkaw.praeter.gui.player.GuiPlayer;

/**
 * The behavior of a slot in a custom gui, deciding which items the slot can hold
 * and which players may change its contents.
 * <p>
 * Since {@link #mayChange(GuiPlayer)} has a default implementation, a behavior
 * that only restricts items can be written as a lambda:
 * <pre>
 * Slot.slot(r, pos, item -&gt; item.getMaxStackSize() &gt; 1);
 * </pre>
 */
@FunctionalInterface
public interface SlotBehavior {

    /**
     * Whether the slot can hold the given item.
     *
     * @param item The item.
     * @return Whether the slot can hold the item.
     */
    boolean canHold(GuiItem item);

    /**
     * Whether the given player may change the contents of the slot.
     * <p>
     * Returns true by default.
     *
     * @param player The player.
     * @return Whether the player may change the slot.
     */
    default boolean mayChange(GuiPlayer player) {
        return true;
    }
}
