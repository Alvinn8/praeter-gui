package ca.bkaw.praeter.gui.gui;

import ca.bkaw.praeter.gui.PraeterGui;
import ca.bkaw.praeter.gui.PraeterGuiAssets;
import ca.bkaw.praeter.gui.pack.ResourcePack;
import ca.bkaw.praeter.gui.render.RenderContext;
import ca.bkaw.praeter.gui.render.RenderContextImpl;
import ca.bkaw.praeter.gui.render.RenderStep;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CustomGuiRegistry {
    private final Map<String, CustomGuiType> map = new HashMap<>();

    /**
     * Register a custom gui type with the specified id.
     *
     * @param id The id of the custom gui type. This should be unique across all mods.
     * @param type The custom gui type to register.
     */
    public void register(String id, CustomGuiType type) {
        if (this.map.containsKey(id)) {
            throw new IllegalArgumentException("A custom gui type with the id '" + id + "' is already registered.");
        }
        // This will bootstrap praeter-gui if it is not already initialized.
        PraeterGui praeterGui = PraeterGui.instance();

        // Get setup function
        Consumer<RenderContext> setupFunction = type.getSetupFunction();
        if (setupFunction == null) {
            throw new IllegalStateException(
                "The custom gui type with id '" + id + "' appears to already have been " +
                    "registered with id '" + this.getId(type) + "'."
            );
        }

        // Guess the owning plugin or mod by looking at the class loader of the setup
        // function which is typically a lambda defined in the plugin or mod's code.
        //
        // The plugin or mod instance is used to include all assets from the plugin
        // or mod in to the resource pack. It is also used to set the storage path
        // for generated assets and vanilla assets.
        praeterGui.getPlatform().guessOwner(setupFunction.getClass());

        // If the assets have not been set up yet, set them up now. This must happen
        // after guessing the owner so that the storage path can be set correctly.
        if (!praeterGui.hasAssets()) {
            praeterGui.setupAssets();
        }

        // Create render context
        PraeterGuiAssets assets = praeterGui.getAssets();
        ResourcePack pack = assets.getResourcePack();
        ResourcePack vanillaAssets = assets.getVanillaAssets();
        if (pack == null || vanillaAssets == null) {
            throw new IllegalStateException("Custom gui types must be registered before the server starts.");
        }
        RenderContextImpl r;
        try {
            r = new RenderContextImpl(type.getHeight(), pack, vanillaAssets);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create render context.", e);
        }

        // Call setup function.
        setupFunction.accept(r);

        RenderStep backgroundRenderStep;
        try {
            backgroundRenderStep = r.buildBackgroundRenderStep();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to build background render step.", e);
        }
        ArrayList<RenderStep> renderSteps = new ArrayList<>();
        renderSteps.add(backgroundRenderStep);
        renderSteps.addAll(r.getRootRenderBlock());
        renderSteps.trimToSize();

        type.setRenderSteps(renderSteps);
        type.setStateRefs(r.getStateRefs());

        this.map.put(id, type);
    }


    /**
     * Register a custom gui type with the specified id.
     *
     * @param id The id of the custom gui type. This should be unique across all mods.
     * @param type The custom gui type to register.
     */
    // TODO what to name this?
    public static void register0(String id, CustomGuiType type) {
        PraeterGui.instance().getRegistry().register(id, type);
    }

    public @Nullable String getId(CustomGuiType type) {
        for (Map.Entry<String, CustomGuiType> entry : this.map.entrySet()) {
            if (entry.getValue() == type) {
                return entry.getKey();
            }
        }
        return null;
    }
}
