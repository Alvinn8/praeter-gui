package ca.bkaw.praeter.render;

import ca.bkaw.praeter.gui.CustomGui;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
	 *
	 * @param state The state variable to check the condition on.
	 * @param condition The condition to check on the state variable.
	 * @param renderer The renderer to run if the condition is true.
	 * @param <T> The type of the state variable.
	 */
	<T> RenderIf<T> renderIf(State<T> state, Predicate<T> condition, Consumer<RenderContext> renderer);

	/**
	 * A builder for extending a renderIf with an elseRender.
	 *
	 * @param <T> The type of the state variable.
	 */
	interface RenderIf<T> {
		/**
		 * Set up a renderer that will render something when the earlier condition is false.
		 */
		void elseRender(Consumer<RenderContext> renderer);

	}
}
