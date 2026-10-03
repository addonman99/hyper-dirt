package com.addonman.hyperdirt;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.core.registries.BuiltInRegistries;

@EventBusSubscriber(
    modid = HyperDirt.MOD_ID,
    bus = EventBusSubscriber.Bus.GAME
)
public final class HyperDirtLaunch {

    private static final int COOLDOWN_TICKS = 4;
    private static final double UPWARD_VELOCITY = 6.0;

    private HyperDirtLaunch() {}

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) return;

        var player = event.getEntity();
        var stack = event.getItemStack();

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (!ResourceLocation.fromNamespaceAndPath(HyperDirt.MOD_ID, "hyper_dirt").equals(id)) {
            return;
        }

        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return;
        }

        player.push(0.0, UPWARD_VELOCITY, 0.0);
        player.hasImpulse = true;
        player.hurtMarked = true;

        player.getCooldowns().addCooldown(
            stack.getItem(),
            COOLDOWN_TICKS
        );

        event.setCanceled(true);
    }
}
