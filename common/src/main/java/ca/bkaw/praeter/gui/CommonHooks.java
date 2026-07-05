package ca.bkaw.praeter.gui;

import ca.bkaw.praeter.gui.click.ClickContext;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.item.GuiItem;
import ca.bkaw.praeter.gui.item.ItemRenderer;
import ca.bkaw.praeter.gui.render.RenderContext;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The central collection of methods, "hooks", used to set up custom guis.
 * <p>
 * All methods take the {@link RenderContext} as the first argument and may only
 * be called during gui setup. Statically importing the methods of this class is
 * recommended:
 * <pre>
 * import static ca.bkaw.praeter.gui.CommonHooks.*;
 * </pre>
 * On Paper and Fabric, {@code PaperHooks} and {@code FabricHooks} additionally
 * provide platform-specific methods, and are commonly imported alongside this
 * class.
 * <p>
 * Components are set up using static methods on their own classes, for example
 * {@link ca.bkaw.praeter.gui.components.Slot#slot Slot.slot} and
 * {@link ca.bkaw.praeter.gui.components.Panel#panel Panel.panel}.
 */
public class CommonHooks {
    private CommonHooks() {}

    /**
     * Set up a state variable that will be created for each instance of the gui opened.
     *
     * @param r The render context.
     * @param initializer The function that will be called to create the state variable
     *                    for each instance of the gui opened.
     * @return The state variable.
     * @param <T> The type of the state variable.
     * @see RenderContext#useState(Function)
     */
    public static <T> Ref<T> useState(RenderContext r, Function<CustomGui, T> initializer) {
        return r.useState(initializer);
    }

    /**
     * Set up a state variable that will be created for each instance of the gui opened.
     *
     * @param r The render context.
     * @param initializer The supplier that will be called to create the state variable
     *                    for each instance of the gui opened.
     * @return The state variable.
     * @param <T> The type of the state variable.
     * @see RenderContext#useState(Function)
     */
    public static <T> Ref<T> useState(RenderContext r, Supplier<T> initializer) {
        return r.useState(initializer);
    }

    /**
     * Draw an image, given by an identifier, at the given position.
     *
     * @param r The render context.
     * @param pos The position to draw the image at.
     * @param textureIdentifier The identifier of the image to draw, in the format
     *                          "namespace:path".
     * @see RenderContext#drawImage(DrawPos, String)
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
     * @see RenderContext#drawImage(DrawPos, BufferedImage)
     */
    public static void drawImage(RenderContext r, DrawPos pos, BufferedImage image) {
        r.drawImage(pos, image);
    }

    /**
     * Set up a renderer that will render something when the given condition is true.
     *
     * @param r The render context.
     * @param ref The ref variable to check the condition on.
     * @param condition The condition to check on the ref variable.
     * @param renderer The renderer to run to set up the conditional rendering.
     * @param <T> The type of the ref variable.
     * @return A builder for extending the conditional rendering with elseIf and elseRender.
     * @see RenderContext#renderIf
     */
    public static <T> RenderContext.RenderIf renderIf(RenderContext r, Ref<T> ref, Predicate<T> condition, Runnable renderer) {
        return r.renderIf(ref, condition, renderer);
    }

    /**
     * Render an item at the given slot position, without the position being a slot
     * that can be interacted with.
     * <p>
     * The item is computed from the gui instance each time the gui is updated, so
     * the displayed item can be fully dynamic. This can be used to display
     * tooltips or to show items for decorative purposes.
     * <p>
     * The position must not also have a slot registered.
     *
     * @param r The render context.
     * @param pos The slot position to render the item at.
     * @param itemFunction The function that computes the item to display. May
     *                     return null or the empty item to display nothing.
     */
    public static void renderItem(RenderContext r, SlotPos pos, Function<CustomGui, GuiItem> itemFunction) {
        r.addItemRenderer(new ItemRenderer(pos.slotIndex(), itemFunction));
    }

    /**
     * Register a handler that runs when any slot in the top gui is clicked.
     *
     * @param r The render context.
     * @param handler The handler to run.
     * @see RenderContext#onClick(Consumer)
     */
    public static void onClick(RenderContext r, Consumer<ClickContext> handler) {
        r.onClick(handler);
    }

    /**
     * Register a handler that runs when the given slot is clicked.
     *
     * @param r The render context.
     * @param pos The slot position to register the handler for.
     * @param handler The handler to run.
     * @see RenderContext#onClick(SlotPos, Consumer)
     */
    public static void onClick(RenderContext r, SlotPos pos, Consumer<ClickContext> handler) {
        r.onClick(pos, handler);
    }

    /**
     * Register a handler that runs when any of the given slots are clicked.
     *
     * @param r The render context.
     * @param positions The slot positions to register the handler for.
     * @param handler The handler to run.
     * @see RenderContext#onClick(Iterable, Consumer)
     */
    public static void onClick(RenderContext r, Iterable<SlotPos> positions, Consumer<ClickContext> handler) {
        r.onClick(positions, handler);
    }

    /**
     * Display text when the user hovers the given slot position.
     * <p>
     * The text is displayed using an invisible item whose tooltip contains the
     * text. The position cannot be interacted with.
     *
     * @param r The render context.
     * @param pos The position to display the text at.
     * @param text The lines of text to display.
     */
    public static void hoverText(RenderContext r, SlotPos pos, String... text) {
        if (text.length == 0) {
            throw new IllegalArgumentException("At least one line of text must be provided.");
        }
        GuiItem item = PraeterGui.instance().getPlatform().createHoverTextItem(List.of(text));
        renderItem(r, pos, gui -> item);
    }
}
