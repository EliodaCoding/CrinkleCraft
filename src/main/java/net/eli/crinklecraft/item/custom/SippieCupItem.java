package net.eli.crinklecraft.item.custom;

import net.eli.crinklecraft.component.ModDataComponentTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Sippie cup: max 2 potions, apple fills with apple juice (clears effects when drunk).
 */
public class SippieCupItem extends Item {

    public static final int MAX_POTIONS = 2;
    public static final int USE_DURATION_TICKS = 32;
    public static final float PEE_ON_DRINK = 6f;
    public static final float PEE_BOOST_MULTIPLIER = 1.8f;
    public static final int PEE_BOOST_DURATION_TICKS = 20 * 45; // 45s

    public SippieCupItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.set(ModDataComponentTypes.BOTTLE_CONTENTS.get(), ModDataComponentTypes.BottleContents.EMPTY);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var contents = stack.get(ModDataComponentTypes.BOTTLE_CONTENTS.get());
        if (contents == null) contents = ModDataComponentTypes.BottleContents.EMPTY;

        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack other = player.getItemInHand(otherHand);

        if (contents.isEmpty() && other.is(Items.MILK_BUCKET)) {
            stack.set(ModDataComponentTypes.BOTTLE_CONTENTS.get(),
                    new ModDataComponentTypes.BottleContents(true, false, List.of(), 0));
            if (!player.getAbilities().instabuild) {
                other.shrink(1);
                player.setItemInHand(otherHand, new ItemStack(Items.BUCKET));
            }
            return InteractionResultHolder.success(stack);
        }

        if (contents.isEmpty() && other.is(Items.APPLE)) {
            stack.set(ModDataComponentTypes.BOTTLE_CONTENTS.get(),
                    new ModDataComponentTypes.BottleContents(false, true, List.of(), 0));
            if (!player.getAbilities().instabuild) other.shrink(1);
            return InteractionResultHolder.success(stack);
        }

        if (contents.canAddPotion(MAX_POTIONS) && other.has(DataComponents.POTION_CONTENTS)) {
            PotionContents potionContents = other.get(DataComponents.POTION_CONTENTS);
            List<MobEffectInstance> fromPotion = new ArrayList<>();
            for (MobEffectInstance e : potionContents.getAllEffects())
                fromPotion.add(new MobEffectInstance(e));
            if (!fromPotion.isEmpty()) {
                List<MobEffectInstance> newEffects = new ArrayList<>(contents.effects);
                newEffects.addAll(fromPotion);
                stack.set(ModDataComponentTypes.BOTTLE_CONTENTS.get(),
                        new ModDataComponentTypes.BottleContents(false, false, newEffects, contents.potionCount + 1));
                if (!player.getAbilities().instabuild) other.shrink(1);
                return InteractionResultHolder.success(stack);
            }
        }

        if (!contents.isEmpty()) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) return stack;

        var contents = stack.get(ModDataComponentTypes.BOTTLE_CONTENTS.get());
        if (contents == null || contents.isEmpty()) return stack;

        if (contents.hasMilk || contents.hasAppleJuice) {
            player.removeAllEffects();
        } else {
            for (var effect : contents.effects) {
                player.addEffect(new MobEffectInstance(effect));
            }
        }

        stack.set(ModDataComponentTypes.BOTTLE_CONTENTS.get(), ModDataComponentTypes.BottleContents.EMPTY);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            var pd = net.eli.crinklecraft.potty.PottySavedData.getPlayerData(serverPlayer);
            pd.addPee(PEE_ON_DRINK);
            pd.applyPeeBoost(PEE_BOOST_MULTIPLIER, PEE_BOOST_DURATION_TICKS);
            net.eli.crinklecraft.potty.PottySavedData.get(serverPlayer.serverLevel()).markDirty();
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        var contents = stack.get(ModDataComponentTypes.BOTTLE_CONTENTS.get());
        if (contents != null && !contents.isEmpty()) {
            if (contents.hasMilk) {
                tooltipComponents.add(Component.translatable("item.crinklecraft.baby_bottle.tooltip.milk"));
            } else if (contents.hasAppleJuice) {
                tooltipComponents.add(Component.translatable("item.crinklecraft.bottle.tooltip.apple_juice"));
            } else {
                tooltipComponents.add(Component.translatable("item.crinklecraft.bottle.tooltip.potions", contents.potionCount, MAX_POTIONS));
            }
        }
    }
}
