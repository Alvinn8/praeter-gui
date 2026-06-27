package ca.bkaw.praeter.gui.gui;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.render.RenderContext;
import ca.bkaw.praeter.gui.render.RenderStep;
import ca.bkaw.praeter.gui.render.StateRefImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
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
    private final Consumer<RenderContext> setupFunction;
    private @Nullable List<RenderStep> renderSteps;
    private @Nullable List<StateRefImpl<?>> stateRefs;

    private CustomGuiType(int height, Consumer<RenderContext> setupFunction) {
        this.height = height;
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
            return new CustomGuiType(this.height, this.setupFunction);
        }
    }
}
