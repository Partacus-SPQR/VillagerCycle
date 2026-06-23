package com.villagercycle.client.screen;

import com.villagercycle.config.VillagerCycleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import com.villagercycle.compat.ScreenCompat;
import org.lwjgl.glfw.GLFW;

//? if >=26.1
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if <26.1
/*import net.minecraft.client.gui.GuiGraphics;*/

// A screen that allows the user to drag the Cycle Trades button to reposition it.
// Press ESC to exit and save the position.
public class ButtonDragScreen extends Screen {

    // MerchantScreen dimensions - matches the actual MerchantScreen
    // The texture is 276x166, but we need to match where the GUI actually renders
    private static final int GUI_WIDTH = 276;
    private static final int GUI_HEIGHT = 166;

    private final Screen parent;
    private boolean isDragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean wasMouseDown = false;

    // These correspond to MerchantScreen's leftPos and topPos fields (inherited from AbstractContainerScreen)
    private int guiLeft;
    private int guiTop;

    public ButtonDragScreen(Screen parent) {
        super(Component.literal("Drag Cycle Button"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        // Calculate position exactly like AbstractContainerScreen.init() does:
        // this.leftPos = (this.width - this.imageWidth) / 2;
        // this.topPos = (this.height - this.imageHeight) / 2;
        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;
    }

    //? if >=26.1 {
    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        Minecraft client = Minecraft.getInstance();

        // Check mouse button state using GLFW
        long windowHandle = client.getWindow().handle();
        boolean isMouseDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        // Detect mouse click start
        if (isMouseDown && !wasMouseDown) {
            handleMouseClick(mouseX, mouseY);
        }

        // Detect mouse release
        if (!isMouseDown && wasMouseDown) {
            handleMouseRelease();
        }

        wasMouseDown = isMouseDown;

        // Draw semi-transparent background
        extractor.fill(0, 0, this.width, this.height, 0x80000000);

        // Draw instructions at top
        Font fontRenderer = this.font;
        String instructions = "Drag the button to reposition. Press ESC to save.";
        int instructionWidth = fontRenderer.width(instructions);
        extractor.text(
                fontRenderer,
                instructions,
                (this.width - instructionWidth) / 2,
                10,
                0xFFFFFF
        );

        // Draw a simplified but accurate mock of the MerchantScreen
        drawMockMerchantGui(extractor, fontRenderer);

        // Get config for button position
        VillagerCycleConfig config = VillagerCycleConfig.getInstance();

        // Calculate button position relative to GUI
        int buttonWidth = config.buttonWidth;
        int buttonHeight = config.buttonHeight;
        int buttonX = guiLeft + config.buttonOffsetX;
        int buttonY = guiTop + config.buttonOffsetY;

        // Handle dragging - update position based on mouse
        if (isDragging) {
            int newX = mouseX - dragOffsetX;
            int newY = mouseY - dragOffsetY;

            // Convert to offset from GUI position
            config.buttonOffsetX = newX - guiLeft;
            config.buttonOffsetY = newY - guiTop;

            buttonX = newX;
            buttonY = newY;
        }

        // Draw thin highlight border around button (1 pixel outline)
        int borderColor = isDragging ? 0xFFFFFF00 : 0xFF00FF00; // Yellow when dragging, green otherwise
        // Top border
        extractor.fill(buttonX - 1, buttonY - 1, buttonX + buttonWidth + 1, buttonY, borderColor);
        // Bottom border
        extractor.fill(buttonX - 1, buttonY + buttonHeight, buttonX + buttonWidth + 1, buttonY + buttonHeight + 1, borderColor);
        // Left border
        extractor.fill(buttonX - 1, buttonY, buttonX, buttonY + buttonHeight, borderColor);
        // Right border
        extractor.fill(buttonX + buttonWidth, buttonY, buttonX + buttonWidth + 1, buttonY + buttonHeight, borderColor);

        // Draw button background (Minecraft button style)
        extractor.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, 0xFF555555);
        extractor.fill(buttonX + 1, buttonY + 1, buttonX + buttonWidth - 1, buttonY + buttonHeight - 1, 0xFF8B8B8B);
        extractor.fill(buttonX + 1, buttonY + 1, buttonX + buttonWidth - 2, buttonY + buttonHeight - 2, 0xFF6C6C6C);

        // Draw button text
        String buttonText = "\uD83D\uDD04 Cycle Trades";
        int textWidth = fontRenderer.width(buttonText);
        int textX = buttonX + (buttonWidth - textWidth) / 2;
        int textY = buttonY + (buttonHeight - 8) / 2;
        extractor.text(fontRenderer, Component.literal(buttonText).withStyle(ChatFormatting.YELLOW).getVisualOrderText(), textX, textY, 0xFFFFFF00);

        // Draw "Drag me!" label
        String dragLabel = "\u2190 Drag me!";
        int labelX = buttonX + buttonWidth + 10;
        int labelY = buttonY + (buttonHeight - 8) / 2;
        extractor.text(fontRenderer, dragLabel, labelX, labelY, 0xFF55FF55);

        // Draw current offset info
        String offsetInfo = String.format("Offset: X=%d, Y=%d", config.buttonOffsetX, config.buttonOffsetY);
        int offsetWidth = fontRenderer.width(offsetInfo);
        extractor.text(fontRenderer, offsetInfo, (this.width - offsetWidth) / 2, this.height - 50, 0xFFAAAAAA);

        // Draw ESC instruction
        String escInstruction = "Press ESC to save and exit";
        int escWidth = fontRenderer.width(escInstruction);
        extractor.text(fontRenderer, escInstruction, (this.width - escWidth) / 2, this.height - 25, 0xFF55FF55);

        super.extractRenderState(extractor, mouseX, mouseY, delta);
    }
    //?} else {
    /*@Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Minecraft client = Minecraft.getInstance();

        // Check mouse button state using GLFW
        long windowHandle = client.getWindow().handle();
        boolean isMouseDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        // Detect mouse click start
        if (isMouseDown && !wasMouseDown) {
            handleMouseClick(mouseX, mouseY);
        }

        // Detect mouse release
        if (!isMouseDown && wasMouseDown) {
            handleMouseRelease();
        }

        wasMouseDown = isMouseDown;

        // Draw semi-transparent background
        context.fill(0, 0, this.width, this.height, 0x80000000);

        // Draw instructions at top
        Font fontRenderer = this.font;
        String instructions = "Drag the button to reposition. Press ESC to save.";
        int instructionWidth = fontRenderer.width(instructions);
        context.drawString(
                fontRenderer,
                instructions,
                (this.width - instructionWidth) / 2,
                10,
                0xFFFFFF
        );

        // Draw a simplified but accurate mock of the MerchantScreen
        drawMockMerchantGui(context, fontRenderer);

        // Get config for button position
        VillagerCycleConfig config = VillagerCycleConfig.getInstance();

        // Calculate button position relative to GUI
        int buttonWidth = config.buttonWidth;
        int buttonHeight = config.buttonHeight;
        int buttonX = guiLeft + config.buttonOffsetX;
        int buttonY = guiTop + config.buttonOffsetY;

        // Handle dragging - update position based on mouse
        if (isDragging) {
            int newX = mouseX - dragOffsetX;
            int newY = mouseY - dragOffsetY;

            // Convert to offset from GUI position
            config.buttonOffsetX = newX - guiLeft;
            config.buttonOffsetY = newY - guiTop;

            buttonX = newX;
            buttonY = newY;
        }

        // Draw thin highlight border around button (1 pixel outline)
        int borderColor = isDragging ? 0xFFFFFF00 : 0xFF00FF00; // Yellow when dragging, green otherwise
        // Top border
        context.fill(buttonX - 1, buttonY - 1, buttonX + buttonWidth + 1, buttonY, borderColor);
        // Bottom border
        context.fill(buttonX - 1, buttonY + buttonHeight, buttonX + buttonWidth + 1, buttonY + buttonHeight + 1, borderColor);
        // Left border
        context.fill(buttonX - 1, buttonY, buttonX, buttonY + buttonHeight, borderColor);
        // Right border
        context.fill(buttonX + buttonWidth, buttonY, buttonX + buttonWidth + 1, buttonY + buttonHeight, borderColor);

        // Draw button background (Minecraft button style)
        context.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, 0xFF555555);
        context.fill(buttonX + 1, buttonY + 1, buttonX + buttonWidth - 1, buttonY + buttonHeight - 1, 0xFF8B8B8B);
        context.fill(buttonX + 1, buttonY + 1, buttonX + buttonWidth - 2, buttonY + buttonHeight - 2, 0xFF6C6C6C);

        // Draw button text
        String buttonText = "\uD83D\uDD04 Cycle Trades";
        int textWidth = fontRenderer.width(buttonText);
        int textX = buttonX + (buttonWidth - textWidth) / 2;
        int textY = buttonY + (buttonHeight - 8) / 2;
        context.drawString(fontRenderer, Component.literal(buttonText).withStyle(ChatFormatting.YELLOW), textX, textY, 0xFFFFFF00);

        // Draw "Drag me!" label
        String dragLabel = "\u2190 Drag me!";
        int labelX = buttonX + buttonWidth + 10;
        int labelY = buttonY + (buttonHeight - 8) / 2;
        context.drawString(fontRenderer, dragLabel, labelX, labelY, 0xFF55FF55);

        // Draw current offset info
        String offsetInfo = String.format("Offset: X=%d, Y=%d", config.buttonOffsetX, config.buttonOffsetY);
        int offsetWidth = fontRenderer.width(offsetInfo);
        context.drawString(fontRenderer, offsetInfo, (this.width - offsetWidth) / 2, this.height - 50, 0xFFAAAAAA);

        // Draw ESC instruction
        String escInstruction = "Press ESC to save and exit";
        int escWidth = fontRenderer.width(escInstruction);
        context.drawString(fontRenderer, escInstruction, (this.width - escWidth) / 2, this.height - 25, 0xFF55FF55);

        super.render(context, mouseX, mouseY, delta);
    }*/
    //?}

