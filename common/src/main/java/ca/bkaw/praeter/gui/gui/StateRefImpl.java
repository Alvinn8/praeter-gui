package ca.bkaw.praeter.gui.gui;

import java.util.function.Function;

/**
 * An implementation of {@link Ref} that gets the state from the gui.
 *
 * @param <T> The type of the state variable.
 */
public record StateRefImpl<T>(Function<CustomGui, T> initializer) implements Ref<T> {
    @Override
    public T get(CustomGui gui) {
        return gui.getState(this);
    }
}
