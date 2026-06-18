package ca.bkaw.praeter.gui.fabric;

import ca.bkaw.praeter.gui.Platform;

/**
 * Fabric-backed {@link Platform}.
 * */
public final class FabricPlatform implements Platform {

    @Override
    public String name() {
        return "Fabric";
    }
}
