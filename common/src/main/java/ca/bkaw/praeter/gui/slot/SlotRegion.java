package ca.bkaw.praeter.gui.slot;

import java.util.Iterator;

/**
 * A rectangular region of slots in a gui.
 */
public interface SlotRegion extends Iterable<SlotPos> {
    /**
     * Get the x coordinate, measured in slots [0-8].
     *
     * @return The number of slots to the left.
     */
    int getSlotX();

    /**
     * Get the y coordinate, measured in slots.
     *
     * @return The number of slots up.
     */
    int getSlotY();

    /**
     * Get the width, measured in slots.
     *
     * @return The number of slots.
     */
    int getSlotWidth();

    /**
     * Get the height, measured in slots.
     *
     * @return The number of slots.
     */
    int getSlotHeight();

    @Override
    default Iterator<SlotPos> iterator() {
        return new Iterator<>() {
            private int x = getSlotX();
            private int y = getSlotY();

            @Override
            public boolean hasNext() {
                return y < getSlotY() + getSlotHeight();
            }

            @Override
            public SlotPos next() {
                SlotPos pos = SlotPos.of(x, y);
                x++;
                if (x >= getSlotX() + getSlotWidth()) {
                    x = getSlotX();
                    y++;
                }
                return pos;
            }
        };
    }
}
