package net.eli.crinklecraft.potty;

import net.eli.crinklecraft.Config;
import net.eli.crinklecraft.CrinkleCraft;
import net.eli.crinklecraft.effect.ModEffects;
import net.eli.crinklecraft.util.FlavorTextHelper;
import net.minecraft.core.Holder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.eli.crinklecraft.item.custom.DiaperItem;
import net.eli.crinklecraft.item.custom.MagicDiaperItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Server-side potty events: register commands, tick gauge/QTE.
 * Gauge fills over time; at thresholds a QTE (potty check) starts. Timeout = accident (diaper absorbs if equipped).
 */
@Mod.EventBusSubscriber(modid = CrinkleCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PottyEvents {

    private static final String POTTY_CHECK_WARNING_KEY = "message." + CrinkleCraft.MOD_ID + ".potty_check.warning";

    /** Ticks when Wet was applied (for Rash after too long). Cleared when Wet ends or Rash applied. */
    private static final Map<UUID, Long> wetStartedTick = new HashMap<>();

    /** Registers /crinklecraft commands. */
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CrinkleCraftCommand.register(event.getDispatcher());
    }

    /** Every server tick: fill gauges, start QTE at thresholds, timeout QTE -> accident. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;

        ServerLevel level = serverPlayer.serverLevel();
        PottyPlayerData playerData = PottySavedData.getPlayerData(serverPlayer);
        PottySavedData data = PottySavedData.get(level);

        // Decay transient effects (e.g. drinking boost)
        playerData.tickTransientEffects();

        // Gauge fill
        int t = playerData.getTicksSinceLastGain() + 1;
        playerData.setTicksSinceLastGain(t);
        if (t >= PottyPlayerData.TICKS_BETWEEN_GAIN) {
            playerData.setTicksSinceLastGain(0);
            double gain = PottyPlayerData.DEFAULT_GAIN_PER_TICK * Config.gaugeSpeedMultiplier * (1.0 / Config.getGaugeTickMultiplier()) * playerData.getPeeGainMultiplier();
            playerData.addPee((float) gain);
            if (playerData.isMessingEnabled()) {
                double messGain = PottyPlayerData.DEFAULT_GAIN_PER_TICK * Config.gaugeSpeedMultiplier * (1.0 / Config.getGaugeTickMultiplier());
                playerData.addMess((float) messGain);
            }
            data.markDirty();
        }

        // QTE: start at thresholds
        if (!playerData.isInPottyCheck()) {
            startQTEIfThresholdCrossed(level, data, serverPlayer, playerData);
        } else {
            // QTE timeout -> accident (uses randomized allowed ticks set when check started)
            long now = level.getGameTime();
            int allowed = playerData.getPottyCheckAllowedTicks();
            if (now - playerData.getPottyCheckStartTick() >= allowed) {
                triggerAccident(level, data, serverPlayer, playerData);
            }
        }

        // Wet too long -> Rash (random fire damage when moving)
        if (serverPlayer.hasEffect(Holder.direct(ModEffects.WET_EFFECT.get()))) {
            Long start = wetStartedTick.get(serverPlayer.getUUID());
            if (start == null) {
                start = level.getGameTime();
                wetStartedTick.put(serverPlayer.getUUID(), start);
            }
            long elapsed = level.getGameTime() - start;
            if (elapsed >= Config.rashAfterWetTicks) {
                ModEffects.applyRash(serverPlayer, Config.rashDurationTicks);
                wetStartedTick.remove(serverPlayer.getUUID());
            }
        } else {
            wetStartedTick.remove(serverPlayer.getUUID());
        }

        // Magic diaper: regen one use every REGEN_TICKS when equipped and partially used
        ItemStack diaper = playerData.getEquippedDiaper();
        if (!diaper.isEmpty() && diaper.getItem() instanceof MagicDiaperItem di) {
            int uses = di.getUses(diaper);
            if (uses > 0 && uses < di.getMaxUses(diaper)) {
                long lastRegen = playerData.getLastMagicDiaperRegenTick();
                if (level.getGameTime() - lastRegen >= MagicDiaperItem.REGEN_TICKS) {
                    di.restoreOneUse(diaper);
                    playerData.setLastMagicDiaperRegenTick(level.getGameTime());
                    playerData.setEquippedDiaper(diaper);
                    data.markDirty();
                }
            }
        }
    }

    /** Checks pee/mess gauges against thresholds; starts QTE if crossed, or random early trigger in danger zone. */
    private static void startQTEIfThresholdCrossed(ServerLevel level, PottySavedData data, ServerPlayer player, PottyPlayerData playerData) {
        float pee = playerData.getPeeLevel();
        float mess = playerData.getMessLevel();
        var random = level.getRandom();

        for (float thresh : PottyPlayerData.QTE_THRESHOLDS) {
            if (playerData.getLastPeeThreshold() >= thresh) continue;
            float earlyZone = thresh - 7f;
            boolean inEarlyZone = pee >= earlyZone && pee < thresh;
            boolean hitThreshold = pee >= thresh;
            if (hitThreshold || (inEarlyZone && random.nextFloat() < PottyPlayerData.QTE_EARLY_TRIGGER_CHANCE)) {
                startPottyCheck(level, data, player, playerData, true, thresh, random);
                return;
            }
        }
        for (float thresh : PottyPlayerData.QTE_THRESHOLDS) {
            if (!playerData.isMessingEnabled() || playerData.getLastMessThreshold() >= thresh) continue;
            float earlyZone = thresh - 7f;
            boolean inEarlyZone = mess >= earlyZone && mess < thresh;
            boolean hitThreshold = mess >= thresh;
            if (hitThreshold || (inEarlyZone && random.nextFloat() < PottyPlayerData.QTE_EARLY_TRIGGER_CHANCE)) {
                startPottyCheck(level, data, player, playerData, false, thresh, random);
                return;
            }
        }
    }

    private static void startPottyCheck(ServerLevel level, PottySavedData data, ServerPlayer player, PottyPlayerData playerData, boolean isPee, float thresh, net.minecraft.util.RandomSource random) {
        playerData.setInPottyCheck(true);
        playerData.setPottyCheckStartTick(level.getGameTime());
        playerData.setPottyCheckPee(isPee);
        playerData.setPottyCheckThreshold(thresh);
        int base = playerData.getReactionTicksForCurrentCheck();
        float variance = PottyPlayerData.QTE_REACTION_VARIANCE_MIN + random.nextFloat() * (PottyPlayerData.QTE_REACTION_VARIANCE_MAX - PottyPlayerData.QTE_REACTION_VARIANCE_MIN);
        playerData.setPottyCheckAllowedTicks((int) (base * variance * Config.getReactionTimeMultiplier()));
        player.sendSystemMessage(Component.translatable(POTTY_CHECK_WARNING_KEY, (int) thresh));
        data.markDirty();
    }

    /** Handles QTE timeout: absorb with diaper if equipped and not overused, else broadcast message. Updates gauges and continence. */
    private static void triggerAccident(ServerLevel level, PottySavedData data, ServerPlayer player, PottyPlayerData playerData) {
        boolean hadUsableDiaper = playerData.hasUsableDiaper();
        if (hadUsableDiaper) {
            ItemStack diaper = playerData.getEquippedDiaper();
            DiaperItem di = (DiaperItem) diaper.getItem();
            if (!di.isFullyUsed(diaper)) {
                di.useOne(diaper);
                playerData.setEquippedDiaper(diaper); // keep diaper in slot even when fully used
                data.markDirty();
            }
        }
        boolean leaked = !hadUsableDiaper;
        int radius = hadUsableDiaper ? 5 : 20;
        broadcastAccidentMessage(level, player, radius, leaked);
        if (leaked) {
            ModEffects.applyWet(player, Config.getWetDurationTicks());
            wetStartedTick.put(player.getUUID(), level.getGameTime());
        }

        if (playerData.isPottyCheckPee()) {
            playerData.setPeeLevel(0);
            playerData.setLastPeeThreshold(playerData.getPottyCheckThreshold());
        } else {
            playerData.setMessLevel(0);
            playerData.setLastMessThreshold(playerData.getPottyCheckThreshold());
        }
        playerData.onPottyCheckFail();
        playerData.setInPottyCheck(false);
        data.markDirty();
    }

    /** Sends accident-style messages. leaked=false: hiss (diaper absorbed); leaked=true: drops + leak (diaper leaked or none). */
    public static void broadcastAccidentMessage(ServerLevel level, Player player, int radius, boolean leaked) {
        if (Config.isAccidentMessagesNone()) return;
        var random = level.getRandom();
        if (Config.isAccidentSelfOnly()) {
            String selfKey = leaked ? FlavorTextHelper.pick(random, FlavorTextHelper.ACCIDENT_SELF_LEAKED)
                    : FlavorTextHelper.pick(random, FlavorTextHelper.ACCIDENT_SELF_ABSORBED);
            player.sendSystemMessage(Component.translatable(selfKey));
        } else {
            String selfKey = leaked ? FlavorTextHelper.pick(random, FlavorTextHelper.ACCIDENT_SELF_LEAKED)
                    : FlavorTextHelper.pick(random, FlavorTextHelper.ACCIDENT_SELF_ABSORBED);
            String nearbyKey = leaked ? FlavorTextHelper.pick(random, FlavorTextHelper.ACCIDENT_NEARBY_LEAKED)
                    : FlavorTextHelper.pick(random, FlavorTextHelper.ACCIDENT_NEARBY_ABSORBED);
            player.sendSystemMessage(Component.translatable(selfKey));
            Component nearbyMsg = Component.translatable(nearbyKey);
            double rSq = (double) radius * radius;
            level.players().stream()
                    .filter(p -> p != player && p.distanceToSqr(player) <= rSq)
                    .forEach(p -> p.sendSystemMessage(nearbyMsg));
        }
    }
}
