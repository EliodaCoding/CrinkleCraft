package net.eli.crinklecraft.event;

import net.eli.crinklecraft.CrinkleCraft;
import net.eli.crinklecraft.effect.ComfortedEffect;
import net.eli.crinklecraft.effect.ModEffects;
import net.eli.crinklecraft.item.ModItems;
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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CrinkleCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {
    /** Sync diaper visual to leggings slot when player logs in with diaper equipped. */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        PottySavedData data = PottySavedData.get(serverPlayer.serverLevel());
        PottyPlayerData playerData = data.getOrCreate(serverPlayer.getUUID());
        if (playerData.getEquippedDiaper().isEmpty()) return;
        ItemStack leggings = serverPlayer.getInventory().getArmor(1);
        if (leggings.is(ModItems.DIAPER_ARMOR.get())) return;
        if (!leggings.isEmpty())
            playerData.setStoredLeggings(leggings.copy());
        serverPlayer.getInventory().armor.set(1, new ItemStack(ModItems.DIAPER_ARMOR.get()));
        data.markDirty();
    }

    /** True if player has mittens in chestplate slot (blocks all interactions). */
    private static boolean isWearingMittens(Player player) {
        ItemStack chest = player.getInventory().getArmor(2);
        return !chest.isEmpty() && chest.getItem() instanceof MittensItem;
    }

    // Stuffie: when held in main or off hand, apply Comforted
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        boolean holdingStuffie = (!main.isEmpty() && main.getItem() instanceof StuffieItem)
                || (!off.isEmpty() && off.getItem() instanceof StuffieItem);
        if (holdingStuffie) {
            player.addEffect(new MobEffectInstance(Holder.direct(ModEffects.COMFORTED_EFFECT.get()), 100, 0, true, true));
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
                player.getInventory().armor.set(1, playerData.getStoredLeggings().copy());
                playerData.setStoredLeggings(ItemStack.EMPTY);
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
