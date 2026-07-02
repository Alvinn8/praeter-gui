package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.gui.StateRefImpl;
import ca.bkaw.praeter.gui.item.GuiItem;
import ca.bkaw.praeter.gui.item.ItemRenderer;
import ca.bkaw.praeter.gui.render.RenderContext;
import ca.bkaw.praeter.gui.render.RenderStep;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A render context for testing slot behavior that collects state and slots but
 * ignores all drawing.
 */
public class FakeRenderContext implements RenderContext {
    private final List<StateRefImpl<?>> stateRefs = new ArrayList<>();
    private final List<GuiSlot> guiSlots = new ArrayList<>();
    private final List<ItemRenderer> itemRenderers = new ArrayList<>();

    public List<StateRefImpl<?>> getStateRefs() {
        return this.stateRefs;
    }

    public List<GuiSlot> getGuiSlots() {
        return this.guiSlots;
    }

    public List<ItemRenderer> getItemRenderers() {
        return this.itemRenderers;
    }

    @Override
    public <T> Ref<T> useState(Function<CustomGui, T> initializer) {
        StateRefImpl<T> ref = new StateRefImpl<>(initializer);
        this.stateRefs.add(ref);
        return ref;
    }

    @Override
    public void drawImage(DrawPos pos, String textureIdentifier) {
    }

    @Override
    public void drawImage(DrawPos pos, BufferedImage image) {
    }

    @Override
    public void addSlot(GuiSlot guiSlot) {
        this.guiSlots.add(guiSlot);
    }

    @Override
    public void renderItem(SlotPos pos, Function<CustomGui, GuiItem> itemFunction) {
        this.itemRenderers.add(new ItemRenderer(pos.slotIndex(), itemFunction));
    }

    @Override
    public void addRenderStep(RenderStep step) {
    }

    @Override
    public <T> RenderIf renderIf(Ref<T> ref, Predicate<T> condition, Runnable renderer) {
        renderer.run();
        return new RenderIf() {
            @Override
            public <U> RenderIf elseIf(Ref<U> ref, Predicate<U> condition, Runnable renderer) {
                renderer.run();
                return this;
            }

            @Override
            public void elseRender(Runnable renderer) {
                renderer.run();
            }
        };
    }
}
