package ca.bkaw.praeter.gui.gui;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.click.ClickContext;
import ca.bkaw.praeter.gui.click.SlotClickHandler;
import ca.bkaw.praeter.gui.render.RenderContext;
import ca.bkaw.praeter.gui.render.RenderStep;
import ca.bkaw.praeter.gui.item.ItemRenderer;
import ca.bkaw.praeter.gui.slot.GuiScreenState;
import ca.bkaw.praeter.gui.slot.GuiSlot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A type of custom gui.
 * <p>
 * Only one instance of this class will be created for each type of custom gui.
 *
 * @see CustomGui
 */
public class CustomGuiType {
    private final int height;
    private final BottomRegionType bottomRegionType;
    private final Consumer<RenderContext> setupFunction;
    private @Nullable List<RenderStep> renderSteps;
    private @Nullable List<StateRefImpl<?>> stateRefs;
    private @Nullable Map<Integer, GuiSlot> guiSlots;
    private @Nullable List<ItemRenderer> itemRenderers;
    private @Nullable List<Consumer<ClickContext>> clickHandlers;
    private @Nullable Map<Integer, List<Consumer<ClickContext>>> slotClickHandlers;

    private CustomGuiType(int height, BottomRegionType bottomRegionType, Consumer<RenderContext> setupFunction) {
        this.height = height;
        this.bottomRegionType = bottomRegionType;
        this.setupFunction = setupFunction;
    }

    /**
     * Get the setup function of this gui type. This function will be called to prepare
     * the rendering for this gui type.
     *
     * @return The setup function.
     */
    public Consumer<RenderContext> getSetupFunction() {
        return this.setupFunction;
    }

    /**
     * Get the number of rows in the gui.
     *
     * @return The number of rows in the gui.
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Get what the bottom region of the screen contains for this gui type.
     *
     * @return The bottom region type.
     */
    public BottomRegionType getBottomRegionType() {
        return this.bottomRegionType;
    }

    /**
     * Get the number of slots in the top gui.
     *
     * @return The top slot count.
     */
    public int getTopSlotCount() {
        return this.height * 9;
    }

    /**
     * Get the total number of raw slots on the screen when this gui is open,
     * including the bottom region.
     *
     * @return The total slot count.
     */
    public int getTotalSlotCount() {
        return this.getTopSlotCount() + GuiScreenState.BOTTOM_SLOT_COUNT;
    }

    /**
     * Create a builder for a custom gui type.
     *
     * @return The builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Set the render steps for rendering this gui type.
     *
     * @param renderSteps The list of render steps.
     */
    public void setRenderSteps(List<RenderStep> renderSteps) {
        this.renderSteps = Collections.unmodifiableList(renderSteps);
    }

    /**
     * Get the render steps for rendering this gui type.
     *
     * @return The list of render steps, or null if they have not been set yet.
     */
    public @Nullable List<RenderStep> getRenderSteps() {
        return this.renderSteps;
    }

    /**
     * Set the state references for this gui type.
     *
     * @param stateRefs The list of state references.
     */
    public void setStateRefs(List<StateRefImpl<?>> stateRefs) {
        this.stateRefs = Collections.unmodifiableList(stateRefs);
    }

    /**
     * Get the state references for this gui type.
     *
     * @return The list of state references, or null if they have not been set yet.
     */
    public @Nullable List<StateRefImpl<?>> getStateRefs() {
        return this.stateRefs;
    }

    /**
     * Set the slots of this gui type.
     *
     * @param guiSlots The list of slot definitions.
     */
    public void setGuiSlots(List<GuiSlot> guiSlots) {
        Map<Integer, GuiSlot> map = new HashMap<>();
        for (GuiSlot guiSlot : guiSlots) {
            int rawSlot = guiSlot.getRawSlot();
            if (rawSlot < 0 || rawSlot >= this.getTotalSlotCount()) {
                throw new IllegalArgumentException("Slot index " + rawSlot
                    + " is outside the screen (total slot count: " + this.getTotalSlotCount() + ").");
            }
            if (rawSlot >= this.getTopSlotCount() && this.bottomRegionType == BottomRegionType.PLAYER_INVENTORY) {
                throw new IllegalArgumentException("Slot index " + rawSlot
                    + " is in the bottom region, but the bottom region is the player's inventory."
                    + " Use a custom bottom region to place slots there.");
            }
            if (map.put(rawSlot, guiSlot) != null) {
                throw new IllegalArgumentException("Multiple slots registered at slot index " + rawSlot + ".");
            }
        }
        this.guiSlots = Collections.unmodifiableMap(map);
    }

    /**
     * Get the slot definition at the given raw slot index.
     *
     * @param rawSlot The raw slot index.
     * @return The slot definition, or null if there is no slot at the index.
     */
    public @Nullable GuiSlot getGuiSlotAt(int rawSlot) {
        return this.guiSlots == null ? null : this.guiSlots.get(rawSlot);
    }

    /**
     * Get all slot definitions of this gui type.
     *
     * @return The slot definitions.
     */
    public Collection<GuiSlot> getGuiSlots() {
        return this.guiSlots == null ? List.of() : this.guiSlots.values();
    }

