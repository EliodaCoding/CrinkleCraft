package net.eli.crinklecraft.item.custom;

import net.eli.crinklecraft.potty.CrinkleCraftCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Potty chart item. Right-click to run /crinklecraft chart and view successes, accidents, stars in chat.
 * For single player or multiplayer roleplay - players can share or view their own chart.
 */
public class PottyChartItem extends Item {

    public PottyChartItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            CrinkleCraftCommand.runCommandAsPlayer(serverPlayer, "crinklecraft chart");
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.crinklecraft.potty_chart.tooltip"));
    }
}
