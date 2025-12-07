package com.villagercycle.client;

import com.villagercycle.client.screen.ButtonDragScreen;
import com.villagercycle.config.ModConfigScreen;
import com.villagercycle.config.VillagerCycleConfig;
import com.villagercycle.network.CycleTradePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VillagerCycleClient implements ClientModInitializer {
	public static final String MOD_ID = "villagercycle";
	public static final Logger LOGGER = LoggerFactory.getLogger("VillagerCycle-Client");
	
	// Define a custom category for our keybindings
	private static final KeyBinding.Category VILLAGERCYCLE_CATEGORY = 
		new KeyBinding.Category(Identifier.of(MOD_ID, "category"));
	
	// Keybindings - all unbound by default
	public static KeyBinding toggleButtonKeyBinding;
	public static KeyBinding dragButtonKeyBinding;
	public static KeyBinding openConfigKeyBinding;
	public static KeyBinding reloadConfigKeyBinding;
	public static KeyBinding cycleTradesKeyBinding;

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing Villager Cycle Client");
		
		// Load config
		VillagerCycleConfig.load();
		
		// Register keybindings with Category-based constructor
		// All keybindings are unbound by default - users can set their own in Controls
		toggleButtonKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.villagercycle.toggle",
			GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
			VILLAGERCYCLE_CATEGORY
		));
		
		dragButtonKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.villagercycle.drag",
			GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
			VILLAGERCYCLE_CATEGORY
		));
		
		openConfigKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.villagercycle.config",
			GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
			VILLAGERCYCLE_CATEGORY
		));
		
		reloadConfigKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.villagercycle.reload",
			GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
			VILLAGERCYCLE_CATEGORY
		));
		
		cycleTradesKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.villagercycle.cycle",
			GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
			VILLAGERCYCLE_CATEGORY
		));
		
		// Register tick event for keybinding handling
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Toggle button enable/disable
			while (toggleButtonKeyBinding.wasPressed()) {
				VillagerCycleConfig config = VillagerCycleConfig.getInstance();
				config.enableCycleButton = !config.enableCycleButton;
				config.save();
				LOGGER.info("Cycle Trades button toggled: {}", config.enableCycleButton);
			}
			
			// Open drag screen
			while (dragButtonKeyBinding.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new ButtonDragScreen(null));
				}
			}
			
			// Open config screen
			while (openConfigKeyBinding.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(ModConfigScreen.createConfigScreen(null));
				}
			}
			
			// Reload config from file
			while (reloadConfigKeyBinding.wasPressed()) {
				VillagerCycleConfig.reload();
				LOGGER.info("VillagerCycle config reloaded from file");
			}
			
			// Cycle trades keybind - only works when in merchant screen
			while (cycleTradesKeyBinding.wasPressed()) {
				if (client.currentScreen instanceof MerchantScreen) {
					// Send cycle trade packet (same as clicking the button)
					VillagerCycleConfig cfg = VillagerCycleConfig.getInstance();
					ClientPlayNetworking.send(new CycleTradePayload(cfg.showSuccessMessage, cfg.showWanderingTraderSuccessMessage));
					LOGGER.info("Cycle trades keybind pressed - sending packet");
				}
			}
		});
		
		LOGGER.info("Villager Cycle Client initialized successfully!");
	}
}

