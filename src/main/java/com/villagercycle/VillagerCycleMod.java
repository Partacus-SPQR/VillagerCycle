package com.villagercycle;

import com.villagercycle.config.VillagerCycleConfig;
import com.villagercycle.mixin.MerchantScreenHandlerAccessor;
import com.villagercycle.network.CycleTradePayload;
import com.villagercycle.network.ReloadConfigPayload;
import com.villagercycle.util.VillagerTradeUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//? if >=1.21.11 {
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
//?} else {
/*import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.WanderingTrader;*/
//?}
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
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
        //? if >=26.1 {
        PayloadTypeRegistry.serverboundPlay().register(CycleTradePayload.TYPE, CycleTradePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ReloadConfigPayload.TYPE, ReloadConfigPayload.CODEC);
        //? } else {
        /*PayloadTypeRegistry.playC2S().register(CycleTradePayload.TYPE, CycleTradePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ReloadConfigPayload.TYPE, ReloadConfigPayload.CODEC);*/
        //? }

// Register reload config packet receiver
ServerPlayNetworking.registerGlobalReceiver(ReloadConfigPayload.TYPE, (payload, context) -> {
ServerPlayer player = context.player();

context.server().execute(() -> {
// Only allow operators to reload config
//? if >=1.21.11 {
if (player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.OWNERS))) {
//?} else {
/*if (player.getPermissionLevel() >= 4) {*/
//?}
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
ServerPlayNetworking.registerGlobalReceiver(CycleTradePayload.TYPE, (payload, context) -> {
ServerPlayer player = context.player();

context.server().execute(() -> {
LOGGER.info("Received cycle trade request from player: {}", player.getName().getString());

// Get the villager from the player's current screen handler using accessor
if (!(player.containerMenu instanceof MerchantMenu merchantHandler)) {
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
if (merchant instanceof WanderingTrader) {
LOGGER.info("Detected WanderingTrader");
if (!config.allowWanderingTraders) {
LOGGER.info("Wandering traders disabled in config");
VillagerTradeUtil.sendCannotCycleMessage(player, "Wandering trader cycling is disabled by the server.");
return;
}

WanderingTrader wanderingTrader = (WanderingTrader) merchant;
UUID traderUuid = wanderingTrader.getUUID();

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

player.connection.send(new ClientboundMerchantOffersPacket(
merchantHandler.containerId,
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

if (!(merchant instanceof Villager)) {
LOGGER.info("Merchant is not a Villager");
return;
}

Villager villager = (Villager) merchant;
LOGGER.info("Found villager, cycling trades...");

// Check if villager has a profession
VillagerData villagerData = villager.getVillagerData();
Holder<VillagerProfession> profession = villagerData.profession();

if (profession.is(VillagerProfession.NONE)) {
VillagerTradeUtil.sendCannotCycleMessage(player, "This villager has no profession!");
return;
}

if (profession.is(VillagerProfession.NITWIT)) {
VillagerTradeUtil.sendCannotCycleMessage(player, "Nitwits cannot have trades!");
return;
}

// Check if villager has been traded with
if (villager.getVillagerXp() > 0) {
VillagerTradeUtil.sendCannotCycleMessage(player, "This villager has already been traded with!");
return;
}

// Check villager cycle limit
UUID villagerUuid = villager.getUUID();
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
player.connection.send(new ClientboundMerchantOffersPacket(
merchantHandler.containerId,
villager.getOffers(),
villager.getVillagerData().level(),
villager.getVillagerXp(),
villager.showProgressBar(),
villager.canRestock()
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

// Clear merchant inventory slots and return items to player inventory.
// This prevents item duplication exploit where players could place items
// in trade slots, cycle trades, and keep the output item.
private static void clearMerchantInventory(MerchantMenu handler, ServerPlayer player) {
// Merchant screen handler slots: 0 = first input, 1 = second input, 2 = output
// We need to clear input slots (0 and 1) and return items to player
for (int i = 0; i < 2; i++) {
ItemStack stack = handler.getSlot(i).getItem();
if (!stack.isEmpty()) {
// Return item to player inventory
player.addItem(stack.copy());
// Clear the slot
handler.getSlot(i).set(ItemStack.EMPTY);
LOGGER.info("Returned {} {} to player inventory", stack.getCount(), stack.getHoverName().getString());
}
}

// Clear output slot (slot 2) without returning to player (it's the result)
handler.getSlot(2).set(ItemStack.EMPTY);
}
}