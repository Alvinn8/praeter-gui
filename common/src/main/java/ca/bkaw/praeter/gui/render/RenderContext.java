package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.CommonHooks;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.gui.StateRefImpl;
import ca.bkaw.praeter.gui.slot.GuiSlot;
import ca.bkaw.praeter.gui.slot.ItemRenderer;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The context used when setting up rendering for a component or gui.
 * <p>
 * Instances of {@link RenderContext} may only be used during the setup phase
 * (server startup) and may never be stored, captured, or used after startup.
 * <p>
 * You typically use {@link CommonHooks} and the platform hooks
 * to perform rendering setup, passing the render context as the first argument.
 */
public interface RenderContext {

    /**
     * Add a state variable to the gui.
     *
     * @param stateRef The state ref.
     */
    void addStateRef(StateRefImpl<?> stateRef);

    /**
     * Add a listener that will be called when a new gui instance is created.
     *
     * @param action The listener.
     */
    void addCreatedListener(Consumer<CustomGui> action);

    /**
     * Draw an image, given by an identifier, at the given position.
     * <p>
     * The identifier should be in the format "namespace:path", and the image
     * should be located at "assets/namespace/path.png" in the resources.
     *
     * @param pos The position to draw the image at.
     * @param textureIdentifier The identifier of the image to draw, in the format "namespace:path".
     * @see CommonHooks#drawImage(RenderContext, DrawPos, String)
     */
    void drawImage(DrawPos pos, String textureIdentifier);

    /**
     * Draw the given image at the given position.
     *
     * @param pos The position to draw the image at.
     * @param image The image to draw.
     * @see CommonHooks#drawImage(RenderContext, DrawPos, BufferedImage)
     */
    void drawImage(DrawPos pos, BufferedImage image);

    /**
     * Add a custom render step to the current position in the rendering pipeline.
     * <p>
     * The step will execute each time the gui is rendered, at the point it was added.
     * Any draws accumulated before this call are flushed first so that draw order is
     * preserved.
     *
     * @param step The render step to add.
     */
    void addRenderStep(RenderStep step);

    /**
     * Register a slot where the player can take and place items.
     * <p>
     * Only registers the item movement behavior of the slot. Rendering is handled
     * separately.
     * <p>
     * To use a slot component in a gui that both looks like a slot and supports item
     * movement, use {@link ca.bkaw.praeter.gui.components.Slot#slot}.
     *
     * @param guiSlot The slot definition to register.
     */
    void addSlot(GuiSlot guiSlot);

    /**
     * Register a renderer that will render an item at the given slot index.
     * <p>
     * The position must not also have a slot registered.
     * <p>
     * Typically you want to use {@code renderItem} imported from the platform hooks to
     * render items using the {@code ItemStack} type.
     *
     * @param itemRenderer The item renderer.
     */
    void addItemRenderer(ItemRenderer itemRenderer);

    /**
     * Set up a renderer that will render something when the given condition is true.
     *
     * @param condition The condition to check on the gui.
     * @param renderer The renderer to run to set up the conditional rendering.
     * @return A builder for extending the conditional rendering with elseIf and elseRender.
     * @see CommonHooks#renderIf(RenderContext, Ref, Predicate, Runnable)
     */
    CommonHooks.RenderIf renderIf(Predicate<CustomGui> condition, Runnable renderer);
}
