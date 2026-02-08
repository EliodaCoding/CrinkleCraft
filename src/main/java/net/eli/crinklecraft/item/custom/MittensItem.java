package net.eli.crinklecraft.item.custom;

import net.minecraft.world.item.Item;

/**
 * Mittens worn in a custom curio slot (not armor). When equipped, prevent the player
 * from interacting with blocks and entities. Blocking is done in ModEvents.
 */
public class MittensItem extends Item {

    public MittensItem(Properties properties) {
        super(properties.stacksTo(1));
    }
}
