package ca.bkaw.praeter.gui.render;

import ca.bkaw.praeter.gui.draw.GuiBackgroundPainter;
import ca.bkaw.praeter.gui.draw.GuiFontSequenceBuilder;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.pack.ResourcePack;
import ca.bkaw.praeter.gui.pack.font.FontSequence;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The root implementation of {@link RenderContext}.
 */
public class RenderContextImpl implements RenderContext {
    /**
     * The key used to identify the font to use for gui rendering.
     * <p>
     * The same font key is used for all guis to maximize re-use of common characters
     * such as spaces. Note that this means the characters used by a gui might change
     * between restarts if new guis are added. However, since the characters are not
     * stored past restart, this should not cause any issues.
     */
    public static final String FONT_KEY = "praeter_gui:font";

    private final ResourcePack resourcePack;
    private final GuiBackgroundPainter background;
    private @Nullable GuiFontSequenceBuilder fontSequenceBuilder;
    private final List<RenderStep> rootRenderBlock = new ArrayList<>();
    private List<RenderStep> currentRenderBlock = this.rootRenderBlock;
    private final List<StateRefImpl<?>> stateRefs = new ArrayList<>();

    public RenderContextImpl(int rows, ResourcePack resourcePack, ResourcePack vanillaAssets) throws IOException {
        this.resourcePack = resourcePack;
        this.background = new GuiBackgroundPainter(rows, this.resourcePack, vanillaAssets);
    }

    public GuiBackgroundPainter getBackground() {
        return this.background;
    }

    public List<RenderStep> getRootRenderBlock() {
        return this.rootRenderBlock;
    }

    public List<StateRefImpl<?>> getStateRefs() {
        return this.stateRefs;
    }

    /**
     * Get a render step that renders the background of the gui.
     *
     * @return The render step.
     * @throws IOException If an I/O error occurs.
     */
    public RenderStep buildBackgroundRenderStep() throws IOException {
        FontSequence fontSequence = new GuiFontSequenceBuilder(this.resourcePack, FONT_KEY)
            .drawImage(this.background.getImage(), 0, 0)
            .build();
        return RenderStep.renderFontSequence(fontSequence);
    }

    @Override
    public <T> Ref<T> useState(Function<CustomGui, T> initializer) {
        StateRefImpl<T> ref = new StateRefImpl<>(initializer);
        this.stateRefs.add(ref);
        return ref;
    }

