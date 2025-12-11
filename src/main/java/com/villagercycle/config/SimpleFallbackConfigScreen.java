package com.villagercycle.config;

import com.villagercycle.network.ReloadConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Fallback config screen using vanilla Minecraft widgets.
 * Used when Cloth Config is not installed.
 * 
 * MANDATORY features per guide:
 * - Sliders for all numeric values
 * - Tooltips for all options
 * - Reset buttons for all options
 * - Scrollable content with interactive scrollbar
 * - Footer buttons: Save & Close | Key Binds | Cancel
 */
public class SimpleFallbackConfigScreen extends Screen {
	private static final Logger LOGGER = LoggerFactory.getLogger("VillagerCycle-FallbackConfig");
	
	private final Screen parent;
	private final VillagerCycleConfig config;
	
	// Layout constants - USE THESE FOR ALL MODS
	private static final int HEADER_HEIGHT = 35;
	private static final int FOOTER_HEIGHT = 35;
	private static final int ROW_HEIGHT = 24;
	private static final int WIDGET_WIDTH = 180;
	private static final int RESET_BTN_WIDTH = 40;
	private static final int SPACING = 4;
	private static final int SCROLL_SPEED = 10;
	private static final int SCROLLBAR_WIDTH = 6;
	
	// Scroll state
	private int scrollOffset = 0;
	private int maxScrollOffset = 0;
	private int contentHeight = 0;
	private boolean isDraggingScrollbar = false;
	private int scrollbarDragOffset = 0;
	
	// Tooltip tracking
	private record TooltipEntry(int x, int y, int width, int height, List<Text> tooltip) {}
	private final List<TooltipEntry> tooltips = new ArrayList<>();
	private List<Text> currentTooltip = null;
	
	// Track scrollable widgets with their original Y positions
	private record WidgetEntry(ClickableWidget widget, int originalY) {}
	private final List<WidgetEntry> scrollableWidgets = new ArrayList<>();
	
	// Track footer buttons (non-scrollable)
	private final List<ClickableWidget> footerButtons = new ArrayList<>();
	
	// Widget references for updating
	private ButtonWidget enableCycleButtonToggle;
	private ButtonWidget showSuccessMessageToggle;
	private ButtonWidget showWanderingTraderSuccessMessageToggle;
	private ButtonWidget allowWanderingTradersToggle;
	private IntSlider wanderingTraderCycleLimitSlider;
	private IntSlider villagerCycleLimitSlider;
	private IntSlider buttonWidthSlider;
	private IntSlider buttonHeightSlider;
	private IntSlider buttonOffsetXSlider;
	private IntSlider buttonOffsetYSlider;
	
	// Permission state
	private boolean isOperator = false;
	private boolean isSingleplayer = false;
	private final boolean originalWanderingTraderValue;
	
