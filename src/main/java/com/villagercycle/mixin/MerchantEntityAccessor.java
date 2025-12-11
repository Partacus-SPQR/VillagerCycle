package com.villagercycle.mixin;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MerchantEntity.class)
public interface MerchantEntityAccessor {
	@Invoker("fillRecipes")
	void invokeFillRecipes(ServerWorld world);
}
