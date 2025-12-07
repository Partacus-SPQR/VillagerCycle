package com.villagercycle.client.screen;

import com.villagercycle.config.VillagerCycleConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

/**
 * A screen that allows the user to drag the Cycle Trades button to reposition it.
 * Press ESC to exit and save the position.
 */
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
	
	// These correspond to MerchantScreen's x and y fields (inherited from HandledScreen)
	private int guiLeft;
	private int guiTop;

	public ButtonDragScreen(Screen parent) {
		super(Text.literal("Drag Cycle Button"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		// Calculate position exactly like HandledScreen.init() does:
		// this.x = (this.width - this.backgroundWidth) / 2;
		// this.y = (this.height - this.backgroundHeight) / 2;
		this.guiLeft = (this.width - GUI_WIDTH) / 2;
		this.guiTop = (this.height - GUI_HEIGHT) / 2;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		MinecraftClient client = MinecraftClient.getInstance();
		
		// Check mouse button state using GLFW
		long windowHandle = client.getWindow().getHandle();
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
		TextRenderer textRenderer = this.textRenderer;
		String instructions = "Drag the button to reposition. Press ESC to save.";
		int instructionWidth = textRenderer.getWidth(instructions);
		context.drawTextWithShadow(
			textRenderer,
			instructions,
			(this.width - instructionWidth) / 2,
			10,
			0xFFFFFF
		);
		
		// Draw a simplified but accurate mock of the MerchantScreen
		// The real merchant GUI has a light gray background with darker borders
		drawMockMerchantGui(context, textRenderer);
		
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
		String buttonText = "🔄 Cycle Trades";
		int textWidth = textRenderer.getWidth(buttonText);
		int textX = buttonX + (buttonWidth - textWidth) / 2;
		int textY = buttonY + (buttonHeight - 8) / 2;
		context.drawTextWithShadow(textRenderer, Text.literal(buttonText).formatted(Formatting.YELLOW), textX, textY, 0xFFFFFF00);
		
		// Draw "Drag me!" label
		String dragLabel = "← Drag me!";
		int labelX = buttonX + buttonWidth + 10;
		int labelY = buttonY + (buttonHeight - 8) / 2;
		context.drawTextWithShadow(textRenderer, dragLabel, labelX, labelY, 0xFF55FF55);
		
		// Draw current offset info
		String offsetInfo = String.format("Offset: X=%d, Y=%d", config.buttonOffsetX, config.buttonOffsetY);
		int offsetWidth = textRenderer.getWidth(offsetInfo);
		context.drawTextWithShadow(textRenderer, offsetInfo, (this.width - offsetWidth) / 2, this.height - 50, 0xFFAAAAAA);
		
		// Draw ESC instruction
		String escInstruction = "Press ESC to save and exit";
		int escWidth = textRenderer.getWidth(escInstruction);
		context.drawTextWithShadow(textRenderer, escInstruction, (this.width - escWidth) / 2, this.height - 25, 0xFF55FF55);
		
		super.render(context, mouseX, mouseY, delta);
	}
	
	/**
	 * Draw a mock merchant GUI that closely matches the actual appearance
	 */
	private void drawMockMerchantGui(DrawContext context, TextRenderer textRenderer) {
		// Main GUI background - light grayish brown like the actual texture
		// The actual GUI has a slight border and rounded appearance, we'll approximate
		
		// Outer dark border (simulating the GUI edge shadow)
		context.fill(guiLeft - 2, guiTop - 2, guiLeft + GUI_WIDTH + 2, guiTop + GUI_HEIGHT + 2, 0xFF373737);
		
		// Main background - the tan/gray color of the merchant GUI
		context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xFFC6C6C6);
		
		// Draw the left sidebar (trades list) - darker recessed area
		// This is approximately where "Trades" section is
		int tradesX = guiLeft + 4;
		int tradesY = guiTop + 17;
		int tradesWidth = 97;
		int tradesHeight = 141;
		
		// Dark border around trades area
		context.fill(tradesX, tradesY, tradesX + tradesWidth, tradesY + tradesHeight, 0xFF555555);
		// Inner dark background
		context.fill(tradesX + 1, tradesY + 1, tradesX + tradesWidth - 1, tradesY + tradesHeight - 1, 0xFF8B8B8B);
		
		// "Trades" title
		context.drawText(textRenderer, "Trades", guiLeft + 5, guiTop + 6, 0x404040, false);
		
		// Draw some mock trade entries (just rectangles to show where trades would be)
		for (int i = 0; i < 3; i++) {
			int tradeY = tradesY + 2 + (i * 20);
			if (tradeY + 18 < tradesY + tradesHeight) {
				// Trade entry background
				context.fill(tradesX + 2, tradeY, tradesX + tradesWidth - 3, tradeY + 18, 0xFF4A4A4A);
				context.fill(tradesX + 3, tradeY + 1, tradesX + tradesWidth - 4, tradeY + 17, 0xFF6A6A6A);
			}
		}
		
		// Right side - villager info and trade display area
		int rightX = guiLeft + 107;
		int rightY = guiTop + 5;
		
		// Villager name label area
		context.drawText(textRenderer, "Villager - Novice", rightX, rightY, 0x404040, false);
		
		// XP bar area (thin rectangle)
		context.fill(rightX, guiTop + 16, guiLeft + GUI_WIDTH - 9, guiTop + 20, 0xFF8B8B8B);
		context.fill(rightX + 1, guiTop + 17, guiLeft + GUI_WIDTH - 10, guiTop + 19, 0xFFAAAAAA);
		
		// Trade slots area (the big area where you see item slots)
		int slotsY = guiTop + 24;
		context.fill(rightX, slotsY, guiLeft + GUI_WIDTH - 9, slotsY + 36, 0xFF8B8B8B);
		
		// Draw 3 slot boxes (input 1, input 2, output)
		drawSlot(context, rightX + 4, slotsY + 4, 28);
		drawSlot(context, rightX + 38, slotsY + 4, 28);
		// Arrow area
		context.fill(rightX + 70, slotsY + 10, rightX + 86, slotsY + 26, 0xFF555555);
		// Output slot
		drawSlot(context, rightX + 92, slotsY + 4, 28);
		
		// Inventory area label
		context.drawText(textRenderer, "Inventory", guiLeft + 108, guiTop + 62, 0x404040, false);
		
		// Draw simplified inventory grid
		int invStartX = guiLeft + 108;
		int invStartY = guiTop + 74;
		int slotSize = 18;
		
		// Main inventory (3 rows x 9 cols)
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				drawSlot(context, invStartX + col * slotSize, invStartY + row * slotSize, slotSize - 2);
			}
		}
		
		// Hotbar (1 row x 9 cols) with gap
		int hotbarY = invStartY + 3 * slotSize + 4;
		for (int col = 0; col < 9; col++) {
			drawSlot(context, invStartX + col * slotSize, hotbarY, slotSize - 2);
		}
		
		// Preview mode watermark
		String previewLabel = "[ Preview ]";
		int previewWidth = textRenderer.getWidth(previewLabel);
		context.drawTextWithShadow(textRenderer, previewLabel, 
			guiLeft + (GUI_WIDTH - previewWidth) / 2, 
			guiTop + GUI_HEIGHT / 2 + 20, 
			0x60FFFFFF);
	}
	
	/**
	 * Draw a simple inventory slot
	 */
	private void drawSlot(DrawContext context, int x, int y, int size) {
		// Slot border (dark)
		context.fill(x, y, x + size, y + size, 0xFF373737);
		// Slot inner (lighter)
		context.fill(x + 1, y + 1, x + size - 1, y + size - 1, 0xFF8B8B8B);
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
	public void close() {
		// Save position when closing
		VillagerCycleConfig config = VillagerCycleConfig.getInstance();
		config.save();
		
		if (this.client != null) {
			this.client.setScreen(parent);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
