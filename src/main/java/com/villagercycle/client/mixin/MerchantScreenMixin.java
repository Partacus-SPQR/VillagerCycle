package com.villagercycle.client.mixin;

import com.villagercycle.client.VillagerCycleClient;
import com.villagercycle.config.VillagerCycleConfig;
import com.villagercycle.network.CycleTradePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends HandledScreen<MerchantScreenHandler> {
	
	@Unique
	private boolean villagercycle$keyWasDown = false;
	
	private MerchantScreenMixin() {
		super(null, null, null);
	}
	
	@Inject(method = "init", at = @At("TAIL"))
	private void addCycleTradeButton(CallbackInfo ci) {
		VillagerCycleConfig config = VillagerCycleConfig.getInstance();
		
		// Check if button is enabled in config
		if (!config.enableCycleButton) {
			return;
		}
		
		// Use config values for button position and size
		int buttonX = this.x + config.buttonOffsetX;
		int buttonY = this.y + config.buttonOffsetY;
		int buttonWidth = config.buttonWidth;
		int buttonHeight = config.buttonHeight;
		
		ButtonWidget cycleButton = ButtonWidget.builder(
			Text.literal("🔄 Cycle Trades").formatted(Formatting.YELLOW),
			button -> {
				// Send packet with client's message preferences for both villagers and wandering traders
				VillagerCycleConfig cfg = VillagerCycleConfig.getInstance();
				ClientPlayNetworking.send(new CycleTradePayload(cfg.showSuccessMessage, cfg.showWanderingTraderSuccessMessage));
				// Remove focus from button after clicking to reset outline
				button.setFocused(false);
			}
		)
		.dimensions(buttonX, buttonY, buttonWidth, buttonHeight)
		.build();
		
		this.addDrawableChild(cycleButton);
	}
	
	@Inject(method = "drawBackground", at = @At("TAIL"))
	private void checkKeybindOnRender(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
		// Check if the cycle trades keybind is pressed
		// We need to check this in render because wasPressed() doesn't work with screens open
		if (VillagerCycleClient.cycleTradesKeyBinding != null && this.client != null) {
			// Get the currently bound key (not the default)
			InputUtil.Key boundKey = InputUtil.fromTranslationKey(VillagerCycleClient.cycleTradesKeyBinding.getBoundKeyTranslationKey());
			
			// Only check if a key is actually bound (not UNKNOWN)
			if (boundKey.getCode() != InputUtil.UNKNOWN_KEY.getCode()) {
				long windowHandle = this.client.getWindow().getHandle();
				boolean isKeyDown = GLFW.glfwGetKey(windowHandle, boundKey.getCode()) == GLFW.GLFW_PRESS;
				
				// Trigger on key press (not while held)
				if (isKeyDown && !villagercycle$keyWasDown) {
					// Send the cycle trade packet with both message preferences
					VillagerCycleConfig cfg = VillagerCycleConfig.getInstance();
					ClientPlayNetworking.send(new CycleTradePayload(cfg.showSuccessMessage, cfg.showWanderingTraderSuccessMessage));
				}
				
				villagercycle$keyWasDown = isKeyDown;
			}
		}
	}
}
