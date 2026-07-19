package ca.bkaw.praeter.gui;

import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.gui.StateRefImpl;
import ca.bkaw.praeter.gui.platform.GuiItem;
import ca.bkaw.praeter.gui.render.RenderContext;
import ca.bkaw.praeter.gui.slot.ItemRenderer;

import java.awt.image.BufferedImage;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A central collection of methods, "hooks", used during gui setup.
 * <p>
 * All methods that take {@link RenderContext} as the first argument may only
 * be called during gui setup. Statically importing methods of this class is
 * recommended.
 * <p>
 * On Paper and Fabric, {@code PaperHooks} and {@code FabricHooks} ("the platform
 * hooks") additionally provide platform-specific methods, and are commonly imported
 * alongside this
 * class.
 * <p>
 * Components are set up using static methods on their own classes, for example
 * {@link ca.bkaw.praeter.gui.components.Slot#slot Slot.slot} and
 * {@link ca.bkaw.praeter.gui.components.Button#button Button.button}.
 */
public class CommonHooks {
    private CommonHooks() {}

    /**
     * Set up a state variable that will be created for each instance of the gui opened.
     * <p>
     * Example usage:
     * <pre>
     * class Counter { int count = 0; }
     * Ref&lt;Counter&gt; counter = useState(r, gui -&gt; new Counter());
     * onClick(r, gui -&gt; {
     *   counter.get(gui).value++;
     * });
     * </pre>
     *
     * @param r The render context.
     * @param initializer The function that will be called to create the state variable
     *                    for each instance of the gui opened.
     * @return The state variable.
     * @param <T> The type of the state variable.
     */
    public static <T> Ref<T> useState(RenderContext r, Function<CustomGui, T> initializer) {
        StateRefImpl<T> ref = new StateRefImpl<>(initializer);
        r.addStateRef(ref);
        return ref;
    }

    /**
     * Set up a state variable that will be created for each instance of the gui opened.
     *
     * @param initializer The supplier that will be called to create the state variable
     *                    for each instance of the gui opened.
     * @return The state variable.
     * @param <T> The type of the state variable.
     * @see #useState(RenderContext, Function)
     */
    public static <T> Ref<T> useState(RenderContext r, Supplier<T> initializer) {
        return useState(r, _ -> initializer.get());
    }

    /**
     * Draw an image, given by an identifier, at the given position.
     * <p>
     * The identifier should be in the format "namespace:path", and the image
     * should be located at "assets/namespace/path.png" in the resources.
     *
     * @param r The render context.
     * @param pos The position to draw the image at.
     * @param textureIdentifier The identifier of the image to draw, in the format "namespace:path".
     */
    public static void drawImage(RenderContext r, DrawPos pos, String textureIdentifier) {
        r.drawImage(pos, textureIdentifier);
    }

    /**
     * Draw the given image at the given position.
     *
     * @param r The render context.
     * @param pos The position to draw the image at.
     * @param image The image to draw.
     */
    public static void drawImage(RenderContext r, DrawPos pos, BufferedImage image) {
        r.drawImage(pos, image);
    }

    /**
     * Set up a renderer that will render something when the given condition is true.
     * <p>
     * Example usage:
     * <pre>
     * class Counter { int count = 0; }
     * Ref&lt;Counter&gt; COUNTER = useState(r, gui -&gt; new Counter());
     * renderIf(r, COUNTER, counter -&gt; counter.count % 2 == 0, () -&gt; {
     *   r.drawImage(DrawPos.slotCorner(0, 0), "example:gui/even_icon");
     * }).elseRender(() -&gt; {
     *   r.drawImage(DrawPos.slotCorner(0, 0), "example:gui/odd_icon");
     * });
     * </pre>
     *
     * @param ref The ref variable to check the condition on.
     * @param condition The condition to check on the ref variable.
     * @param renderer The renderer to run to set up the conditional rendering.
     * @param <T> The type of the ref variable.
     * @return A builder for extending the conditional rendering with elseIf and elseRender.
     */
    public static <T> RenderContext.RenderIf renderIf(RenderContext r, Ref<T> ref, Predicate<T> condition, Runnable renderer) {
        return r.renderIf(ref, condition, renderer);
    }

    /**
     * Render a gui item at the given slot position.
     * <p>
     * Typically, you want to use {@code renderItem} imported from the platform hooks
     * to render items using the {@code ItemStack} type.
     *
     * @param r The render context.
     * @param pos The slot position to render the item at.
     * @param itemFunction A function that takes the gui and returns the gui item to render.
     */
    public static void renderGuiItem(RenderContext r, SlotPos pos, Function<CustomGui, GuiItem> itemFunction) {
        r.addItemRenderer(new ItemRenderer(pos.slotIndex(), itemFunction));
    }

    /**
     * Display text when the user hovers the given slot position.
     * <p>
     * There may not be any slots or item renderers at the position.
     *
     * @param r The render context.
     * @param pos The position to display the text at.
     * @param text The lines of text to display.
     */
    public static void hoverText(RenderContext r, SlotPos pos, String... text) {
        if (text.length == 0) {
            throw new IllegalArgumentException("At least one line of text must be provided.");
        }
        PraeterGui.instance().getPlatform().plainTextHoverText(r, pos, text);
    }
}
