package net.eli.crinklecraft.effect;

import net.eli.crinklecraft.CrinkleCraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CrinkleCraft.MOD_ID);

    public static final RegistryObject<MobEffect> WET_EFFECT = MOB_EFFECTS.register("wet",
            () -> new WetEffect(MobEffectCategory.NEUTRAL, 0xE6D700));

    public static final RegistryObject<MobEffect> RASH_EFFECT = MOB_EFFECTS.register("rash",
            () -> new RashEffect(MobEffectCategory.HARMFUL, 0xCC4422));

    public static final RegistryObject<MobEffect> COMFORTED_EFFECT = MOB_EFFECTS.register("comforted",
            () -> new ComfortedEffect(MobEffectCategory.BENEFICIAL, 0xb3afc7)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath(CrinkleCraft.MOD_ID, "comforted"),
                            -0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_SPEED, ResourceLocation.fromNamespaceAndPath(CrinkleCraft.MOD_ID, "comforted_attack"),
                            -0.3f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, ResourceLocation.fromNamespaceAndPath(CrinkleCraft.MOD_ID, "comforted_knockback"),
                            0.25f, AttributeModifier.Operation.ADD_VALUE));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }

    /** Apply Comforted effect for the given duration. */
    public static void applyComforted(ServerPlayer player, int durationTicks) {
        player.addEffect(new MobEffectInstance(Holder.direct(COMFORTED_EFFECT.get()), durationTicks, 0, true, true));
    }

    /** Apply Wet effect for the given duration. */
    public static void applyWet(ServerPlayer player, int durationTicks) {
        player.addEffect(new MobEffectInstance(Holder.direct(WET_EFFECT.get()), durationTicks, 0, false, true, true));
    }

    /** Remove Wet effect from the player. Returns true if the effect was removed. */
    public static boolean removeWet(Player player) {
        return player.removeEffect(Holder.direct(WET_EFFECT.get()));
    }

    /** Apply Rash effect for the given duration (e.g. from staying wet too long). */
    public static void applyRash(ServerPlayer player, int durationTicks) {
        player.addEffect(new MobEffectInstance(Holder.direct(RASH_EFFECT.get()), durationTicks, 0, false, true, true));
    }

    /** Remove Rash effect from the player. Returns true if the effect was removed. */
    public static boolean removeRash(Player player) {
        boolean removed = player.removeEffect(Holder.direct(RASH_EFFECT.get()));
        if (removed) RashEffect.clearPlayerData(player.getUUID());
        return removed;
    }
}
