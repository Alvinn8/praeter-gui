package ca.bkaw.praeter.gui.gui;

import java.util.function.Function;

/**
 * An implementation of {@link Ref} that gets the state from the gui.
 *
 * @param <T> The type of the state variable.
 */
public final class StateRefImpl<T> implements Ref<T> {
    private final Function<CustomGui, T> initializer;

    public StateRefImpl(Function<CustomGui, T> initializer) {
        this.initializer = initializer;
    }

    public Function<CustomGui, T> initializer() {
        return this.initializer;
    }

    @Override
    public T get(CustomGui gui) {
        return gui.getState(this);
    }
}
