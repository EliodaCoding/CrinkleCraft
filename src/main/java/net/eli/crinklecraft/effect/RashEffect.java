package net.eli.crinklecraft.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Rash from staying wet too long. Causes random fire damage when moving. */
public class RashEffect extends MobEffect {

    private static final double MOVEMENT_THRESHOLD_SQ = 0.01; // ~0.1 block
    private static final float DAMAGE_AMOUNT = 1.0f; // 0.5 hearts
    private static final float DAMAGE_CHANCE = 0.2f; // 20% per tick when moving
    private static final int TICKS_BETWEEN_CHECKS = 20; // 1 second

    private static final Map<UUID, Vec3> lastPosition = new HashMap<>();

    public RashEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public static void clearPlayerData(UUID playerId) {
        lastPosition.remove(playerId);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player) || !(entity.level() instanceof ServerLevel)) {
            return false;
        }
        if (player.isCreative() || player.isSpectator()) return false;

        Vec3 pos = player.position();
        Vec3 last = lastPosition.put(player.getUUID(), pos);

        if (last != null && pos.distanceToSqr(last) >= MOVEMENT_THRESHOLD_SQ) {
            if (player.getRandom().nextFloat() < DAMAGE_CHANCE) {
                player.hurt(player.damageSources().onFire(), DAMAGE_AMOUNT);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % TICKS_BETWEEN_CHECKS == 1;
    }
}
