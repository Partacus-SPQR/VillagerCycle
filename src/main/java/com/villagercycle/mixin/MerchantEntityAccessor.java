package com.villagercycle.mixin;

import net.minecraft.entity.passive.MerchantEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MerchantEntity.class)
public interface MerchantEntityAccessor {
	@Invoker("fillRecipes")
	void invokeFillRecipes();
}
