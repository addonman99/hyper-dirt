package com.addonman.hyperdirt;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = HyperDirt.MOD_ID)
public final class HyperSandRegistration {

    public static final Item HYPER_SAND = new HyperSandItem();

    public static final EntityType<HyperSandProjectile> HYPER_SAND_PROJECTILE =
            EntityType.Builder.of(
                    HyperSandProjectile::new,
                    MobCategory.MISC
            )
            .sized(0.25F, 0.25F)
            .clientTrackingRange(8)
            .updateInterval(1)
            .build("hyper_sand_projectile");

    private HyperSandRegistration() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(
                Registries.ITEM,
                helper -> helper.register(
                        ResourceLocation.fromNamespaceAndPath(
                                HyperDirt.MOD_ID,
                                "hyper_sand"
                        ),
                        HYPER_SAND
                )
        );

        event.register(
                Registries.ENTITY_TYPE,
                helper -> helper.register(
                        ResourceLocation.fromNamespaceAndPath(
                                HyperDirt.MOD_ID,
                                "hyper_sand_projectile"
                        ),
                        HYPER_SAND_PROJECTILE
                )
        );
    }

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(CreativeModeTabs.COMBAT)) {
            return;
        }

        ItemStack stack = new ItemStack(HYPER_SAND);

        var lookup = event.getParameters()
                .holders()
                .lookupOrThrow(Registries.ENCHANTMENT);

        stack.enchant(
                lookup.getOrThrow(Enchantments.POWER),
                5
        );

        stack.enchant(
                lookup.getOrThrow(Enchantments.MENDING),
                1
        );

        event.accept(stack);
    }
}
