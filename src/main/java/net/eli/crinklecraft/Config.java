package net.eli.crinklecraft;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = CrinkleCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    /*
     * CrinkleCraft is designed for cozy, non-explicit, care-focused play.
     * We do not support or add explicit content. We hope the customization and care
     * mechanics give you a satisfying experience. Tune the options below to match
     * your preferred intensity and privacy.
     */

    private static final ForgeConfigSpec.DoubleValue GAUGE_SPEED_MULTIPLIER = BUILDER
            .comment("Multiplier for how fast pee/mess gauges fill. 1.0 = default, 0.5 = slower/chill, 2.0 = faster.")
            .defineInRange("gaugeSpeedMultiplier", 1.0, 0.25, 4.0);
    private static final ForgeConfigSpec.ConfigValue<String> ACCIDENT_MESSAGE_VISIBILITY = BUILDER
            .comment("all = you + nearby, self_only = only you (privacy), none = no messages.")
            .defineInList("accidentMessageVisibility", "all", List.of("all", "self_only", "none"));
    private static final ForgeConfigSpec.BooleanValue INTENSE_MODE = BUILDER
            .comment("When true: faster gauge, shorter reaction time, longer Wet effect.")
            .define("intenseMode", false);
    private static final ForgeConfigSpec.ConfigValue<String> POTTY_CHECK_FREQUENCY = BUILDER
            .comment("relaxed = fewer checks, focused = more frequent.")
            .defineInList("pottyCheckFrequency", "normal", List.of("relaxed", "normal", "focused"));
    private static final ForgeConfigSpec.IntValue WET_DURATION_TICKS = BUILDER
            .comment("Wet effect duration after leak (ticks). Intense mode doubles this.")
            .defineInRange("wetDurationTicks", 2400, 20, 72000);
    private static final ForgeConfigSpec.IntValue RASH_AFTER_WET_TICKS = BUILDER
            .comment("Ticks of Wet before Rash appears (staying wet too long). 3600 = 3 min.")
            .defineInRange("rashAfterWetTicks", 3600, 60, 72000);
    private static final ForgeConfigSpec.IntValue RASH_DURATION_TICKS = BUILDER
            .comment("Rash effect duration once applied (ticks). Cleanup command removes it immediately.")
            .defineInRange("rashDurationTicks", 6000, 100, 72000);

    // Potty / continence (used by potty check events)
    private static final ForgeConfigSpec.DoubleValue CONTINENCE_ON_SUCCESS = BUILDER
            .comment("Continence change when player succeeds a potty check (e.g. uses potty in time).")
            .defineInRange("continenceOnSuccess", 3.0, -100, 100);
    private static final ForgeConfigSpec.DoubleValue CONTINENCE_ON_FAIL = BUILDER
            .comment("Continence change when player fails a potty check (accident).")
            .defineInRange("continenceOnFail", -12.0, -100, 100);
    private static final ForgeConfigSpec.DoubleValue CONTINENCE_ON_CHOSE_DIAPER = BUILDER
            .comment("Continence change when player deliberately uses diaper during a potty check (habit/dependence).")
            .defineInRange("continenceOnChoseDiaper", -8.0, -100, 100);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static double gaugeSpeedMultiplier;
    public static String accidentMessageVisibility;
    public static boolean intenseMode;
    public static String pottyCheckFrequency;
    public static int wetDurationTicks;
    public static int rashAfterWetTicks;
    public static int rashDurationTicks;
    public static double continenceOnSuccess;
    public static double continenceOnFail;
    public static double continenceOnChoseDiaper;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        gaugeSpeedMultiplier = GAUGE_SPEED_MULTIPLIER.get();
        accidentMessageVisibility = ACCIDENT_MESSAGE_VISIBILITY.get();
        intenseMode = INTENSE_MODE.get();
        pottyCheckFrequency = POTTY_CHECK_FREQUENCY.get();
        wetDurationTicks = WET_DURATION_TICKS.get();
        rashAfterWetTicks = RASH_AFTER_WET_TICKS.get();
        rashDurationTicks = RASH_DURATION_TICKS.get();
        continenceOnSuccess = CONTINENCE_ON_SUCCESS.get();
        continenceOnFail = CONTINENCE_ON_FAIL.get();
        continenceOnChoseDiaper = CONTINENCE_ON_CHOSE_DIAPER.get();
    }

    /** True if accident messages go only to self (not nearby). */
    public static boolean isAccidentSelfOnly() {
        return "self_only".equals(accidentMessageVisibility);
    }

    /** True if accident messages are disabled. */
    public static boolean isAccidentMessagesNone() {
        return "none".equals(accidentMessageVisibility);
    }

    /** Reaction time multiplier: intense = 0.6, else 1.0. */
    public static double getReactionTimeMultiplier() {
        return intenseMode ? 0.6 : 1.0;
    }

    /** Gauge tick multiplier: relaxed = 1.5, normal = 1.0, focused = 0.6. */
    public static double getGaugeTickMultiplier() {
        return switch (pottyCheckFrequency) {
            case "relaxed" -> 1.5;
            case "focused" -> 0.6;
            default -> 1.0;
        };
    }

    /** Wet effect duration (ticks). Intense mode doubles. */
    public static int getWetDurationTicks() {
        int base = wetDurationTicks;
        return intenseMode ? base * 2 : base;
    }
}
