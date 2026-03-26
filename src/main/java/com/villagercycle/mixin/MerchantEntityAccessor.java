package com.villagercycle.mixin;

//? if >=1.21.11 {
import net.minecraft.world.entity.npc.villager.AbstractVillager;
//?} else {
/*import net.minecraft.world.entity.npc.AbstractVillager;*/
//?}
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractVillager.class)
public interface MerchantEntityAccessor {
        //? if >=26.1 {
        @Invoker(value = "updateTrades", remap = false)
        //?} else {
        /*@Invoker("updateTrades")*/
        //?}
        void invokeUpdateTrades(ServerLevel world);
}