    // Draw a mock merchant GUI that closely matches the actual appearance
    //? if >=26.1 {
    private void drawMockMerchantGui(GuiGraphicsExtractor gfx, Font fontRenderer) {
    //?} else {
    /*private void drawMockMerchantGui(GuiGraphics gfx, Font fontRenderer) {*/
    //?}
        // Main GUI background - light grayish brown like the actual texture
        // Outer dark border (simulating the GUI edge shadow)
        gfx.fill(guiLeft - 2, guiTop - 2, guiLeft + GUI_WIDTH + 2, guiTop + GUI_HEIGHT + 2, 0xFF373737);

        // Main background - the tan/gray color of the merchant GUI
        gfx.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xFFC6C6C6);

        // Draw the left sidebar (trades list) - darker recessed area
        int tradesX = guiLeft + 4;
        int tradesY = guiTop + 17;
        int tradesWidth = 97;
        int tradesHeight = 141;

        // Dark border around trades area
        gfx.fill(tradesX, tradesY, tradesX + tradesWidth, tradesY + tradesHeight, 0xFF555555);
        // Inner dark background
        gfx.fill(tradesX + 1, tradesY + 1, tradesX + tradesWidth - 1, tradesY + tradesHeight - 1, 0xFF8B8B8B);

        // "Trades" title
        //? if >=26.1
        gfx.text(fontRenderer, "Trades", guiLeft + 5, guiTop + 6, 0x404040, false);
        //? if <26.1
        /*gfx.drawString(fontRenderer, "Trades", guiLeft + 5, guiTop + 6, 0x404040, false);*/

