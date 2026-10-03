package com.addonman.hyperdirt;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import io.netty.buffer.Unpooled;

@EventBusSubscriber(
        modid = HyperDirt.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD
)
public final class HyperDirtNetwork {

    private HyperDirtNetwork() {}

    public record HitFlash() implements CustomPacketPayload {

        public static final Type<HitFlash> TYPE =
                new Type<>(
                        ResourceLocation.fromNamespaceAndPath(
                                HyperDirt.MOD_ID,
                                "hit_flash"
                        )
                );

        public static final net.minecraft.network.codec.StreamCodec<
                RegistryFriendlyByteBuf,
                HitFlash
                > STREAM_CODEC =
                net.minecraft.network.codec.StreamCodec.unit(
                        new HitFlash()
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {

        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                HitFlash.TYPE,
                HitFlash.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(
                                HyperDirtClient::triggerHitFlash
                        )
        );
    }

    public static void flash(
            net.minecraft.server.level.ServerPlayer player
    ) {
        PacketDistributor.sendToPlayer(
                player,
                new HitFlash()
        );
    }
}
