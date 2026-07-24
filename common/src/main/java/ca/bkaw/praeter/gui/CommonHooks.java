package ca.bkaw.praeter.gui;

import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.gui.ClickContext;
import ca.bkaw.praeter.gui.slot.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.gui.StateRefImpl;
import ca.bkaw.praeter.gui.platform.GuiItem;
import ca.bkaw.praeter.gui.render.RenderContext;
import ca.bkaw.praeter.gui.slot.ItemRenderer;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;
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
     * Set up a listener that will be called when a new gui instance is created.
     *
     * @param r The render context.
     * @param action The listener.
     */
    public static void onCreated(RenderContext r, Consumer<CustomGui> action) {
        r.addCreatedListener(action);
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
     * renderIf(r, gui -&gt; COUNTER.get(gui).count % 2 == 0, () -&gt; {
     *   r.drawImage(DrawPos.slotCorner(0, 0), "example:gui/even_icon");
     * }).elseRender(() -&gt; {
     *   r.drawImage(DrawPos.slotCorner(0, 0), "example:gui/odd_icon");
     * });
     * </pre>
     *
     * @param condition The condition to check on the ref variable.
     * @param renderer The renderer to run to set up the conditional rendering.
     * @return A builder for extending the conditional rendering with elseIf and elseRender.
     */
    public static RenderIf renderIf(RenderContext r, Predicate<CustomGui> condition, Runnable renderer) {
        return r.renderIf(condition, renderer);
    }

    /**
     * Set up a renderer that will render something when the given condition is true.
     * <p>
     * This method is a convenience method for checking a condition on a state variable.
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
    public static <T> RenderIf renderIf(RenderContext r, Ref<T> ref, Predicate<T> condition, Runnable renderer) {
        return r.renderIf(gui -> condition.test(ref.get(gui)), renderer);
    }

    /**
     * A builder for extending a {@link #renderIf} with elseIf and elseRender.
     */
    public interface RenderIf {
        /**
         * Set up a renderer that will render something when the earlier condition is false
         * and this condition is true.
         *
         * @param condition The condition to check on the gui.
         * @param renderer The renderer to run to set up the conditional rendering.
         * @return A builder for extending the conditional rendering with more elseIf and elseRender.
         */
        RenderIf elseIf(Predicate<CustomGui> condition, Runnable renderer);

        /**
         * Set up a renderer that will render something when the earlier condition is false
         * and this condition is true.
         *
         * @param ref The ref variable to check the condition on.
         * @param condition The condition to check on the state variable.
         * @param renderer The renderer to run to set up the conditional rendering.
         * @return A builder for extending the conditional rendering with more elseIf and elseRender.
         * @param <T> The type of the state variable for the elseIf condition.
         */
        default <T> RenderIf elseIf(Ref<T> ref, Predicate<T> condition, Runnable renderer) {
            return elseIf(gui -> condition.test(ref.get(gui)), renderer);
        }

        /**
         * Set up a renderer that will render something when the earlier condition is false.
         *
         * @param renderer The renderer to run to set up the conditional rendering.
         */
        void elseRender(Runnable renderer);
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

    // Click handlers. You probably want to use onClick imported from the platform
    // hooks instead, which will give you a click context with platform-specific
    // methods.

    /**
     * Call the callback when the user clicks any slot in the gui.
     *
     * @param gui The gui instance.
     * @param action The listener.
     */
    public static void commonOnClick(CustomGui gui, Consumer<ClickContext> action) {
        gui.addClickListener(action);
    }

    /**
     * Call the callback when the user clicks the given slots.
     *
     * @param gui The gui instance.
     * @param pos The slot positions.
     * @param action The listener.
     */
    public static void commonOnClick(CustomGui gui, Iterable<SlotPos> pos, Consumer<ClickContext> action) {
        commonOnClick(gui, clickContext -> {
            for (SlotPos slotPos : pos) {
                if (clickContext.getSlotPos().equals(slotPos)) {
                    action.accept(clickContext);
                    return;
                }
            }
        });
    }

    /**
     * Call the callback when the user clicks any slot in the gui.
     *
     * @param r The render context.
     * @param action The listener.
     */
    public static void commonOnClick(RenderContext r, Consumer<ClickContext> action) {
        onCreated(r, gui -> gui.addClickListener(action));
    }

    /**
     * Call the callback when the user clicks the given slots.
     *
     * @param r The render context.
     * @param pos The slot positions.
     * @param action The listener.
     */
    public static void commonOnClick(RenderContext r, Iterable<SlotPos> pos, Consumer<ClickContext> action) {
        onCreated(r, gui -> commonOnClick(gui, pos, action));
    }

    /**
     * Call the callback when the user clicks the given slots.
     *
     * @param r The render context.
     * @param posRef A reference to the slot positions.
     * @param action The listener.
     * @param <T> The type of the reference, eg. Button or SlotPos.
     */
    public static <T extends Iterable<SlotPos>> void commonOnClick(RenderContext r, Ref<T> posRef, Consumer<ClickContext> action) {
        onCreated(r, gui -> commonOnClick(gui, posRef.get(gui), action));
    }
}
