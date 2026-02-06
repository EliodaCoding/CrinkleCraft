package net.eli.crinklecraft.contract;

import net.eli.crinklecraft.CrinkleCraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Server-side contract events: send contract to chat on login, freeze unsigned players.
 * Contract is sent only to the joining player. Player signs via /crinklecraft contract sign <secret>.
 * Frozen players have zero movement, no gravity, no physics until they sign.
 */
@Mod.EventBusSubscriber(modid = CrinkleCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ContractEvents {

    /** Sends contract to chat (only to joining player) when unsigned. Player signs with command. */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ContractSavedData data = ContractSavedData.get(player.serverLevel());
        if (!data.hasSigned(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("message.crinklecraft.contract.chat1"));
            player.sendSystemMessage(Component.translatable("message.crinklecraft.contract.chat2"));
            player.sendSystemMessage(Component.translatable("message.crinklecraft.contract.sign_hint"));
        }
    }

    /** Every tick: if signed, restore gravity/physics. If unsigned, freeze (zero movement, no gravity). */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ContractSavedData data = ContractSavedData.get(player.serverLevel());
        if (data.hasSigned(player.getUUID())) {
            if (player.isNoGravity()) {
                player.setNoGravity(false);
                player.noPhysics = false;
            }
            return;
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.setNoGravity(true);
        player.noPhysics = true;
    }
}
