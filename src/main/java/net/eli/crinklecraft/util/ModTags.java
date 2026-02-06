package net.eli.crinklecraft.util;

import net.eli.crinklecraft.CrinkleCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Helper for creating mod item tags.
 */
public class ModTags {
    public static class Items {
        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(CrinkleCraft.MOD_ID, name));
        }
    }
}
