package com.addonman.hyperdirt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = HyperDirt.MOD_ID)
public final class HyperDirtFeatures {
    private static final ResourceLocation ITEM_ID =
            ResourceLocation.fromNamespaceAndPath(HyperDirt.MOD_ID, "hyper_dirt");
    private static final Map<UUID, Integer> DASH = new HashMap<>();

    private static boolean hyper(ItemStack s) {
        return !s.isEmpty() && ITEM_ID.equals(
                net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem()));
    }

    private static void block(PlayerInteractEvent e) {
        if (!hyper(e.getItemStack())) return;
        if (e instanceof PlayerInteractEvent.RightClickItem x)
            x.setCancellationResult(InteractionResult.SUCCESS);
        if (e instanceof PlayerInteractEvent.RightClickBlock x)
            x.setCancellationResult(InteractionResult.SUCCESS);
        e.setCanceled(true);

        if (!e.getLevel().isClientSide())
            dash(e.getEntity(), e.getItemStack());
    }

    @SubscribeEvent
    public static void item(PlayerInteractEvent.RightClickItem e) { block(e); }

    @SubscribeEvent
    public static void block(PlayerInteractEvent.RightClickBlock e) { block(e); }

    @SubscribeEvent
    public static void entity(PlayerInteractEvent.EntityInteract e) { block(e); }

    @SubscribeEvent
    public static void entitySpecific(PlayerInteractEvent.EntityInteractSpecific e) { block(e); }

    private static void dash(Player p, ItemStack stack) {
        if (!(p.level() instanceof ServerLevel level)) return;

        p.setDeltaMovement(p.getLookAngle().scale(2.45D));
        p.hasImpulse = true;
        p.hurtMarked = true;
        level.playSound(null, p.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_3,
                SoundSource.PLAYERS, 1.2F, 1.0F);

        DASH.put(p.getUUID(), 6);
        p.getCooldowns().addCooldown(stack.getItem(), 8);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post e) {
        Player p = e.getEntity();
        if (p.level().isClientSide()) return;

        Integer ticks = DASH.get(p.getUUID());
        if (ticks == null) return;
        if (ticks <= 0) {
            DASH.remove(p.getUUID());
            return;
        }

        ServerLevel level = (ServerLevel)p.level();
        AABB box = p.getBoundingBox().expandTowards(p.getDeltaMovement()).inflate(0.8D);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box,
                x -> x != p && x.isAlive())) {
            explode(level, p, target.position());
            DASH.remove(p.getUUID());
            break;
        }

        DASH.put(p.getUUID(), ticks - 1);
    }

    private static void explode(ServerLevel level, Player attacker, Vec3 pos) {
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.FLAME,
                pos.x, pos.y, pos.z, 35, 1.1, 1.1, 1.1, 0.08);
        level.sendParticles(ParticleTypes.CLOUD,
                pos.x, pos.y, pos.z, 24, 1.0, 1.0, 1.0, 0.08);

        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS, 1.7F, 0.9F);

        AABB area = new AABB(pos, pos).inflate(4.0D);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                x -> x != attacker && x.isAlive())) {
            double d = Math.sqrt(target.distanceToSqr(pos));
            float damage = (float)Math.max(8.0D, 32.0D * (1.0D - d / 4.0D));

            if (target.hurt(level.damageSources().playerAttack(attacker), damage)) {
                Vec3 push = target.position().subtract(pos).normalize()
                        .scale(Math.max(0.3D, 1.0D - d / 5.0D))
                        .add(0, 0.45D, 0);
                target.push(push.x, push.y, push.z);
                target.hurtMarked = true;
            }
        }
    }

    @SubscribeEvent
    public static void hit(LivingDamageEvent.Post e) {
        if (e.getSource().getEntity() instanceof ServerPlayer p && hyper(p.getMainHandItem()))
            HyperDirtNetwork.send(p, 1);
    }

    @SubscribeEvent
    public static void kill(LivingDeathEvent e) {
        if (e.getSource().getEntity() instanceof ServerPlayer p && hyper(p.getMainHandItem())
                && HyperDirtConfig.IMPACT_FRAMES.get())
            HyperDirtNetwork.send(p, 2);
    }

    private HyperDirtFeatures() {}
}
