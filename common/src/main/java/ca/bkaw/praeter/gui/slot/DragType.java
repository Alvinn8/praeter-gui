package ca.bkaw.praeter.gui.slot;

/**
 * The type of a drag ("quick craft") performed by holding a mouse button and
 * moving the cursor over multiple slots.
 */
public enum DragType {
    /**
     * A left-click drag. The cursor items are split evenly between the dragged slots.
     */
    EVEN,
    /**
     * A right-click drag. One item is placed in each dragged slot.
     */
    SINGLE,
    /**
     * A middle-click drag, only available in creative mode. A full stack is placed in
     * each dragged slot without consuming the cursor.
     */
    CLONE
}
