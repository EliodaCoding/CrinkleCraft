package net.eli.crinklecraft.item;

import net.eli.crinklecraft.CrinkleCraft;
import net.eli.crinklecraft.item.custom.BabyBottleItem;
import net.eli.crinklecraft.item.custom.DiaperItem;
import net.eli.crinklecraft.item.custom.PacifierItem;
import net.eli.crinklecraft.item.custom.SippieCupItem;
import net.eli.crinklecraft.item.custom.StuffieItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Helper to register CrinkleCraft items with a single line.
 * Use these methods in ModItems to add diapers, bottles, etc. without duplicating boilerplate.
 * Items are registered here and added to creative tab via ModCreativeModTabs.
 */
public class ItemRegistryHelper {

    /**
     * Registers a diaper variant. Each use (absorbed accident or voluntary) consumes one charge.
     * @param items The DeferredRegister from ModItems
     * @param id Registry id (e.g. "diaper", "pullups")
     * @param maxUses Max charges (e.g. 3 for diaper, 1 for pullups)
     * @return RegistryObject for creative tab and recipes
     */
    public static RegistryObject<Item> registerDiaper(DeferredRegister<Item> items, String id, int maxUses) {
        return items.register(id, () -> new DiaperItem(new Item.Properties(), maxUses));
    }

    /**
     * Registers a baby bottle. Holds milk (clears effects) or up to 3 potions. Milk from bucket.
     * @param items The DeferredRegister from ModItems
     * @param id Registry id (e.g. "baby_bottle")
     * @return RegistryObject for creative tab and recipes
     */
    public static RegistryObject<Item> registerBabyBottle(DeferredRegister<Item> items, String id) {
        return items.register(id, () -> new BabyBottleItem(new Item.Properties()));
    }

    /**
     * Registers a sippie cup. Holds milk, apple juice (apple), or up to 2 potions. Apple juice clears effects.
     * @param items The DeferredRegister from ModItems
     * @param id Registry id (e.g. "sippie_cup")
     * @return RegistryObject for creative tab and recipes
     */
    public static RegistryObject<Item> registerSippieCup(DeferredRegister<Item> items, String id) {
        return items.register(id, () -> new SippieCupItem(new Item.Properties()));
    }

    /**
     * Registers a stuffie. When held in main or off hand, gives Comforted effect (via ModEvents).
     * @param items The DeferredRegister from ModItems
     * @param id Registry id (e.g. "stuffie", "teddy")
     * @return RegistryObject for creative tab and recipes
     */
    public static RegistryObject<Item> registerStuffie(DeferredRegister<Item> items, String id) {
        return items.register(id, () -> new StuffieItem(new Item.Properties()));
    }

    /**
     * Registers a pacifier. When in offhand, chat is transformed to baby talk (via ChatEvents).
     * @param items The DeferredRegister from ModItems
     * @param id Registry id (e.g. "pacifier")
     * @return RegistryObject for creative tab and recipes
     */
    public static RegistryObject<Item> registerPacifier(DeferredRegister<Item> items, String id) {
        return items.register(id, () -> new PacifierItem(new Item.Properties()));
    }
}
