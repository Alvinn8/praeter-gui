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
    private final List<RenderStep> renderSteps = new ArrayList<>();
    private List<RenderStep> currentRenderBlock = this.renderSteps;

    public RenderContextImpl(int rows, ResourcePack resourcePack, ResourcePack vanillaAssets) throws IOException {
        this.resourcePack = resourcePack;
        this.background = new GuiBackgroundPainter(rows, this.resourcePack, vanillaAssets);
    }

    @Override
    public <T> Ref<T> useState(Function<CustomGui, T> initializer) {
        return null;
    }

    @Override
    public void drawImage(DrawPos pos, String textureIdentifier) {
        try {
            if (this.fontSequenceBuilder == null) {
                this.background.drawImage(textureIdentifier, pos.x(), pos.y());
            } else {
                this.fontSequenceBuilder.drawImage(textureIdentifier, pos.x(), pos.y());
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
                this.fontSequenceBuilder.drawImage(image, pos.x(), pos.y());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to draw image.", e);
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

    /**
     * Finalize the current render block by building any font sequence being built and
     * adding it to the current render block.
     *
     * @return The render step.
     */
    private RenderStep finalizeRenderBlock() {
        if (this.fontSequenceBuilder != null) {
            FontSequence fontSequence = this.fontSequenceBuilder.build();
            this.currentRenderBlock.add(RenderStep.renderFontSequence(fontSequence));
            this.fontSequenceBuilder = null;
        }
        List<RenderStep> renderBlock = this.currentRenderBlock;
        this.currentRenderBlock = new ArrayList<>();
        return RenderStep.renderBlock(renderBlock);
    }

    private RenderStep buildRenderBlock(Runnable renderer) {
        // If there is already a font sequence builder, build the font sequence and add
        // it to the current render block before running the conditional rendering.
        GuiFontSequenceBuilder previous = this.fontSequenceBuilder;
        if (previous != null) {
            FontSequence fontSequence = previous.build();
            this.currentRenderBlock.add(RenderStep.renderFontSequence(fontSequence));
        }
        List<RenderStep> previousRenderBlock = this.currentRenderBlock;
        try {
            // Run the renderer with a different painter so that the font sequences can be
            // used for conditional rendering.
            this.currentRenderBlock = new ArrayList<>();
            this.fontSequenceBuilder = new GuiFontSequenceBuilder(this.resourcePack, FONT_KEY);
            renderer.run();
            RenderStep renderStep = this.finalizeRenderBlock();

            // If there was a previous font sequence builder, restore it so that rendering
            // can continue after the conditional rendering.
            if (previous != null) {
                this.fontSequenceBuilder = new GuiFontSequenceBuilder(this.resourcePack, FONT_KEY);
                this.currentRenderBlock = previousRenderBlock;
            }
            return renderStep;
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up conditional rendering.", e);
        }
    }

    public class RenderIfImpl<T> implements RenderIf<T> {
        private final ConditionalRenderStep<T> renderStep;

        public RenderIfImpl(ConditionalRenderStep<T> renderStep) {
            this.renderStep = renderStep;
        }

        @Override
        public RenderIf<T> elseIf(Predicate<T> condition, Runnable renderer) {
            RenderIfImpl<T> elseBranch = renderIf(this.renderStep.ref, condition, renderer);
            this.renderStep.elseStep = elseBranch.renderStep;
            return elseBranch;
        }

        @Override
        public void elseRender(Runnable renderer) {
            this.renderStep.elseStep = buildRenderBlock(renderer);
        }
    }

    @Override
    public <T> RenderIfImpl<T> renderIf(Ref<T> ref, Predicate<T> condition, Runnable renderer) {
        RenderStep ifBlock = this.buildRenderBlock(renderer);
        ConditionalRenderStep<T> renderStep = new ConditionalRenderStep<>(ref, condition, ifBlock);
        this.currentRenderBlock.add(renderStep);
        return new RenderIfImpl<>(renderStep);
    }
}
