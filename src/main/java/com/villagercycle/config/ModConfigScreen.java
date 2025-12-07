package com.villagercycle.config;

import com.villagercycle.client.VillagerCycleClient;
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
			LOGGER.info("Config saved and reloaded - allowWanderingTraders: {}, wanderingCycleLimit: {}, villagerCycleLimit: {}", 
				VillagerCycleConfig.getInstance().allowWanderingTraders,
				VillagerCycleConfig.getInstance().wanderingTraderCycleLimit,
				VillagerCycleConfig.getInstance().villagerCycleLimit);
			
			// Send packet to server with the actual config values (not just reload command)
			if (ClientPlayNetworking.canSend(ReloadConfigPayload.ID)) {
				VillagerCycleConfig savedConfig = VillagerCycleConfig.getInstance();
				ClientPlayNetworking.send(new ReloadConfigPayload(
					savedConfig.allowWanderingTraders,
					savedConfig.wanderingTraderCycleLimit,
					savedConfig.villagerCycleLimit
				));
				LOGGER.info("Sent config update to server - allowWanderingTraders: {}, wanderingCycleLimit: {}, villagerCycleLimit: {}", 
					savedConfig.allowWanderingTraders, savedConfig.wanderingTraderCycleLimit, savedConfig.villagerCycleLimit);
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
		
		basicCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.literal("Show Villager Success Message"),
			config.showSuccessMessage)
			.setDefaultValue(true)
			.setTooltip(
				Text.literal("Show a success message in chat when villager trades are cycled."),
				Text.literal("Disable to reduce chat spam.").formatted(Formatting.GRAY)
			)
			.setSaveConsumer(newValue -> config.showSuccessMessage = newValue)
			.build());
		
		basicCategory.addEntry(entryBuilder.startBooleanToggle(
			Text.literal("Show Wandering Trader Success Message"),
			config.showWanderingTraderSuccessMessage)
			.setDefaultValue(true)
			.setTooltip(
				Text.literal("Show a success message in chat when wandering trader offers are cycled."),
				Text.literal("Disable to reduce chat spam.").formatted(Formatting.GRAY)
			)
			.setSaveConsumer(newValue -> config.showWanderingTraderSuccessMessage = newValue)
			.build());
		
		// Check if player has operator permissions for wandering trader toggle
		MinecraftClient client = MinecraftClient.getInstance();
		boolean isOperator = false;
		boolean isSingleplayer = false;
		
		if (client.player != null) {
			isSingleplayer = client.isInSingleplayer();
			isOperator = client.player.hasPermissionLevel(4);
		}
		
		// Store the original value for non-operators to revert to
		final boolean originalWanderingTraderValue = config.allowWanderingTraders;
		
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
						Text.literal("Server Admin Option - Operator level 4 required.").formatted(Formatting.GOLD)
				)
				.setSaveConsumer(newValue -> {
					config.allowWanderingTraders = newValue;
					if (client.player != null) {
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
			
			// Wandering Trader Cycle Limit slider (operator only)
			basicCategory.addEntry(entryBuilder.startIntSlider(
				Text.literal("Wandering Trader Cycle Limit"),
				config.wanderingTraderCycleLimit, -1, 100)
				.setDefaultValue(1)
				.setTooltip(
					Text.literal("Maximum times a wandering trader can be cycled."),
					Text.literal("-1 = Unlimited, 0 = Disabled, 1+ = Limited cycles").formatted(Formatting.GRAY),
					Text.literal("Default: 1 (one cycle per trader)").formatted(Formatting.GRAY),
					isSingleplayer ? 
						Text.literal("") : 
						Text.literal("Server Admin Option - Operator level 4 required.").formatted(Formatting.GOLD)
				)
				.setTextGetter(value -> {
					if (value < 0) return Text.literal("Unlimited");
					if (value == 0) return Text.literal("Disabled");
					return Text.literal(String.valueOf(value));
				})
				.setSaveConsumer(newValue -> {
					config.wanderingTraderCycleLimit = newValue;
					LOGGER.info("Wandering trader cycle limit set to: {}", newValue);
				})
				.build());
			
			// Villager Cycle Limit slider (operator only)
			basicCategory.addEntry(entryBuilder.startIntSlider(
				Text.literal("Villager Cycle Limit"),
				config.villagerCycleLimit, -1, 100)
				.setDefaultValue(-1)
				.setTooltip(
					Text.literal("Maximum times a villager can be cycled."),
					Text.literal("-1 = Unlimited (default), 0 = Disabled, 1+ = Limited cycles").formatted(Formatting.GRAY),
					Text.literal("Default: -1 (Unlimited)").formatted(Formatting.GRAY),
					isSingleplayer ? 
						Text.literal("") : 
						Text.literal("Server Admin Option - Operator level 4 required.").formatted(Formatting.GOLD)
				)
				.setTextGetter(value -> {
					if (value < 0) return Text.literal("Unlimited");
					if (value == 0) return Text.literal("Disabled");
					return Text.literal(String.valueOf(value));
				})
				.setSaveConsumer(newValue -> {
					config.villagerCycleLimit = newValue;
					LOGGER.info("Villager cycle limit set to: {}", newValue);
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
					// Always revert to original value for non-operators
					config.allowWanderingTraders = originalWanderingTraderValue;
					if (client.player != null && newValue != originalWanderingTraderValue) {
						String playerName = client.player.getName().getString();
						LOGGER.warn("Non-operator {} attempted to change wandering trader setting", playerName);
						
						client.player.sendMessage(
							Text.literal("❌ You need operator level 4 permission to change this setting.")
								.formatted(Formatting.RED),
							false
						);
					}
				})
				.build());
			
			// Show cycle limits as read-only info for non-operators
			basicCategory.addEntry(entryBuilder.startTextDescription(
				Text.literal("Wandering Trader Cycle Limit: ").formatted(Formatting.GRAY)
					.append(Text.literal(config.wanderingTraderCycleLimit < 0 ? "Unlimited" : 
						String.valueOf(config.wanderingTraderCycleLimit)).formatted(Formatting.WHITE))
					.append(Text.literal(" (Server controlled)").formatted(Formatting.DARK_GRAY)))
				.build());
			
			basicCategory.addEntry(entryBuilder.startTextDescription(
				Text.literal("Villager Cycle Limit: ").formatted(Formatting.GRAY)
					.append(Text.literal(config.villagerCycleLimit < 0 ? "Unlimited" : 
						String.valueOf(config.villagerCycleLimit)).formatted(Formatting.WHITE))
					.append(Text.literal(" (Server controlled)").formatted(Formatting.DARK_GRAY)))
				.build());
		}
		
		// Button Appearance Category
		ConfigCategory buttonAppearance = builder.getOrCreateCategory(Text.literal("Button Appearance"));
		
		// Note about drag button feature
		buttonAppearance.addEntry(entryBuilder.startTextDescription(
			Text.literal("Note: ").formatted(Formatting.GOLD)
				.append(Text.literal("Set a keybind to use the Drag Button feature.").formatted(Formatting.WHITE)))
			.build());
		
		buttonAppearance.addEntry(entryBuilder.startIntField(
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
		
		buttonAppearance.addEntry(entryBuilder.startIntField(
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
		
		buttonAppearance.addEntry(entryBuilder.startIntSlider(
			Text.literal("Button Width"),
			config.buttonWidth, 20, 200)
			.setDefaultValue(100)
			.setTooltip(
				Text.literal("Width of the button in pixels."),
				Text.literal("Range: 20-200"),
				Text.literal("Default: 100").formatted(Formatting.GRAY)
			)
			.setSaveConsumer(newValue -> config.buttonWidth = newValue)
			.build());
		
		buttonAppearance.addEntry(entryBuilder.startIntSlider(
			Text.literal("Button Height"),
			config.buttonHeight, 10, 100)
			.setDefaultValue(20)
			.setTooltip(
				Text.literal("Height of the button in pixels."),
				Text.literal("Range: 10-100"),
				Text.literal("Default: 20").formatted(Formatting.GRAY)
			)
			.setSaveConsumer(newValue -> config.buttonHeight = newValue)
			.build());
		
		// Keybindings Category
		ConfigCategory keybindings = builder.getOrCreateCategory(Text.literal("Keybindings"));
		
		keybindings.addEntry(entryBuilder.fillKeybindingField(
			Text.literal("Toggle Button Visibility"),
			VillagerCycleClient.toggleButtonKeyBinding)
			.setTooltip(Text.literal("Keybind to show/hide the Cycle Trades button"))
			.build());
		
		keybindings.addEntry(entryBuilder.fillKeybindingField(
			Text.literal("Open Button Position Screen"),
			VillagerCycleClient.dragButtonKeyBinding)
			.setTooltip(Text.literal("Keybind to open the visual button position editor"))
			.build());
		
		keybindings.addEntry(entryBuilder.fillKeybindingField(
			Text.literal("Open Config Screen"),
			VillagerCycleClient.openConfigKeyBinding)
			.setTooltip(Text.literal("Keybind to open this configuration screen"))
			.build());
		
		keybindings.addEntry(entryBuilder.fillKeybindingField(
			Text.literal("Reload Config File"),
			VillagerCycleClient.reloadConfigKeyBinding)
			.setTooltip(Text.literal("Keybind to reload config from disk"))
			.build());
		
		keybindings.addEntry(entryBuilder.fillKeybindingField(
			Text.literal("Cycle Trades"),
			VillagerCycleClient.cycleTradesKeyBinding)
			.setTooltip(
				Text.literal("Keybind to cycle trades while in a merchant screen."),
				Text.literal("Works the same as clicking the Cycle Trades button.").formatted(Formatting.GRAY)
			)
			.build());
		
		return builder.build();
	}
}
