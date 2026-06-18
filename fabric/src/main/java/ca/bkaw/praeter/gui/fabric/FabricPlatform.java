package ca.bkaw.praeter.gui.fabric;

import ca.bkaw.praeter.gui.Platform;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric-backed {@link Platform}.
 * */
public final class FabricPlatform implements Platform {

    @Override
    public @NotNull String name() {
        return "Fabric";
    }
}
