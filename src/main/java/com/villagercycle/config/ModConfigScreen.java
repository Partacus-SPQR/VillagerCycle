//? if <26.1 {
/*package com.villagercycle.config;

import com.villagercycle.client.VillagerCycleClient;
import com.villagercycle.network.ReloadConfigPayload;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//? if >=1.21.11 {
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
//?}

public class ModConfigScreen {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerCycle-Config");

    public static Screen createConfigScreen(Screen parent) {
        VillagerCycleConfig config = VillagerCycleConfig.getInstance();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Villager Cycle Configuration"));

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
            if (ClientPlayNetworking.canSend(ReloadConfigPayload.TYPE)) {
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
        ConfigCategory basicCategory = builder.getOrCreateCategory(Component.literal("Basic Options"));

        basicCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Enable Cycle Button"),
                config.enableCycleButton)
                .setDefaultValue(true)
                .setTooltip(Component.literal("Toggles the entire mod on or off."))
                .setSaveConsumer(newValue -> config.enableCycleButton = newValue)
                .build());

        basicCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Show Villager Success Message"),
                config.showSuccessMessage)
                .setDefaultValue(true)
                .setTooltip(
                        Component.literal("Show a success message in chat when villager trades are cycled."),
                        Component.literal("Disable to reduce chat spam.").withStyle(ChatFormatting.GRAY)
                )
                .setSaveConsumer(newValue -> config.showSuccessMessage = newValue)
                .build());

        basicCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Show Wandering Trader Success Message"),
                config.showWanderingTraderSuccessMessage)
                .setDefaultValue(true)
                .setTooltip(
                        Component.literal("Show a success message in chat when wandering trader offers are cycled."),
                        Component.literal("Disable to reduce chat spam.").withStyle(ChatFormatting.GRAY)
                )
                .setSaveConsumer(newValue -> config.showWanderingTraderSuccessMessage = newValue)
                .build());

        // Check if player has operator permissions for wandering trader toggle
        Minecraft client = Minecraft.getInstance();
        boolean isOperator = false;
        boolean isSingleplayer = false;

        if (client.player != null) {
            isSingleplayer = client.isLocalServer();
            //? if >=1.21.11
            isOperator = client.player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.OWNERS));
            //? if <1.21.11
            *//*isOperator = client.player.getPermissionLevel() >= 4;*//*
        }

        // Store the original value for non-operators to revert to
        final boolean originalWanderingTraderValue = config.allowWanderingTraders;

        // Allow in singleplayer or if player is operator level 4
        if (isSingleplayer || isOperator) {
            basicCategory.addEntry(entryBuilder.startBooleanToggle(
                    Component.literal("Allow Wandering Traders"),
                    config.allowWanderingTraders)
                    .setDefaultValue(false)
                    .setTooltip(
                            Component.literal("Allow cycling trades for wandering traders."),
                            Component.literal("Note: Wandering trader offers will be completely refreshed."),
                            isSingleplayer ?
                                    Component.literal("") :
                                    Component.literal("Server Admin Option - Operator level 4 required.").withStyle(ChatFormatting.GOLD)
                    )
                    .setSaveConsumer(newValue -> {
                        config.allowWanderingTraders = newValue;
                        if (client.player != null) {
                            String playerName = client.player.getName().getString();
                            LOGGER.info("Operator {} {} wandering trader cycling",
                                    playerName, newValue ? "enabled" : "disabled");

                            client.player.displayClientMessage(
                                    Component.literal("Wandering trader cycling is now " +
                                                    (newValue ? "ENABLED" : "DISABLED"))
                                            .withStyle(newValue ? ChatFormatting.GREEN : ChatFormatting.RED),
                                    false
                            );
                        }
                    })
                    .build());

            // Wandering Trader Cycle Limit slider (operator only)
            basicCategory.addEntry(entryBuilder.startIntSlider(
                    Component.literal("Wandering Trader Cycle Limit"),
                    config.wanderingTraderCycleLimit, -1, 100)
                    .setDefaultValue(1)
                    .setTooltip(
                            Component.literal("Maximum times a wandering trader can be cycled."),
                            Component.literal("-1 = Unlimited, 0 = Disabled, 1+ = Limited cycles").withStyle(ChatFormatting.GRAY),
                            Component.literal("Default: 1 (one cycle per trader)").withStyle(ChatFormatting.GRAY),
                            isSingleplayer ?
                                    Component.literal("") :
                                    Component.literal("Server Admin Option - Operator level 4 required.").withStyle(ChatFormatting.GOLD)
                    )
                    .setTextGetter(value -> {
                        if (value < 0) return Component.literal("Unlimited");
                        if (value == 0) return Component.literal("Disabled");
                        return Component.literal(String.valueOf(value));
                    })
                    .setSaveConsumer(newValue -> {
                        config.wanderingTraderCycleLimit = newValue;
                        LOGGER.info("Wandering trader cycle limit set to: {}", newValue);
                    })
                    .build());

            // Villager Cycle Limit slider (operator only)
            basicCategory.addEntry(entryBuilder.startIntSlider(
                    Component.literal("Villager Cycle Limit"),
                    config.villagerCycleLimit, -1, 100)
                    .setDefaultValue(-1)
                    .setTooltip(
                            Component.literal("Maximum times a villager can be cycled."),
                            Component.literal("-1 = Unlimited (default), 0 = Disabled, 1+ = Limited cycles").withStyle(ChatFormatting.GRAY),
                            Component.literal("Default: -1 (Unlimited)").withStyle(ChatFormatting.GRAY),
                            isSingleplayer ?
                                    Component.literal("") :
                                    Component.literal("Server Admin Option - Operator level 4 required.").withStyle(ChatFormatting.GOLD)
                    )
                    .setTextGetter(value -> {
                        if (value < 0) return Component.literal("Unlimited");
                        if (value == 0) return Component.literal("Disabled");
                        return Component.literal(String.valueOf(value));
                    })
                    .setSaveConsumer(newValue -> {
                        config.villagerCycleLimit = newValue;
                        LOGGER.info("Villager cycle limit set to: {}", newValue);
                    })
                    .build());
        } else {
            // Show disabled toggle with explanation for non-operators
            basicCategory.addEntry(entryBuilder.startBooleanToggle(
                    Component.literal("Allow Wandering Traders"),
                    config.allowWanderingTraders)
                    .setDefaultValue(false)
                    .setTooltip(
                            Component.literal("Allow cycling trades for wandering traders."),
                            Component.literal("\u26A0 You do not have permission to change this setting.").withStyle(ChatFormatting.RED),
                            Component.literal("Operator level 4 is required on multiplayer servers.").withStyle(ChatFormatting.GOLD)
                    )
                    .setSaveConsumer(newValue -> {
                        // Always revert to original value for non-operators
                        config.allowWanderingTraders = originalWanderingTraderValue;
                        if (client.player != null && newValue != originalWanderingTraderValue) {
                            String playerName = client.player.getName().getString();
                            LOGGER.warn("Non-operator {} attempted to change wandering trader setting", playerName);

                            client.player.displayClientMessage(
                                    Component.literal("\u274C You need operator level 4 permission to change this setting.")
                                            .withStyle(ChatFormatting.RED),
                                    false
                            );
                        }
                    })
                    .build());

            // Show cycle limits as read-only info for non-operators
            basicCategory.addEntry(entryBuilder.startTextDescription(
                    Component.literal("Wandering Trader Cycle Limit: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(config.wanderingTraderCycleLimit < 0 ? "Unlimited" :
                                    String.valueOf(config.wanderingTraderCycleLimit)).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" (Server controlled)").withStyle(ChatFormatting.DARK_GRAY)))
                    .build());

            basicCategory.addEntry(entryBuilder.startTextDescription(
                    Component.literal("Villager Cycle Limit: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(config.villagerCycleLimit < 0 ? "Unlimited" :
                                    String.valueOf(config.villagerCycleLimit)).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" (Server controlled)").withStyle(ChatFormatting.DARK_GRAY)))
                    .build());
        }

        // Button Appearance Category
        ConfigCategory buttonAppearance = builder.getOrCreateCategory(Component.literal("Button Appearance"));

        // Note about drag button feature
        buttonAppearance.addEntry(entryBuilder.startTextDescription(
                Component.literal("Note: ").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal("Set a keybind to use the Drag Button feature.").withStyle(ChatFormatting.WHITE)))
                .build());

        buttonAppearance.addEntry(entryBuilder.startIntField(
                Component.literal("Button Offset X"),
                config.buttonOffsetX)
                .setDefaultValue(6)
                .setTooltip(
                        Component.literal("Horizontal position offset from the left edge of the trading GUI."),
                        Component.literal("Adjust if the button overlaps with other UI elements."),
                        Component.literal("Default: 6").withStyle(ChatFormatting.GRAY)
                )
                .setSaveConsumer(newValue -> config.buttonOffsetX = newValue)
                .build());

        buttonAppearance.addEntry(entryBuilder.startIntField(
                Component.literal("Button Offset Y"),
                config.buttonOffsetY)
                .setDefaultValue(-25)
                .setTooltip(
                        Component.literal("Vertical position offset from the top edge of the trading GUI."),
                        Component.literal("Negative values place the button above the GUI."),
                        Component.literal("Default: -25").withStyle(ChatFormatting.GRAY)
                )
                .setSaveConsumer(newValue -> config.buttonOffsetY = newValue)
                .build());

        buttonAppearance.addEntry(entryBuilder.startIntSlider(
                Component.literal("Button Width"),
                config.buttonWidth, 20, 200)
                .setDefaultValue(100)
                .setTooltip(
                        Component.literal("Width of the button in pixels."),
                        Component.literal("Range: 20-200"),
                        Component.literal("Default: 100").withStyle(ChatFormatting.GRAY)
                )
                .setSaveConsumer(newValue -> config.buttonWidth = newValue)
                .build());

        buttonAppearance.addEntry(entryBuilder.startIntSlider(
                Component.literal("Button Height"),
                config.buttonHeight, 10, 100)
                .setDefaultValue(20)
                .setTooltip(
                        Component.literal("Height of the button in pixels."),
                        Component.literal("Range: 10-100"),
                        Component.literal("Default: 20").withStyle(ChatFormatting.GRAY)
                )
                .setSaveConsumer(newValue -> config.buttonHeight = newValue)
                .build());

        // Keybindings Category
        ConfigCategory keybindings = builder.getOrCreateCategory(Component.literal("Keybindings"));

        keybindings.addEntry(entryBuilder.fillKeybindingField(
                Component.literal("Toggle Button Visibility"),
                VillagerCycleClient.toggleButtonKeyMapping)
                .setTooltip(Component.literal("Keybind to show/hide the Cycle Trades button"))
                .build());

        keybindings.addEntry(entryBuilder.fillKeybindingField(
                Component.literal("Open Button Position Screen"),
                VillagerCycleClient.dragButtonKeyMapping)
                .setTooltip(Component.literal("Keybind to open the visual button position editor"))
                .build());

        keybindings.addEntry(entryBuilder.fillKeybindingField(
                Component.literal("Open Config Screen"),
                VillagerCycleClient.openConfigKeyMapping)
                .setTooltip(Component.literal("Keybind to open this configuration screen"))
                .build());

        keybindings.addEntry(entryBuilder.fillKeybindingField(
                Component.literal("Reload Config File"),
                VillagerCycleClient.reloadConfigKeyMapping)
                .setTooltip(Component.literal("Keybind to reload config from disk"))
                .build());

        keybindings.addEntry(entryBuilder.fillKeybindingField(
                Component.literal("Cycle Trades"),
                VillagerCycleClient.cycleTradesKeyMapping)
                .setTooltip(
                        Component.literal("Keybind to cycle trades while in a merchant screen."),
                        Component.literal("Works the same as clicking the Cycle Trades button.").withStyle(ChatFormatting.GRAY)
                )
                .build());

        return builder.build();
    }
}
*/
//?}