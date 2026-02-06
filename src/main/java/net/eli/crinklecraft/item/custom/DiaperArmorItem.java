package net.eli.crinklecraft.item.custom;

import net.eli.crinklecraft.item.ModArmorMaterials;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

/**
 * Display-only armor for the leggings slot so the equipped diaper is visible on the player.
 * Mechanics (absorption, uses) use PottyPlayerData.equippedDiaper; this item only provides the model.
 */
public class DiaperArmorItem extends ArmorItem {
    public DiaperArmorItem(Item.Properties properties) {
        super(ModArmorMaterials.DIAPER_ARMOR_MATERIAL, Type.LEGGINGS, properties);
    }
}
