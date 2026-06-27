package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.pack.font.FontSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * An object created when a gui is being rendered.
 */
public class RenderDispatcher {
    private final List<FontSequence> renderTitle = new ArrayList<>();

    /**
     * Add the specified font sequence to the render title of the gui.
     *
     * @param fontSequence The font sequence to add to the render title.
     */
    public void render(FontSequence fontSequence) {
        this.renderTitle.add(fontSequence);
    }

    /**
     * Get the list of font sequences that are rendered as the title of the gui.
     *
     * @return The list of font sequences.
     */
    public List<FontSequence> getRenderTitle() {
        return this.renderTitle;
    }
}
