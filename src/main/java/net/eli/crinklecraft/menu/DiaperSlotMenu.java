package net.eli.crinklecraft.menu;

import net.eli.crinklecraft.item.custom.DiaperItem;
import net.eli.crinklecraft.potty.PottySavedData;
import net.eli.crinklecraft.potty.PottyPlayerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;

/**
 * Menu with one diaper slot (syncs to PottyPlayerData) plus player inventory.
 */
public class DiaperSlotMenu extends AbstractContainerMenu {
    public static final int DIAPER_SLOT_X = 80;
    public static final int DIAPER_SLOT_Y = 18;
    private final ItemStackHandler diaperHandler;
    private final ContainerLevelAccess access;
    @Nullable
    private final Player player;

    /** Client constructor (no extra data). */
    public DiaperSlotMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, new ItemStackHandler(1), ContainerLevelAccess.NULL, null);
    }

    /** Server constructor: diaperHandler is synced with PottyPlayerData. */
    public DiaperSlotMenu(int containerId, Inventory playerInv, ItemStackHandler diaperHandler, ContainerLevelAccess access, @Nullable Player player) {
        super(ModMenuTypes.DIAPER_SLOT.get(), containerId);
        this.diaperHandler = diaperHandler;
        this.access = access;
        this.player = player;

        addSlot(new SlotItemHandler(diaperHandler, 0, DIAPER_SLOT_X, DIAPER_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                if (stack.isEmpty()) return true;
                return stack.getItem() instanceof DiaperItem; // allow fully-used diapers to stay
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 50 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 108));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                PottySavedData data = PottySavedData.get(serverPlayer.serverLevel());
                PottyPlayerData playerData = data.getOrCreate(serverPlayer.getUUID());
                playerData.setEquippedDiaper(diaperHandler.getStackInSlot(0).copy());
                data.markDirty();
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
            if (index == 0) {
                if (!moveItemStackTo(stack, 1, slots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                if (stack.getItem() instanceof DiaperItem) {
                    if (!moveItemStackTo(stack, 0, 1, false))
                        return ItemStack.EMPTY;
                } else {
                    return ItemStack.EMPTY;
                }
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
