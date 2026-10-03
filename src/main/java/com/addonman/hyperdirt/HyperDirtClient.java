package com.addonman.hyperdirt;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(
        modid = HyperDirt.MOD_ID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.GAME
)
public final class HyperDirtClient {

    private static long flashStart = -1L;

    private HyperDirtClient() {}

    public static void triggerHitFlash() {
        flashStart = System.currentTimeMillis();
    }

    @SubscribeEvent
    public static void renderFlash(RenderGuiEvent.Post event) {

        if (flashStart < 0L) {
            return;
        }

        long elapsed = System.currentTimeMillis() - flashStart;

        if (elapsed >= 500L) {
            flashStart = -1L;
            return;
        }

        float progress = elapsed / 500.0F;

        // Sharp impact -> smooth fade.
        float fade = 1.0F - progress;
        float alpha = 0.58F * fade * fade;

        int a = Math.max(
                0,
                Math.min(255, (int)(alpha * 255.0F))
        );

        if (a <= 0) {
            return;
        }

        GuiGraphics gui = event.getGuiGraphics();

        int w = gui.guiWidth();
        int h = gui.guiHeight();

        int edge = Math.max(
                30,
                Math.min(120, Math.min(w, h) / 6)
        );

        int red = (a << 24) | 0xFF0000;
        int clear = 0x00FF0000;

        // Top
        gui.fillGradient(
                0, 0,
                w, edge,
                red,
                clear
        );

        // Bottom
        gui.fillGradient(
                0, h - edge,
                w, h,
                clear,
                red
        );

        // Left
        gui.fillGradient(
                0, edge,
                edge, h - edge,
                red,
                clear
        );

        // Right
        gui.fillGradient(
                w - edge, edge,
                w, h - edge,
                clear,
                red
        );
    }
}
