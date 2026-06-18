package ca.bkaw.praeter.gui;

import org.jetbrains.annotations.NotNull;

/**
 * An abstraction over the platform that the game runs on.
 */
public interface Platform {

    /**
     * A human-readable name for this platform, e.g. {@code "Paper"}.
     */
    String name();
}
