package com.villagercycle.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CycleTradePayload(boolean showVillagerSuccessMessage, boolean showWanderingTraderSuccessMessage) implements CustomPayload {
	
	public static final CustomPayload.Id<CycleTradePayload> ID = 
		new CustomPayload.Id<>(Identifier.of("villagercycle", "cycle_trade"));
	
	public static final PacketCodec<RegistryByteBuf, CycleTradePayload> CODEC = 
		new PacketCodec<>() {
			@Override
			public CycleTradePayload decode(RegistryByteBuf buf) {
				return new CycleTradePayload(buf.readBoolean(), buf.readBoolean());
			}
			
			@Override
			public void encode(RegistryByteBuf buf, CycleTradePayload payload) {
				buf.writeBoolean(payload.showVillagerSuccessMessage());
				buf.writeBoolean(payload.showWanderingTraderSuccessMessage());
			}
		};
	
	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
