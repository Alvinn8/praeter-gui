package ca.bkaw.praeter.gui.draw;

/**
 * A position of a slot in a gui.
 */
public interface SlotPos {
    /**
     * Get the slot index used by the game for this slot.
     *
     * @return The slot index.
     */
    int slotIndex();

    /**
     * Get the top-left corner pixel of the slot.
     */
    DrawPos cornerPixel();

    /**
     * Create a {@link SlotPos} from the given slot index.
     *
     * @param slotIndex The index of the slot.
     * @return A {@link SlotPos} representing the slot at the given index.
     */
    static SlotPos of(int slotIndex) {
        return new GenricContainer(slotIndex);
    }

    /**
     * Create a {@link SlotPos} from the given slot coordinates in a 9xN grid.
     *
     * @param slotX The x coordinate of the slot [0-8].
     * @param slotY The y coordinate of the slot.
     * @return A {@link SlotPos} representing the slot at the given coordinates.
     */
    static SlotPos of(int slotX, int slotY) {
        return new GenricContainer(slotY * 9 + slotX);
    }

    final class GenricContainer implements SlotPos {
        private final int slotIndex;
        GenricContainer(int slotIndex) { this.slotIndex = slotIndex; }
        @Override public int slotIndex() { return this.slotIndex; }
        @Override
        public DrawPos cornerPixel() {
            return DrawPos.slotOrigin().add(
                (this.slotIndex % 9) * DrawPos.SLOT_SIZE,
                (this.slotIndex / 9) * DrawPos.SLOT_SIZE
            );
        }
    }
}
