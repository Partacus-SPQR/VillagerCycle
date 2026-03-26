package com.villagercycle.util;

//? if >=1.21.11 {
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
//?} else {
/*import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;*/
//?}
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffers;

public class VillagerTradeUtil {

public static boolean canCycleTrades(Entity entity) {
if (!(entity instanceof Villager villager)) {
return false;
}

VillagerData data = villager.getVillagerData();
Holder<VillagerProfession> profession = data.profession();
if (profession.is(VillagerProfession.NONE) ||
profession.is(VillagerProfession.NITWIT)) {
return false;
}

// Check if villager has been traded with
if (villager.getVillagerXp() > 0) {
return false;
}

// Check if any trades have been used
MerchantOffers offers = villager.getOffers();
if (offers != null) {
for (int i = 0; i < offers.size(); i++) {
if (offers.get(i).getUses() > 0) {
return false;
}
}
}

return true;
}

public static boolean cycleTrades(Villager villager, ServerPlayer player, boolean showSuccessMessage) {
if (!canCycleTrades(villager)) {
return false;
}

VillagerData villagerData = villager.getVillagerData();
Holder<VillagerProfession> currentProfession = villagerData.profession();

// The trick: temporarily set to NONE profession, then back to current
// This forces trade regeneration
Holder.Reference<VillagerProfession> noneEntry = BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE);
villager.setVillagerData(villagerData.withProfession(noneEntry));
villager.setVillagerData(villagerData.withProfession(currentProfession).withLevel(1));

// Trigger trade regeneration by clearing offers
villager.setOffers(null);

// Send success message if enabled (client controls this via packet)
if (showSuccessMessage) {
player.sendSystemMessage(
Component.literal("\u2705 Villager trades have been cycled!")
.withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
false
);
player.sendSystemMessage(
Component.literal("\uD83D\uDD04 The villager now has new Level 1 trades.")
.withStyle(ChatFormatting.YELLOW),
false
);
}

return true;
}

public static boolean cycleWanderingTraderTrades(AbstractVillager trader, ServerPlayer player, boolean showSuccessMessage) {
// Check if any trades have been used (prevent abuse)
MerchantOffers offers = trader.getOffers();
if (offers != null) {
for (int i = 0; i < offers.size(); i++) {
if (offers.get(i).getUses() > 0) {
sendCannotCycleMessage(player, "This wandering trader has already been traded with!");
return false;
}
}
}

// Use accessor to regenerate trades
com.villagercycle.mixin.MerchantEntityAccessor accessor = (com.villagercycle.mixin.MerchantEntityAccessor) trader;

// Clear offers and regenerate
trader.getOffers().clear();
accessor.invokeUpdateTrades((ServerLevel) trader.level());

// Send success message if enabled (client controls this via packet)
if (showSuccessMessage) {
player.sendSystemMessage(
Component.literal("\u2705 Wandering trader offers have been refreshed!")
.withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
false
);
player.sendSystemMessage(
Component.literal("\uD83D\uDD04 The trader now has new offers.")
.withStyle(ChatFormatting.YELLOW),
false
);
}

return true;
}

public static void sendCannotCycleMessage(ServerPlayer player, String reason) {
player.sendSystemMessage(
Component.literal("\u274C " + reason)
.withStyle(ChatFormatting.RED),
false
);
}
}
