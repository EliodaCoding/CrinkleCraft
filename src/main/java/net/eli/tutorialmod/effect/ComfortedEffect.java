package net.eli.tutorialmod.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ComfortedEffect extends MobEffect {
    private static final int STILL_TICKS_FOR_REGEN = 60; // 3 seconds at 20 TPS
    private static final double MOVEMENT_THRESHOLD = 0.001;
    private static final float HUNGER_REDUCTION = 0.6f; // 40% less hunger drain = multiply gain by 0.6

    // Per-player state: last game tick when a disruptive action occurred
    private static final Map<UUID, Long> lastDisruptTick = new HashMap<>();
    private static final Map<UUID, Float> lastExhaustion = new HashMap<>();
    private static final Map<UUID, Boolean> lastOnGround = new HashMap<>();
    private static final Map<UUID, Boolean> weAddedRegen = new HashMap<>();

    public ComfortedEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    /**
     * Call this from event handlers when the player does something that should pause healing:
     * attacks, takes knockback, sprints, or jumps.
     */
    public static void recordDisrupt(Player player) {
        if (player.level() instanceof ServerLevel) {
            lastDisruptTick.put(player.getUUID(), player.level().getGameTime());
        }
    }

    public static void clearPlayerData(UUID playerId) {
        lastDisruptTick.remove(playerId);
        lastExhaustion.remove(playerId);
        lastOnGround.remove(playerId);
        weAddedRegen.remove(playerId);
    }

    @Override
    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!(pLivingEntity instanceof Player player) || !(pLivingEntity.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        long currentTick = serverLevel.getGameTime();
        UUID uuid = player.getUUID();

        // ----- Disrupt detection: sprint and jump -----
        if (player.isSprinting()) {
            recordDisrupt(player);
        }
        boolean wasOnGround = lastOnGround.getOrDefault(uuid, true);
        boolean isOnGround = player.onGround();
        lastOnGround.put(uuid, isOnGround);
        if (wasOnGround && !isOnGround && player.getDeltaMovement().y > 0.1) {
            recordDisrupt(player); // Jumped
        }

        // ----- Hunger drain reduction: -40% -----
        float currentExhaustion = player.getFoodData().getExhaustionLevel();
        float prevExhaustion = lastExhaustion.getOrDefault(uuid, currentExhaustion);
        lastExhaustion.put(uuid, currentExhaustion);
        if (currentExhaustion > prevExhaustion) {
            float added = currentExhaustion - prevExhaustion;
            float reducedAdded = added * HUNGER_REDUCTION;
            player.getFoodData().setExhaustion(prevExhaustion + reducedAdded);
            lastExhaustion.put(uuid, prevExhaustion + reducedAdded);
        }

        // ----- Regeneration passive: still/sneaking for 3 seconds -----
        long lastDisrupt = lastDisruptTick.getOrDefault(uuid, 0L);
        long ticksSinceDisrupt = currentTick - lastDisrupt;

        boolean isStill = player.getDeltaMovement().horizontalDistanceSqr() < MOVEMENT_THRESHOLD * MOVEMENT_THRESHOLD;
        boolean isSneaking = player.isCrouching();
        boolean canGainRegen = (isStill || isSneaking) && ticksSinceDisrupt >= STILL_TICKS_FOR_REGEN;

        if (canGainRegen) {
            if (!player.hasEffect(MobEffects.REGENERATION) || player.getEffect(MobEffects.REGENERATION).getAmplifier() < 0) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, true, false)); // showIcon=false to avoid effect HUD conflicts
                weAddedRegen.put(uuid, true);
            }
        } else {
            if (weAddedRegen.getOrDefault(uuid, false)) {
                player.removeEffect(MobEffects.REGENERATION);
                weAddedRegen.put(uuid, false);
            }
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }
}
