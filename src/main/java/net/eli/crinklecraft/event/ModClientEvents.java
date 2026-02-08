package net.eli.crinklecraft.event;

import net.minecraft.core.Holder;
import net.eli.crinklecraft.CrinkleCraft;
import net.eli.crinklecraft.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side tick events: Hold It keybinding, Wet effect particles.
 */
@Mod.EventBusSubscriber(modid = CrinkleCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModClientEvents {

    /** Every tick: Handle Hold It keybinding (sends holdit command). */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;

        if (CrinkleCraft.ClientModEvents.HOLD_IT.consumeClick()) {
            mc.player.connection.sendCommand("crinklecraft holdit");
        }
        if (CrinkleCraft.ClientModEvents.CHOSE_DIAPER.consumeClick()) {
            mc.player.connection.sendCommand("crinklecraft diaper");
        }
        if (CrinkleCraft.ClientModEvents.CRINKLECRAFT_SLOTS.consumeClick()) {
            mc.player.connection.sendCommand("crinklecraft slots");
        }
    }

    /** Spawn yellow dripping particles when players with Wet effect walk. */
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.level.isClientSide() && event.level.players().isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        for (Player player : event.level.players()) {
            if (!player.hasEffect(Holder.direct(ModEffects.WET_EFFECT.get()))) continue;
            boolean moved = player.getX() != player.xOld || player.getZ() != player.zOld;
            if (!moved) continue;
            if (event.level.random.nextInt(3) != 0) continue;
            double x = player.getX() + (event.level.random.nextDouble() - 0.5);
            double y = player.getY();
            double z = player.getZ() + (event.level.random.nextDouble() - 0.5);
            event.level.addParticle(ParticleTypes.DRIPPING_HONEY, x, y, z, 0, 0, 0);
        }
    }
}
