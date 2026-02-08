package net.eli.crinklecraft.item;

import net.eli.crinklecraft.CrinkleCraft;
import net.eli.crinklecraft.item.custom.ClothDiaperItem;
import net.eli.crinklecraft.item.custom.MagicDiaperItem;
import net.eli.crinklecraft.item.custom.MittensItem;
import net.eli.crinklecraft.item.custom.OnesieItem;
import net.eli.crinklecraft.item.custom.TrainingPantsItem;
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
    public static final RegistryObject<Item> STUFFIE_BROWN_BEAR = ItemRegistryHelper.registerStuffie(ITEMS, "stuffie_brown_bear");
    public static final RegistryObject<Item> STUFFIE_BLUE_SHARK = ItemRegistryHelper.registerStuffie(ITEMS, "stuffie_blue_shark");
    public static final RegistryObject<Item> PACIFIER = ItemRegistryHelper.registerPacifier(ITEMS, "pacifier");
    public static final RegistryObject<Item> POTTY_CHART = ITEMS.register("potty_chart",
            () -> new net.eli.crinklecraft.item.custom.PottyChartItem(new Item.Properties()));
    public static final RegistryObject<Item> MITTENS = ITEMS.register("mittens",
            () -> new MittensItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CLOTH_DIAPER = ITEMS.register("cloth_diaper",
            () -> new ClothDiaperItem(new Item.Properties()));
    public static final RegistryObject<Item> TRAINING_PANTS = ITEMS.register("training_pants",
            () -> new TrainingPantsItem(new Item.Properties()));
    public static final RegistryObject<Item> MAGIC_DIAPER = ITEMS.register("magic_diaper",
            () -> new MagicDiaperItem(new Item.Properties()));
    public static final RegistryObject<Item> ONESIE = ITEMS.register("onesie",
            () -> new OnesieItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
