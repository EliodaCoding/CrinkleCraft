package net.eli.crinklecraft.item.custom;

import net.minecraft.world.item.Item;

/**
 * Stuffie (stuffed animal). When held in main or off hand, gives Comforted effect.
 * Comfort application is handled in ModEvents via player tick.
 */
public class StuffieItem extends Item {

    public StuffieItem(Properties properties) {
        super(properties.stacksTo(1));
    }
}
