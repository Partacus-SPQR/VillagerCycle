package com.villagercycle.client.mixin;

import com.villagercycle.client.VillagerCycleClient;
import com.villagercycle.config.VillagerCycleConfig;
import com.villagercycle.network.CycleTradePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.1
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if <26.1
/*import net.minecraft.client.gui.GuiGraphics;*/

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {
    @Unique
    private boolean villagercycle$keyWasDown = false;

    private MerchantScreenMixin() {
        super(null, null, null);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addCycleButton(CallbackInfo ci) {
        VillagerCycleConfig config = VillagerCycleConfig.getInstance();
        if (!config.enableCycleButton) return;

        int buttonWidth = config.buttonWidth;
        int buttonHeight = config.buttonHeight;
        int buttonX = this.leftPos + config.buttonOffsetX;
        int buttonY = this.topPos + config.buttonOffsetY;

        Button cycleButton = Button.builder(
            Component.literal("Cycle Trades").withStyle(ChatFormatting.GREEN),
            button -> {
                ClientPlayNetworking.send(new CycleTradePayload(
                    config.showSuccessMessage,
                    config.showWanderingTraderSuccessMessage
                ));
            }
        )
        .bounds(buttonX, buttonY, buttonWidth, buttonHeight)
        .build();

        this.addRenderableWidget(cycleButton);
    }

    //? if >=26.1 {
    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void checkKeybindOnRender(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "renderBg", at = @At("TAIL"))
    private void checkKeybindOnRender(GuiGraphics context, float delta, int mouseX, int mouseY, CallbackInfo ci) {*/
    //?}
        // Check if the cycle trades keybind is pressed
        // We need to check this in render because consumeClick() doesn't work with screens open
        if (VillagerCycleClient.cycleTradesKeyMapping != null && this.minecraft != null) {
            // Check if keybind is unbound
            if (VillagerCycleClient.cycleTradesKeyMapping.isUnbound()) {
                return;
            }

            // Get the bound key and parse it to get the key code
            String keyName = VillagerCycleClient.cycleTradesKeyMapping.saveString();
            InputConstants.Key boundKey = InputConstants.getKey(keyName);

            long windowHandle = this.minecraft.getWindow().handle();
            boolean isKeyDown = false;

            // Check keyboard keys
            if (boundKey.getType() == InputConstants.Type.KEYSYM) {
                isKeyDown = GLFW.glfwGetKey(windowHandle, boundKey.getValue()) == GLFW.GLFW_PRESS;
            }
            // Check mouse buttons (Button 4 = GLFW_MOUSE_BUTTON_4, Button 5 = GLFW_MOUSE_BUTTON_5, etc.)
            else if (boundKey.getType() == InputConstants.Type.MOUSE) {
                isKeyDown = GLFW.glfwGetMouseButton(windowHandle, boundKey.getValue()) == GLFW.GLFW_PRESS;
            }

            // Trigger on key/button press (not while held)
            if (isKeyDown && !villagercycle$keyWasDown) {
                // Send the cycle trade packet with both message preferences
                VillagerCycleConfig cfg = VillagerCycleConfig.getInstance();
                ClientPlayNetworking.send(new CycleTradePayload(cfg.showSuccessMessage, cfg.showWanderingTraderSuccessMessage));
            }

            villagercycle$keyWasDown = isKeyDown;
        }
    }
}