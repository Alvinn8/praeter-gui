package ca.bkaw.praeter.paper;

import ca.bkaw.praeter.Platform;
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
