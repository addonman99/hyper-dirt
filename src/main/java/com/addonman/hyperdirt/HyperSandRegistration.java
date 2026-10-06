package com.addonman.hyperdirt;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HyperSandRegistration {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(HyperDirt.MOD_ID);

    public static final DeferredItem<HyperSandItem> HYPER_SAND =
            ITEMS.registerItem(
                    "hyper_sand",
                    HyperSandItem::new,
                    new Item.Properties()
                            .stacksTo(1)
                            .durability(5000)
            );

    private HyperSandRegistration() {}
}
