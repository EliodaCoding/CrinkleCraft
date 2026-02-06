package net.eli.crinklecraft.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.eli.crinklecraft.CrinkleCraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Custom data components for items: diaper uses, bottle contents.
 * BottleContents holds milk, apple juice, or up to N potion effects. Use canAddPotion(max) for item-specific limits.
 */
public class ModDataComponentTypes {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, CrinkleCraft.MOD_ID);

    /** Diaper/pullups: number of times used (absorbed or voluntary). Item defines max (e.g. 3 for diaper, 1 for pullups). */
    public static final RegistryObject<DataComponentType<Integer>> DIAPER_USES = register("diaper_uses",
            builder -> builder.persistent(Codec.intRange(0, 99)));

    /** Bottle: either has milk (clear effects) or up to 3 potions' effects (all applied in one drink). */
    public static final RegistryObject<DataComponentType<BottleContents>> BOTTLE_CONTENTS = register("bottle_contents",
            builder -> builder.persistent(BottleContents.CODEC));

    /** Bottle contents: milk (clear effects), apple juice (clear effects), or list of potion effects. potionCount tracks slots used. */
    public static final class BottleContents {
        public static final Codec<BottleContents> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.BOOL.optionalFieldOf("milk", false).forGetter((BottleContents b) -> b.hasMilk),
                Codec.BOOL.optionalFieldOf("apple_juice", false).forGetter((BottleContents b) -> b.hasAppleJuice),
                MobEffectInstance.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter((BottleContents b) -> b.effects),
                Codec.intRange(0, 3).optionalFieldOf("potion_count", 0).forGetter((BottleContents b) -> b.potionCount)
        ).apply(inst, BottleContents::new));

        public static final BottleContents EMPTY = new BottleContents(false, false, List.of(), 0);
        public static final int MAX_POTIONS = 3;

        public final boolean hasMilk;
        public final boolean hasAppleJuice;
        public final List<MobEffectInstance> effects;
        public final int potionCount;

        public BottleContents(boolean hasMilk, boolean hasAppleJuice, List<MobEffectInstance> effects, int potionCount) {
            this.hasMilk = hasMilk;
            this.hasAppleJuice = hasAppleJuice;
            this.effects = effects != null ? List.copyOf(effects) : List.of();
            this.potionCount = Math.max(0, Math.min(MAX_POTIONS, potionCount));
        }

        /** Legacy constructor for milk-only (no apple juice). */
        public BottleContents(boolean hasMilk, List<MobEffectInstance> effects, int potionCount) {
            this(hasMilk, false, effects, potionCount);
        }

        public boolean isEmpty() {
            return !hasMilk && !hasAppleJuice && effects.isEmpty();
        }

        /** Returns true if another potion can be added (no milk/juice, under maxPotions for this item type). */
        public boolean canAddPotion(int maxPotions) {
            return !hasMilk && !hasAppleJuice && potionCount < maxPotions;
        }
    }

    private static <T>RegistryObject<DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
