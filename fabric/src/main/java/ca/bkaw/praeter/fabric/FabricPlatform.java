package ca.bkaw.praeter.fabric;

import ca.bkaw.praeter.Platform;
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
