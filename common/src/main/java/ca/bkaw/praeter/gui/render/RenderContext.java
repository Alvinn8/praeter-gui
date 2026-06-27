package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.Ref;

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
     * class Counter { int count = 0; }
     * Ref&lt;Counter&gt; counter = r.useState(gui -&gt; new Counter());
     * r.onClick(gui -&gt; {
     *   counter.get(gui).value++;
     * });
     * </pre>
     *
     * @param initializer The function that will be called to create the state variable
     *                    for each instance of the gui opened.
     * @return The state variable.
     * @param <T> The type of the state variable.
     */
    <T> Ref<T> useState(Function<CustomGui, T> initializer);

    /**
     * Set up a state variable that will be created for each instance of the gui opened.
     *
     * @param initializer The supplier that will be called to create the state variable
     *                    for each instance of the gui opened.
     * @return The state variable.
     * @param <T> The type of the state variable.
     * @see #useState(Function)
     */
    default <T> Ref<T> useState(Supplier<T> initializer) {
        return useState(_ -> initializer.get());
    }

    /**
     * Draw an image, given by an identifier, at the given position.
     * <p>
     * The identifier should be in the format "namespace:path", and the image
     * should be located at "assets/namespace/path.png" in the resources.
     *
     * @param pos The position to draw the image at.
     * @param textureIdentifier The identifier of the image to draw, in the format "namespace:path".
     */
    void drawImage(DrawPos pos, String textureIdentifier);

    /**
     * Draw the given image at the given position.
     *
     * @param pos The position to draw the image at.
     * @param image The image to draw.
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
     * Set up a renderer that will render something when the given condition is true.
     * <p>
     * Example usage:
     * <pre>
     * Ref&lt;Integer&gt; counter = r.useState(() -&gt; 0);
     * r.renderIf(counter, count -&gt; count % 2 == 0, () -&gt; {
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
    <T> RenderIf renderIf(Ref<T> ref, Predicate<T> condition, Runnable renderer);

    /**
     * A builder for extending a renderIf with an elseRender.
     */
    interface RenderIf {
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
        <T> RenderIf elseIf(Ref<T> ref, Predicate<T> condition, Runnable renderer);

        /**
         * Set up a renderer that will render something when the earlier condition is false.
         *
         * @param renderer The renderer to run to set up the conditional rendering.
         */
        void elseRender(Runnable renderer);

    }
}
