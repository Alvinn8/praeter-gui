package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.platform.GuiItem;
import ca.bkaw.praeter.gui.platform.GuiPlayer;

/**
 * An interface that determines which items a slot can hold and which players can
 * modify the slot.
 * <p>
 * To create a slot that can only hold certain items, use {@code slotCanHold}
 * imported from the platform hooks.
 * <pre>
 *     slotCanHold((gui, item) -> true or false)
 * </pre>
 * <p>
 * To create a slot that can only be modified by certain players, use {@code slotMayChange}
 * imported from the platform hooks.
 * <pre>
 *     slotMayChange((gui, player) -> true or false)
 * </pre>
 * <p>
 * To use both, use {@code slotBehavior} imported from the platform hooks.
 * <pre>
 *     slotBehavior((gui, item) -> true or false, (gui, player) -> true or false)
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
        public boolean canHold(CustomGui gui, GuiItem item) {
            return true;
        }

        @Override
        public boolean mayChange(CustomGui gui, GuiPlayer player) {
            return true;
        }
    };

    /**
     * Whether the slot can hold the given item.
     *
     * @param gui The gui instance the slot belongs to, which can be used to access state.
     * @param item The item.
     * @return Whether the slot can hold the item.
     */
    boolean canHold(CustomGui gui, GuiItem item);

    /**
     * Whether the given player may change the contents of the slot.
     * <p>
     * Returns true by default.
     *
     * @param gui The gui instance the slot belongs to, which can be used to access state.
     * @param player The player.
     * @return Whether the player may change the slot.
     */
    boolean mayChange(CustomGui gui, GuiPlayer player);
}