    /**
     * Set the item renderers of this gui type.
     * <p>
     * Must be called after {@link #setGuiSlots(List)} so that positions that
     * clash with slots can be rejected.
     *
     * @param itemRenderers The list of item renderers.
     */
    public void setItemRenderers(List<ItemRenderer> itemRenderers) {
        for (ItemRenderer itemRenderer : itemRenderers) {
            int rawSlot = itemRenderer.rawSlot();
            if (rawSlot < 0 || rawSlot >= this.getTotalSlotCount()) {
                throw new IllegalArgumentException("Item renderer slot index " + rawSlot
                    + " is outside the screen (total slot count: " + this.getTotalSlotCount() + ").");
            }
            if (rawSlot >= this.getTopSlotCount() && this.bottomRegionType == BottomRegionType.PLAYER_INVENTORY) {
                throw new IllegalArgumentException("Item renderer slot index " + rawSlot
                    + " is in the bottom region, but the bottom region is the player's inventory.");
            }
            if (this.getGuiSlotAt(rawSlot) != null) {
                throw new IllegalArgumentException("Item renderer slot index " + rawSlot
                    + " clashes with a slot registered at the same position.");
            }
        }
        this.itemRenderers = List.copyOf(itemRenderers);
    }

    /**
     * Get the item renderers of this gui type.
     *
     * @return The item renderers.
     */
    public List<ItemRenderer> getItemRenderers() {
        return this.itemRenderers == null ? List.of() : this.itemRenderers;
    }

    /**
     * Set the global click handlers of this gui type, that run when any slot in
     * the top gui is clicked.
     *
     * @param clickHandlers The list of click handlers.
     */
    public void setClickHandlers(List<Consumer<ClickContext>> clickHandlers) {
        this.clickHandlers = List.copyOf(clickHandlers);
    }

    /**
     * Get the global click handlers of this gui type.
     *
     * @return The click handlers.
     */
    public List<Consumer<ClickContext>> getClickHandlers() {
        return this.clickHandlers == null ? List.of() : this.clickHandlers;
    }

    /**
     * Set the per-slot click handlers of this gui type.
     *
     * @param slotClickHandlers The list of slot click handlers.
     */
    public void setSlotClickHandlers(List<SlotClickHandler> slotClickHandlers) {
        Map<Integer, List<Consumer<ClickContext>>> map = new HashMap<>();
        for (SlotClickHandler slotClickHandler : slotClickHandlers) {
            int rawSlot = slotClickHandler.rawSlot();
            if (rawSlot < 0 || rawSlot >= this.getTopSlotCount()) {
                throw new IllegalArgumentException("Click handler slot index " + rawSlot
                    + " is outside the top gui (top slot count: " + this.getTopSlotCount() + ").");
            }
            map.computeIfAbsent(rawSlot, _ -> new ArrayList<>()).add(slotClickHandler.handler());
        }
        this.slotClickHandlers = Collections.unmodifiableMap(map);
    }

    /**
     * Get the click handlers registered for the given raw slot index.
     *
     * @param rawSlot The raw slot index.
     * @return The click handlers, or an empty list if there are none.
     */
    public List<Consumer<ClickContext>> getSlotClickHandlersAt(int rawSlot) {
        if (this.slotClickHandlers == null) {
            return List.of();
        }
        return this.slotClickHandlers.getOrDefault(rawSlot, List.of());
    }

    /**
     * Create a new instance of the custom gui type.
     *
     * @return The new instance of the custom gui.
     */
    public CustomGui create() {
        return PraeterGui.instance().getPlatform().createGui(this);
    }

    /**
     * A builder for a custom gui type.
     */
    public static class Builder {
        private int height = 6;
        private BottomRegionType bottomRegionType = BottomRegionType.PLAYER_INVENTORY;
        private @Nullable Consumer<RenderContext> setupFunction;

        private Builder() {}

        /**
         * Set the height of the gui, also known as the number of rows.
         *
         * @param height The height. [1-6] (inclusive, inclusive)
         * @return The builder, for chaining.
         */
        @Contract("_ -> this")
        public Builder height(int height) {
            this.height = height;
            return this;
        }

        /**
         * Set what the bottom region of the screen contains for this gui type.
         * <p>
         * By default the bottom region is {@link BottomRegionType#PLAYER_INVENTORY
         * the player's inventory}.
         *
         * @param bottomRegionType The bottom region type.
         * @return The builder, for chaining.
         */
        @Contract("_ -> this")
        public Builder bottomRegion(BottomRegionType bottomRegionType) {
            this.bottomRegionType = bottomRegionType;
            return this;
        }

        /**
         * Set the setup function of the gui. This function will be called once during
         * startup to prepare the rendering for this gui type.
         *
         * @param setupFunction The setup function.
         * @return The builder, for chaining.
         */
        @Contract("_ -> this")
        public Builder setup(Consumer<RenderContext> setupFunction) {
            this.setupFunction = setupFunction;
            return this;
        }

        /**
         * Build the custom gui type.
         * <p>
         * Remember to register it. TODO reference registration here
         *
         * @return The custom gui type.
         */
        public CustomGuiType build() {
            if (setupFunction == null) {
                throw new IllegalStateException("Setup function must be set");
            }
            return new CustomGuiType(this.height, this.bottomRegionType, this.setupFunction);
        }
    }
}
