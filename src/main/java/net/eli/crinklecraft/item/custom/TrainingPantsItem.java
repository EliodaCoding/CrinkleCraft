package net.eli.crinklecraft.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Potty training pants. 3 uses, washable in water or craft with water bucket. */
public class TrainingPantsItem extends DiaperItem {

    public TrainingPantsItem(Properties properties) {
        super(properties, 3);
    }

    @Override
    public boolean isWashable(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.crinklecraft.training_pants.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, flag);
    }
}
