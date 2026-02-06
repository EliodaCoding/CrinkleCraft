package net.eli.crinklecraft.effect;

import net.eli.crinklecraft.CrinkleCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CrinkleCraft.MOD_ID);

    public static final RegistryObject<MobEffect> WET_EFFECT = MOB_EFFECTS.register("wet",
            () -> new WetEffect(MobEffectCategory.NEUTRAL, 0xE6D700));

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
}
