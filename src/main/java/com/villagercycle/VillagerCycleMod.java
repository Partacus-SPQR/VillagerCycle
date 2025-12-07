package com.villagercycle;

import com.villagercycle.config.VillagerCycleConfig;
import com.villagercycle.mixin.MerchantScreenHandlerAccessor;
import com.villagercycle.network.CycleTradePayload;
import com.villagercycle.network.ReloadConfigPayload;
import com.villagercycle.util.VillagerTradeUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.Merchant;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillagerCycleMod implements ModInitializer {
	public static final String MOD_ID = "villagercycle";
	public static final Logger LOGGER = LoggerFactory.getLogger("VillagerCycle");
	
	// Track cycle counts per entity UUID
	private static final Map<UUID, Integer> wanderingTraderCycleCounts = new HashMap<>();
	private static final Map<UUID, Integer> villagerCycleCounts = new HashMap<>();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Villager Cycle Mod");
		
		// Load config
		VillagerCycleConfig.load();
		
		// Register network packets
		PayloadTypeRegistry.playC2S().register(CycleTradePayload.ID, CycleTradePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ReloadConfigPayload.ID, ReloadConfigPayload.CODEC);
		
		// Register reload config packet receiver
		ServerPlayNetworking.registerGlobalReceiver(ReloadConfigPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			
			context.server().execute(() -> {
				// Only allow operators to reload config
				if (player.hasPermissionLevel(4)) {
					VillagerCycleConfig config = VillagerCycleConfig.getInstance();
					// Update the server's config with the values from client
					config.allowWanderingTraders = payload.allowWanderingTraders();
					config.wanderingTraderCycleLimit = payload.wanderingTraderCycleLimit();
					config.villagerCycleLimit = payload.villagerCycleLimit();
					config.save(); // Save to server's config file
					LOGGER.info("Config updated by operator {} - allowWanderingTraders: {}, wanderingCycleLimit: {}, villagerCycleLimit: {}", 
						player.getName().getString(), payload.allowWanderingTraders(), 
						payload.wanderingTraderCycleLimit(), payload.villagerCycleLimit());
				} else {
					LOGGER.warn("Non-operator {} attempted to reload config", player.getName().getString());
				}
			});
		});
		
		// Register packet receiver for trade cycling
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
				LOGGER.info("Config loaded - allowWanderingTraders: {}", config.allowWanderingTraders);
				
				// Clear merchant inventory slots to prevent item duplication exploit
				clearMerchantInventory(merchantHandler, player);
				
				// Handle wandering traders based on config
				if (merchant instanceof WanderingTraderEntity) {
					LOGGER.info("Detected WanderingTraderEntity");
					if (!config.allowWanderingTraders) {
						LOGGER.info("Wandering traders disabled in config");
						VillagerTradeUtil.sendCannotCycleMessage(player, "Wandering trader cycling is disabled by the server.");
						return;
					}
					
					WanderingTraderEntity wanderingTrader = (WanderingTraderEntity) merchant;
					UUID traderUuid = wanderingTrader.getUuid();
					
					// Check cycle limit (if not unlimited)
					int cycleLimit = config.wanderingTraderCycleLimit;
					if (cycleLimit >= 0) { // -1 means unlimited
						int currentCount = wanderingTraderCycleCounts.getOrDefault(traderUuid, 0);
						if (currentCount >= cycleLimit) {
							VillagerTradeUtil.sendCannotCycleMessage(player, 
								"Wandering trader cycle limit reached (" + cycleLimit + " cycles max).");
							return;
						}
					}
					
					LOGGER.info("Wandering traders enabled, cycling...");
					boolean showMessage = payload.showWanderingTraderSuccessMessage();
					boolean success = VillagerTradeUtil.cycleWanderingTraderTrades(wanderingTrader, player, showMessage);
					if (success) {
						// Increment cycle count
						int newCount = wanderingTraderCycleCounts.getOrDefault(traderUuid, 0) + 1;
						wanderingTraderCycleCounts.put(traderUuid, newCount);
						
						player.networkHandler.sendPacket(new SetTradeOffersS2CPacket(
							merchantHandler.syncId,
							wanderingTrader.getOffers(),
							1,
							0,
							false,
							false
						));
						LOGGER.info("Successfully cycled wandering trader trades and updated client GUI (cycle {}/{})", 
							newCount, cycleLimit < 0 ? "unlimited" : cycleLimit);
					} else {
						VillagerTradeUtil.sendCannotCycleMessage(player, "Unable to cycle trades at this time.");
					}
					return;
				}
				
				if (!(merchant instanceof VillagerEntity)) {
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
				
				// Check villager cycle limit
				UUID villagerUuid = villager.getUuid();
				int villagerCycleLimit = config.villagerCycleLimit;
				if (villagerCycleLimit == 0) {
					VillagerTradeUtil.sendCannotCycleMessage(player, "Villager cycling is disabled by the server.");
					return;
				}
				if (villagerCycleLimit > 0) { // -1 means unlimited
					int currentCount = villagerCycleCounts.getOrDefault(villagerUuid, 0);
					if (currentCount >= villagerCycleLimit) {
						VillagerTradeUtil.sendCannotCycleMessage(player, 
							"Villager cycle limit reached (" + villagerCycleLimit + " cycles max).");
						return;
					}
				}
				
				// Perform the trade cycle
				boolean showMessage = payload.showVillagerSuccessMessage();
				boolean success = VillagerTradeUtil.cycleTrades(villager, player, showMessage);
				if (success) {
					// Increment cycle count
					int newCount = villagerCycleCounts.getOrDefault(villagerUuid, 0) + 1;
					villagerCycleCounts.put(villagerUuid, newCount);
					
					// Send updated trade list packet directly to client for real-time refresh
					player.networkHandler.sendPacket(new SetTradeOffersS2CPacket(
						merchantHandler.syncId,
						villager.getOffers(),
						villager.getVillagerData().level(),
						villager.getExperience(),
						villager.isLeveledMerchant(),
						villager.canRefreshTrades()
					));
					LOGGER.info("Successfully cycled trades and updated client GUI (cycle {}/{})", 
						newCount, villagerCycleLimit < 0 ? "unlimited" : villagerCycleLimit);
				} else {
					VillagerTradeUtil.sendCannotCycleMessage(player, "Unable to cycle trades at this time.");
				}
			});
		});
		
		LOGGER.info("Villager Cycle Mod initialized successfully!");
	}
	
	/**
	 * Clear merchant inventory slots and return items to player inventory.
	 * This prevents item duplication exploit where players could place items
	 * in trade slots, cycle trades, and keep the output item.
	 */
	private static void clearMerchantInventory(MerchantScreenHandler handler, ServerPlayerEntity player) {
		// Merchant screen handler slots: 0 = first input, 1 = second input, 2 = output
		// We need to clear input slots (0 and 1) and return items to player
		for (int i = 0; i < 2; i++) {
			ItemStack stack = handler.getSlot(i).getStack();
			if (!stack.isEmpty()) {
				// Return item to player inventory
				player.giveItemStack(stack.copy());
				// Clear the slot
				handler.getSlot(i).setStack(ItemStack.EMPTY);
				LOGGER.info("Returned {} {} to player inventory", stack.getCount(), stack.getName().getString());
			}
		}
		
		// Clear output slot (slot 2) without returning to player (it's the result)
		handler.getSlot(2).setStack(ItemStack.EMPTY);
	}
}
