package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.platform.GuiItem;
import ca.bkaw.praeter.gui.platform.GuiPlayer;

/**
 * An interface that determines which items a slot can hold and which players can
 * modify the slot.
 * <p>
 * To create a slot that can only hold certain items, use {@code slotCanHold}
 * imported from the platform hooks.
 * <pre>
 *     slotCanHold(item -> true or false)
 * </pre>
 * <p>
 * To create a slot that can only be modified by certain players, use {@code slotCanModify}
 * imported from the platform hooks.
 * <pre>
 *     slotCanModify(player -> true or false)
 * </pre>
 * <p>
 * To use both, use {@code slotBehavior} imported from the platform hooks.
 * <pre>
 *     slotBehavior(item -> true or false, player -> true or false)
 * </pre>
 * <p>
 * The default slot behavior allows any item and any player to modify the slot.
 */
public interface SlotBehavior {
    /**
     * A default slot behavior that allows any item and any player to modify the slot.
     */
    SlotBehavior DEFAULT = new SlotBehavior() {
        @Override
        public boolean canHold(GuiItem item) {
            return true;
        }

        @Override
        public boolean mayChange(GuiPlayer player) {
            return true;
        }
    };

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
    boolean mayChange(GuiPlayer player);
}
