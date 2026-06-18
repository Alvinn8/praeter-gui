package ca.bkaw.praeter.render;

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
	 * The top-left corner of the first slot.
	 *
	 * @return A {@link DrawPos} representing the coordinates.
	 */
	static DrawPos slotOrigin() {
		return of(HORIZONTAL_PADDING, TOP_PADDING);
	}

	/**
	 * The top-left corner pixel of the given slot.
	 *
	 * @param slotX The x coordinate of the slot [0-8].
	 * @param slotY The y coordinate of the slot.
	 * @return A {@link DrawPos} representing the coordinates.
	 */
	static DrawPos slotCorner(int slotX, int slotY) {
		int x = 8 + slotX * 18;
		int y = 8 + slotY * 18;
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
