package com.addonman.hyperdirt;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(
        modid = HyperDirt.MOD_ID,
        bus = EventBusSubscriber.Bus.GAME
)
public final class HyperDirtCombatEvents {

    private HyperDirtCombatEvents() {}

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();

        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();

        if (!weapon.is(HyperDirt.HYPER_DIRT.get())) {
            return;
        }

        HyperDirtNetwork.flash(player);
    }
}
