package com.addonman.hyperdirt;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = HyperDirt.MOD_ID,
        bus = EventBusSubscriber.Bus.GAME
)
public final class HyperDirtEvents {

    private static final String DASH_ACTIVE =
            "HyperDirtDash";

    private static final String DASH_HIT =
            "HyperDirtDashHit";

    private static final String DASH_TICKS =
            "HyperDirtDashTicks";

    private static final double EXPLOSION_RADIUS =
            4.0D;

    private static final float EXPLOSION_DAMAGE =
            28.0F;

    private HyperDirtEvents() {}

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {

        Player player =
                event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        if (!player.getPersistentData()
                .getBoolean(DASH_ACTIVE)) {
            return;
        }

        int ticks =
                player.getPersistentData()
                        .getInt(DASH_TICKS);

        if (ticks <= 0) {
            stopDash(player);
            return;
        }

        player.getPersistentData()
                .putInt(DASH_TICKS, ticks - 1);

        if (player.getPersistentData()
                .getBoolean(DASH_HIT)) {
            return;
        }

        AABB box =
                player.getBoundingBox()
                        .inflate(0.9D);

        for (Entity entity :
                player.level().getEntities(
                        player,
                        box,
                        entity ->
                                entity instanceof LivingEntity
                                && entity.isAlive()
                                && entity != player
                )) {

            LivingEntity target =
                    (LivingEntity) entity;

            detonate(player, target);

            player.getPersistentData()
                    .putBoolean(DASH_HIT, true);

            break;
        }
    }

    private static void detonate(
            Player attacker,
            LivingEntity impact
    ) {

        ServerLevel level =
                (ServerLevel) attacker.level();

        Vec3 pos =
                impact.position();

        // TNT-style visual.
        level.sendParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                pos.x,
                pos.y + 0.35D,
                pos.z,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );

        // Riptide-style impact sound.
        level.playSound(
                null,
                pos.x,
                pos.y,
                pos.z,
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS,
                1.9F,
                0.9F
        );

        AABB damageBox =
                new AABB(
                        pos.x - EXPLOSION_RADIUS,
                        pos.y - EXPLOSION_RADIUS,
                        pos.z - EXPLOSION_RADIUS,
                        pos.x + EXPLOSION_RADIUS,
                        pos.y + EXPLOSION_RADIUS,
                        pos.z + EXPLOSION_RADIUS
                );

        // IMPORTANT:
        // attacker is explicitly excluded.
        for (Entity entity :
                level.getEntities(
                        attacker,
                        damageBox,
                        entity ->
                                entity instanceof LivingEntity
                                && entity.isAlive()
                                && entity != attacker
                )) {

            LivingEntity target =
                    (LivingEntity) entity;

            double distance =
                    target.distanceTo(impact);

            double scale =
                    Math.max(
                            0.15D,
                            1.0D -
                                    Math.min(
                                            distance /
                                                    EXPLOSION_RADIUS,
                                            1.0D
                                    )
                    );

            float damage =
                    (float)
                            (EXPLOSION_DAMAGE * scale);

            target.hurt(
                    attacker.damageSources()
                            .explosion(
                                    attacker,
                                    attacker
                            ),
                    damage
            );

            Vec3 direction =
                    target.position()
                            .subtract(pos);

            if (direction.lengthSqr() >
                    0.0001D) {

                direction =
                        direction.normalize();
            } else {
                direction =
                        new Vec3(0, 0, 0);
            }

            target.push(
                    direction.x * 1.7D * scale,
                    0.8D * scale,
                    direction.z * 1.7D * scale
            );

            target.hurtMarked = true;
        }

        if (attacker instanceof ServerPlayer serverPlayer) {
            HyperDirtNetwork.flash(serverPlayer);
        }
    }

    public static void beginDash(Player player) {

        player.getPersistentData()
                .putBoolean(DASH_ACTIVE, true);

        player.getPersistentData()
                .putBoolean(DASH_HIT, false);

        // 12 ticks of collision detection.
        player.getPersistentData()
                .putInt(DASH_TICKS, 12);
    }

    private static void stopDash(Player player) {

        player.getPersistentData()
                .putBoolean(DASH_ACTIVE, false);

        player.getPersistentData()
                .putBoolean(DASH_HIT, false);

        player.getPersistentData()
                .putInt(DASH_TICKS, 0);
    }
}
