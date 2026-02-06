package net.eli.crinklecraft.item;

import net.eli.crinklecraft.CrinkleCraft;
import net.eli.crinklecraft.item.custom.MittensItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CrinkleCraft.MOD_ID);

    // CrinkleCraft ABDL items (using helpers for diapers/bottles)
    public static final RegistryObject<Item> DIAPER = ItemRegistryHelper.registerDiaper(ITEMS, "diaper", 3);
    public static final RegistryObject<Item> PULLUPS = ItemRegistryHelper.registerDiaper(ITEMS, "pullups", 1);
    public static final RegistryObject<Item> BABY_BOTTLE = ItemRegistryHelper.registerBabyBottle(ITEMS, "baby_bottle");
    public static final RegistryObject<Item> SIPPIE_CUP = ItemRegistryHelper.registerSippieCup(ITEMS, "sippie_cup");
    public static final RegistryObject<Item> STUFFIE = ItemRegistryHelper.registerStuffie(ITEMS, "stuffie");
    public static final RegistryObject<Item> PACIFIER = ItemRegistryHelper.registerPacifier(ITEMS, "pacifier");
    public static final RegistryObject<Item> POTTY_CHART = ITEMS.register("potty_chart",
            () -> new net.eli.crinklecraft.item.custom.PottyChartItem(new Item.Properties()));
    public static final RegistryObject<Item> DIAPER_ARMOR = ITEMS.register("diaper_armor",
            () -> new net.eli.crinklecraft.item.custom.DiaperArmorItem(new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(15))));
    public static final RegistryObject<Item> MITTENS = ITEMS.register("mittens",
            () -> new MittensItem(ModArmorMaterials.MITTENS_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(8))));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
