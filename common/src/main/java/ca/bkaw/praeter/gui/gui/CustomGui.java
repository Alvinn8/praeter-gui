package ca.bkaw.praeter.gui.gui;

import ca.bkaw.praeter.gui.render.StateRefImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A custom graphical user interface.
 * <p>
 * Instances this class will be created for each gui that is opened.
 *
 * @see CustomGuiType
 */
public class CustomGui {
    private final Map<StateRefImpl<?>, Object> stateMap = new HashMap<>();

    /**
     * Create a new custom gui with the given state references.
     *
     * @param stateRefs The state references to initialize for this gui.
     */
    public CustomGui(List<StateRefImpl<?>> stateRefs) {
        for (StateRefImpl<?> stateRef : stateRefs) {
            this.stateMap.put(stateRef, stateRef.initializer().apply(this));
        }
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
}
