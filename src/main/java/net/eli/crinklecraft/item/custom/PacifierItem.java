package net.eli.crinklecraft.item.custom;

import net.minecraft.world.item.Item;

/**
 * Pacifier. When held in offhand, outgoing chat is transformed to baby talk/babbling.
 * Chat transformation is handled in ChatEvents via ServerChatEvent.
 */
public class PacifierItem extends Item {

    public PacifierItem(Properties properties) {
        super(properties.stacksTo(1));
    }
}
