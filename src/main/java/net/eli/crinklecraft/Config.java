package net.eli.crinklecraft;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CrinkleCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

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

    public static double continenceOnSuccess;
    public static double continenceOnFail;
    public static double continenceOnChoseDiaper;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        continenceOnSuccess = CONTINENCE_ON_SUCCESS.get();
        continenceOnFail = CONTINENCE_ON_FAIL.get();
        continenceOnChoseDiaper = CONTINENCE_ON_CHOSE_DIAPER.get();
    }
}
