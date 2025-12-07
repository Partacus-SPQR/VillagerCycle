package com.villagercycle.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReloadConfigPayload(boolean allowWanderingTraders, int wanderingTraderCycleLimit, int villagerCycleLimit) implements CustomPayload {
	
	public static final CustomPayload.Id<ReloadConfigPayload> ID = 
		new CustomPayload.Id<>(Identifier.of("villagercycle", "reload_config"));
	
	public static final PacketCodec<RegistryByteBuf, ReloadConfigPayload> CODEC = 
		new PacketCodec<>() {
			@Override
			public ReloadConfigPayload decode(RegistryByteBuf buf) {
				return new ReloadConfigPayload(buf.readBoolean(), buf.readInt(), buf.readInt());
			}
			
			@Override
			public void encode(RegistryByteBuf buf, ReloadConfigPayload payload) {
				buf.writeBoolean(payload.allowWanderingTraders());
				buf.writeInt(payload.wanderingTraderCycleLimit());
				buf.writeInt(payload.villagerCycleLimit());
			}
		};
	
	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
