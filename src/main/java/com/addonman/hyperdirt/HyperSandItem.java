package com.addonman.hyperdirt;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HyperSandItem extends Item {

    private static final int COOLDOWN_TICKS = 4;

    public HyperSandItem(Item.Properties properties) {
        super(properties.durability(5000));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        if (!level.isClientSide()) {
            Vec3 look = player.getLookAngle();

            double x = player.getX() + look.x * 0.8;
            double y = player.getEyeY() + look.y * 0.8;
            double z = player.getZ() + look.z * 0.8;

            HyperSandProjectile projectile =
                    new HyperSandProjectile(
                            HyperSandRegistration.HYPER_SAND_PROJECTILE.get(),
                            level
                    );

            projectile.setPath(
                    new Vec3(x, y, z),
                    look
            );

            level.addFreshEntity(projectile);
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide()
        );
    }
}