        // Draw some mock trade entries
        for (int i = 0; i < 3; i++) {
            int tradeY = tradesY + 2 + (i * 20);
            if (tradeY + 18 < tradesY + tradesHeight) {
                gfx.fill(tradesX + 2, tradeY, tradesX + tradesWidth - 3, tradeY + 18, 0xFF4A4A4A);
                gfx.fill(tradesX + 3, tradeY + 1, tradesX + tradesWidth - 4, tradeY + 17, 0xFF6A6A6A);
            }
        }

        // Right side - villager info and trade display area
        int rightX = guiLeft + 107;
        int rightY = guiTop + 5;

        // Villager name label area
        //? if >=26.1
        gfx.text(fontRenderer, "Villager - Novice", rightX, rightY, 0x404040, false);
        //? if <26.1
        /*gfx.drawString(fontRenderer, "Villager - Novice", rightX, rightY, 0x404040, false);*/

        // XP bar area (thin rectangle)
        gfx.fill(rightX, guiTop + 16, guiLeft + GUI_WIDTH - 9, guiTop + 20, 0xFF8B8B8B);
        gfx.fill(rightX + 1, guiTop + 17, guiLeft + GUI_WIDTH - 10, guiTop + 19, 0xFFAAAAAA);

        // Trade slots area
        int slotsY = guiTop + 24;
        gfx.fill(rightX, slotsY, guiLeft + GUI_WIDTH - 9, slotsY + 36, 0xFF8B8B8B);

