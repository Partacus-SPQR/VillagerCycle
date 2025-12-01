package com.villagercycle.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReloadConfigPayload(boolean allowWanderingTraders) implements CustomPayload {
	
	public static final CustomPayload.Id<ReloadConfigPayload> ID = 
		new CustomPayload.Id<>(Identifier.of("villagercycle", "reload_config"));
	
	public static final PacketCodec<RegistryByteBuf, ReloadConfigPayload> CODEC = 
		PacketCodec.tuple(
			PacketCodecs.BOOLEAN, ReloadConfigPayload::allowWanderingTraders,
			ReloadConfigPayload::new
		);
	
	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
