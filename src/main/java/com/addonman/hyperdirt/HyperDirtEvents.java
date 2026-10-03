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

    private static final String ACTIVE =
            "HyperDirtRiptide";

    private static final String HIT =
            "HyperDirtRiptideHit";

    private static final double RADIUS =
            4.0D;

    private static final float MAX_DAMAGE =
            28.0F;

    private HyperDirtEvents() {}

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {

        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        boolean active =
                player.getPersistentData()
                        .getBoolean(ACTIVE);

        if (!active) {
            return;
        }

        // Riptide has ended.
        if (!player.isAutoSpinAttack()) {
            player.getPersistentData()
                    .putBoolean(ACTIVE, false);

            player.getPersistentData()
                    .putBoolean(HIT, false);

            return;
        }

        // Already hit something during this Riptide.
        if (player.getPersistentData()
                .getBoolean(HIT)) {
            return;
        }

        /*
         * Detect entities touching the player during
         * the Riptide spin.
         */
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
                    .putBoolean(HIT, true);

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

        // ----------------------------------------------------
        // TNT-STYLE VISUAL
        // ----------------------------------------------------

        level.sendParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                pos.x,
                pos.y + 0.4D,
                pos.z,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );

        level.playSound(
                null,
                pos.x,
                pos.y,
                pos.z,
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS,
                1.8F,
                0.85F
                        + level.random.nextFloat()
                        * 0.15F
        );

        // ----------------------------------------------------
        // ENTITY-ONLY DAMAGE
        // ----------------------------------------------------

        AABB damageBox =
                new AABB(
                        pos.x - RADIUS,
                        pos.y - RADIUS,
                        pos.z - RADIUS,
                        pos.x + RADIUS,
                        pos.y + RADIUS,
                        pos.z + RADIUS
                );

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
                            1.0D
                                    - Math.min(
                                            distance / RADIUS,
                                            1.0D
                                    )
                    );

            float damage =
                    (float)(MAX_DAMAGE * scale);

            /*
             * IMPORTANT:
             * attacker is excluded from the entity query,
             * so the custom explosion cannot hurt its owner.
             */
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

            if (direction.lengthSqr() > 0.0001D) {
                direction =
                        direction.normalize();
            } else {
                direction =
                        new Vec3(0, 0, 0);
            }

            target.push(
                    direction.x
                            * 1.6D
                            * scale,

                    0.75D * scale,

                    direction.z
                            * 1.6D
                            * scale
            );

            target.hurtMarked = true;
        }

        // ----------------------------------------------------
        // ATTACKER GETS THE RED HIT FLASH ONLY
        // ----------------------------------------------------

        if (attacker instanceof ServerPlayer serverPlayer) {
            HyperDirtNetwork.flash(serverPlayer);
        }
    }

    public static void beginRiptide(Player player) {

        player.getPersistentData()
                .putBoolean(ACTIVE, true);

        player.getPersistentData()
                .putBoolean(HIT, false);
    }
}
