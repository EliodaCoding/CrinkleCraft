package net.eli.crinklecraft.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Visual/state effect when player leaked (accident with no diaper or overused). Emits particles when walking. */
public class WetEffect extends MobEffect {
    public WetEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
