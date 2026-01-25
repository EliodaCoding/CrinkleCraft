package net.eli.tutorialmod.datagen;

import net.eli.tutorialmod.TutorialMod;
import net.eli.tutorialmod.blocks.ModBlocks;
import net.eli.tutorialmod.blocks.custom.CervaliteLampBlock;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TutorialMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        //"Normal" Blocks
        blockWithItem(ModBlocks.CERVALITE_BLOCK);
        blockWithItem(ModBlocks.RAW_CERVALITE_BLOCK);

        blockWithItem(ModBlocks.CERVALITE_ORE);
        blockWithItem(ModBlocks.CERVALITE_DEEPSLATE_ORE);

        blockWithItem(ModBlocks.MAGIC_BLOCK);

        //"Normal" Non-Solid blocks
        stairsBlock(ModBlocks.CERVALITE_STAIRS.get(), blockTexture(ModBlocks.CERVALITE_BLOCK.get()));
        slabBlock(ModBlocks.CERVALITE_SLAB.get(), blockTexture(ModBlocks.CERVALITE_BLOCK.get()), blockTexture(ModBlocks.CERVALITE_BLOCK.get()));

        buttonBlock(ModBlocks.CERVALITE_BUTTON.get(), blockTexture(ModBlocks.CERVALITE_BLOCK.get()));
        pressurePlateBlock(ModBlocks.CERVALITE_PRESSURE_PLATE.get(), blockTexture(ModBlocks.CERVALITE_BLOCK.get()));

        fenceBlock(ModBlocks.CERVALITE_FENCE.get(), blockTexture(ModBlocks.CERVALITE_BLOCK.get()));
        fenceGateBlock(ModBlocks.CERVALITE_FENCE_GATE.get(), blockTexture(ModBlocks.CERVALITE_BLOCK.get()));
        wallBlock(ModBlocks.CERVALITE_WALL.get(), blockTexture(ModBlocks.CERVALITE_BLOCK.get()));

        doorBlockWithRenderType(ModBlocks.CERVALITE_DOOR.get(), modLoc("block/cervalite_door_bottom"), modLoc("block/cervalite_door_top"), "cutout");
        trapdoorBlockWithRenderType(ModBlocks.CERVALITE_TRAPDOOR.get(), modLoc("block/cervalite_trapdoor"), true, "cutout");

        blockItem(ModBlocks.CERVALITE_STAIRS);
        blockItem(ModBlocks.CERVALITE_SLAB);
        blockItem(ModBlocks.CERVALITE_PRESSURE_PLATE);
        blockItem(ModBlocks.CERVALITE_FENCE_GATE);
        blockItem(ModBlocks.CERVALITE_TRAPDOOR, "_bottom");

        //Multi-BlockState Blocks
        customLamp();
    }

    private void customLamp() {
        getVariantBuilder(ModBlocks.CERVALITE_LAMP.get()).forAllStates(state -> {
            if(state.getValue(CervaliteLampBlock.CLICKED)) {
                return new ConfiguredModel[]{new ConfiguredModel(models().cubeAll("cervalite_lamp_on",
                        ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "block/" + "cervalite_lamp_on")))};
            } else {
                return new ConfiguredModel[]{new ConfiguredModel(models().cubeAll("cervalite_lamp_off",
                        ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "block/" + "cervalite_lamp_off")))};
            }
        });
        simpleBlockItem(ModBlocks.CERVALITE_LAMP.get(), models().cubeAll("cervalite_lamp_on",
                ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "block/" + "cervalite_lamp_on")));
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void blockItem(RegistryObject<? extends Block> blockRegistryObject) {
        simpleBlockItem(blockRegistryObject.get(), new ModelFile.UncheckedModelFile("tutorialmod:block/" +
                ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath()));
    }

    private void blockItem(RegistryObject<? extends Block> blockRegistryObject, String appendix) {
        simpleBlockItem(blockRegistryObject.get(), new ModelFile.UncheckedModelFile("tutorialmod:block/" +
                ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath() + appendix));
    }
}
