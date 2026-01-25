package net.eli.tutorialmod.datagen;

import net.eli.tutorialmod.blocks.ModBlocks;
import net.eli.tutorialmod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    protected ModBlockLootTableProvider(HolderLookup.Provider pRegistries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), pRegistries);
    }

    @Override
    protected void generate() {
        //"Normal" block drops
        dropSelf(ModBlocks.CERVALITE_BLOCK.get());
        dropSelf(ModBlocks.RAW_CERVALITE_BLOCK.get());
        dropSelf(ModBlocks.MAGIC_BLOCK.get());
        dropSelf(ModBlocks.CERVALITE_LAMP.get());


        //"Normal" Non-solid Blocks
        dropSelf(ModBlocks.CERVALITE_STAIRS.get());
        this.add(ModBlocks.CERVALITE_SLAB.get(),
                block -> createSlabItemTable(ModBlocks.CERVALITE_SLAB.get()));

        dropSelf(ModBlocks.CERVALITE_PRESSURE_PLATE.get());
        dropSelf(ModBlocks.CERVALITE_BUTTON.get());

        dropSelf(ModBlocks.CERVALITE_FENCE.get());
        dropSelf(ModBlocks.CERVALITE_FENCE_GATE.get());
        dropSelf(ModBlocks.CERVALITE_WALL.get());

        this.add(ModBlocks.CERVALITE_DOOR.get(),
                block -> createDoorTable(ModBlocks.CERVALITE_DOOR.get()));
        dropSelf(ModBlocks.CERVALITE_TRAPDOOR.get());

        //Ores
        this.add(ModBlocks.CERVALITE_ORE.get(),
                block -> createOreDrop(ModBlocks.CERVALITE_ORE.get(), ModItems.RAW_CERVALITE.get()));
        this.add(ModBlocks.CERVALITE_DEEPSLATE_ORE.get(),
                block -> createMultipleOreDrops(ModBlocks.CERVALITE_DEEPSLATE_ORE.get(), ModItems.RAW_CERVALITE.get(), 2, 6));
    }

    //Helper method to make ores that drop multiple ore drops
    protected LootTable.Builder createMultipleOreDrops(Block pBlock, Item item, float minDrops, float maxDrops) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable(
                pBlock, this.applyExplosionDecay(
                        pBlock, LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(minDrops, maxDrops)))
                                .apply(ApplyBonusCount.addOreBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))
                )
        );
    }

    //checks all modblocks
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
