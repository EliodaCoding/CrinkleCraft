package net.eli.crinklecraft.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Magic diaper. 3 uses, slowly regains uses over time when equipped. Not washable. */
public class MagicDiaperItem extends DiaperItem {

    /** Ticks between each use restoration (10 minutes). */
    public static final int REGEN_TICKS = 20 * 60 * 10;

    public MagicDiaperItem(Properties properties) {
        super(properties, 3);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.crinklecraft.magic_diaper.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, flag);
    }
}
