package com.addonman.hyperdirt;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(
        modid = HyperDirt.MOD_ID,
        bus = EventBusSubscriber.Bus.GAME
)
public final class HyperDirtCombatEvents {

    private HyperDirtCombatEvents() {}

    private static boolean isHyperDirt(Player player) {
        return player.getMainHandItem()
                .getItem()
                .toString()
                .equals("hyperdirt:hyper_dirt");
    }

    @SubscribeEvent
    public static void onDamage(
            LivingDamageEvent.Post event
    ) {

        if (!(event.getSource().getEntity()
                instanceof ServerPlayer player)) {
            return;
        }

        if (!isHyperDirt(player)) {
            return;
        }

        HyperDirtNetwork.flash(player);
    }

    @SubscribeEvent
    public static void onDeath(
            LivingDeathEvent event
    ) {

        if (!(event.getSource().getEntity()
                instanceof ServerPlayer player)) {
            return;
        }

        if (!isHyperDirt(player)) {
            return;
        }

        HyperDirtNetwork.impact(player);
    }
}
