package ca.bkaw.praeter.gui.paper;

import ca.bkaw.praeter.gui.Platform;
import org.jetbrains.annotations.NotNull;

/**
 * Paper-backed {@link Platform}.
 */
public final class PaperPlatform implements Platform {

    @Override
    public @NotNull String name() {
        return "Paper";
    }
}
