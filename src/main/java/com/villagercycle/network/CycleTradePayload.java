package com.villagercycle.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CycleTradePayload() implements CustomPayload {
	
	public static final CustomPayload.Id<CycleTradePayload> ID = 
		new CustomPayload.Id<>(Identifier.of("villagercycle", "cycle_trade"));
	
	public static final PacketCodec<RegistryByteBuf, CycleTradePayload> CODEC = 
		PacketCodec.unit(new CycleTradePayload());
	
	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
