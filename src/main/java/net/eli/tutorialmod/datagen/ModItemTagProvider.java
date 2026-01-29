package net.eli.tutorialmod.datagen;

import net.eli.tutorialmod.TutorialMod;
import net.eli.tutorialmod.item.ModItems;
import net.eli.tutorialmod.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;


public class ModItemTagProvider extends ItemTagsProvider {


    public ModItemTagProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider,
                              CompletableFuture<TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, TutorialMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(ModTags.Items.TRANSFORMABLE_ITEMS)
                .add(ModItems.CERVALITE.get())
                .add(ModItems.RAW_CERVALITE.get())
                .add(Items.EMERALD)
                .add(Items.QUARTZ);

        tag(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.CERVALITE_HELMET.get())
                .add(ModItems.CERVALITE_CHESTPLATE.get())
                .add(ModItems.CERVALITE_LEGGINGS.get())
                .add(ModItems.CERVALITE_BOOTS.get());
    }


}
