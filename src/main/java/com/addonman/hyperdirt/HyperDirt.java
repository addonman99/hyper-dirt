package com.addonman.hyperdirt;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
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

    /*
     * MaceItem gives us the vanilla mace combat path:
     * falling smash, Density, Breach and Wind Burst can therefore
     * operate through Minecraft's existing mechanics.
     *
     * Sword attributes give the item a real melee attack instead
     * of leaving it with zero useful attack attributes.
     */
    public static final DeferredItem<Item> HYPER_DIRT =
            ITEMS.register("hyper_dirt", () ->
                    new HyperDirtItem(
                            new Item.Properties()
                                    .durability(5000)
                                    .enchantable(30)
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
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            ItemStack stack = HYPER_DIRT.get().getDefaultInstance();

            /*
             * ItemStack.enchant() writes directly to the enchantment
             * data component. This deliberately bypasses the normal
             * "can these enchantments coexist?" enchanting-table/anvil
             * selection process.
             */
            stack.enchant(
                    BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(Enchantments.SHARPNESS),
                    5
            );

            stack.enchant(
                    BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(Enchantments.SMITE),
                    5
            );

            stack.enchant(
                    BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(Enchantments.DENSITY),
                    5
            );

            stack.enchant(
                    BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(Enchantments.BREACH),
                    3
            );

            stack.enchant(
                    BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(Enchantments.WIND_BURST),
                    2
            );

            stack.enchant(
                    BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(Enchantments.RIPTIDE),
                    3
            );

            stack.enchant(
                    BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(Enchantments.MENDING),
                    1
            );

            event.accept(stack);
        }
    }

    public static class HyperDirtItem extends net.minecraft.world.item.MaceItem {

        public HyperDirtItem(Item.Properties properties) {
            super(properties);
        }

        /*
         * Riptide is normally tied to the TridentItem use path.
         *
         * This keeps the vanilla Riptide enchantment itself and its
         * vanilla enchantment-effect calculation, but removes the
         * water/rain requirement.
         */
        @Override
        public InteractionResultHolder<ItemStack> use(
                Level level,
                Player player,
                InteractionHand hand
        ) {
            ItemStack stack = player.getItemInHand(hand);

            float riptideStrength =
                    EnchantmentHelper.getTridentSpinAttackStrength(stack, player);

            if (riptideStrength <= 0.0F) {
                return InteractionResultHolder.pass(stack);
            }

            if (!level.isClientSide) {
                var look = player.getViewVector(1.0F);

                /*
                 * Vanilla-style Riptide movement.
                 * The important difference is that there is deliberately
                 * no isInWaterOrRain() check.
                 */
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
