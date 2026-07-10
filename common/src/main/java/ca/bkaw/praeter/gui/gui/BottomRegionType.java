package ca.bkaw.praeter.gui.gui;

/**
 * What the bottom region of the screen (where the player inventory is normally
 * displayed) contains for a custom gui type.
 */
public enum BottomRegionType {
    /**
     * The bottom region shows the player's real inventory, like vanilla container
     * screens. Items moved into the bottom region are moved into the player's
     * inventory.
     */
    PLAYER_INVENTORY,
    /**
     * The bottom region is part of the custom gui. Custom slots and components can
     * be placed there, and the player's real inventory is not visible nor touched
     * while the gui is open.
     * <p>
     * @deprecated Not yet supported.
     */
    @Deprecated()
    CUSTOM;

    /**
     * The number of client-side slots in the bottom region of the screen.
     * <p>
     * This is constant, since even if the bottom region is custom, it will still be the
     * player's inventory slots client-side.
     */
    public static final int SLOT_COUNT = 36;

    /**
     * Gets {@link #SLOT_COUNT}.
     *
     * @return The number of client-side slots in the bottom region of the screen.
     */
    public int getSlotCount() {
        return SLOT_COUNT;
    }
}