        // Draw 3 slot boxes (input 1, input 2, output)
        drawSlot(gfx, rightX + 4, slotsY + 4, 28);
        drawSlot(gfx, rightX + 38, slotsY + 4, 28);
        // Arrow area
        gfx.fill(rightX + 70, slotsY + 10, rightX + 86, slotsY + 26, 0xFF555555);
        // Output slot
        drawSlot(gfx, rightX + 92, slotsY + 4, 28);

        // Inventory area label
        //? if >=26.1
        gfx.text(fontRenderer, "Inventory", guiLeft + 108, guiTop + 62, 0x404040, false);
        //? if <26.1
        /*gfx.drawString(fontRenderer, "Inventory", guiLeft + 108, guiTop + 62, 0x404040, false);*/

        // Draw simplified inventory grid
        int invStartX = guiLeft + 108;
        int invStartY = guiTop + 74;
        int slotSize = 18;

        // Main inventory (3 rows x 9 cols)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(gfx, invStartX + col * slotSize, invStartY + row * slotSize, slotSize - 2);
            }
        }

        // Hotbar (1 row x 9 cols) with gap
        int hotbarY = invStartY + 3 * slotSize + 4;
        for (int col = 0; col < 9; col++) {
            drawSlot(gfx, invStartX + col * slotSize, hotbarY, slotSize - 2);
        }

        // Preview mode watermark
        String previewLabel = "[ Preview ]";
        int previewWidth = fontRenderer.width(previewLabel);
        //? if >=26.1
        gfx.text(fontRenderer, previewLabel, guiLeft + (GUI_WIDTH - previewWidth) / 2, guiTop + GUI_HEIGHT / 2 + 20, 0x60FFFFFF);
        //? if <26.1
        /*gfx.drawString(fontRenderer, previewLabel, guiLeft + (GUI_WIDTH - previewWidth) / 2, guiTop + GUI_HEIGHT / 2 + 20, 0x60FFFFFF);*/
    }

    // Draw a simple inventory slot
    //? if >=26.1 {
    private void drawSlot(GuiGraphicsExtractor gfx, int x, int y, int size) {
    //?} else {
    /*private void drawSlot(GuiGraphics gfx, int x, int y, int size) {*/
    //?}
        // Slot border (dark)
        gfx.fill(x, y, x + size, y + size, 0xFF373737);
        // Slot inner (lighter)
        gfx.fill(x + 1, y + 1, x + size - 1, y + size - 1, 0xFF8B8B8B);
    }

    private void handleMouseClick(int mouseX, int mouseY) {
        VillagerCycleConfig config = VillagerCycleConfig.getInstance();

        int buttonWidth = config.buttonWidth;
        int buttonHeight = config.buttonHeight;
        int buttonX = guiLeft + config.buttonOffsetX;
        int buttonY = guiTop + config.buttonOffsetY;
        int padding = 4;

        // Check if click is within button bounds
        if (mouseX >= buttonX - padding && mouseX <= buttonX + buttonWidth + padding &&
                mouseY >= buttonY - padding && mouseY <= buttonY + buttonHeight + padding) {
            isDragging = true;
            dragOffsetX = mouseX - buttonX;
            dragOffsetY = mouseY - buttonY;
        }
    }

    private void handleMouseRelease() {
        if (isDragging) {
            isDragging = false;
            // Save the new position
            VillagerCycleConfig config = VillagerCycleConfig.getInstance();
            config.save();
        }
    }

    @Override
    public void onClose() {
        // Save position when closing
        VillagerCycleConfig config = VillagerCycleConfig.getInstance();
        config.save();

        if (this.minecraft != null) {
            ScreenCompat.open(this.minecraft, parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}