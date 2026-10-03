package com.addonman.hyperdirt;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = HyperDirt.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class HyperDirtNetwork {
    public record FX(int kind) implements CustomPacketPayload {
        public static final Type<FX> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(HyperDirt.MOD_ID, "fx"));

        public static final StreamCodec<RegistryFriendlyByteBuf, FX> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, FX::kind, FX::new);

        @Override
        public Type<FX> type() {
            return TYPE;
        }
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent e) {
        e.registrar("1").playToClient(TYPE(), STREAM(), (payload, ctx) -> {
            try {
                Class<?> c = Class.forName("com.addonman.hyperdirt.HyperDirtClient");
                c.getMethod("receive", int.class).invoke(null, payload.kind());
            } catch (Throwable ignored) {}
        });
    }

    private static CustomPacketPayload.Type<FX> TYPE() { return FX.TYPE; }
    private static StreamCodec<RegistryFriendlyByteBuf, FX> STREAM() { return FX.STREAM_CODEC; }

    public static void send(ServerPlayer player, int kind) {
        PacketDistributor.sendToPlayer(player, new FX(kind));
    }

    private HyperDirtNetwork() {}
}
