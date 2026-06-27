package ca.bkaw.praeter.gui.render;

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
     * The size of a slot in pixels. A slot is 16x64 pixels, with a 1-pixel border on
     * each side.
     */
    int SLOT_SIZE = 18;

    /**
     * The height of the top edge, including all the curvature.
     */
    int TOP_EDGE_HEIGHT = 4;

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
     * @param slotX The x coordinate of the slot [0-8].
     * @param slotY The y coordinate of the slot.
     * @return A {@link DrawPos} representing the coordinates.
     */
    static DrawPos slotCorner(int slotX, int slotY) {
        int x = 8 + slotX * SLOT_SIZE; // TODO why 8, introduce constant.
        int y = 8 + slotY * SLOT_SIZE;
        return slotOrigin().add(x, y);
    }

    /**
     * The top-left corner pixel of the given slot.
     *
     * @param slot The slot index, starting at 0 in the top-left corner.
     * @return A {@link DrawPos} representing the coordinates.
     */
    static DrawPos slotCorner(int slot) {
        int slotX = slot % 9;
        int slotY = slot / 9;
        return slotCorner(slotX, slotY);
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
