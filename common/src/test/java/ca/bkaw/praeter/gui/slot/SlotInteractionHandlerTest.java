package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.draw.SlotPos;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.item.GuiItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SlotInteractionHandlerTest {
    private static final FakeGuiItem STONE_64 = FakeGuiItem.of("stone", 64);
    private static final FakeGuiItem STONE_10 = FakeGuiItem.of("stone", 10);
    private static final FakeGuiItem DIRT_5 = FakeGuiItem.of("dirt", 5);

    private CustomGuiType type;
    private CustomGui gui;
    private Ref<Slot> slotA; // slot index 2 (top row)
    private Ref<Slot> slotB; // slot index 4 (top row)

    /**
     * Create a 1-row gui with custom slots at raw slots 2 and 4. Slot B only
     * accepts stone. Raw slots 9-44 are the player inventory (9-35 main, 36-44
     * hotbar).
     */
    @BeforeEach
    public void setup() {
        this.type = CustomGuiType.builder()
            .height(1)
            .setup(r -> {})
            .build();
        FakeRenderContext r = new FakeRenderContext();
        Predicate<GuiItem> onlyStone = item -> item.canStackWith(STONE_64);
        this.slotA = Slot.slot(r, SlotPos.of(2, 0));
        this.slotB = Slot.slot(r, SlotPos.of(4, 0), onlyStone);
        this.type.setStateRefs(r.getStateRefs());
        this.type.setGuiSlots(r.getGuiSlots());
        this.gui = new CustomGui(this.type);
    }

    private GuiScreenState state() {
        return GuiScreenState.create(this.gui);
    }

    private SlotInteractionResult handle(GuiScreenState state, SlotInteraction interaction) {
        return SlotInteractionHandler.handle(this.gui, state, interaction);
    }

    private GuiItem slotItem(Ref<Slot> ref) {
        return ref.get(this.gui).getItem();
    }

    private static FakeGuiItem stone(int amount) {
        return FakeGuiItem.of("stone", amount);
    }

    // PICKUP

    @Test
    public void pickupWholeStack() {
        this.slotA.get(this.gui).setItem(STONE_10);
        GuiScreenState state = this.state();

        SlotInteractionResult result = this.handle(state, new SlotInteraction.PickupLeft(2));

        assertTrue(result.cursorChanged());
        assertEquals(stone(10), result.cursor());
        assertTrue(this.slotItem(this.slotA).isEmpty());
        assertTrue(result.customSlotsChanged());
        assertTrue(result.playerInventoryChanges().isEmpty());
    }

    @Test
    public void pickupHalfRoundsUp() {
        this.slotA.get(this.gui).setItem(stone(11));
        GuiScreenState state = this.state();

        SlotInteractionResult result = this.handle(state, new SlotInteraction.PickupRight(2));

        assertEquals(stone(6), result.cursor());
        assertEquals(stone(5), this.slotItem(this.slotA));
    }

    @Test
    public void placeWholeStackInEmptySlot() {
        GuiScreenState state = this.state();
        state.setCursor(STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.PickupLeft(2));

        assertTrue(result.cursor().isEmpty());
        assertEquals(stone(10), this.slotItem(this.slotA));
    }

    @Test
    public void placeSingleItem() {
        GuiScreenState state = this.state();
        state.setCursor(STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.PickupRight(2));

        assertEquals(stone(9), result.cursor());
        assertEquals(stone(1), this.slotItem(this.slotA));
    }

    @Test
    public void mergeClampsAtMaxStackSize() {
        this.slotA.get(this.gui).setItem(stone(60));
        GuiScreenState state = this.state();
        state.setCursor(STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.PickupLeft(2));

        assertEquals(stone(64), this.slotItem(this.slotA));
        assertEquals(stone(6), result.cursor());
    }

    @Test
    public void swapDifferentItems() {
        this.slotA.get(this.gui).setItem(STONE_10);
        GuiScreenState state = this.state();
        state.setCursor(DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.PickupLeft(2));

        assertEquals(DIRT_5, this.slotItem(this.slotA));
        assertEquals(stone(10), result.cursor());
    }

    @Test
    public void restrictedSlotRejectsItem() {
        GuiScreenState state = this.state();
        state.setCursor(DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.PickupLeft(4));

        assertFalse(result.cursorChanged());
        assertTrue(this.slotItem(this.slotB).isEmpty());
        assertFalse(result.customSlotsChanged());
    }

    @Test
    public void inertPositionDoesNothing() {
        // Raw slot 0 has no registered slot.
        GuiScreenState state = this.state();
        state.setCursor(STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.PickupLeft(0));

        assertFalse(result.cursorChanged());
        assertFalse(result.customSlotsChanged());
        assertTrue(result.playerInventoryChanges().isEmpty());
    }

    @Test
    public void pickupFromPlayerInventory() {
        GuiScreenState state = this.state();
        state.setSlot(9, STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.PickupLeft(9));

        assertEquals(stone(10), result.cursor());
        assertTrue(result.playerInventoryChanges().get(9).isEmpty());
        assertFalse(result.customSlotsChanged());
    }

    // SHIFT CLICK

    @Test
    public void shiftClickFromGuiFillsPlayerInventoryBackwards() {
        this.slotA.get(this.gui).setItem(STONE_10);
        GuiScreenState state = this.state();

        SlotInteractionResult result = this.handle(state, new SlotInteraction.ShiftClick(2));

        assertTrue(this.slotItem(this.slotA).isEmpty());
        // The last hotbar slot is raw slot 44 for a 1-row gui.
        assertEquals(stone(10), result.playerInventoryChanges().get(44));
    }

    @Test
    public void shiftClickFromGuiMergesBeforeFillingEmpty() {
        this.slotA.get(this.gui).setItem(STONE_10);
        GuiScreenState state = this.state();
        state.setSlot(9, stone(60));

        SlotInteractionResult result = this.handle(state, new SlotInteraction.ShiftClick(2));

        assertTrue(this.slotItem(this.slotA).isEmpty());
        assertEquals(stone(64), result.playerInventoryChanges().get(9));
        assertEquals(stone(6), result.playerInventoryChanges().get(44));
    }

    @Test
    public void shiftClickFromPlayerInventoryToGuiSlots() {
        GuiScreenState state = this.state();
        state.setSlot(20, STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.ShiftClick(20));

        // Fills the first custom slot (raw slot 2) forwards.
        assertEquals(stone(10), this.slotItem(this.slotA));
        assertTrue(result.playerInventoryChanges().get(20).isEmpty());
        assertTrue(result.customSlotsChanged());
    }

    @Test
    public void shiftClickRespectsCanHold() {
        GuiScreenState state = this.state();
        state.setSlot(20, DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.ShiftClick(20));

        // Slot A takes it; slot B would reject dirt.
        assertEquals(DIRT_5, this.slotItem(this.slotA));
        assertTrue(this.slotItem(this.slotB).isEmpty());
        assertTrue(result.playerInventoryChanges().get(20).isEmpty());
    }

    @Test
    public void shiftClickWithNoRoomDoesNothing() {
        this.slotA.get(this.gui).setItem(FakeGuiItem.of("dirt", 64));
        this.slotB.get(this.gui).setItem(stone(64));
        GuiScreenState state = this.state();
        state.setSlot(20, DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.ShiftClick(20));

        assertTrue(result.playerInventoryChanges().isEmpty());
        assertFalse(result.customSlotsChanged());
    }

    // HOTBAR / OFFHAND SWAP

    @Test
    public void hotbarSwap() {
        this.slotA.get(this.gui).setItem(STONE_10);
        GuiScreenState state = this.state();
        state.setSlot(state.getHotbarRawSlot(0), DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.HotbarSwap(2, 0));

        assertEquals(DIRT_5, this.slotItem(this.slotA));
        assertEquals(stone(10), result.playerInventoryChanges().get(36));
    }

    @Test
    public void hotbarSwapRespectsCanHold() {
        this.slotB.get(this.gui).setItem(STONE_10);
        GuiScreenState state = this.state();
        state.setSlot(state.getHotbarRawSlot(0), DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.HotbarSwap(4, 0));

        assertEquals(stone(10), this.slotItem(this.slotB));
        assertTrue(result.playerInventoryChanges().isEmpty());
    }

    @Test
    public void offhandSwap() {
        this.slotA.get(this.gui).setItem(STONE_10);
        GuiScreenState state = this.state();
        state.setOffhand(DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.OffhandSwap(2));

        assertEquals(DIRT_5, this.slotItem(this.slotA));
        assertEquals(stone(10), result.offhand());
    }

    // DROPS

    @Test
    public void dropCursorAll() {
        GuiScreenState state = this.state();
        state.setCursor(STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.DropCursor(true));

        assertEquals(List.of(stone(10)), result.droppedItems());
        assertTrue(result.cursor().isEmpty());
    }

    @Test
    public void dropCursorOne() {
        GuiScreenState state = this.state();
        state.setCursor(STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.DropCursor(false));

        assertEquals(List.of(stone(1)), result.droppedItems());
        assertEquals(stone(9), result.cursor());
    }

    @Test
    public void dropFromSlot() {
        this.slotA.get(this.gui).setItem(STONE_10);
        GuiScreenState state = this.state();

        SlotInteractionResult result = this.handle(state, new SlotInteraction.DropSlot(2, false));

        assertEquals(List.of(stone(1)), result.droppedItems());
        assertEquals(stone(9), this.slotItem(this.slotA));
    }

    @Test
    public void dropFromSlotRequiresEmptyCursor() {
        this.slotA.get(this.gui).setItem(STONE_10);
        GuiScreenState state = this.state();
        state.setCursor(DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.DropSlot(2, true));

        assertTrue(result.droppedItems().isEmpty());
        assertEquals(stone(10), this.slotItem(this.slotA));
    }

    // DOUBLE CLICK

    @Test
    public void doubleClickCollectsPartialStacksFirst() {
        this.slotA.get(this.gui).setItem(stone(64));
        GuiScreenState state = this.state();
        state.setCursor(stone(1));
        state.setSlot(9, stone(32));
        state.setSlot(10, stone(40));

        SlotInteractionResult result = this.handle(state, new SlotInteraction.DoubleClick(20, false));

        // Cursor: 1 + 32 + 31 from slot 10 = 64. The full stack in the gui slot
        // and the rest of slot 10 remain.
        assertEquals(stone(64), result.cursor());
        assertTrue(result.playerInventoryChanges().get(9).isEmpty());
        assertEquals(stone(9), result.playerInventoryChanges().get(10));
        assertEquals(stone(64), this.slotItem(this.slotA));
    }

    @Test
    public void doubleClickCollectsFromCustomSlots() {
        this.slotA.get(this.gui).setItem(STONE_10);
        GuiScreenState state = this.state();
        state.setCursor(stone(1));

        SlotInteractionResult result = this.handle(state, new SlotInteraction.DoubleClick(20, false));

        assertEquals(stone(11), result.cursor());
        assertTrue(this.slotItem(this.slotA).isEmpty());
    }

    // DRAG

    @Test
    public void dragEvenSplitsEvenly() {
        GuiScreenState state = this.state();
        state.setCursor(stone(10));

        SlotInteractionResult result = this.handle(state,
            new SlotInteraction.Drag(DragType.EVEN, List.of(2, 4, 9)));

        assertEquals(stone(3), this.slotItem(this.slotA));
        assertEquals(stone(3), this.slotItem(this.slotB));
        assertEquals(stone(3), result.playerInventoryChanges().get(9));
        assertEquals(stone(1), result.cursor());
    }

    @Test
    public void dragSinglePlacesOneEach() {
        GuiScreenState state = this.state();
        state.setCursor(stone(10));

        SlotInteractionResult result = this.handle(state,
            new SlotInteraction.Drag(DragType.SINGLE, List.of(2, 9)));

        assertEquals(stone(1), this.slotItem(this.slotA));
        assertEquals(stone(1), result.playerInventoryChanges().get(9));
        assertEquals(stone(8), result.cursor());
    }

    @Test
    public void dragSkipsInvalidSlots() {
        GuiScreenState state = this.state();
        state.setCursor(FakeGuiItem.of("dirt", 10));
        state.setSlot(9, stone(5));

        SlotInteractionResult result = this.handle(state,
            // Slot B only holds stone, slot 9 holds a different item, slot 0 is
            // inert. Only slot 2 and the empty player slot 10 are valid.
            new SlotInteraction.Drag(DragType.EVEN, List.of(0, 4, 9, 2, 10)));

        assertEquals(FakeGuiItem.of("dirt", 5), this.slotItem(this.slotA));
        assertEquals(FakeGuiItem.of("dirt", 5), result.playerInventoryChanges().get(10));
        assertTrue(result.cursor().isEmpty());
    }

    @Test
    public void dragWithSingleValidSlotFallsBackToClick() {
        GuiScreenState state = this.state();
        state.setCursor(stone(10));

        SlotInteractionResult result = this.handle(state,
            new SlotInteraction.Drag(DragType.SINGLE, List.of(2)));

        // A single-slot right-click drag behaves like a right click: place one.
        assertEquals(stone(1), this.slotItem(this.slotA));
        assertEquals(stone(9), result.cursor());
    }

    // CLONE

    @Test
    public void cloneStackFillsCursor() {
        this.slotA.get(this.gui).setItem(STONE_10);
        GuiScreenState state = this.state();

        SlotInteractionResult result = this.handle(state, new SlotInteraction.Clone(2));

        assertEquals(stone(64), result.cursor());
        assertEquals(stone(10), this.slotItem(this.slotA));
    }
}
