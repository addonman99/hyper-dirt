package com.addonman.hyperdirt;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HyperSandRenderer extends EntityRenderer<HyperSandProjectile> {

    private final ItemRenderer itemRenderer;

    public HyperSandRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            HyperSandProjectile entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        ItemStack stack = entity.getItem();

        poseStack.pushPose();

        float scale = 0.8F;
        poseStack.scale(scale, scale, scale);

        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();

        super.render(
                entity,
                entityYaw,
                partialTick,
                poseStack,
                buffer,
                packedLight
        );
    }

    @Override
    public ResourceLocation getTextureLocation(HyperSandProjectile entity) {
        return ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
    }
}
