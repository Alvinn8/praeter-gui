package ca.bkaw.praeter.gui.click;

import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Fires click handlers registered for a gui.
 */
public final class ClickDispatcher {
    private ClickDispatcher() {}

    /**
     * Fire the global and per-slot click handlers registered for the given raw
     * slot, if the raw slot is inside the gui's top inventory.
     * <p>
     * The context supplier is only invoked if there is at least one handler to
     * run, so platforms can defer constructing a {@link ClickContext} that wraps
     * a native event until it is actually needed.
     *
     * @param gui The gui that was clicked.
     * @param rawSlot The raw slot index that was clicked.
     * @param contextSupplier Supplies the click context to pass to the handlers.
     * @return Whether any handler was fired.
     */
    public static boolean fire(CustomGui gui, int rawSlot, Supplier<ClickContext> contextSupplier) {
        CustomGuiType type = gui.getType();
        if (rawSlot < 0 || rawSlot >= type.getTopSlotCount()) {
            return false;
        }
        List<Consumer<ClickContext>> global = type.getClickHandlers();
        List<Consumer<ClickContext>> slot = type.getSlotClickHandlersAt(rawSlot);
        if (global.isEmpty() && slot.isEmpty()) {
            return false;
        }
        ClickContext ctx = contextSupplier.get();
        global.forEach(handler -> handler.accept(ctx));
        slot.forEach(handler -> handler.accept(ctx));
        return true;
    }
}
