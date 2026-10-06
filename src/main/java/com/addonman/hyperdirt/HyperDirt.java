package com.addonman.hyperdirt;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(HyperDirt.MOD_ID)
public class HyperDirt {

    public static final String MOD_ID = "hyperdirt";

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MOD_ID);

    public static final DeferredItem<Item> HYPER_DIRT =
            ITEMS.register("hyper_dirt", () ->
                    new HyperDirtItem(
                            new Item.Properties()
                                    .durability(5000)
                                    .attributes(
                                            SwordItem.createAttributes(
                                                    Tiers.NETHERITE,
                                                    6.0F,
                                                    -2.4F
                                            )
                                    )
                    )
            );

    public HyperDirt(IEventBus modEventBus) {
        HyperSandRegistration.ITEMS.register(modEventBus);

        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.COMBAT) {
            return;
        }

        ItemStack stack = HYPER_DIRT.get().getDefaultInstance();

        HolderLookup.RegistryLookup<Enchantment> enchantments =
                event.getParameters()
                        .holders()
                        .lookupOrThrow(Registries.ENCHANTMENT);

        add(stack, enchantments, Enchantments.SHARPNESS, 5);
        add(stack, enchantments, Enchantments.SMITE, 5);
        add(stack, enchantments, Enchantments.DENSITY, 5);
        add(stack, enchantments, Enchantments.BREACH, 3);
        add(stack, enchantments, Enchantments.WIND_BURST, 2);
        add(stack, enchantments, Enchantments.RIPTIDE, 3);
        add(stack, enchantments, Enchantments.MENDING, 1);

        event.accept(stack);
    }

    private static void add(
            ItemStack stack,
            HolderLookup.RegistryLookup<Enchantment> lookup,
            net.minecraft.resources.ResourceKey<Enchantment> key,
            int level
    ) {
        Holder.Reference<Enchantment> holder = lookup.getOrThrow(key);
        stack.enchant(holder, level);
    }

    public static class HyperDirtItem extends MaceItem {

        public HyperDirtItem(Item.Properties properties) {
            super(properties);
        }

        /*
         * Vanilla Riptide is normally handled by TridentItem.
         * This item deliberately uses the same enchantment but performs
         * the launch without requiring water or rain.
         */
        @Override
        public InteractionResultHolder<ItemStack> use(
                Level level,
                Player player,
                InteractionHand hand
        ) {
            ItemStack stack = player.getItemInHand(hand);

            float riptideStrength =
                    EnchantmentHelper.getTridentSpinAttackStrength(
                            stack,
                            player
                    );

            if (riptideStrength <= 0.0F) {
                return InteractionResultHolder.pass(stack);
            }

            if (!level.isClientSide) {
                var look = player.getViewVector(1.0F);

                double speed = 2.5D * riptideStrength;

                player.setDeltaMovement(
                        look.x * speed,
                        look.y * speed,
                        look.z * speed
                );

                player.startAutoSpinAttack(
                        20,
                        8.0F,
                        stack
                );

                stack.hurtAndBreak(
                        1,
                        player,
                        LivingEntity.getSlotForHand(hand)
                );
            }

            return InteractionResultHolder.sidedSuccess(
                    stack,
                    level.isClientSide
            );
        }
    }



}
