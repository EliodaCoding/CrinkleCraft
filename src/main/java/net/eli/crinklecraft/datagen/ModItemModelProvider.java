package net.eli.crinklecraft.datagen;

import net.eli.crinklecraft.CrinkleCraft;
import net.eli.crinklecraft.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CrinkleCraft.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Diaper, baby bottle, mittens use custom models in src/main/resources
        // Add basicItem() for any items that need generated models
    }
}
