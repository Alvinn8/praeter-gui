package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.gui.CustomGui;

/**
 * A reference to a value that can be retrieved for a given gui instance.
 *
 * @param <T> The type of the value.
 */
public interface Ref<T> {
    /**
     * Get the value of this reference for the given gui instance.
     *
     * @param gui The gui instance.
     * @return The value.
     */
    T get(CustomGui gui);
}
