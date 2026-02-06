package net.eli.crinklecraft.item;

import net.eli.crinklecraft.CrinkleCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CrinkleCraft.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.DIAPER.get()))
                    .title(Component.translatable("creativetab.crinklecraft.main"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.DIAPER.get());
                        pOutput.accept(ModItems.PULLUPS.get());
                        pOutput.accept(ModItems.BABY_BOTTLE.get());
                        pOutput.accept(ModItems.SIPPIE_CUP.get());
                        pOutput.accept(ModItems.STUFFIE_BROWN_BEAR.get());
                        pOutput.accept(ModItems.STUFFIE_BLUE_SHARK.get());
                        pOutput.accept(ModItems.PACIFIER.get());
                        pOutput.accept(ModItems.POTTY_CHART.get());
                        pOutput.accept(ModItems.MITTENS.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
