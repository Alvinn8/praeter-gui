package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.pack.font.FontSequence;

import java.util.List;

/**
 * A step in the rendering process.
 * <p>
 * This is used to represent a single step in the rendering process, such as drawing an image or rendering a conditional block.
 */
public interface RenderStep {
    /**
     * Execute this render step.
     *
     * @param rd  The render dispatcher to render font sequences to.
     * @param gui The gui instance to render for.
     */
    void render(RenderDispatcher rd, CustomGui gui);

    /**
     * Create a render step that renders the specified font sequence.
     *
     * @param fontSequence The font sequence to render.
     * @return A render step that renders the specified font sequence.
     */
    static RenderStep renderFontSequence(FontSequence fontSequence) {
        if (fontSequence.length() == 0) {
            return (_, _) -> {};
        }
        return (rd, _) -> rd.render(fontSequence);
    }

    /**
     * Create a render step that renders the render block (list of render steps).
     *
     * @param steps The list of render steps.
     * @return A render step that renders the render block.
     */
    static RenderStep renderBlock(List<RenderStep> steps) {
        return (rd, gui) -> {
            for (RenderStep step : steps) {
                step.render(rd, gui);
            }
        };
    }
}
