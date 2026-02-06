package net.eli.crinklecraft.event;

import net.eli.crinklecraft.CrinkleCraft;
import net.eli.crinklecraft.effect.ComfortedEffect;
import net.eli.crinklecraft.effect.ModEffects;
import net.eli.crinklecraft.item.custom.MittensItem;
import net.eli.crinklecraft.item.custom.StuffieItem;
import net.eli.crinklecraft.potty.PottySavedData;
import net.eli.crinklecraft.potty.PottyPlayerData;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CrinkleCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {
    private static final int STUFFIE_HOLD_TICKS_REQUIRED = 15 * 20;  // 15 seconds
    private static final int COMFORTED_DURATION_TICKS = 15 * 20;     // 15 seconds
    private static final Map<UUID, Integer> stuffieHoldTicks = new HashMap<>();

    /** True if player has mittens in chestplate slot (blocks all interactions). */
    private static boolean isWearingMittens(Player player) {
        ItemStack chest = player.getInventory().getArmor(2);
        return !chest.isEmpty() && chest.getItem() instanceof MittensItem;
    }

    // Stuffie: hold for 15 sec then get Comforted for 15 sec (no spam)
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        boolean holdingStuffie = (!main.isEmpty() && main.getItem() instanceof StuffieItem)
                || (!off.isEmpty() && off.getItem() instanceof StuffieItem);

        int ticks = stuffieHoldTicks.getOrDefault(player.getUUID(), 0);
        if (holdingStuffie) {
            ticks++;
            if (ticks >= STUFFIE_HOLD_TICKS_REQUIRED) {
                player.addEffect(new MobEffectInstance(Holder.direct(ModEffects.COMFORTED_EFFECT.get()), COMFORTED_DURATION_TICKS, 0, true, true));
                ticks = 0;
            }
            stuffieHoldTicks.put(player.getUUID(), ticks);
        } else {
            if (ticks != 0) stuffieHoldTicks.put(player.getUUID(), 0);
        }
    }

    // Mittens: block all interactions when worn
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isWearingMittens(event.getEntity())) {
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (isWearingMittens(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (isWearingMittens(event.getEntity())) {
            event.setCanceled(true);
            return;
        }
        // Shift + empty hand: unequip diaper from custom slot
        Player player = event.getEntity();
        if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()
                && player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel
                && player instanceof ServerPlayer serverPlayer) {
            PottySavedData data = PottySavedData.get(serverLevel);
            PottyPlayerData playerData = data.getOrCreate(serverPlayer.getUUID());
            ItemStack diaper = playerData.getEquippedDiaper();
            if (!diaper.isEmpty()) {
                playerData.setEquippedDiaper(ItemStack.EMPTY);
                data.markDirty();
                if (!player.getInventory().add(diaper)) {
                    player.drop(diaper, false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (isWearingMittens(player)) {
            event.setCanceled(true);
            return;
        }
        if (player.hasEffect(Holder.direct(ModEffects.COMFORTED_EFFECT.get()))) {
            ComfortedEffect.recordDisrupt(player);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (isWearingMittens(event.getPlayer())) {
            event.setCanceled(true);
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
