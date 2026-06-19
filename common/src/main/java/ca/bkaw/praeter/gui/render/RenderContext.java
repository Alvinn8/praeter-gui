package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.gui.CustomGui;

import java.awt.image.BufferedImage;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The context used when setting up rendering for a component or gui.
 * <p>
 * Instances of {@link RenderContext} may only be used during the setup phase
 * (server startup) and may never be stored, captured, or used after startup.
 */
public interface RenderContext {

    /**
     * Set up a state variable that will be created for each instance of the gui opened.
     * <p>
     * Example usage:
     * <pre>
     * State&lt;Integer&gt; counter = r.useState(gui -&gt; 0);
     * r.onClick(gui -&gt; {
     *   int count = counter.get(gui);
     *   counter.set(gui, count + 1);
     * });
     * </pre>
     *
     * @param initializer The function that will be called to create the state variable
     *                    for each instance of the gui opened.
     * @return The state variable.
     * @param <T> The type of the state variable.
     */
    <T> State<T> useState(Function<CustomGui, T> initializer);

    /**
     * Set up a state variable that will be created for each instance of the gui opened.
     *
     * @param initializer The supplier that will be called to create the state variable
     *                    for each instance of the gui opened.
     * @return The state variable.
     * @param <T> The type of the state variable.
     * @see #useState(Function)
     */
    default <T> State<T> useState(Supplier<T> initializer) {
        return useState(_ -> initializer.get());
    }

    /**
     * Draw an image, given by a resource location, at the given position.
     * <p>
     * The resource location should be in the format "namespace:path", and the image
     * should be located at "assets/namespace/path.png" in the resources.
     *
     * @param pos The position to draw the image at.
     * @param resourceLocation The resource location of the image to draw, in the format "namespace:path".
     */
    void drawImage(DrawPos pos, String resourceLocation);

    /**
     * Draw the given image at the given position.
     *
     * @param pos The position to draw the image at.
     * @param image The image to draw.
     */
    void drawImage(DrawPos pos, BufferedImage image);

    /**
     * Set up a renderer that will render something when the given condition is true.
     * <p>
     * Example usage:
     * <pre>
     * State&lt;Integer&gt; counter = r.useState(() -&gt; 0);
     * r.renderIf(counter, count -&gt; count % 2 == 0, () -&gt; {
     *   r.drawImage(DrawPos.slotCorner(0, 0), "example:gui/even_icon");
     * }).elseRender(() -&gt; {
     *   r.drawImage(DrawPos.slotCorner(0, 0), "example:gui/odd_icon");
     * });
     * </pre>
     *
     * @param state The state variable to check the condition on.
     * @param condition The condition to check on the state variable.
     * @param renderer The renderer to run to set up the conditional rendering.
     * @param <T> The type of the state variable.
     * @return A builder for extending the conditional rendering with elseIf and elseRender.
     */
    <T> RenderIf<T> renderIf(State<T> state, Predicate<T> condition, Runnable renderer);

    /**
     * A builder for extending a renderIf with an elseRender.
     *
     * @param <T> The type of the state variable.
     */
    interface RenderIf<T> {
        /**
         * Set up a renderer that will render something when the earlier condition is false
         * and this condition is true.
         *
         * @param condition The condition to check on the state variable.
         * @param renderer The renderer to run to set up the conditional rendering.
         * @return A builder for extending the conditional rendering with more elseIf and elseRender.
         */
        RenderIf<T> elseIf(Predicate<T> condition, Runnable renderer);

        /**
         * Set up a renderer that will render something when the earlier condition is false.
         *
         * @param renderer The renderer to run to set up the conditional rendering.
         */
        void elseRender(Runnable renderer);

    }
}
