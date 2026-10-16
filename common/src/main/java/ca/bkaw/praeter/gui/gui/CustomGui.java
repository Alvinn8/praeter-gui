package ca.bkaw.praeter.gui.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final Set<StateRefImpl<?>> initializingStates = new HashSet<>();
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
            // The state may already have been initialized if an earlier state
            // initializer depended on it.
            if (!this.stateMap.containsKey(stateRef)) {
                this.initializeState(stateRef);
            }
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
     * <p>
     * If the state variable has not been initialized yet, which can happen when a
     * state initializer accesses a state declared after it, it will be initialized
     * now.
     *
     * @param stateRef The state reference to get the state variable.
     * @return The value of the state variable.
     * @param <T> The type of the state variable.
     */
    public <T> T getState(StateRefImpl<T> stateRef) {
        if (!this.stateMap.containsKey(stateRef)) {
            List<StateRefImpl<?>> stateRefs = this.type.getStateRefs();
            if (stateRefs == null || !stateRefs.contains(stateRef)) {
                throw new IllegalStateException("State variable does not belong to this gui.");
            }
            return this.initializeState(stateRef);
        }
        // noinspection unchecked
        return (T) this.stateMap.get(stateRef);
    }

    private <T> T initializeState(StateRefImpl<T> stateRef) {
        if (!this.initializingStates.add(stateRef)) {
            throw new IllegalStateException("Circular dependency between state variables: "
                + "a state initializer depends on its own state.");
        }
        try {
            T value = stateRef.initializer().apply(this);
            this.stateMap.put(stateRef, value);
            return value;
        } finally {
            this.initializingStates.remove(stateRef);
        }
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

    /**
     * Open this gui for the given player.
     *
     * @param player The ServerPlayer/HumanEntity that will see the gui.
     *               The type of this object depends on the platform.
     */
    public abstract void show(Object player);
}
