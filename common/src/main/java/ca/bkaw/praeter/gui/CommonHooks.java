package ca.bkaw.praeter.gui;

import ca.bkaw.praeter.gui.components.Button;
import ca.bkaw.praeter.gui.components.HoverText;
import ca.bkaw.praeter.gui.components.Panel;
import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.item.GuiItem;
import ca.bkaw.praeter.gui.item.ItemRenderer;
import ca.bkaw.praeter.gui.render.RenderContext;
import ca.bkaw.praeter.gui.slot.SlotBehavior;

import java.awt.image.BufferedImage;
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
 * On Paper and Fabric, {@code PaperHooks} and {@code FabricHooks} provide the
 * same methods along with variants that use platform types. Static import the
 * platform's hooks instead when writing platform-specific code.
 */
public class CommonHooks {
    protected CommonHooks() {}

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
     * A slot where the user can take and place items, that can hold any item.
     *
     * @param r The render context.
     * @param pos The position of the slot.
     * @return A reference to the slot state.
     */
    public static Ref<Slot> slot(RenderContext r, SlotPos pos) {
        return Slot.slot(r, pos);
    }

    /**
     * A slot where the user can take and place items, with the given behavior.
     *
     * @param r The render context.
     * @param pos The position of the slot.
     * @param behavior The behavior of the slot.
     * @return A reference to the slot state.
     */
    public static Ref<Slot> slot(RenderContext r, SlotPos pos, SlotBehavior behavior) {
        return Slot.slot(r, pos, behavior);
    }

    /**
     * A button with the given text.
     *
     * @param r The render context.
     * @param text The text on the button.
     * @param pos The position to draw the button.
     * @param width The width, in pixels, of the button.
     * @param height The height, in pixels, of the button.
     * @return A reference to the button component.
     */
    public static Ref<Button> button(RenderContext r, String text, DrawPos pos, int width, int height) {
        return Button.button(r, text, pos, width, height);
    }

    /**
     * An indented area that looks like a slot, but with any size.
     *
     * @param r The render context.
     * @param pos The position to render the panel at.
     * @param width The width of the panel.
     * @param height The height of the panel.
     */
    public static void panel(RenderContext r, DrawPos pos, int width, int height) {
        Panel.panel(r, pos, width, height);
    }

    /**
     * Display text when the user hovers the given slot position.
     *
     * @param r The render context.
     * @param pos The position to display the text at.
     * @param text The lines of text to display.
     */
    public static void hoverText(RenderContext r, SlotPos pos, String... text) {
        HoverText.hoverText(r, pos, text);
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
}
