package com.addonman.hyperdirt;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(
        modid = HyperDirt.MOD_ID,
        value = Dist.CLIENT
)
public final class HyperSandClient {

    private HyperSandClient() {
    }

    @SubscribeEvent
    public static void registerRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {
        event.registerEntityRenderer(
                HyperSandRegistration.HYPER_SAND_PROJECTILE,
                HyperSandRenderer::new
        );
    }
}
