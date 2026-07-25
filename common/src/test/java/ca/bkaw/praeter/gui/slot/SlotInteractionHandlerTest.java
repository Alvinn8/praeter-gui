package ca.bkaw.praeter.gui.slot;

import ca.bkaw.praeter.gui.components.Slot;
import ca.bkaw.praeter.gui.gui.CustomGui;
import ca.bkaw.praeter.gui.gui.CustomGuiType;
import ca.bkaw.praeter.gui.gui.MockCustomGui;
import ca.bkaw.praeter.gui.gui.Ref;
import ca.bkaw.praeter.gui.platform.GuiItem;
import ca.bkaw.praeter.gui.platform.GuiPlayer;
import ca.bkaw.praeter.gui.platform.MockGuiItem;
import ca.bkaw.praeter.gui.platform.MockGuiPlayer;
import ca.bkaw.praeter.gui.render.MockRenderContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SlotInteractionHandlerTest {
    private static final MockGuiItem STONE_64 = MockGuiItem.of("stone", 64);
    private static final MockGuiItem STONE_10 = MockGuiItem.of("stone", 10);
    private static final MockGuiItem DIRT_5 = MockGuiItem.of("dirt", 5);

    private static final GuiPlayer PLAYER = new MockGuiPlayer();
    private static final int SLOT_A_INDEX = 2;
    /** Only stone. */
    private static final int SLOT_B_INDEX = 4;
    /** May not be changed. */
    private static final int SLOT_C_INDEX = 6;

    private CustomGui gui;
    private Ref<Slot> slotA;
    /** Only stone. */
    private Ref<Slot> slotB;
    /** May not be changed. */
    private Ref<Slot> slotC;

    /**
     * Create a 1-row gui with custom slots at raw slots 2, 4, and 6. Slot B only
     * accepts stone, slot C may not be changed by players. Raw slots 9-44 are the
     * player inventory (9-35 main, 36-44 hotbar).
     */
    @BeforeEach
    public void setup() {
        CustomGuiType type = CustomGuiType.builder()
            .height(1)
            .setup(_ -> {})
            .build();
        MockRenderContext r = new MockRenderContext();
        SlotBehavior onlyStone = new SlotBehavior() {
            @Override
            public boolean canHold(GuiItem item) {
                return item.canStackWith(STONE_64);
            }

            @Override
            public boolean mayChange(GuiPlayer player) {
                return true;
            }
        };
        SlotBehavior locked = new SlotBehavior() {
            @Override
            public boolean canHold(GuiItem item) {
                return true;
            }

            @Override
            public boolean mayChange(GuiPlayer player) {
                return false;
            }
        };
        this.slotA = Slot.slot(r, SlotPos.of(SLOT_A_INDEX));
        this.slotB = Slot.slot(r, SlotPos.of(SLOT_B_INDEX), onlyStone);
        this.slotC = Slot.slot(r, SlotPos.of(SLOT_C_INDEX), locked);
        type.setStateRefs(r.getStateRefs());
        type.setGuiSlots(r.getGuiSlots());
        this.gui = new MockCustomGui(type);
    }

    private GuiSlotsState createState() {
        int bottomSlotCount = this.gui.getType().getBottomRegionType().getSlotCount();
        GuiItem[] playerInventorySlots = new GuiItem[bottomSlotCount];
        Arrays.fill(playerInventorySlots, GuiItem.empty());
        return new GuiSlotsState(this.gui, playerInventorySlots, GuiItem.empty(), GuiItem.empty());
    }

    private SlotInteractionResult handle(GuiSlotsState state, SlotInteraction interaction) {
        return SlotInteractionHandler.handle(this.gui, state, interaction, PLAYER);
    }

    private GuiItem slotItem(Ref<Slot> ref) {
        return ref.get(this.gui).getGuiItem();
    }

    private static MockGuiItem stone(int amount) {
        return MockGuiItem.of("stone", amount);
    }

    // PICKUP

    @Test
    public void pickupWholeStack() {
        this.slotA.get(this.gui).setGuiItem(STONE_10);
        GuiSlotsState state = this.createState();

        SlotInteractionResult result = this.handle(state, new SlotInteraction.LeftClick(SLOT_A_INDEX));

        assertTrue(result.cursorChanged());
        assertEquals(stone(10), result.cursor());
        assertTrue(this.slotItem(this.slotA).isEmpty());
        assertTrue(result.customSlotsChanged());
        assertTrue(result.playerInventoryChanges().isEmpty());
    }

    @Test
    public void pickupHalfRoundsUp() {
        this.slotA.get(this.gui).setGuiItem(stone(11));
        GuiSlotsState state = this.createState();

        SlotInteractionResult result = this.handle(state, new SlotInteraction.RightClick(SLOT_A_INDEX));

        assertEquals(stone(SLOT_C_INDEX), result.cursor());
        assertEquals(stone(5), this.slotItem(this.slotA));
    }

    @Test
    public void placeWholeStackInEmptySlot() {
        GuiSlotsState state = this.createState();
        state.setCursor(STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.LeftClick(SLOT_A_INDEX));

        assertTrue(result.cursor().isEmpty());
        assertEquals(stone(10), this.slotItem(this.slotA));
    }

    @Test
    public void placeSingleItem() {
        GuiSlotsState state = this.createState();
        state.setCursor(STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.RightClick(SLOT_A_INDEX));

        assertEquals(stone(9), result.cursor());
        assertEquals(stone(1), this.slotItem(this.slotA));
    }

    @Test
    public void mergeClampsAtMaxStackSize() {
        this.slotA.get(this.gui).setGuiItem(stone(60));
        GuiSlotsState state = this.createState();
        state.setCursor(STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.LeftClick(SLOT_A_INDEX));

        assertEquals(stone(64), this.slotItem(this.slotA));
        assertEquals(stone(SLOT_C_INDEX), result.cursor());
    }

    @Test
    public void swapDifferentItems() {
        this.slotA.get(this.gui).setGuiItem(STONE_10);
        GuiSlotsState state = this.createState();
        state.setCursor(DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.LeftClick(SLOT_A_INDEX));

        assertEquals(DIRT_5, this.slotItem(this.slotA));
        assertEquals(stone(10), result.cursor());
    }

    @Test
    public void restrictedSlotRejectsItem() {
        GuiSlotsState state = this.createState();
        state.setCursor(DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.LeftClick(SLOT_B_INDEX));

        assertFalse(result.cursorChanged());
        assertTrue(this.slotItem(this.slotB).isEmpty());
        assertFalse(result.customSlotsChanged());
    }

    @Test
    public void inertPositionDoesNothing() {
        // Raw slot 0 has no registered slot.
        GuiSlotsState state = this.createState();
        state.setCursor(STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.LeftClick(0));

        assertFalse(result.cursorChanged());
        assertFalse(result.customSlotsChanged());
        assertTrue(result.playerInventoryChanges().isEmpty());
    }

    @Test
    public void pickupFromPlayerInventory() {
        GuiSlotsState state = this.createState();
        state.setItem(9, STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.LeftClick(9));

        assertEquals(stone(10), result.cursor());
        assertTrue(result.playerInventoryChanges().get(9).isEmpty());
        assertFalse(result.customSlotsChanged());
    }

    // SHIFT CLICK

    @Test
    public void shiftClickFromGuiFillsPlayerInventoryBackwards() {
        this.slotA.get(this.gui).setGuiItem(STONE_10);
        GuiSlotsState state = this.createState();

        SlotInteractionResult result = this.handle(state, new SlotInteraction.ShiftClick(SLOT_A_INDEX));

        assertTrue(this.slotItem(this.slotA).isEmpty());
        // The last hotbar slot is raw slot 44 for a 1-row gui.
        assertEquals(stone(10), result.playerInventoryChanges().get(44));
    }

    @Test
    public void shiftClickFromGuiMergesBeforeFillingEmpty() {
        this.slotA.get(this.gui).setGuiItem(STONE_10);
        GuiSlotsState state = this.createState();
        state.setItem(9, stone(60));

        SlotInteractionResult result = this.handle(state, new SlotInteraction.ShiftClick(SLOT_A_INDEX));

        assertTrue(this.slotItem(this.slotA).isEmpty());
        assertEquals(stone(64), result.playerInventoryChanges().get(9));
        assertEquals(stone(SLOT_C_INDEX), result.playerInventoryChanges().get(44));
    }

    @Test
    public void shiftClickFromPlayerInventoryToGuiSlots() {
        GuiSlotsState state = this.createState();
        state.setItem(20, STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.ShiftClick(20));

        // Fills the first custom slot (raw slot 2) forwards.
        assertEquals(stone(10), this.slotItem(this.slotA));
        assertTrue(result.playerInventoryChanges().get(20).isEmpty());
        assertTrue(result.customSlotsChanged());
    }

    @Test
    public void shiftClickRespectsCanHold() {
        GuiSlotsState state = this.createState();
        state.setItem(20, DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.ShiftClick(20));

        // Slot A takes it; slot B would reject dirt.
        assertEquals(DIRT_5, this.slotItem(this.slotA));
        assertTrue(this.slotItem(this.slotB).isEmpty());
        assertTrue(result.playerInventoryChanges().get(20).isEmpty());
    }

    @Test
    public void shiftClickWithNoRoomDoesNothing() {
        this.slotA.get(this.gui).setGuiItem(MockGuiItem.of("dirt", 64));
        this.slotB.get(this.gui).setGuiItem(stone(64));
        GuiSlotsState state = this.createState();
        state.setItem(20, DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.ShiftClick(20));

        assertTrue(result.playerInventoryChanges().isEmpty());
        assertFalse(result.customSlotsChanged());
    }

    // HOTBAR / OFFHAND SWAP

    @Test
    public void hotbarSwap() {
        this.slotA.get(this.gui).setGuiItem(STONE_10);
        GuiSlotsState state = this.createState();
        state.setItem(state.getHotbarSlotIndex(0), DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.HotbarSwap(SLOT_A_INDEX, 0));

        assertEquals(DIRT_5, this.slotItem(this.slotA));
        assertEquals(stone(10), result.playerInventoryChanges().get(36));
    }

    @Test
    public void hotbarSwapRespectsCanHold() {
        this.slotB.get(this.gui).setGuiItem(STONE_10);
        GuiSlotsState state = this.createState();
        state.setItem(state.getHotbarSlotIndex(0), DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.HotbarSwap(SLOT_B_INDEX, 0));

        assertEquals(stone(10), this.slotItem(this.slotB));
        assertTrue(result.playerInventoryChanges().isEmpty());
    }

    @Test
    public void offhandSwap() {
        this.slotA.get(this.gui).setGuiItem(STONE_10);
        GuiSlotsState state = this.createState();
        state.setOffhand(DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.OffhandSwap(SLOT_A_INDEX));

        assertEquals(DIRT_5, this.slotItem(this.slotA));
        assertEquals(stone(10), result.offhand());
    }

    // DROPS

    @Test
    public void dropCursorAll() {
        GuiSlotsState state = this.createState();
        state.setCursor(STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.LeftClickOutside());

        assertEquals(List.of(stone(10)), result.droppedItems());
        assertTrue(result.cursor().isEmpty());
    }

    @Test
    public void dropCursorOne() {
        GuiSlotsState state = this.createState();
        state.setCursor(STONE_10);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.RightClickOutside());

        assertEquals(List.of(stone(1)), result.droppedItems());
        assertEquals(stone(9), result.cursor());
    }

    @Test
    public void dropFromSlot() {
        this.slotA.get(this.gui).setGuiItem(STONE_10);
        GuiSlotsState state = this.createState();

        SlotInteractionResult result = this.handle(state, new SlotInteraction.DropSlot(SLOT_A_INDEX, false));

        assertEquals(List.of(stone(1)), result.droppedItems());
        assertEquals(stone(9), this.slotItem(this.slotA));
    }

    @Test
    public void dropFromSlotRequiresEmptyCursor() {
        this.slotA.get(this.gui).setGuiItem(STONE_10);
        GuiSlotsState state = this.createState();
        state.setCursor(DIRT_5);

        SlotInteractionResult result = this.handle(state, new SlotInteraction.DropSlot(SLOT_A_INDEX, true));

        assertTrue(result.droppedItems().isEmpty());
        assertEquals(stone(10), this.slotItem(this.slotA));
    }

    // DOUBLE CLICK

    @Test
    public void doubleClickCollectsPartialStacksFirst() {
        this.slotA.get(this.gui).setGuiItem(stone(64));
        GuiSlotsState state = this.createState();
        state.setCursor(stone(1));
        state.setItem(9, stone(32));
        state.setItem(10, stone(40));

        SlotInteractionResult result = this.handle(state, new SlotInteraction.DoubleClick(20));

        // Cursor: 1 + 32 + 31 from slot 10 = 64. The full stack in the gui slot
        // and the rest of slot 10 remain.
        assertEquals(stone(64), result.cursor());
        assertTrue(result.playerInventoryChanges().get(9).isEmpty());
        assertEquals(stone(9), result.playerInventoryChanges().get(10));
        assertEquals(stone(64), this.slotItem(this.slotA));
    }

    @Test
    public void doubleClickCollectsFromCustomSlots() {
        this.slotA.get(this.gui).setGuiItem(STONE_10);
        GuiSlotsState state = this.createState();
        state.setCursor(stone(1));

        SlotInteractionResult result = this.handle(state, new SlotInteraction.DoubleClick(20));

        assertEquals(stone(11), result.cursor());
        assertTrue(this.slotItem(this.slotA).isEmpty());
    }

    // DRAG

    @Test
    public void dragEvenSplitsEvenly() {
        GuiSlotsState state = this.createState();
        state.setCursor(stone(10));

        SlotInteractionResult result = this.handle(state,
            new SlotInteraction.Drag(SlotInteraction.DragType.LEFT, List.of(SLOT_A_INDEX, SLOT_B_INDEX, 9)));

        assertEquals(stone(3), this.slotItem(this.slotA));
        assertEquals(stone(3), this.slotItem(this.slotB));
        assertEquals(stone(3), result.playerInventoryChanges().get(9));
        assertEquals(stone(1), result.cursor());
    }

    @Test
    public void dragSinglePlacesOneEach() {
        GuiSlotsState state = this.createState();
        state.setCursor(stone(10));

        SlotInteractionResult result = this.handle(state,
            new SlotInteraction.Drag(SlotInteraction.DragType.RIGHT, List.of(SLOT_A_INDEX, 9)));

        assertEquals(stone(1), this.slotItem(this.slotA));
        assertEquals(stone(1), result.playerInventoryChanges().get(9));
        assertEquals(stone(8), result.cursor());
    }

    @Test
    public void dragSkipsInvalidSlots() {
        GuiSlotsState state = this.createState();
        state.setCursor(MockGuiItem.of("dirt", 10));
        state.setItem(9, stone(5));

        SlotInteractionResult result = this.handle(state,
            // Slot B only holds stone, slot 9 holds a different item, slot 0 is
            // inert. Only slot 2 and the empty player slot 10 are valid.
            new SlotInteraction.Drag(SlotInteraction.DragType.LEFT, List.of(0, SLOT_B_INDEX, 9, SLOT_A_INDEX, 10)));

        assertEquals(MockGuiItem.of("dirt", 5), this.slotItem(this.slotA));
        assertEquals(MockGuiItem.of("dirt", 5), result.playerInventoryChanges().get(10));
        assertTrue(result.cursor().isEmpty());
    }

    @Test
    public void dragWithSingleValidSlotFallsBackToClick() {
        GuiSlotsState state = this.createState();
        state.setCursor(stone(10));

        SlotInteractionResult result = this.handle(state,
            new SlotInteraction.Drag(SlotInteraction.DragType.RIGHT, List.of(SLOT_A_INDEX)));

        // A single-slot right-click drag behaves like a right click: place one.
        assertEquals(stone(1), this.slotItem(this.slotA));
        assertEquals(stone(9), result.cursor());
    }

    // MAY CHANGE

    @Test
    public void mayChangeBlocksPickup() {
        this.slotC.get(this.gui).setGuiItem(STONE_10);
        GuiSlotsState state = this.createState();

        SlotInteractionResult result = this.handle(state, new SlotInteraction.LeftClick(SLOT_C_INDEX));

        assertFalse(result.cursorChanged());
        assertEquals(stone(10), this.slotItem(this.slotC));
        assertFalse(result.customSlotsChanged());
    }

    @Test
    public void mayChangeBlocksDoubleClickCollection() {
        this.slotC.get(this.gui).setGuiItem(STONE_10);
        GuiSlotsState state = this.createState();
        state.setCursor(stone(1));

        SlotInteractionResult result = this.handle(state, new SlotInteraction.DoubleClick(20));

        assertEquals(stone(1), result.cursor());
        assertEquals(stone(10), this.slotItem(this.slotC));
    }

    // CLONE

    @Test
    public void cloneStackFillsCursor() {
        this.slotA.get(this.gui).setGuiItem(STONE_10);
        GuiSlotsState state = this.createState();

        SlotInteractionResult result = this.handle(state, new SlotInteraction.Clone(SLOT_A_INDEX));

        assertEquals(stone(64), result.cursor());
        assertEquals(stone(10), this.slotItem(this.slotA));
    }
}
