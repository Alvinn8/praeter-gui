package ca.bkaw.praeter.gui.draw;

/**
 * Coordinates to draw something at in a gui.
 */
public interface DrawPos {

    /**
     * The number of pixels of padding that exist on the left and right side of the
     * slots. Aka, the size of the parts on the edges of the slots.
     */
    int HORIZONTAL_PADDING = 7;

    /**
     * The number of pixels between the top edge and the slots.
     */
    int TOP_PADDING = 17;

    /**
     * The number of pixels between the bottom edge and the slots.
     */
    int BOTTOM_PADDING = 7;

    /**
     * The size of a slot in pixels. A slot is 16x64 pixels, with a 1-pixel border on
     * each side.
     */
    int SLOT_SIZE = 18;

    /**
     * The height of the top edge, including all the curvature.
     */
    int TOP_EDGE_HEIGHT = 4;

    /**
     * The number of pixels between the container slots and the player inventory slots.
     */
    int INVENTORY_VIEW_GAP = 14;

    /**
     * The number of pixels between the player inventory slots and the hotbar slots.
     */
    int HOTBAR_GAP = 4;

    /** y-coordinate in pixels relative to the top-left corner. */
    int x();
    /** y-coordinate in pixels relative to the top-left corner. */
    int y();

    /**
     * Create a new {@link DrawPos} with the given pixel coordinates.
     *
     * @param x The x-coordinate in pixels relative to the top-left corner.
     * @param y The y-coordinate in pixels relative to the top-left corner.
     * @return A {@link DrawPos} representing the coordinates.
     */
    static DrawPos of(int x, int y) {
        return new DrawPosImpl(x, y);
    }

    /**
     * The top-left corner of the gui, where the background is drawn.
     *
     * @return A {@link DrawPos} representing the coordinates.
     */
    static DrawPos guiOrigin() {
        // By convention, the top-left corner of the gui is at (0, 0) in pixel
        // coordinates.
        return of(0, 0);
    }

    /**
     * The top-left corner of the first slot.
     *
     * @return A {@link DrawPos} representing the coordinates.
     */
    static DrawPos slotOrigin() {
        return guiOrigin().add(HORIZONTAL_PADDING, TOP_PADDING);
    }

    /**
     * The top-left corner pixel of the given slot.
     *
     * @param slotPos The slot position.
     * @return A {@link DrawPos} representing the coordinates.
     * @see SlotPos#cornerPixel()
     */
    static DrawPos slotCorner(SlotPos slotPos) {
        return slotPos.cornerPixel();
    }

    /**
     * Create a new {@link DrawPos} that is offset from this one by the given number of pixels.
     *
     * @param dx The number of pixels to offset in the x direction.
     * @param dy The number of pixels to offset in the y direction.
     * @return A new {@link DrawPos} that is offset from this one by the given number of pixels.
     */
    default DrawPos add(int dx, int dy) {
        return of(x() + dx, y() + dy);
    }

    /**
     * A simple implementation of {@link DrawPos}.
     *
     * @param x The x-coordinate in pixels relative to the top-left corner.
     * @param y The y-coordinate in pixels relative to the top-left corner.
     */
    record DrawPosImpl(int x, int y) implements DrawPos {}
}
