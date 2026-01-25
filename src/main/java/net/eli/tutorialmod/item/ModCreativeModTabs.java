package net.eli.tutorialmod.item;

import net.eli.tutorialmod.TutorialMod;
import net.eli.tutorialmod.blocks.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TutorialMod.MOD_ID);

    public static  final RegistryObject<CreativeModeTab> CERVALITE_ITEMS_TABS = CREATIVE_MODE_TABS.register("cervalite_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.CERVALITE.get()))
                    .title(Component.translatable("creativetab.tutorialmod.cervalite_items"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.CERVALITE.get());
                        pOutput.accept(ModItems.RAW_CERVALITE.get());

                        pOutput.accept(ModItems.CHISEL.get());

                        pOutput.accept(ModItems.STRAWBERRY.get());
                        pOutput.accept(ModItems.SPIRIT_ASHES.get());

                    })
                    .build());


    public static  final RegistryObject<CreativeModeTab> CERVALITE_BLOCKS_TABS = CREATIVE_MODE_TABS.register("cervalite_blocks_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.CERVALITE_BLOCK.get()))
                    .withTabsBefore(CERVALITE_ITEMS_TABS.getId())
                    .title(Component.translatable("creativetab.tutorialmod.cervalite_blocks"))
                    .displayItems((pParameters, pOutput) -> {
                        //Blocks
                        pOutput.accept(ModBlocks.CERVALITE_BLOCK.get());
                        pOutput.accept(ModBlocks.RAW_CERVALITE_BLOCK.get());

                        pOutput.accept(ModBlocks.CERVALITE_ORE.get());
                        pOutput.accept(ModBlocks.CERVALITE_DEEPSLATE_ORE.get());

                        pOutput.accept(ModBlocks.MAGIC_BLOCK.get());

                        //Non-Solid Blocks
                        pOutput.accept(ModBlocks.CERVALITE_STAIRS.get());
                        pOutput.accept(ModBlocks.CERVALITE_SLAB.get());

                        pOutput.accept(ModBlocks.CERVALITE_PRESSURE_PLATE.get());
                        pOutput.accept(ModBlocks.CERVALITE_BUTTON.get());

                        pOutput.accept(ModBlocks.CERVALITE_FENCE.get());
                        pOutput.accept(ModBlocks.CERVALITE_FENCE_GATE.get());
                        pOutput.accept(ModBlocks.CERVALITE_WALL.get());

                        pOutput.accept(ModBlocks.CERVALITE_DOOR.get());
                        pOutput.accept(ModBlocks.CERVALITE_TRAPDOOR.get());

                        pOutput.accept(ModBlocks.CERVALITE_LAMP.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

}
