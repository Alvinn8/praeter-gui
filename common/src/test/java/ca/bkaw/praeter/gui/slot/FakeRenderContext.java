package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.click.ClickContext;
import ca.bkaw.praeter.gui.click.SlotClickHandler;
import ca.bkaw.praeter.gui.draw.DrawPos;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.gui.StateRefImpl;
import ca.bkaw.praeter.gui.item.ItemRenderer;
import ca.bkaw.praeter.gui.render.RenderContext;
import ca.bkaw.praeter.gui.render.RenderStep;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
    private final List<Consumer<ClickContext>> clickHandlers = new ArrayList<>();
    private final List<SlotClickHandler> slotClickHandlers = new ArrayList<>();

    public List<StateRefImpl<?>> getStateRefs() {
        return this.stateRefs;
    }

    public List<GuiSlot> getGuiSlots() {
        return this.guiSlots;
    }

    public List<ItemRenderer> getItemRenderers() {
        return this.itemRenderers;
    }

    public List<Consumer<ClickContext>> getClickHandlers() {
        return this.clickHandlers;
    }

    public List<SlotClickHandler> getSlotClickHandlers() {
        return this.slotClickHandlers;
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
    public void addItemRenderer(ItemRenderer itemRenderer) {
        this.itemRenderers.add(itemRenderer);
    }

    @Override
    public void addRenderStep(RenderStep step) {
    }

    @Override
    public void onClick(Consumer<ClickContext> handler) {
        this.clickHandlers.add(handler);
    }

    @Override
    public void onClick(SlotPos pos, Consumer<ClickContext> handler) {
        this.slotClickHandlers.add(new SlotClickHandler(pos.slotIndex(), handler));
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
