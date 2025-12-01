package com.villagercycle.client.mixin;

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
		// Position button ABOVE the GUI window, outside the GUI frame
		int buttonX = this.x + 6;
		int buttonY = this.y - 25;  // NEGATIVE offset to place ABOVE the window
		int buttonWidth = 100;
		int buttonHeight = 20;
		
		ButtonWidget cycleButton = ButtonWidget.builder(
			Text.literal("🔄 Cycle Trades").formatted(Formatting.YELLOW),
			button -> {
				// Always send packet - server will validate
				ClientPlayNetworking.send(new CycleTradePayload());
			}
		)
		.dimensions(buttonX, buttonY, buttonWidth, buttonHeight)
		.build();
		
		this.addDrawableChild(cycleButton);
	}
}
