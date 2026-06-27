package ca.bkaw.praeter.gui.components;

import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.render.RenderContext;

/**
 * A slot where the user can take and place items.
 */
public class Slot {
    private Object itemStack; // TODO

    private Slot() {}

    /**
     * A {@link Slot} in a gui.
     *
     * @param r The render context.
     * @param pos The position of the slot.
     * @return A reference to the slot state.
     */
    public static Ref<Slot> slot(RenderContext r, SlotPos pos) {
        Ref<Slot> ref = r.useState(Slot::new);
        // TODO track slots for item movement.

        // Render the slot using a panel
        Panel.panel(r, DrawPos.slotCorner(pos), DrawPos.SLOT_SIZE, DrawPos.SLOT_SIZE);

        return ref;
    }
}
