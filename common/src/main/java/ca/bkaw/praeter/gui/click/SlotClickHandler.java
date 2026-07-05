package ca.bkaw.praeter.gui.click;

import java.util.function.Consumer;

/**
 * A click handler registered for a specific raw slot index.
 *
 * @param rawSlot The raw slot index the handler is registered for.
 * @param handler The handler to run when that raw slot is clicked.
 * @see ca.bkaw.praeter.gui.render.RenderContext#onClick(ca.bkaw.praeter.gui.draw.SlotPos, Consumer)
 */
public record SlotClickHandler(int rawSlot, Consumer<ClickContext> handler) {
}
