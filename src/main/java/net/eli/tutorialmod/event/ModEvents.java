package net.eli.tutorialmod.event;

import net.eli.tutorialmod.TutorialMod;
import net.eli.tutorialmod.effect.ComfortedEffect;
import net.eli.tutorialmod.effect.ModEffects;
import net.minecraft.core.Holder;
import net.eli.tutorialmod.item.custom.HammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {
    private static final Set<BlockPos> HARVESTED_BLOCKS = new HashSet<>();

    // Done with the help of https://github.com/CoFH/CoFHCore/blob/1.19.x/src/main/java/cofh/core/event/AreaEffectEvents.java
    // Don't be a jerk License
    @SubscribeEvent
    public static void onHammerUsage(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getMainHandItem();

        if(mainHandItem.getItem() instanceof HammerItem hammer && player instanceof ServerPlayer serverPlayer) {
            BlockPos initialBlockPos = event.getPos();
            if(HARVESTED_BLOCKS.contains(initialBlockPos)) {
                return;
            }

            for(BlockPos pos : HammerItem.getBlocksToBeDestroyed(1, initialBlockPos, serverPlayer)) {
                if(pos == initialBlockPos || !hammer.isCorrectToolForDrops(mainHandItem, event.getLevel().getBlockState(pos))) {
                    continue;
                }

                //stops recursive loop
                HARVESTED_BLOCKS.add(pos);
                serverPlayer.gameMode.destroyBlock(pos);
                HARVESTED_BLOCKS.remove(pos);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if(event.getEntity() instanceof Sheep sheep && event.getSource().getDirectEntity() instanceof Player player) {
            if(player.getMainHandItem().getItem() == Items.STICK) {
                player.sendSystemMessage(Component.literal(player.getName().getString() + " JUST HIT A SHEEP WITH STICK! we took the stick away"));
                sheep.addEffect(new MobEffectInstance(MobEffects.POISON, 600, 5));
                player.getMainHandItem().shrink(1);
            }
        }
    }

    // Comforted effect: pause healing when player attacks
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.hasEffect(Holder.direct(ModEffects.COMFORTED_EFFECT.get()))) {
            ComfortedEffect.recordDisrupt(player);
        }
    }

    // Comforted effect: pause healing when player takes knockback
    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof Player player && player.hasEffect(Holder.direct(ModEffects.COMFORTED_EFFECT.get()))) {
            ComfortedEffect.recordDisrupt(player);
        }
    }

    // Clean up Comforted per-player data when effect is removed
    @SubscribeEvent
    public static void onMobEffectRemove(MobEffectEvent.Remove event) {
        if (event.getEffect() == ModEffects.COMFORTED_EFFECT.get() && event.getEntity() instanceof Player player) {
            ComfortedEffect.clearPlayerData(player.getUUID());
        }
    }
}
