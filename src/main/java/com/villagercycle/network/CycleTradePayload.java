package com.villagercycle.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//? } else {
/*import net.minecraft.resources.ResourceLocation;*/
//? }

public record CycleTradePayload(boolean showVillagerSuccessMessage, boolean showWanderingTraderSuccessMessage) implements CustomPacketPayload {
    //? if >=1.21.11 {
    public static final CustomPacketPayload.Type<CycleTradePayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("villagercycle:cycle_trade"));
    //? } else {
    /*public static final CustomPacketPayload.Type<CycleTradePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.parse("villagercycle:cycle_trade"));*/
    //? }

    public static final StreamCodec<RegistryFriendlyByteBuf, CycleTradePayload> CODEC =
            new StreamCodec<>() {
                @Override
                public CycleTradePayload decode(RegistryFriendlyByteBuf buf) {
                    return new CycleTradePayload(buf.readBoolean(), buf.readBoolean());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, CycleTradePayload payload) {
                    buf.writeBoolean(payload.showVillagerSuccessMessage());
                    buf.writeBoolean(payload.showWanderingTraderSuccessMessage());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}