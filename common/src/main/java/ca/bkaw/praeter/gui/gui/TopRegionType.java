package ca.bkaw.praeter.gui.gui;

import ca.bkaw.praeter.gui.draw.DrawPos;

/**
 * What the top region of the screen contains.
 * <p>
 * Most custom guis will use a generic top region which can be customized with
 * components. Some custom guis may use a special top region, such as the anvil
 * gui, which has a special top region that allows the player to rename items.
 * <p>
 * Currently, only generic top regions are supported.
 */
public enum TopRegionType {
    /** A generic top region with 1 row. */
    GENERIC_9X1(9, DrawPos.SLOT_SIZE),
    /** A generic top region with 2 rows. */
    GENERIC_9X2(9 * 2, 2 * DrawPos.SLOT_SIZE),
    /** A generic top region with 3 rows. */
    GENERIC_9X3(9 * 3, 3 * DrawPos.SLOT_SIZE),
    /** A generic top region with 4 rows. */
    GENERIC_9X4(9 * 4, 4 * DrawPos.SLOT_SIZE),
    /** A generic top region with 5 rows. */
    GENERIC_9X5(9 * 5, 5 * DrawPos.SLOT_SIZE),
    /** A generic top region with 6 rows. */
    GENERIC_9X6(9 * 6, 6 * DrawPos.SLOT_SIZE),
    ;

    private final int slotCount;
    private final int contentPixelHeight;

    TopRegionType(int slotCount, int contentPixelHeight) {
        this.slotCount = slotCount;
        this.contentPixelHeight = contentPixelHeight;
    }

    /**
     * Get the number of client-side slots in the top region.
     *
     * @return The top slot count.
     */
    public int getSlotCount() {
        return this.slotCount;
    }

    /**
     * Get the content height of the top region in pixels, excluding the top and
     * bottom edges.
     *
     * @return The number of pixels in height.
     */
    public int getContentPixelHeight() {
        return this.contentPixelHeight;
    }
}
