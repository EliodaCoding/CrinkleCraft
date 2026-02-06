package net.eli.crinklecraft.menu;

import net.eli.crinklecraft.CrinkleCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, CrinkleCraft.MOD_ID);

    public static final RegistryObject<MenuType<DiaperSlotMenu>> DIAPER_SLOT =
            MENUS.register("diaper_slot", () -> IForgeMenuType.create((id, inv, buf) -> new DiaperSlotMenu(id, inv)));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
