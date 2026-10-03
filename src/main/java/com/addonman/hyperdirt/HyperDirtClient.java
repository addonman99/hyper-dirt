package com.addonman.hyperdirt;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = HyperDirt.MOD_ID, value = Dist.CLIENT)
public final class HyperDirtClient {
    private static long redUntil;
    private static long impactStart;
    private static String effect = "";
    private static net.minecraft.client.gui.screens.Screen oldScreen;

    public static void receive(int kind) {
        Minecraft mc = Minecraft.getInstance();
        long now = System.nanoTime();

        if (kind == 1) {
            redUntil = now + 500_000_000L;
            return;
        }

        impactStart = now;
        effect = "";
        oldScreen = mc.screen;

        mc.gameRenderer.loadEffect(
                ResourceLocation.fromNamespaceAndPath(HyperDirt.MOD_ID,
                        "shaders/post/impact_gray.json"));
        mc.pauseGame(true);
    }

    @SubscribeEvent
    public static void frame(RenderFrameEvent.Pre e) {
        if (impactStart == 0) return;

        Minecraft mc = Minecraft.getInstance();
        long dt = System.nanoTime() - impactStart;

        if (dt < 100_000_000L) {
            if (!"gray".equals(effect)) {
                effect = "gray";
                mc.gameRenderer.loadEffect(
                        ResourceLocation.fromNamespaceAndPath(HyperDirt.MOD_ID,
                                "shaders/post/impact_gray.json"));
            }
        } else if (dt < 200_000_000L) {
            if (!"invert".equals(effect)) {
                effect = "invert";
                mc.gameRenderer.loadEffect(
                        ResourceLocation.fromNamespaceAndPath(HyperDirt.MOD_ID,
                                "shaders/post/impact_invert.json"));
            }
        } else {
            mc.gameRenderer.shutdownEffect();
            if (oldScreen == null) mc.setScreen(null);
            else mc.setScreen(oldScreen);
            impactStart = 0;
            effect = "";
            oldScreen = null;
        }
    }

    @SubscribeEvent
    public static void gui(RenderGuiEvent.Post e) {
        long left = redUntil - System.nanoTime();
        if (left <= 0) return;

        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        float fade = Math.min(1F, left / 500_000_000F);

        for (int i = 0; i < 12; i++) {
            float edge = 1F - (i / 12F);
            int a = (int)(105F * fade * edge);
            int color = (a << 24) | 0xCC0000;
            int y1 = i * h / 12;
            int y2 = (i + 1) * h / 12;
            int x1 = i * w / 12;
            int x2 = (i + 1) * w / 12;

            e.getGuiGraphics().fill(0, y1, w, y2, color);
            e.getGuiGraphics().fill(0, h - y2, w, h - y1, color);
            e.getGuiGraphics().fill(x1, 0, x2, h, color);
            e.getGuiGraphics().fill(w - x2, 0, w - x1, h, color);
        }
    }

    private HyperDirtClient() {}
}
