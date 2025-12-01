package com.villagercycle;

import com.villagercycle.config.VillagerCycleConfig;
import com.villagercycle.mixin.MerchantScreenHandlerAccessor;
import com.villagercycle.network.CycleTradePayload;
import com.villagercycle.util.VillagerTradeUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.Merchant;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VillagerCycleMod implements ModInitializer {
	public static final String MOD_ID = "villagercycle";
	public static final Logger LOGGER = LoggerFactory.getLogger("VillagerCycle");

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Villager Cycle Mod");
		
		// Load config
		VillagerCycleConfig.load();
		
		// Register network packet
		PayloadTypeRegistry.playC2S().register(CycleTradePayload.ID, CycleTradePayload.CODEC);
		
		// Register packet receiver
		ServerPlayNetworking.registerGlobalReceiver(CycleTradePayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			
			context.server().execute(() -> {
				LOGGER.info("Received cycle trade request from player: {}", player.getName().getString());
				
				// Get the villager from the player's current screen handler using accessor
				if (!(player.currentScreenHandler instanceof MerchantScreenHandler merchantHandler)) {
					LOGGER.info("Player is not in a merchant screen");
					return;
				}
				
				// Use accessor mixin to get the merchant
				Merchant merchant = ((MerchantScreenHandlerAccessor) merchantHandler).getMerchant();
				
				if (merchant == null) {
					LOGGER.info("Merchant is null");
					return;
				}
				
				LOGGER.info("Merchant found: {}", merchant.getClass().getName());
				
				VillagerCycleConfig config = VillagerCycleConfig.getInstance();
				
				// Handle wandering traders based on config
				if (merchant instanceof MerchantEntity && !config.allowWanderingTraders) {
					VillagerTradeUtil.sendCannotCycleMessage(player, "Wandering traders are not supported. Enable in config if desired.");
					return;
				}
				
				if (!(merchant instanceof VillagerEntity)) {
					if (merchant instanceof MerchantEntity && config.allowWanderingTraders) {
						// Handle wandering trader if enabled
						MerchantEntity wanderingTrader = (MerchantEntity) merchant;
						boolean success = VillagerTradeUtil.cycleWanderingTraderTrades(wanderingTrader, player);
						if (success) {
							player.networkHandler.sendPacket(new SetTradeOffersS2CPacket(
								merchantHandler.syncId,
								wanderingTrader.getOffers(),
								1,
								0,
								false,
								false
							));
							LOGGER.info("Successfully cycled wandering trader trades and updated client GUI");
						} else {
							VillagerTradeUtil.sendCannotCycleMessage(player, "Unable to cycle trades at this time.");
						}
						return;
					}
					LOGGER.info("Merchant is not a VillagerEntity");
					return;
				}
				
				VillagerEntity villager = (VillagerEntity) merchant;
				LOGGER.info("Found villager, cycling trades...");
				
				// Check if villager has a profession
				VillagerData villagerData = villager.getVillagerData();
				RegistryEntry<VillagerProfession> profession = villagerData.profession();
				
				if (profession.matchesKey(VillagerProfession.NONE)) {
					VillagerTradeUtil.sendCannotCycleMessage(player, "This villager has no profession!");
					return;
				}
				
				if (profession.matchesKey(VillagerProfession.NITWIT)) {
					VillagerTradeUtil.sendCannotCycleMessage(player, "Nitwits cannot have trades!");
					return;
				}
				
				// Check if villager has been traded with
				if (villager.getExperience() > 0) {
					VillagerTradeUtil.sendCannotCycleMessage(player, "This villager has already been traded with!");
					return;
				}
				
				// Perform the trade cycle
				boolean success = VillagerTradeUtil.cycleTrades(villager, player);
				if (success) {
					// Send updated trade list packet directly to client for real-time refresh
					player.networkHandler.sendPacket(new SetTradeOffersS2CPacket(
						merchantHandler.syncId,
						villager.getOffers(),
						villager.getVillagerData().level(),
						villager.getExperience(),
						villager.isLeveledMerchant(),
						villager.canRefreshTrades()
					));
					LOGGER.info("Successfully cycled trades and updated client GUI");
				} else {
					VillagerTradeUtil.sendCannotCycleMessage(player, "Unable to cycle trades at this time.");
				}
			});
		});
		
		LOGGER.info("Villager Cycle Mod initialized successfully!");
	}
}
