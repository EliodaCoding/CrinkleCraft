package net.eli.crinklecraft;

import com.mojang.logging.LogUtils;
import net.eli.crinklecraft.component.ModDataComponentTypes;
import net.eli.crinklecraft.effect.ModEffects;
import net.eli.crinklecraft.item.ModCreativeModTabs;
import net.eli.crinklecraft.item.ModItems;
import net.eli.crinklecraft.sound.ModSounds;
import net.eli.crinklecraft.util.ModItemProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CrinkleCraft.MOD_ID)
public class CrinkleCraft {
    public static final String MOD_ID = "crinklecraft";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CrinkleCraft()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        ModCreativeModTabs.register(modEventBus);

        ModItems.register(modEventBus);
        net.eli.crinklecraft.blocks.ModBlocks.register(modEventBus);

        ModDataComponentTypes.register(modEventBus);
        ModSounds.register(modEventBus);
        ModEffects.register(modEventBus);
        net.eli.crinklecraft.menu.ModMenuTypes.register(modEventBus);
        // HUD + networking: Forge 1.21 API differs - TODO add PottyHudOverlay when RegisterGuiLayersEvent/SimpleChannel available

        modEventBus.addListener(this::addCreative);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void addCreative(net.minecraftforge.event.BuildCreativeModeTabContentsEvent event) {
        // Add items to vanilla tabs if desired
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("CrinkleCraft loaded");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static class ClientModEvents
    {
        public static final net.minecraft.client.KeyMapping HOLD_IT = new net.minecraft.client.KeyMapping(
                "key.crinklecraft.hold_it",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                org.lwjgl.glfw.GLFW.GLFW_KEY_H,
                "key.categories.crinklecraft");
        public static final net.minecraft.client.KeyMapping CHOSE_DIAPER = new net.minecraft.client.KeyMapping(
                "key.crinklecraft.chose_diaper",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                org.lwjgl.glfw.GLFW.GLFW_KEY_G,
                "key.categories.crinklecraft");
        public static final net.minecraft.client.KeyMapping CRINKLECRAFT_SLOTS = new net.minecraft.client.KeyMapping(
                "key.crinklecraft.slots",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
                org.lwjgl.glfw.GLFW.GLFW_KEY_B,
                "key.categories.crinklecraft");

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ModItemProperties.addCustomItemProperties();
            event.enqueueWork(() -> net.minecraft.client.gui.screens.MenuScreens.register(
                    net.eli.crinklecraft.menu.ModMenuTypes.CRINKLECRAFT_SLOTS.get(),
                    net.eli.crinklecraft.client.CrinkleCraftSlotsScreen::new));
        }

        @SubscribeEvent
        public static void registerKeyMappings(net.minecraftforge.client.event.RegisterKeyMappingsEvent event) {
            event.register(HOLD_IT);
            event.register(CHOSE_DIAPER);
            event.register(CRINKLECRAFT_SLOTS);
        }
    }
}
