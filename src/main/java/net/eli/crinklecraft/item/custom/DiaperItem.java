package net.eli.crinklecraft.item.custom;

import net.eli.crinklecraft.component.ModDataComponentTypes;
import net.eli.crinklecraft.item.ModItems;
import net.eli.crinklecraft.potty.PottySavedData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Diaper that goes in the custom diaper slot (not leggings). Dyeable, max uses (e.g. 3).
 * Right-click to equip; shift + right-click with empty hand to unequip.
 */
public class DiaperItem extends Item {

    private static final int DEFAULT_COLOR = 0xA06540;
    /** Max uses before diaper is used up (pullups can override with 1). */
    private final int maxUses;

    public DiaperItem(Properties properties, int maxUses) {
        super(properties.stacksTo(1));
        this.maxUses = Math.max(1, maxUses);
    }

    public int getMaxUses(ItemStack stack) {
        return maxUses;
    }

    public int getUses(ItemStack stack) {
        Integer u = stack.get(ModDataComponentTypes.DIAPER_USES.get());
        return u == null ? 0 : u;
    }

    public boolean isFullyUsed(ItemStack stack) {
        return getUses(stack) >= getMaxUses(stack);
    }

    /** Use one charge (e.g. absorbed accident). Returns true if diaper is now used up. */
    public boolean useOne(ItemStack stack) {
        int u = getUses(stack);
        stack.set(ModDataComponentTypes.DIAPER_USES.get(), Math.min(u + 1, getMaxUses(stack)));
        return getUses(stack) >= getMaxUses(stack);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(DEFAULT_COLOR, true));
        stack.set(ModDataComponentTypes.DIAPER_USES.get(), 0);
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !(stack.getItem() instanceof DiaperItem diaper)) return InteractionResultHolder.pass(stack);
        if (diaper.isFullyUsed(stack)) return InteractionResultHolder.pass(stack);

        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer))
            return InteractionResultHolder.pass(stack);

        var data = PottySavedData.get(serverLevel);
        var playerData = data.getOrCreate(serverPlayer.getUUID());

        if (playerData.getEquippedDiaper().isEmpty()) {
            ItemStack toEquip = stack.copyWithCount(1);
            playerData.setEquippedDiaper(toEquip);
            stack.shrink(1);
            ItemStack leggings = serverPlayer.getInventory().getArmor(1);
            if (!leggings.isEmpty() && !leggings.is(ModItems.DIAPER_ARMOR.get())) {
                playerData.setStoredLeggings(leggings.copy());
            }
            serverPlayer.getInventory().armor.set(1, new ItemStack(ModItems.DIAPER_ARMOR.get()));
            serverPlayer.removeEffect(net.minecraft.core.Holder.direct(net.eli.crinklecraft.effect.ModEffects.WET_EFFECT.get()));
            data.markDirty();
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.crinklecraft.diaper.uses", getUses(stack), getMaxUses(stack)));
    }
}
