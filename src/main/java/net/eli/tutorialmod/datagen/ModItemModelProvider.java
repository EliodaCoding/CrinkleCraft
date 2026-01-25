package net.eli.tutorialmod.datagen;

import net.eli.tutorialmod.TutorialMod;
import net.eli.tutorialmod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TutorialMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.CERVALITE.get());
        basicItem(ModItems.RAW_CERVALITE.get());

        basicItem(ModItems.CHISEL.get());
        basicItem(ModItems.STRAWBERRY.get());
        basicItem(ModItems.SPIRIT_ASHES.get());
    }
}
