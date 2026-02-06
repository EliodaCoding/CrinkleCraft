package net.eli.crinklecraft.item.custom;

import net.eli.crinklecraft.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Stuffie (stuffed animal). When held in main or off hand, gives Comforted effect.
 * Right-click to squeeze: plays a squeak and spawns small particles.
 */
public class StuffieItem extends Item {

    public StuffieItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            // Play squeak (server broadcasts to clients)
            level.playSound(null, player.blockPosition(), ModSounds.STUFFIE_SQUEAK.get(), SoundSource.PLAYERS, 0.6F, 0.9F + level.getRandom().nextFloat() * 0.2F);
            // Small puff particles around the stuffie (squeeze effect)
            for (int i = 0; i < 6; i++) {
                double x = player.getX() + (level.getRandom().nextDouble() - 0.5) * 0.5;
                double y = player.getY() + player.getEyeHeight() * 0.7 + (level.getRandom().nextDouble() - 0.5) * 0.3;
                double z = player.getZ() + (level.getRandom().nextDouble() - 0.5) * 0.5;
                ((net.minecraft.server.level.ServerLevel) level).sendParticles(
                        ParticleTypes.CLOUD, x, y, z, 1, 0.02, 0.02, 0.02, 0.01);
            }
        }
        return InteractionResultHolder.success(stack);
    }
}
