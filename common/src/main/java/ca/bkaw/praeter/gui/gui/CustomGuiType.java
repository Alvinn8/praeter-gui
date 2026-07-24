package ca.bkaw.praeter.gui.gui;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.render.RenderContext;
import ca.bkaw.praeter.gui.render.RenderStep;
import ca.bkaw.praeter.gui.slot.GuiSlot;
import ca.bkaw.praeter.gui.slot.ItemRenderer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

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
    private final TopRegionType topRegionType;
    private final BottomRegionType bottomRegionType;
    private final Consumer<RenderContext> setupFunction;
    private @Nullable List<RenderStep> renderSteps;
    private @Nullable List<StateRefImpl<?>> stateRefs;
    private @Nullable List<Consumer<CustomGui>> createListeners;
    private @Nullable Map<Integer, GuiSlot> guiSlots;
    private @Nullable List<ItemRenderer> itemRenderers;

    private CustomGuiType(TopRegionType topRegionType, BottomRegionType bottomRegionType, Consumer<RenderContext> setupFunction) {
        this.topRegionType = topRegionType;
        this.bottomRegionType = bottomRegionType;
        this.setupFunction = setupFunction;
    }

    /**
     * Get the top region type of this gui type.
     *
     * @return The top region type.
     */
    public TopRegionType getTopRegionType() {
        return this.topRegionType;
    }

    /**
     * Get the bottom region type of this gui type.
     *
     * @return The bottom region type.
     */
    public BottomRegionType getBottomRegionType() {
        return this.bottomRegionType;
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
     * Get the total number of client-side slots on the screen when this gui is open,
     * including the bottom region.
     *
     * @return The total slot count.
     */
    public int getTotalSlotCount() {
        return this.topRegionType.getSlotCount() + this.bottomRegionType.getSlotCount();
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
     * Set the gui slots of this gui type.
     *
     * @param guiSlots The list of slot definitions.
     */
    public void setGuiSlots(List<GuiSlot> guiSlots) {
        Map<Integer, GuiSlot> map = new HashMap<>();
        for (GuiSlot guiSlot : guiSlots) {
            int slotIndex = guiSlot.getSlotIndex();
            if (slotIndex < 0 || slotIndex >= this.getTotalSlotCount()) {
                throw new IllegalArgumentException("Slot index " + slotIndex
                    + " is outside the screen (total slot count: " + this.getTotalSlotCount() + ").");
            }
            if (slotIndex >= this.topRegionType.getSlotCount() && this.bottomRegionType == BottomRegionType.PLAYER_INVENTORY) {
                throw new IllegalArgumentException("Slot index " + slotIndex
                    + " is in the bottom region, but the bottom region is the player's inventory."
                    + " Use a custom bottom region to place slots there.");
            }
            if (map.put(slotIndex, guiSlot) != null) {
                throw new IllegalArgumentException("Multiple slots registered at slot index " + slotIndex + ".");
            }
        }
        this.guiSlots = Collections.unmodifiableMap(map);
    }

    /**
     * Get the slot definition at the given raw slot index in the inventory view.
     *
     * @param slotIndex The raw slot index in the inventory view.
     * @return The slot definition, or null if there is no slot at the index.
     */
    public @Nullable GuiSlot getGuiSlotAt(int slotIndex) {
        return this.guiSlots == null ? null : this.guiSlots.get(slotIndex);
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
            int slotIndex = itemRenderer.slotIndex();
            if (slotIndex < 0 || slotIndex >= this.getTotalSlotCount()) {
                throw new IllegalArgumentException("Item renderer slot index " + slotIndex
                    + " is outside the screen (total slot count: " + this.getTotalSlotCount() + ").");
            }
            if (slotIndex >= this.topRegionType.getSlotCount() && this.bottomRegionType == BottomRegionType.PLAYER_INVENTORY) {
                throw new IllegalArgumentException("Item renderer slot index " + slotIndex
                    + " is in the bottom region, but the bottom region is the player's inventory.");
            }
            if (this.getGuiSlotAt(slotIndex) != null) {
                throw new IllegalArgumentException("Item renderer slot index " + slotIndex
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
     * Set the list of listeners that will be called when a new instance of this gui
     * type is created.
     *
     * @param createListeners The list of listeners.
     */
    public void setCreateListeners(List<Consumer<CustomGui>> createListeners) {
        this.createListeners = createListeners;
    }

    /**
     * Create a new instance of the custom gui type.
     *
     * @return The new instance of the custom gui.
     */
    public CustomGui create() {
        if (this.createListeners == null) {
            throw new IllegalStateException("Tried to create a gui that was not registered. Did you forget to register?");
        }
        CustomGui gui = PraeterGui.instance().getPlatform().createGui(this);
        for (Consumer<CustomGui> listener : this.createListeners) {
            listener.accept(gui);
        }
        return gui;
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
     * A builder for a custom gui type.
     */
    public static class Builder {
        private TopRegionType topRegionType = TopRegionType.GENERIC_9X6;
        private BottomRegionType bottomRegionType = BottomRegionType.PLAYER_INVENTORY;
        private @Nullable Consumer<RenderContext> setupFunction;

        private Builder() {}

        /**
         * Set the height of the gui, also known as the number of rows.
         * <p>
         * This is equivalent to calling {@link #topRegionType(TopRegionType)} with the
         * corresponding generic top region type.
         *
         * @param height The height. [1-6] (inclusive, inclusive)
         * @return The builder, for chaining.
         */
        @Contract("_ -> this")
        public Builder height(@Range(from = 1, to = 6) int height) {
            this.topRegionType = switch (height) {
                case 1 -> TopRegionType.GENERIC_9X1;
                case 2 -> TopRegionType.GENERIC_9X2;
                case 3 -> TopRegionType.GENERIC_9X3;
                case 4 -> TopRegionType.GENERIC_9X4;
                case 5 -> TopRegionType.GENERIC_9X5;
                case 6 -> TopRegionType.GENERIC_9X6;
                default -> throw new IllegalArgumentException("Invalid height. Must be between 1 and 6.");
            };
            return this;
        }

        /**
         * Set what the top region of the screen should render as. The default is
         * {@link TopRegionType#GENERIC_9X6}.
         *
         * @param topRegionType The top region type.
         * @return The builder, for chaining.
         */
        public Builder topRegionType(TopRegionType topRegionType) {
            this.topRegionType = topRegionType;
            return this;
        }

        /**
         * Set what the bottom region of the screen should render as. The default is
         * {@link BottomRegionType#PLAYER_INVENTORY the player's inventory}.
         *
         * @param bottomRegionType The bottom region type.
         * @return The builder, for chaining.
         */
        public Builder bottomRegionType(BottomRegionType bottomRegionType) {
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
         * Remember to register it using {@link CustomGuiRegistry#register0(String, CustomGuiType)}.
         *
         * @return The custom gui type.
         */
        public CustomGuiType build() {
            if (this.setupFunction == null) {
                throw new IllegalStateException("Setup function must be set");
            }
            return new CustomGuiType(this.topRegionType, this.bottomRegionType, this.setupFunction);
        }
    }
}