	public SimpleFallbackConfigScreen(Screen parent) {
		super(Text.literal("Villager Cycle Configuration"));
		this.parent = parent;
		this.config = VillagerCycleConfig.getInstance();
		this.originalWanderingTraderValue = config.allowWanderingTraders;
		
		// Check permissions
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			isSingleplayer = client.isInSingleplayer();
			isOperator = client.player.getPermissions().hasPermission(new Permission.Level(PermissionLevel.OWNERS));
		}
	}
	
	@Override
	protected void init() {
		super.init();
		tooltips.clear();
		scrollableWidgets.clear();
		footerButtons.clear();
		
		int centerX = this.width / 2;
		int widgetX = centerX - (WIDGET_WIDTH + SPACING + RESET_BTN_WIDTH) / 2;
		int resetX = widgetX + WIDGET_WIDTH + SPACING;
		
		// Calculate content area
		int scrollableHeight = this.height - HEADER_HEIGHT - FOOTER_HEIGHT;
		int y = HEADER_HEIGHT + 10;
		int startY = y;
		
		// ============================================
		// SECTION: Basic Options
		// ============================================
		
		// Section header
		y += 5;
		
		// === Enable Cycle Button Toggle ===
		enableCycleButtonToggle = ButtonWidget.builder(
			Text.literal("Enable Cycle Button: " + (config.enableCycleButton ? "ON" : "OFF")),
			button -> {
				config.enableCycleButton = !config.enableCycleButton;
				button.setMessage(Text.literal("Enable Cycle Button: " + (config.enableCycleButton ? "ON" : "OFF")));
			}
		).dimensions(widgetX, y, WIDGET_WIDTH, 20).build();
		addScrollableWidget(enableCycleButtonToggle, y);
		addTooltip(widgetX, y, WIDGET_WIDTH, 20, 
			Text.literal("Toggles the entire mod on or off."),
			Text.literal("Default: ON").formatted(Formatting.GRAY));
		
		ButtonWidget resetEnableBtn = ButtonWidget.builder(Text.literal("↺"), button -> {
			config.enableCycleButton = true;
			enableCycleButtonToggle.setMessage(Text.literal("Enable Cycle Button: ON"));
		}).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build();
		addScrollableWidget(resetEnableBtn, y);
		
		y += ROW_HEIGHT;
		
		// === Show Villager Success Message Toggle ===
		showSuccessMessageToggle = ButtonWidget.builder(
			Text.literal("Show Villager Success Msg: " + (config.showSuccessMessage ? "ON" : "OFF")),
			button -> {
				config.showSuccessMessage = !config.showSuccessMessage;
				button.setMessage(Text.literal("Show Villager Success Msg: " + (config.showSuccessMessage ? "ON" : "OFF")));
			}
		).dimensions(widgetX, y, WIDGET_WIDTH, 20).build();
		addScrollableWidget(showSuccessMessageToggle, y);
		addTooltip(widgetX, y, WIDGET_WIDTH, 20,
			Text.literal("Show a success message in chat when villager trades are cycled."),
			Text.literal("Disable to reduce chat spam.").formatted(Formatting.GRAY),
			Text.literal("Default: ON").formatted(Formatting.GRAY));
		
		ButtonWidget resetVillagerMsgBtn = ButtonWidget.builder(Text.literal("↺"), button -> {
			config.showSuccessMessage = true;
			showSuccessMessageToggle.setMessage(Text.literal("Show Villager Success Msg: ON"));
		}).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build();
		addScrollableWidget(resetVillagerMsgBtn, y);
		
		y += ROW_HEIGHT;
		
		// === Show Wandering Trader Success Message Toggle ===
		showWanderingTraderSuccessMessageToggle = ButtonWidget.builder(
			Text.literal("Show Trader Success Msg: " + (config.showWanderingTraderSuccessMessage ? "ON" : "OFF")),
			button -> {
				config.showWanderingTraderSuccessMessage = !config.showWanderingTraderSuccessMessage;
				button.setMessage(Text.literal("Show Trader Success Msg: " + (config.showWanderingTraderSuccessMessage ? "ON" : "OFF")));
			}
		).dimensions(widgetX, y, WIDGET_WIDTH, 20).build();
		addScrollableWidget(showWanderingTraderSuccessMessageToggle, y);
		addTooltip(widgetX, y, WIDGET_WIDTH, 20,
			Text.literal("Show a success message in chat when wandering trader offers are cycled."),
			Text.literal("Disable to reduce chat spam.").formatted(Formatting.GRAY),
			Text.literal("Default: ON").formatted(Formatting.GRAY));
		
		ButtonWidget resetTraderMsgBtn = ButtonWidget.builder(Text.literal("↺"), button -> {
			config.showWanderingTraderSuccessMessage = true;
			showWanderingTraderSuccessMessageToggle.setMessage(Text.literal("Show Trader Success Msg: ON"));
		}).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build();
		addScrollableWidget(resetTraderMsgBtn, y);
		
		y += ROW_HEIGHT + 10; // Extra spacing before operator section
		
		// ============================================
		// SECTION: Operator Options (with permission check)
		// ============================================
		
		if (isSingleplayer || isOperator) {
			// === Allow Wandering Traders Toggle (Operator Only) ===
			allowWanderingTradersToggle = ButtonWidget.builder(
				Text.literal("Allow Wandering Traders: " + (config.allowWanderingTraders ? "ON" : "OFF")),
				button -> {
					config.allowWanderingTraders = !config.allowWanderingTraders;
					button.setMessage(Text.literal("Allow Wandering Traders: " + (config.allowWanderingTraders ? "ON" : "OFF")));
					
					MinecraftClient client = MinecraftClient.getInstance();
					if (client.player != null) {
						String playerName = client.player.getName().getString();
						LOGGER.info("Operator {} {} wandering trader cycling", 
							playerName, config.allowWanderingTraders ? "enabled" : "disabled");
						
						client.player.sendMessage(
							Text.literal("Wandering trader cycling is now " + 
								(config.allowWanderingTraders ? "ENABLED" : "DISABLED"))
								.formatted(config.allowWanderingTraders ? Formatting.GREEN : Formatting.RED),
							false
						);
					}
				}
			).dimensions(widgetX, y, WIDGET_WIDTH, 20).build();
			addScrollableWidget(allowWanderingTradersToggle, y);
			
			List<Text> wanderingTooltip = new ArrayList<>();
			wanderingTooltip.add(Text.literal("Allow cycling trades for wandering traders."));
			wanderingTooltip.add(Text.literal("Note: Wandering trader offers will be completely refreshed."));
			if (!isSingleplayer) {
				wanderingTooltip.add(Text.literal("Server Admin Option - Operator level 4 required.").formatted(Formatting.GOLD));
			}
			wanderingTooltip.add(Text.literal("Default: OFF").formatted(Formatting.GRAY));
			addTooltip(widgetX, y, WIDGET_WIDTH, 20, wanderingTooltip.toArray(new Text[0]));
			
			ButtonWidget resetWanderingBtn = ButtonWidget.builder(Text.literal("↺"), button -> {
				config.allowWanderingTraders = false;
				allowWanderingTradersToggle.setMessage(Text.literal("Allow Wandering Traders: OFF"));
			}).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build();
			addScrollableWidget(resetWanderingBtn, y);
			
			y += ROW_HEIGHT;
			
			// === Wandering Trader Cycle Limit Slider (Operator Only) ===
			wanderingTraderCycleLimitSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20,
				getCycleLimitText("Wandering Trader Limit", config.wanderingTraderCycleLimit),
				config.wanderingTraderCycleLimit, -1, 100) {
				@Override
				protected void updateMessage() {
					setMessage(getCycleLimitText("Wandering Trader Limit", getValue()));
				}
				@Override
				protected void applyValue() {
					config.wanderingTraderCycleLimit = getValue();
				}
			};
			addScrollableWidget(wanderingTraderCycleLimitSlider, y);
			
			List<Text> wanderingLimitTooltip = new ArrayList<>();
			wanderingLimitTooltip.add(Text.literal("Maximum times a wandering trader can be cycled."));
			wanderingLimitTooltip.add(Text.literal("-1 = Unlimited, 0 = Disabled, 1+ = Limited cycles").formatted(Formatting.GRAY));
			if (!isSingleplayer) {
				wanderingLimitTooltip.add(Text.literal("Server Admin Option - Operator level 4 required.").formatted(Formatting.GOLD));
			}
			wanderingLimitTooltip.add(Text.literal("Default: 1 (one cycle per trader)").formatted(Formatting.GRAY));
			addTooltip(widgetX, y, WIDGET_WIDTH, 20, wanderingLimitTooltip.toArray(new Text[0]));
			
			ButtonWidget resetWanderingLimitBtn = ButtonWidget.builder(Text.literal("↺"), button -> {
				wanderingTraderCycleLimitSlider.setValue(1, -1, 100);
				config.wanderingTraderCycleLimit = 1;
			}).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build();
			addScrollableWidget(resetWanderingLimitBtn, y);
			
			y += ROW_HEIGHT;
			
			// === Villager Cycle Limit Slider (Operator Only) ===
			villagerCycleLimitSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20,
				getCycleLimitText("Villager Cycle Limit", config.villagerCycleLimit),
				config.villagerCycleLimit, -1, 100) {
				@Override
				protected void updateMessage() {
					setMessage(getCycleLimitText("Villager Cycle Limit", getValue()));
				}
				@Override
				protected void applyValue() {
					config.villagerCycleLimit = getValue();
				}
			};
			addScrollableWidget(villagerCycleLimitSlider, y);
			
			List<Text> villagerLimitTooltip = new ArrayList<>();
			villagerLimitTooltip.add(Text.literal("Maximum times a villager can be cycled."));
			villagerLimitTooltip.add(Text.literal("-1 = Unlimited (default), 0 = Disabled, 1+ = Limited cycles").formatted(Formatting.GRAY));
			if (!isSingleplayer) {
				villagerLimitTooltip.add(Text.literal("Server Admin Option - Operator level 4 required.").formatted(Formatting.GOLD));
			}
			villagerLimitTooltip.add(Text.literal("Default: -1 (Unlimited)").formatted(Formatting.GRAY));
			addTooltip(widgetX, y, WIDGET_WIDTH, 20, villagerLimitTooltip.toArray(new Text[0]));
			
			ButtonWidget resetVillagerLimitBtn = ButtonWidget.builder(Text.literal("↺"), button -> {
				villagerCycleLimitSlider.setValue(-1, -1, 100);
				config.villagerCycleLimit = -1;
			}).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build();
			addScrollableWidget(resetVillagerLimitBtn, y);
			
			y += ROW_HEIGHT;
			
		} else {
			// Non-operator: show read-only info
			// Show disabled toggle
			allowWanderingTradersToggle = ButtonWidget.builder(
				Text.literal("Allow Wandering Traders: " + (config.allowWanderingTraders ? "ON" : "OFF")),
				button -> {
					// Revert and show error
					MinecraftClient client = MinecraftClient.getInstance();
					if (client.player != null) {
						client.player.sendMessage(
							Text.literal("❌ You need operator level 4 permission to change this setting.")
								.formatted(Formatting.RED),
							false
						);
					}
				}
			).dimensions(widgetX, y, WIDGET_WIDTH, 20).build();
			addScrollableWidget(allowWanderingTradersToggle, y);
			addTooltip(widgetX, y, WIDGET_WIDTH, 20,
				Text.literal("Allow cycling trades for wandering traders."),
				Text.literal("⚠ You do not have permission to change this setting.").formatted(Formatting.RED),
				Text.literal("Operator level 4 is required on multiplayer servers.").formatted(Formatting.GOLD));
			
			y += ROW_HEIGHT;
			
			// Show cycle limits as read-only text (no widget, just in tooltip area)
			// We'll add text description widgets for non-operators
		}
		
		y += 10; // Extra spacing before button appearance section
		
		// ============================================
		// SECTION: Button Appearance
		// ============================================
		
		// Note about drag button feature
		// (This is drawn as text in render(), not a widget)
		y += 15; // Space for the note
		
		// === Button Width Slider ===
		buttonWidthSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20,
			Text.literal("Button Width: " + config.buttonWidth),
			config.buttonWidth, 20, 200) {
			@Override
			protected void updateMessage() {
				setMessage(Text.literal("Button Width: " + getValue()));
			}
			@Override
			protected void applyValue() {
				config.buttonWidth = getValue();
			}
		};
		addScrollableWidget(buttonWidthSlider, y);
		addTooltip(widgetX, y, WIDGET_WIDTH, 20,
			Text.literal("Width of the button in pixels."),
			Text.literal("Range: 20-200").formatted(Formatting.GRAY),
			Text.literal("Default: 100").formatted(Formatting.GRAY));
		
		ButtonWidget resetWidthBtn = ButtonWidget.builder(Text.literal("↺"), button -> {
			buttonWidthSlider.setValue(100, 20, 200);
			config.buttonWidth = 100;
		}).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build();
		addScrollableWidget(resetWidthBtn, y);
		
		y += ROW_HEIGHT;
		
		// === Button Height Slider ===
		buttonHeightSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20,
			Text.literal("Button Height: " + config.buttonHeight),
			config.buttonHeight, 10, 100) {
			@Override
			protected void updateMessage() {
				setMessage(Text.literal("Button Height: " + getValue()));
			}
			@Override
			protected void applyValue() {
				config.buttonHeight = getValue();
			}
		};
		addScrollableWidget(buttonHeightSlider, y);
		addTooltip(widgetX, y, WIDGET_WIDTH, 20,
			Text.literal("Height of the button in pixels."),
			Text.literal("Range: 10-100").formatted(Formatting.GRAY),
			Text.literal("Default: 20").formatted(Formatting.GRAY));
		
		ButtonWidget resetHeightBtn = ButtonWidget.builder(Text.literal("↺"), button -> {
			buttonHeightSlider.setValue(20, 10, 100);
			config.buttonHeight = 20;
		}).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build();
		addScrollableWidget(resetHeightBtn, y);
		
		y += ROW_HEIGHT;
		
		// === Button Offset X Slider ===
		buttonOffsetXSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20,
			Text.literal("Button Offset X: " + config.buttonOffsetX),
			config.buttonOffsetX, -200, 200) {
			@Override
			protected void updateMessage() {
				setMessage(Text.literal("Button Offset X: " + getValue()));
			}
			@Override
			protected void applyValue() {
				config.buttonOffsetX = getValue();
			}
		};
		addScrollableWidget(buttonOffsetXSlider, y);
		addTooltip(widgetX, y, WIDGET_WIDTH, 20,
			Text.literal("Horizontal position offset from the left edge of the trading GUI."),
			Text.literal("Adjust if the button overlaps with other UI elements."),
			Text.literal("Default: 6").formatted(Formatting.GRAY));
		
		ButtonWidget resetOffsetXBtn = ButtonWidget.builder(Text.literal("↺"), button -> {
			buttonOffsetXSlider.setValue(6, -200, 200);
			config.buttonOffsetX = 6;
		}).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build();
		addScrollableWidget(resetOffsetXBtn, y);
		
		y += ROW_HEIGHT;
		
		// === Button Offset Y Slider ===
		buttonOffsetYSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20,
			Text.literal("Button Offset Y: " + config.buttonOffsetY),
			config.buttonOffsetY, -200, 200) {
			@Override
			protected void updateMessage() {
				setMessage(Text.literal("Button Offset Y: " + getValue()));
			}
			@Override
			protected void applyValue() {
				config.buttonOffsetY = getValue();
			}
		};
		addScrollableWidget(buttonOffsetYSlider, y);
		addTooltip(widgetX, y, WIDGET_WIDTH, 20,
			Text.literal("Vertical position offset from the top edge of the trading GUI."),
			Text.literal("Negative values place the button above the GUI."),
			Text.literal("Default: -25").formatted(Formatting.GRAY));
		
		ButtonWidget resetOffsetYBtn = ButtonWidget.builder(Text.literal("↺"), button -> {
			buttonOffsetYSlider.setValue(-25, -200, 200);
			config.buttonOffsetY = -25;
		}).dimensions(resetX, y, RESET_BTN_WIDTH, 20).build();
		addScrollableWidget(resetOffsetYBtn, y);
		
		y += ROW_HEIGHT;
		
		// Calculate scroll bounds
		contentHeight = y - startY;
		maxScrollOffset = Math.max(0, contentHeight - (this.height - HEADER_HEIGHT - FOOTER_HEIGHT) + 20);
		
		// ============================================
		// FOOTER BUTTONS (Fixed position, not scrollable)
		// ============================================
		int footerY = this.height - FOOTER_HEIGHT + 7;
		int buttonSpacing = 5;
		int buttonWidth = 90;
		int totalWidth = (buttonWidth * 3) + (buttonSpacing * 2);
		int startX = centerX - totalWidth / 2;
		
		// Save & Close button
		ButtonWidget saveBtn = ButtonWidget.builder(Text.literal("Save & Close"), button -> {
			saveConfig();
			close();
		}).dimensions(startX, footerY, buttonWidth, 20).build();
		this.addDrawableChild(saveBtn);
		footerButtons.add(saveBtn);
		
		// Key Binds button
		ButtonWidget keyBindsBtn = ButtonWidget.builder(Text.literal("Key Binds"), button -> {
			if (this.client != null) {
				this.client.setScreen(new KeybindsScreen(this, this.client.options));
			}
		}).dimensions(startX + buttonWidth + buttonSpacing, footerY, buttonWidth, 20).build();
		this.addDrawableChild(keyBindsBtn);
		footerButtons.add(keyBindsBtn);
		
		// Cancel button
		ButtonWidget cancelBtn = ButtonWidget.builder(Text.literal("Cancel"), button -> {
			// Revert non-operator changes
			if (!isSingleplayer && !isOperator) {
				config.allowWanderingTraders = originalWanderingTraderValue;
			}
			close();
		}).dimensions(startX + (buttonWidth + buttonSpacing) * 2, footerY, buttonWidth, 20).build();
		this.addDrawableChild(cancelBtn);
		footerButtons.add(cancelBtn);
		
		updateWidgetPositions();
	}
	
	private void addScrollableWidget(ClickableWidget widget, int originalY) {
		this.addDrawableChild(widget);
		scrollableWidgets.add(new WidgetEntry(widget, originalY));
	}
	
	private void addTooltip(int x, int y, int width, int height, Text... lines) {
		List<Text> tooltipLines = new ArrayList<>();
		for (Text line : lines) {
			tooltipLines.add(line);
		}
		tooltips.add(new TooltipEntry(x, y, width, height, tooltipLines));
	}
	
	private Text getCycleLimitText(String prefix, int value) {
		if (value < 0) return Text.literal(prefix + ": Unlimited");
		if (value == 0) return Text.literal(prefix + ": Disabled");
		return Text.literal(prefix + ": " + value);
	}
	
	private void saveConfig() {
		config.save();
		VillagerCycleConfig.reload();
		
		LOGGER.info("Config saved - allowWanderingTraders: {}, wanderingCycleLimit: {}, villagerCycleLimit: {}", 
			config.allowWanderingTraders, config.wanderingTraderCycleLimit, config.villagerCycleLimit);
		
		// Send packet to server with the config values
		if (ClientPlayNetworking.canSend(ReloadConfigPayload.ID)) {
			ClientPlayNetworking.send(new ReloadConfigPayload(
				config.allowWanderingTraders,
				config.wanderingTraderCycleLimit,
				config.villagerCycleLimit
			));
			LOGGER.info("Sent config update to server");
		}
	}
	
	private void updateWidgetPositions() {
		for (WidgetEntry entry : scrollableWidgets) {
			entry.widget.setY(entry.originalY - scrollOffset);
		}
	}
	
	// ========================================
	// Mouse wheel scrolling
	// ========================================
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (mouseY >= HEADER_HEIGHT && mouseY < this.height - FOOTER_HEIGHT && maxScrollOffset > 0) {
			scrollOffset -= (int)(verticalAmount * SCROLL_SPEED);
			scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
			updateWidgetPositions();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
	
	// ========================================
	// Scrollbar click handling
	// ========================================
	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		// Check if clicking on scrollbar
		if (button == 0 && maxScrollOffset > 0) {
			int scrollbarX = this.width - SCROLLBAR_WIDTH - 4;
			int scrollbarHeight = this.height - HEADER_HEIGHT - FOOTER_HEIGHT;
			
			if (mouseX >= scrollbarX && mouseX < scrollbarX + SCROLLBAR_WIDTH &&
				mouseY >= HEADER_HEIGHT && mouseY < this.height - FOOTER_HEIGHT) {
				
				int thumbHeight = Math.max(20, (int)((float)scrollbarHeight * scrollbarHeight / (scrollbarHeight + maxScrollOffset)));
				int thumbY = HEADER_HEIGHT + (int)((float)scrollOffset / maxScrollOffset * (scrollbarHeight - thumbHeight));
				
				if (mouseY >= thumbY && mouseY < thumbY + thumbHeight) {
					isDraggingScrollbar = true;
					scrollbarDragOffset = (int)(mouseY - thumbY);
					return true;
				} else {
					float ratio = (float)(mouseY - HEADER_HEIGHT - thumbHeight / 2) / (scrollbarHeight - thumbHeight);
					scrollOffset = (int)(ratio * maxScrollOffset);
					scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
					updateWidgetPositions();
					return true;
				}
			}
		}
		return super.mouseClicked(click, doubled);
	}
	
	@Override
	public boolean mouseDragged(Click click, double deltaX, double deltaY) {
		double mouseY = click.y();
		int button = click.button();
		if (isDraggingScrollbar && button == 0) {
			int scrollbarHeight = this.height - HEADER_HEIGHT - FOOTER_HEIGHT;
			int thumbHeight = Math.max(20, (int)((float)scrollbarHeight * scrollbarHeight / (scrollbarHeight + maxScrollOffset)));
			
			float newThumbY = (float)(mouseY - HEADER_HEIGHT - scrollbarDragOffset);
			float maxThumbY = scrollbarHeight - thumbHeight;
			float ratio = newThumbY / maxThumbY;
			
			scrollOffset = (int)(ratio * maxScrollOffset);
			scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
			updateWidgetPositions();
			return true;
		}
		return super.mouseDragged(click, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseReleased(Click click) {
		int button = click.button();
		if (button == 0) {
			isDraggingScrollbar = false;
		}
		return super.mouseReleased(click);
	}
	
	// ========================================
	// Rendering
	// ========================================
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		// Draw dark background
		context.fill(0, 0, this.width, this.height, 0xC0101010);
		
		// Draw title
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
		
		// Draw header separator
		context.fill(10, HEADER_HEIGHT - 2, this.width - 10, HEADER_HEIGHT - 1, 0xFF555555);
		
		// Enable scissoring for scrollable content area
		context.enableScissor(0, HEADER_HEIGHT, this.width, this.height - FOOTER_HEIGHT);
		
		// Render scrollable widgets only
		for (WidgetEntry entry : scrollableWidgets) {
			entry.widget.render(context, mouseX, mouseY, delta);
		}
		
		// Draw the "Note" text for button appearance section (calculate position based on scroll)
		int noteY = HEADER_HEIGHT + 10 + (ROW_HEIGHT * 3) + 10; // After first 3 options
		if (isSingleplayer || isOperator) {
			noteY += ROW_HEIGHT * 3 + 10; // After operator options
		} else {
			noteY += ROW_HEIGHT + 10; // After read-only toggle
		}
		int adjustedNoteY = noteY - scrollOffset;
		if (adjustedNoteY >= HEADER_HEIGHT - 10 && adjustedNoteY <= this.height - FOOTER_HEIGHT) {
			context.drawTextWithShadow(this.textRenderer, 
				Text.literal("Note: ").formatted(Formatting.GOLD)
					.append(Text.literal("Set a keybind to use the Drag Button feature.").formatted(Formatting.WHITE)),
				this.width / 2 - 140, adjustedNoteY, 0xFFFFFF);
		}
		
		// Disable scissoring
		context.disableScissor();
		
		// Draw footer separator
		context.fill(10, this.height - FOOTER_HEIGHT, this.width - 10, this.height - FOOTER_HEIGHT + 1, 0xFF555555);
		
		// Render footer buttons (OUTSIDE scissor region!)
		for (ClickableWidget button : footerButtons) {
			button.render(context, mouseX, mouseY, delta);
		}
		
		// Draw scrollbar if needed
		if (maxScrollOffset > 0) {
			int scrollbarX = this.width - SCROLLBAR_WIDTH - 4;
			int scrollbarHeight = this.height - HEADER_HEIGHT - FOOTER_HEIGHT;
			
			// Draw scrollbar track
			context.fill(scrollbarX, HEADER_HEIGHT, scrollbarX + SCROLLBAR_WIDTH, this.height - FOOTER_HEIGHT, 0xFF333333);
			
			// Draw scrollbar thumb
			int thumbHeight = Math.max(20, (int)((float)scrollbarHeight * scrollbarHeight / (scrollbarHeight + maxScrollOffset)));
			int thumbY = HEADER_HEIGHT + (int)((float)scrollOffset / maxScrollOffset * (scrollbarHeight - thumbHeight));
			
			int thumbColor = isDraggingScrollbar ? 0xFFAAAAAA : 0xFF666666;
			context.fill(scrollbarX, thumbY, scrollbarX + SCROLLBAR_WIDTH, thumbY + thumbHeight, thumbColor);
		}
		
		// Draw scroll indicators
		if (scrollOffset > 0) {
			context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("▲").formatted(Formatting.GRAY), 
				this.width / 2, HEADER_HEIGHT + 2, 0xFFFFFF);
		}
		if (scrollOffset < maxScrollOffset && maxScrollOffset > 0) {
			context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("▼").formatted(Formatting.GRAY), 
				this.width / 2, this.height - FOOTER_HEIGHT - 12, 0xFFFFFF);
		}
		
		// Check for tooltip (only in scrollable area)
		currentTooltip = null;
		if (mouseY >= HEADER_HEIGHT && mouseY < this.height - FOOTER_HEIGHT) {
			for (TooltipEntry entry : tooltips) {
				int adjustedY = entry.y - scrollOffset;
				if (mouseX >= entry.x && mouseX < entry.x + entry.width &&
					mouseY >= adjustedY && mouseY < adjustedY + entry.height &&
					adjustedY >= HEADER_HEIGHT && adjustedY + entry.height <= this.height - FOOTER_HEIGHT) {
					currentTooltip = entry.tooltip;
					break;
				}
			}
		}
		
		// Draw tooltip AFTER disabling scissor so it's not clipped
		if (currentTooltip != null && !currentTooltip.isEmpty()) {
			context.drawTooltip(this.textRenderer, currentTooltip, mouseX, mouseY);
		}
	}
	
	@Override
	public void close() {
		if (this.client != null) {
			this.client.setScreen(parent);
		}
	}
	
	@Override
	public boolean keyPressed(KeyInput input) {
		if (input.isEscape()) {
			close();
			return true;
		}
		return super.keyPressed(input);
	}
	
	/**
	 * Custom integer slider widget
	 */
	private abstract static class IntSlider extends SliderWidget {
		private final int min;
		private final int max;
		
		public IntSlider(int x, int y, int width, int height, Text text, int value, int min, int max) {
			super(x, y, width, height, text, (double)(value - min) / (max - min));
			this.min = min;
			this.max = max;
		}
		
		public int getValue() {
			return (int)(this.value * (max - min) + min);
		}
		
		public void setValue(int value, int min, int max) {
			this.value = (double)(value - min) / (max - min);
			updateMessage();
		}
	}
}
