package com.villagercycle.config;

import com.villagercycle.network.ReloadConfigPayload;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModConfigScreen {
	private static final Logger LOGGER = LoggerFactory.getLogger("VillagerCycle-Config");
	
	public static Screen createConfigScreen(Screen parent) {
		VillagerCycleConfig config = VillagerCycleConfig.getInstance();
		
		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Text.literal("Villager Cycle Configuration"));
		
		builder.setSavingRunnable(() -> {
			// Get fresh instance to ensure we have latest values from setSaveConsumer
			VillagerCycleConfig currentConfig = VillagerCycleConfig.getInstance();
			currentConfig.save();
			// Force reload to ensure server-side code sees the changes
			VillagerCycleConfig.reload();
			LOGGER.info("Config saved and reloaded - allowWanderingTraders: {}", 
				VillagerCycleConfig.getInstance().allowWanderingTraders);
			
			// Send packet to server with the actual config value (not just reload command)
			if (ClientPlayNetworking.canSend(ReloadConfigPayload.ID)) {
				boolean wanderingTraderValue = VillagerCycleConfig.getInstance().allowWanderingTraders;
				ClientPlayNetworking.send(new ReloadConfigPayload(wanderingTraderValue));
				LOGGER.info("Sent config update to server - allowWanderingTraders: {}", wanderingTraderValue);
			}
		});
		
		ConfigEntryBuilder entryBuilder = builder.entryBuilder();
		
		// Basic Options Category
		ConfigCategory basicCategory = builder.getOrCreateCategory(Text.literal("Basic Options"));
		
		basicCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.literal("Enable Cycle Button"),
			config.enableCycleButton)
			.setDefaultValue(true)
			.setTooltip(Text.literal("Toggles the entire mod on or off."))
			.setSaveConsumer(newValue -> config.enableCycleButton = newValue)
			.build());
		
		// Check if player has operator permissions for wandering trader toggle
		MinecraftClient client = MinecraftClient.getInstance();
		boolean isOperator = false;
		boolean isSingleplayer = false;
		
		if (client.player != null) {
			isSingleplayer = client.isInSingleplayer();
			isOperator = client.player.hasPermissionLevel(4);
		}
		
		// Allow in singleplayer or if player is operator level 4
		if (isSingleplayer || isOperator) {
			basicCategory.addEntry(entryBuilder.startBooleanToggle(
				Text.literal("Allow Wandering Traders"),
				config.allowWanderingTraders)
				.setDefaultValue(false)
				.setTooltip(
					Text.literal("Allow cycling trades for wandering traders."),
					Text.literal("Note: Wandering trader offers will be completely refreshed."),
					isSingleplayer ? 
						Text.literal("") : 
						Text.literal("Requires operator level 4 permission.").formatted(Formatting.GOLD)
				)
				.setSaveConsumer(newValue -> {
					config.allowWanderingTraders = newValue;
					if (client.player != null) {
						// Log the change with player name
						String playerName = client.player.getName().getString();
						LOGGER.info("Operator {} {} wandering trader cycling", 
							playerName, newValue ? "enabled" : "disabled");
						
						client.player.sendMessage(
							Text.literal("Wandering trader cycling is now " + 
								(newValue ? "ENABLED" : "DISABLED"))
								.formatted(newValue ? Formatting.GREEN : Formatting.RED),
							false
						);
					}
				})
				.build());
		} else {
			// Show disabled toggle with explanation for non-operators
			basicCategory.addEntry(entryBuilder.startBooleanToggle(
				Text.literal("Allow Wandering Traders"),
				config.allowWanderingTraders)
				.setDefaultValue(false)
				.setTooltip(
					Text.literal("Allow cycling trades for wandering traders."),
					Text.literal("⚠ You do not have permission to change this setting.").formatted(Formatting.RED),
					Text.literal("Operator level 4 is required on multiplayer servers.").formatted(Formatting.GOLD)
				)
				.setSaveConsumer(newValue -> {
					// Revert the change and notify user
					if (client.player != null) {
						String playerName = client.player.getName().getString();
						LOGGER.warn("Non-operator {} attempted to change wandering trader setting", playerName);
						
						client.player.sendMessage(
							Text.literal("❌ You need operator level 4 permission to change this setting.")
								.formatted(Formatting.RED),
							false
						);
					}
					// Don't actually change the value
				})
				.build());
		}
		
		// Advanced Options Category
		ConfigCategory advancedCategory = builder.getOrCreateCategory(Text.literal("Advanced Options"));
		
		advancedCategory.addEntry(entryBuilder.startIntField(
			Text.literal("Button Offset X"),
			config.buttonOffsetX)
			.setDefaultValue(6)
			.setTooltip(
				Text.literal("Horizontal position offset from the left edge of the trading GUI."),
				Text.literal("Adjust if the button overlaps with other UI elements."),
				Text.literal("Default: 6").formatted(Formatting.GRAY)
			)
			.setSaveConsumer(newValue -> config.buttonOffsetX = newValue)
			.build());
		
		advancedCategory.addEntry(entryBuilder.startIntField(
			Text.literal("Button Offset Y"),
			config.buttonOffsetY)
			.setDefaultValue(-25)
			.setTooltip(
				Text.literal("Vertical position offset from the top edge of the trading GUI."),
				Text.literal("Negative values place the button above the GUI."),
				Text.literal("Default: -25").formatted(Formatting.GRAY)
			)
			.setSaveConsumer(newValue -> config.buttonOffsetY = newValue)
			.build());
		
		advancedCategory.addEntry(entryBuilder.startIntField(
			Text.literal("Button Width"),
			config.buttonWidth)
			.setDefaultValue(100)
			.setMin(50)
			.setMax(200)
			.setTooltip(
				Text.literal("Width of the button in pixels."),
				Text.literal("Range: 50-200"),
				Text.literal("Default: 100").formatted(Formatting.GRAY)
			)
			.setSaveConsumer(newValue -> config.buttonWidth = newValue)
			.build());
		
		advancedCategory.addEntry(entryBuilder.startIntField(
			Text.literal("Button Height"),
			config.buttonHeight)
			.setDefaultValue(20)
			.setMin(10)
			.setMax(40)
			.setTooltip(
				Text.literal("Height of the button in pixels."),
				Text.literal("Range: 10-40"),
				Text.literal("Default: 20").formatted(Formatting.GRAY)
			)
			.setSaveConsumer(newValue -> config.buttonHeight = newValue)
			.build());
		
		return builder.build();
	}
}
