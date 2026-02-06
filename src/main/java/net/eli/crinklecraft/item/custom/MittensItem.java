package net.eli.crinklecraft.item.custom;

import net.eli.crinklecraft.item.ModArmorMaterials;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

/**
 * Mittens worn in chestplate slot. When worn, prevent the player from interacting with blocks and entities.
 * (Actual blocking is done in ModEvents.)
 */
public class MittensItem extends ArmorItem {

    public MittensItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }
}
