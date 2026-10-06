package com.addonman.hyperdirt;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

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

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(
                    net.minecraft.core.registries.Registries.ENTITY_TYPE,
                    HyperDirt.MOD_ID
            );

    public static final DeferredHolder<EntityType<?>, EntityType<HyperSandProjectile>>
            HYPER_SAND_PROJECTILE =
            ENTITIES.register(
                    "hyper_sand_projectile",
                    () -> EntityType.Builder
                            .of(HyperSandProjectile::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("hyperdirt:hyper_sand_projectile")
            );

    private HyperSandRegistration() {}
}
