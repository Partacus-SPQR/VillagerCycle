package com.villagercycle.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;

public class VillagerTradeUtil {
	
	public static boolean canCycleTrades(Entity entity) {
		if (!(entity instanceof VillagerEntity villager)) {
			return false;
		}
		
		VillagerData data = villager.getVillagerData();
		RegistryEntry<VillagerProfession> profession = data.profession();
		if (profession.matchesKey(VillagerProfession.NONE) || 
		    profession.matchesKey(VillagerProfession.NITWIT)) {
			return false;
		}
		
		// Check if villager has been traded with
		if (villager.getExperience() > 0) {
			return false;
		}
		
		// Check if any trades have been used
		TradeOfferList offers = villager.getOffers();
		if (offers != null) {
			for (int i = 0; i < offers.size(); i++) {
				if (offers.get(i).hasBeenUsed()) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static boolean cycleTrades(VillagerEntity villager, ServerPlayerEntity player, boolean showSuccessMessage) {
		if (!canCycleTrades(villager)) {
			return false;
		}
		
		VillagerData villagerData = villager.getVillagerData();
		RegistryEntry<VillagerProfession> currentProfession = villagerData.profession();
		
		// The trick: temporarily set to NONE profession, then back to current
		// This forces trade regeneration
		RegistryEntry.Reference<VillagerProfession> noneEntry = Registries.VILLAGER_PROFESSION.getDefaultEntry().orElseThrow();
		villager.setVillagerData(villagerData.withProfession(noneEntry));
		villager.setVillagerData(villagerData.withProfession(currentProfession).withLevel(1));
		
		// Trigger trade regeneration by clearing offers
		villager.setOffers(null);
		
		// Send success message if enabled (client controls this via packet)
		if (showSuccessMessage) {
			player.sendMessage(
				Text.literal("✅ Villager trades have been cycled!")
					.formatted(Formatting.GREEN, Formatting.BOLD),
				false
			);
			player.sendMessage(
				Text.literal("🔄 The villager now has new Level 1 trades.")
					.formatted(Formatting.YELLOW),
				false
			);
		}
		
		return true;
	}
	
	public static boolean cycleWanderingTraderTrades(MerchantEntity trader, ServerPlayerEntity player, boolean showSuccessMessage) {
		// Check if any trades have been used (prevent abuse)
		TradeOfferList offers = trader.getOffers();
		if (offers != null) {
			for (int i = 0; i < offers.size(); i++) {
				if (offers.get(i).hasBeenUsed()) {
					sendCannotCycleMessage(player, "This wandering trader has already been traded with!");
					return false;
				}
			}
		}
		
		// Use accessor to regenerate trades
		com.villagercycle.mixin.MerchantEntityAccessor accessor = (com.villagercycle.mixin.MerchantEntityAccessor) trader;
		
		// Clear offers and regenerate
		trader.getOffers().clear();
		accessor.invokeFillRecipes((net.minecraft.server.world.ServerWorld) trader.getEntityWorld());
		
		// Send success message if enabled (client controls this via packet)
		if (showSuccessMessage) {
			player.sendMessage(
				Text.literal("✅ Wandering trader offers have been refreshed!")
					.formatted(Formatting.GREEN, Formatting.BOLD),
				false
			);
			player.sendMessage(
				Text.literal("🔄 The trader now has new offers.")
					.formatted(Formatting.YELLOW),
				false
			);
		}
		
		return true;
	}
	
	public static void sendCannotCycleMessage(ServerPlayerEntity player, String reason) {
		player.sendMessage(
			Text.literal("❌ " + reason)
				.formatted(Formatting.RED),
			false
		);
	}
}
