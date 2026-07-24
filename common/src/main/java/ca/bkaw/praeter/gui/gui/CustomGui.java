package ca.bkaw.praeter.gui.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A custom graphical user interface.
 * <p>
 * Instances this class will be created for each gui that is opened.
 *
 * @see CustomGuiType
 */
public abstract class CustomGui {
    private final Map<StateRefImpl<?>, Object> stateMap = new HashMap<>();
    private final CustomGuiType type;
    private final List<Consumer<ClickContext>> clickListeners = new ArrayList<>();

    /**
     * Create a new custom gui with the given state references.
     *
     * @param type The type of custom gui. Usually from a static constant.
     */
    public CustomGui(CustomGuiType type) {
        this.type = type;
        List<StateRefImpl<?>> stateRefs = type.getStateRefs();
        if (stateRefs == null) {
            throw new IllegalStateException("Tried to create a custom gui that was not registered.");
        }
        for (StateRefImpl<?> stateRef : stateRefs) {
            this.stateMap.put(stateRef, stateRef.initializer().apply(this));
        }
    }

    /**
     * Get the type of this custom gui.
     *
     * @return The type of this custom gui.
     */
    public CustomGuiType getType() {
        return this.type;
    }

    /**
     * Get the state variable for the given state reference.
     *
     * @param stateRef The state reference to get the state variable.
     * @return The value of the state variable.
     * @param <T> The type of the state variable.
     */
    public <T> T getState(StateRefImpl<T> stateRef) {
        if (!this.stateMap.containsKey(stateRef)) {
            throw new IllegalStateException("State variable not initialized for this gui.");
        }
        // noinspection unchecked
        return (T) this.stateMap.get(stateRef);
    }

    /**
     * Add a click listener to this gui, called when any slot is clicked, including
     * player inventory slots.
     *
     * @param listener The listener.
     */
    public void addClickListener(Consumer<ClickContext> listener) {
        this.clickListeners.add(listener);
    }

    /**
     * Get the click listeners for this gui.
     *
     * @return The click listeners.
     */
    public List<Consumer<ClickContext>> getClickListeners() {
        return this.clickListeners;
    }

    /**
     * Re-render the gui.
     * <p>
     * This method should be called after changing the state of the gui, to update the
     * gui for all viewers.
     */
    public abstract void update();
}
