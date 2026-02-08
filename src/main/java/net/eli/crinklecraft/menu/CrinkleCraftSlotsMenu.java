package net.eli.crinklecraft.menu;

import net.eli.crinklecraft.item.custom.DiaperItem;
import net.eli.crinklecraft.item.custom.MittensItem;
import net.eli.crinklecraft.item.custom.OnesieItem;
import net.eli.crinklecraft.item.custom.PacifierItem;
import net.eli.crinklecraft.potty.PottyPlayerData;
import net.eli.crinklecraft.potty.PottySavedData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;

/**
 * Menu with diaper, pacifier, mittens, and onesie slots (syncs to PottyPlayerData).
 * Compact layout - equipment slots only plus hotbar for quick access.
 */
public class CrinkleCraftSlotsMenu extends AbstractContainerMenu {

    public static final int SLOT_SIZE = 18;
    public static final int SLOT_X = 62;
    public static final int SLOT_1_Y = 18;
    public static final int SLOT_2_Y = 36;
    public static final int SLOT_3_Y = 54;
    /** Onesie slot: top right of dispenser GUI */
    public static final int ONESIE_SLOT_X = 98;
    public static final int ONESIE_SLOT_Y = 18;

    private final ItemStackHandler diaperHandler;
    private final ItemStackHandler pacifierHandler;
    private final ItemStackHandler mittensHandler;
    private final ItemStackHandler onesieHandler;
    private final ContainerLevelAccess access;
    @Nullable
    private final Player player;

    /** Client constructor (no extra data). */
    public CrinkleCraftSlotsMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, new ItemStackHandler(1), new ItemStackHandler(1), new ItemStackHandler(1),
                new ItemStackHandler(1), ContainerLevelAccess.NULL, null);
    }

    /** Server constructor: handlers are synced with PottyPlayerData. */
    public CrinkleCraftSlotsMenu(int containerId, Inventory playerInv, ItemStackHandler diaperHandler,
                                 ItemStackHandler pacifierHandler, ItemStackHandler mittensHandler,
                                 ItemStackHandler onesieHandler, ContainerLevelAccess access, @Nullable Player player) {
        super(ModMenuTypes.CRINKLECRAFT_SLOTS.get(), containerId);
        this.diaperHandler = diaperHandler;
        this.pacifierHandler = pacifierHandler;
        this.mittensHandler = mittensHandler;
        this.onesieHandler = onesieHandler;
        this.access = access;
        this.player = player;

        addSlot(new SlotItemHandler(diaperHandler, 0, SLOT_X, SLOT_1_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof DiaperItem;
            }
        });
        addSlot(new SlotItemHandler(pacifierHandler, 0, SLOT_X, SLOT_2_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof PacifierItem;
            }
        });
        addSlot(new SlotItemHandler(mittensHandler, 0, SLOT_X, SLOT_3_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof MittensItem;
            }
        });
        addSlot(new SlotItemHandler(onesieHandler, 0, ONESIE_SLOT_X, ONESIE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof OnesieItem;
            }
        });

        // Hotbar only - compact
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * SLOT_SIZE, 118));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                PottyPlayerData pd = PottySavedData.getPlayerData(serverPlayer);
                pd.setEquippedDiaper(diaperHandler.getStackInSlot(0).copy());
                pd.setEquippedPacifier(pacifierHandler.getStackInSlot(0).copy());
                pd.setEquippedMittens(mittensHandler.getStackInSlot(0).copy());
                pd.setEquippedOnesie(onesieHandler.getStackInSlot(0).copy());
                PottySavedData.get(serverPlayer.serverLevel()).markDirty();
            }
        });
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (index < 4) {
                if (!moveItemStackTo(stack, 4, slots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                boolean moved = false;
                if (stack.getItem() instanceof DiaperItem)
                    moved = moveItemStackTo(stack, 0, 1, false);
                else if (stack.getItem() instanceof PacifierItem)
                    moved = moveItemStackTo(stack, 1, 2, false);
                else if (stack.getItem() instanceof MittensItem)
                    moved = moveItemStackTo(stack, 2, 3, false);
                else if (stack.getItem() instanceof OnesieItem)
                    moved = moveItemStackTo(stack, 3, 4, false);
                if (!moved)
                    return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
