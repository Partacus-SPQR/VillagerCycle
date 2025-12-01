package com.villagercycle.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VillagerCycleClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("VillagerCycle-Client");

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing Villager Cycle Client");
		
		// Network packet is already registered by the server-side mod
		// Client-side mixin will handle sending the packet
		
		LOGGER.info("Villager Cycle Client initialized successfully!");
	}
}
