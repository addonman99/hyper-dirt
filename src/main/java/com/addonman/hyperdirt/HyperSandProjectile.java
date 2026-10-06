package com.addonman.hyperdirt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HyperSandProjectile extends Entity {

    private static final double DISTANCE = 8.0;
    private static final double ARC_HEIGHT = 2.5;
    private static final int OUTBOUND_TICKS = 12;
    private static final int TOTAL_TICKS = 24;

    private final ItemStack item = new ItemStack(Items.SAND);

    private Vec3 start = Vec3.ZERO;
    private Vec3 direction = new Vec3(0, 0, 1);
    private int age;

    public HyperSandProjectile(
            EntityType<? extends HyperSandProjectile> type,
            Level level
    ) {
        super(type, level);
        setNoGravity(true);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setPath(Vec3 start, Vec3 look) {
        this.start = start;

        Vec3 horizontal = new Vec3(
                look.x,
                0.0,
                look.z
        );

        if (horizontal.lengthSqr() < 0.0001) {
            horizontal = new Vec3(0, 0, 1);
        }

        direction = horizontal.normalize();
        setPos(start);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            return;
        }

        age++;

        if (age > TOTAL_TICKS) {
            discard();
            return;
        }

        double t;

        if (age <= OUTBOUND_TICKS) {
            t = (double) age / OUTBOUND_TICKS;
        } else {
            t = (double) (TOTAL_TICKS - age) / OUTBOUND_TICKS;
        }

        double forward = DISTANCE * t;
        double height = Math.sin(Math.PI * t) * ARC_HEIGHT;

        setPos(
                start.x + direction.x * forward,
                start.y + height,
                start.z + direction.z * forward
        );
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
