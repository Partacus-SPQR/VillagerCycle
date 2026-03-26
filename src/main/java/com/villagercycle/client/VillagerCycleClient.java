package com.villagercycle.client;

import com.villagercycle.client.screen.ButtonDragScreen;
import com.villagercycle.config.ModMenuIntegration;
import com.villagercycle.config.VillagerCycleConfig;
import com.villagercycle.network.CycleTradePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if >=26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.resources.Identifier;
//?} else if >=1.21.11 {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.resources.Identifier;*/
//?} else {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.resources.ResourceLocation;*/
//?}
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VillagerCycleClient implements ClientModInitializer {
public static final String MOD_ID = "villagercycle";
public static final Logger LOGGER = LoggerFactory.getLogger("VillagerCycle-Client");

// Define a custom category for our keybindings
//? if >=1.21.11 {
private static final KeyMapping.Category VILLAGERCYCLE_CATEGORY =
new KeyMapping.Category(Identifier.fromNamespaceAndPath(MOD_ID, "category"));
//?} else {
/*private static final KeyMapping.Category VILLAGERCYCLE_CATEGORY =
new KeyMapping.Category(ResourceLocation.fromNamespaceAndPath(MOD_ID, "category"));*/
//?}

// Keybindings - all unbound by default
public static KeyMapping toggleButtonKeyMapping;
public static KeyMapping dragButtonKeyMapping;
public static KeyMapping openConfigKeyMapping;
public static KeyMapping reloadConfigKeyMapping;
public static KeyMapping cycleTradesKeyMapping;

@Override
public void onInitializeClient() {
LOGGER.info("Initializing Villager Cycle Client");

// Load config
VillagerCycleConfig.load();

// Register keybindings with Category-based constructor
// All keybindings are unbound by default - users can set their own in Controls
//? if >=26.1 {
toggleButtonKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
//?} else {
/*toggleButtonKeyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(*/
//?}
"key.villagercycle.toggle",
GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
VILLAGERCYCLE_CATEGORY
));

//? if >=26.1 {
dragButtonKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
//?} else {
/*dragButtonKeyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(*/
//?}
"key.villagercycle.drag",
GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
VILLAGERCYCLE_CATEGORY
));

//? if >=26.1 {
openConfigKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
//?} else {
/*openConfigKeyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(*/
//?}
"key.villagercycle.config",
GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
VILLAGERCYCLE_CATEGORY
));

//? if >=26.1 {
reloadConfigKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
//?} else {
/*reloadConfigKeyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(*/
//?}
"key.villagercycle.reload",
GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
VILLAGERCYCLE_CATEGORY
));

//? if >=26.1 {
cycleTradesKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
//?} else {
/*cycleTradesKeyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(*/
//?}
"key.villagercycle.cycle",
GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
VILLAGERCYCLE_CATEGORY
));

// Register tick event for keybinding handling
ClientTickEvents.END_CLIENT_TICK.register(client -> {
// Toggle button enable/disable
while (toggleButtonKeyMapping.consumeClick()) {
VillagerCycleConfig config = VillagerCycleConfig.getInstance();
config.enableCycleButton = !config.enableCycleButton;
config.save();
LOGGER.info("Cycle Trades button toggled: {}", config.enableCycleButton);
}

// Open drag screen
while (dragButtonKeyMapping.consumeClick()) {
if (client.screen == null) {
client.setScreen(new ButtonDragScreen(null));
}
}

// Open config screen
while (openConfigKeyMapping.consumeClick()) {
if (client.screen == null) {
client.setScreen(ModMenuIntegration.getConfigScreen(null));
}
}

// Reload config from file
while (reloadConfigKeyMapping.consumeClick()) {
VillagerCycleConfig.reload();
LOGGER.info("VillagerCycle config reloaded from file");
}

// Cycle trades keybind - only works when in merchant screen
while (cycleTradesKeyMapping.consumeClick()) {
if (client.screen instanceof MerchantScreen) {
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
