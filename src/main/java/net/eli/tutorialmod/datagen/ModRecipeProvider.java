package net.eli.tutorialmod.datagen;

import net.eli.tutorialmod.TutorialMod;
import net.eli.tutorialmod.blocks.ModBlocks;
import net.eli.tutorialmod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(RecipeOutput pRecipeOutput) {
        List<ItemLike> CERVALITE_SMELTABLES = List.of(ModItems.RAW_CERVALITE.get(),
                ModBlocks.CERVALITE_ORE.get(), ModBlocks.CERVALITE_DEEPSLATE_ORE.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CERVALITE_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.CERVALITE.get())
                .unlockedBy(getHasName(ModItems.CERVALITE.get()), has(ModItems.CERVALITE.get())).save(pRecipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CERVALITE.get(), 9)
                .requires(ModBlocks.CERVALITE_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.CERVALITE_BLOCK.get()), has(ModBlocks.CERVALITE_BLOCK.get()))
                .save(pRecipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CERVALITE.get(), 32)
                .requires(ModBlocks.MAGIC_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.MAGIC_BLOCK.get()), has(ModBlocks.MAGIC_BLOCK.get()))
                .save(pRecipeOutput, TutorialMod.MOD_ID + ":cervalite_from_magic_block");

        oreSmelting(pRecipeOutput, CERVALITE_SMELTABLES, RecipeCategory.MISC, ModItems.CERVALITE.get(), 0.25f, 200, "cervalite");
        oreBlasting(pRecipeOutput, CERVALITE_SMELTABLES, RecipeCategory.MISC, ModItems.CERVALITE.get(), 0.25f, 100, "cervalite");

    }

    //extra code to make sure smelting recipes go into the mod folder and not the minecraft one
    protected static void oreSmelting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTime, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput recipeOutput, RecipeSerializer<T> pCookingSerializer, AbstractCookingRecipe.Factory<T> factory,
                                                                       List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer, factory).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(recipeOutput, TutorialMod.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }
}
