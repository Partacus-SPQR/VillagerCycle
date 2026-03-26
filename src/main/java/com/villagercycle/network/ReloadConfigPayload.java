package com.villagercycle.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//? } else {
/*import net.minecraft.resources.ResourceLocation;*/
//? }

public record ReloadConfigPayload(boolean allowWanderingTraders, int wanderingTraderCycleLimit, int villagerCycleLimit) implements CustomPacketPayload {
    //? if >=1.21.11 {
    public static final CustomPacketPayload.Type<ReloadConfigPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("villagercycle:reload_config"));
    //? } else {
    /*public static final CustomPacketPayload.Type<ReloadConfigPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.parse("villagercycle:reload_config"));*/
    //? }

    public static final StreamCodec<RegistryFriendlyByteBuf, ReloadConfigPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public ReloadConfigPayload decode(RegistryFriendlyByteBuf buf) {
                    return new ReloadConfigPayload(buf.readBoolean(), buf.readInt(), buf.readInt());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ReloadConfigPayload payload) {
                    buf.writeBoolean(payload.allowWanderingTraders());
                    buf.writeInt(payload.wanderingTraderCycleLimit());
                    buf.writeInt(payload.villagerCycleLimit());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}