    @Override
    public void drawImage(DrawPos pos, String textureIdentifier) {
        try {
            if (this.fontSequenceBuilder == null) {
                this.background.drawImage(textureIdentifier, pos.x(), pos.y());
            } else {
                int x = pos.x() + GuiFontSequenceBuilder.GUI_ORIGIN_OFFSET_X;
                int y = pos.y() + GuiFontSequenceBuilder.GUI_ORIGIN_OFFSET_Y;
                this.fontSequenceBuilder.drawImage(textureIdentifier, x, y);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to draw image with identifier: " + textureIdentifier, e);
        }
    }

    @Override
    public void drawImage(DrawPos pos, BufferedImage image) {
        try {
            if (this.fontSequenceBuilder == null) {
                this.background.drawImage(image, pos.x(), pos.y());
            } else {
                int x = pos.x() + GuiFontSequenceBuilder.GUI_ORIGIN_OFFSET_X;
                int y = pos.y() + GuiFontSequenceBuilder.GUI_ORIGIN_OFFSET_Y;
                this.fontSequenceBuilder.drawImage(image, x, y);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to draw image.", e);
        }
    }

    /**
     * Flush the current font sequence builder, if any, by building it and adding the
     * resulting font sequence as a render step to the current render block.
     * <p>
     * After this call, there is no active font sequence builder.
     */
    private void flushFontSequence() {
        if (this.fontSequenceBuilder != null) {
            FontSequence fontSequence = this.fontSequenceBuilder.build();
            this.currentRenderBlock.add(RenderStep.renderFontSequence(fontSequence));
            this.fontSequenceBuilder = null;
        }
    }

    /**
     * Run the given renderer in isolation, collecting everything it draws into a new
     * render block that is returned as a single render step.
     * <p>
     * The current render block and font sequence builder are saved before and restored
     * after (even on failure), so the caller's drawing context is left untouched.
     *
     * @param renderer The renderer to run.
     * @return The render step containing everything the renderer drew.
     */
    private RenderStep buildRenderBlock(Runnable renderer) {
        GuiFontSequenceBuilder previousBuilder = this.fontSequenceBuilder;
        List<RenderStep> previousBlock = this.currentRenderBlock;
        try {
            // Run the renderer with a fresh block and font sequence builder so that
            // everything it draws becomes conditionally renderable font sequences.
            this.currentRenderBlock = new ArrayList<>();
            this.fontSequenceBuilder = new GuiFontSequenceBuilder(this.resourcePack, FONT_KEY);
            renderer.run();
            this.flushFontSequence();
            return RenderStep.renderBlock(this.currentRenderBlock);
        } catch (IOException e) {
            throw new RuntimeException("Failed to set up conditional rendering.", e);
        } finally {
            // Restore the caller's drawing context.
            this.currentRenderBlock = previousBlock;
            this.fontSequenceBuilder = previousBuilder;
        }
    }

    @Override
    public void addRenderStep(RenderStep step) {
        boolean hadBuilder = this.fontSequenceBuilder != null;
        this.flushFontSequence();
        this.currentRenderBlock.add(step);
        if (hadBuilder) {
            try {
                this.fontSequenceBuilder = new GuiFontSequenceBuilder(this.resourcePack, FONT_KEY);
            } catch (IOException e) {
                throw new RuntimeException("Failed to resume rendering after custom step.", e);
            }
        }
    }

    public static class ConditionalRenderStep<T> implements RenderStep {
        private final Ref<T> ref;
        private final Predicate<T> condition;
        private final RenderStep ifBlock;
        private @Nullable RenderStep elseStep = null;

        public ConditionalRenderStep(Ref<T> ref, Predicate<T> condition, RenderStep ifBlock) {
            this.ref = ref;
            this.condition = condition;
            this.ifBlock = ifBlock;
        }

        @Override
        public void render(RenderDispatcher rd, CustomGui gui) {
            T value = this.ref.get(gui);
            if (this.condition.test(value)) {
                this.ifBlock.render(rd, gui);
            } else if (this.elseStep != null) {
                this.elseStep.render(rd, gui);
            }
        }
    }

    public class RenderIfImpl implements RenderIf {
        private final ConditionalRenderStep<?> renderStep;

        public RenderIfImpl(ConditionalRenderStep<?> renderStep) {
            this.renderStep = renderStep;
        }

        @Override
        public <T> RenderIf elseIf(Ref<T> ref, Predicate<T> condition, Runnable renderer) {
            // The else branch is only reachable through the else chain, so it must not
            // be added to the current render block as a standalone step.
            RenderIfImpl elseBranch = createRenderIf(ref, condition, renderer);
            this.renderStep.elseStep = elseBranch.renderStep;
            return elseBranch;
        }

        @Override
        public void elseRender(Runnable renderer) {
            this.renderStep.elseStep = buildRenderBlock(renderer);
        }
    }

    /**
     * Build a conditional render step without adding it to the current render block.
     */
    private <T> RenderIfImpl createRenderIf(Ref<T> ref, Predicate<T> condition, Runnable renderer) {
        RenderStep ifBlock = this.buildRenderBlock(renderer);
        ConditionalRenderStep<T> renderStep = new ConditionalRenderStep<>(ref, condition, ifBlock);
        return new RenderIfImpl(renderStep);
    }

    @Override
    public <T> RenderIfImpl renderIf(Ref<T> ref, Predicate<T> condition, Runnable renderer) {
        // Whether the following draws should continue going to a font sequence (we are
        // already inside a conditional) or back to the baked background (top level).
        boolean insideConditional = this.fontSequenceBuilder != null;

        // Commit anything drawn before this conditional so draw order is preserved.
        this.flushFontSequence();

        RenderIfImpl result = this.createRenderIf(ref, condition, renderer);
        this.currentRenderBlock.add(result.renderStep);

        // Resume drawing into the current block when we are inside a conditional, so
        // draws after this renderIf still render (conditionally, and on top).
        // TODO background will always render below. Perhaps we should only render
        //  using font sequences after the first conditional, so that z-indexing is predictable?
        if (insideConditional) {
            try {
                this.fontSequenceBuilder = new GuiFontSequenceBuilder(this.resourcePack, FONT_KEY);
            } catch (IOException e) {
                throw new RuntimeException("Failed to set up rendering after conditional.", e);
            }
        }
        return result;
    }
}
