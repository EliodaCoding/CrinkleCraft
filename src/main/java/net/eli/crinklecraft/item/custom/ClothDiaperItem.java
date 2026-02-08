package net.eli.crinklecraft.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Reusable cloth diaper. 5 uses, washable in water or craft with water bucket. */
public class ClothDiaperItem extends DiaperItem {

    public ClothDiaperItem(Properties properties) {
        super(properties, 5);
    }

    @Override
    public boolean isWashable(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.crinklecraft.cloth_diaper.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, flag);
    }
}
