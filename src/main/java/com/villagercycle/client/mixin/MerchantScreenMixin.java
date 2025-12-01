package com.villagercycle.client.mixin;

import com.villagercycle.config.VillagerCycleConfig;
import com.villagercycle.network.CycleTradePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends HandledScreen<MerchantScreenHandler> {
	
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
				// Always send packet - server will validate
				ClientPlayNetworking.send(new CycleTradePayload());
				// Remove focus from button after clicking to reset outline
				button.setFocused(false);
			}
		)
		.dimensions(buttonX, buttonY, buttonWidth, buttonHeight)
		.build();
		
		this.addDrawableChild(cycleButton);
	}
}
