package com.villagercycle.config;

import com.villagercycle.network.ReloadConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import com.villagercycle.compat.ScreenCompat;
//? if <26.1 {
/*import net.minecraft.client.gui.GuiGraphics;*/
//?} else {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
//? if >=1.21.11 {
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
//?}
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

// Fallback config screen using vanilla Minecraft widgets.
// Used when Cloth Config is not installed.
//
// MANDATORY features per guide:
// - Sliders for all numeric values
// - Tooltips for all options
// - Reset buttons for all options
// - Scrollable content with interactive scrollbar
// - Footer buttons: Save & Close | Key Binds | Cancel
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
        private record TooltipEntry(int x, int y, int width, int height, List<Component> tooltip) {}
        private final List<TooltipEntry> tooltips = new ArrayList<>();
        private List<Component> currentTooltip = null;

        // Track scrollable widgets with their original Y positions
        private record WidgetEntry(AbstractWidget widget, int originalY) {}
        private final List<WidgetEntry> scrollableWidgets = new ArrayList<>();

        // Track footer buttons (non-scrollable)
        private final List<AbstractWidget> footerButtons = new ArrayList<>();

        // Widget references for updating
        private Button enableCycleButtonToggle;
        private Button showSuccessMessageToggle;
        private Button showWanderingTraderSuccessMessageToggle;
        private Button allowWanderingTradersToggle;
        private CycleLimitSlider wanderingTraderCycleLimitSlider;
        private CycleLimitSlider villagerCycleLimitSlider;
        private IntSlider buttonWidthSlider;
        private IntSlider buttonHeightSlider;
        private IntSlider buttonOffsetXSlider;
        private IntSlider buttonOffsetYSlider;

        // Permission state
        private boolean isOperator = false;
        private boolean isSingleplayer = false;
        private final boolean originalWanderingTraderValue;

        public SimpleFallbackConfigScreen(Screen parent) {
                super(Component.literal("Villager Cycle Configuration"));
                this.parent = parent;
                this.config = VillagerCycleConfig.getInstance();
                this.originalWanderingTraderValue = config.allowWanderingTraders;

                // Check permissions
                Minecraft client = Minecraft.getInstance();
                if (client.player != null) {
                        isSingleplayer = client.isLocalServer();
                        //? if >=1.21.11 {
                        isOperator = client.player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.OWNERS));
                        //?} else {
                        /*isOperator = client.player.getPermissionLevel() >= 4;*/
                        //?}
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
                enableCycleButtonToggle = Button.builder(
                        Component.literal("Enable Cycle Button: " + (config.enableCycleButton ? "ON" : "OFF")),
                        button -> {
                                config.enableCycleButton = !config.enableCycleButton;
                                button.setMessage(Component.literal("Enable Cycle Button: " + (config.enableCycleButton ? "ON" : "OFF")));
                        }
                ).bounds(widgetX, y, WIDGET_WIDTH, 20).build();
                addScrollableWidget(enableCycleButtonToggle, y);
                addTooltip(widgetX, y, WIDGET_WIDTH, 20,
                        Component.literal("Toggles the entire mod on or off."),
                        Component.literal("Default: ON").withStyle(ChatFormatting.GRAY));

                Button resetEnableBtn = Button.builder(Component.literal("\u21BA"), button -> {
                        config.enableCycleButton = true;
                        enableCycleButtonToggle.setMessage(Component.literal("Enable Cycle Button: ON"));
                }).bounds(resetX, y, RESET_BTN_WIDTH, 20).build();
                addScrollableWidget(resetEnableBtn, y);

                y += ROW_HEIGHT;

                // === Show Villager Success Message Toggle ===
                showSuccessMessageToggle = Button.builder(
                        Component.literal("Show Villager Success Msg: " + (config.showSuccessMessage ? "ON" : "OFF")),
                        button -> {
                                config.showSuccessMessage = !config.showSuccessMessage;
                                button.setMessage(Component.literal("Show Villager Success Msg: " + (config.showSuccessMessage ? "ON" : "OFF")));
                        }
                ).bounds(widgetX, y, WIDGET_WIDTH, 20).build();
                addScrollableWidget(showSuccessMessageToggle, y);
                addTooltip(widgetX, y, WIDGET_WIDTH, 20,
                        Component.literal("Show a success message in chat when villager trades are cycled."),
                        Component.literal("Disable to reduce chat spam.").withStyle(ChatFormatting.GRAY),
                        Component.literal("Default: ON").withStyle(ChatFormatting.GRAY));

                Button resetVillagerMsgBtn = Button.builder(Component.literal("\u21BA"), button -> {
                        config.showSuccessMessage = true;
                        showSuccessMessageToggle.setMessage(Component.literal("Show Villager Success Msg: ON"));
                }).bounds(resetX, y, RESET_BTN_WIDTH, 20).build();
                addScrollableWidget(resetVillagerMsgBtn, y);

                y += ROW_HEIGHT;

                // === Show Wandering Trader Success Message Toggle ===
                showWanderingTraderSuccessMessageToggle = Button.builder(
                        Component.literal("Show Trader Success Msg: " + (config.showWanderingTraderSuccessMessage ? "ON" : "OFF")),
                        button -> {
                                config.showWanderingTraderSuccessMessage = !config.showWanderingTraderSuccessMessage;
                                button.setMessage(Component.literal("Show Trader Success Msg: " + (config.showWanderingTraderSuccessMessage ? "ON" : "OFF")));
                        }
                ).bounds(widgetX, y, WIDGET_WIDTH, 20).build();
                addScrollableWidget(showWanderingTraderSuccessMessageToggle, y);
                addTooltip(widgetX, y, WIDGET_WIDTH, 20,
                        Component.literal("Show a success message in chat when wandering trader offers are cycled."),
                        Component.literal("Disable to reduce chat spam.").withStyle(ChatFormatting.GRAY),
                        Component.literal("Default: ON").withStyle(ChatFormatting.GRAY));

                Button resetTraderMsgBtn = Button.builder(Component.literal("\u21BA"), button -> {
                        config.showWanderingTraderSuccessMessage = true;
                        showWanderingTraderSuccessMessageToggle.setMessage(Component.literal("Show Trader Success Msg: ON"));
                }).bounds(resetX, y, RESET_BTN_WIDTH, 20).build();
                addScrollableWidget(resetTraderMsgBtn, y);

                y += ROW_HEIGHT + 10; // Extra spacing before operator section

                // ============================================
                // SECTION: Operator Options (with permission check)
                // ============================================

                if (isSingleplayer || isOperator) {
                        // === Allow Wandering Traders Toggle (Operator Only) ===
                        allowWanderingTradersToggle = Button.builder(
                                Component.literal("Allow Wandering Traders: " + (config.allowWanderingTraders ? "ON" : "OFF")),
                                button -> {
                                        config.allowWanderingTraders = !config.allowWanderingTraders;
                                        button.setMessage(Component.literal("Allow Wandering Traders: " + (config.allowWanderingTraders ? "ON" : "OFF")));

                                        Minecraft client = Minecraft.getInstance();
                                        if (client.player != null) {
                                                String playerName = client.player.getName().getString();
                                                LOGGER.info("Operator {} {} wandering trader cycling",
                                                        playerName, config.allowWanderingTraders ? "enabled" : "disabled");
                                                //? if >=26.1 {
                                                client.player.sendSystemMessage(
                                                        Component.literal("Wandering trader cycling is now " +
                                                                (config.allowWanderingTraders ? "ENABLED" : "DISABLED"))
                                                                .withStyle(config.allowWanderingTraders ? ChatFormatting.GREEN : ChatFormatting.RED)
                                                );
                                                //? } else {
                                                /*client.player.displayClientMessage(
                                                        Component.literal("Wandering trader cycling is now " +
                                                                (config.allowWanderingTraders ? "ENABLED" : "DISABLED"))
                                                                .withStyle(config.allowWanderingTraders ? ChatFormatting.GREEN : ChatFormatting.RED),
                                                        false
                                                );*/
                                                //? }
                                        }
                                }
                        ).bounds(widgetX, y, WIDGET_WIDTH, 20).build();
                        addScrollableWidget(allowWanderingTradersToggle, y);

                        List<Component> wanderingTooltip = new ArrayList<>();
                        wanderingTooltip.add(Component.literal("Allow cycling trades for wandering traders."));
                        wanderingTooltip.add(Component.literal("Note: Wandering trader offers will be completely refreshed."));
                        if (!isSingleplayer) {
                                wanderingTooltip.add(Component.literal("Server Admin Option - Operator level 4 required.").withStyle(ChatFormatting.GOLD));
                        }
                        wanderingTooltip.add(Component.literal("Default: OFF").withStyle(ChatFormatting.GRAY));
                        addTooltip(widgetX, y, WIDGET_WIDTH, 20, wanderingTooltip.toArray(new Component[0]));

                        Button resetWanderingBtn = Button.builder(Component.literal("\u21BA"), button -> {
                                config.allowWanderingTraders = false;
                                allowWanderingTradersToggle.setMessage(Component.literal("Allow Wandering Traders: OFF"));
                        }).bounds(resetX, y, RESET_BTN_WIDTH, 20).build();
                        addScrollableWidget(resetWanderingBtn, y);

                        y += ROW_HEIGHT;

                        // === Wandering Trader Cycle Limit Slider (Operator Only) ===
                        wanderingTraderCycleLimitSlider = new CycleLimitSlider(widgetX, y, WIDGET_WIDTH, 20,
                                getCycleLimitText("Wandering Trader Limit", config.wanderingTraderCycleLimit),
                                config.wanderingTraderCycleLimit, 100) {
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

                        List<Component> wanderingLimitTooltip = new ArrayList<>();
                        wanderingLimitTooltip.add(Component.literal("Maximum times a wandering trader can be cycled."));
                        wanderingLimitTooltip.add(Component.literal("-1 = Unlimited, 0 = Disabled, 1+ = Limited cycles").withStyle(ChatFormatting.GRAY));
                        if (!isSingleplayer) {
                                wanderingLimitTooltip.add(Component.literal("Server Admin Option - Operator level 4 required.").withStyle(ChatFormatting.GOLD));
                        }
                        wanderingLimitTooltip.add(Component.literal("Default: 1 (one cycle per trader)").withStyle(ChatFormatting.GRAY));
                        addTooltip(widgetX, y, WIDGET_WIDTH, 20, wanderingLimitTooltip.toArray(new Component[0]));

                        Button resetWanderingLimitBtn = Button.builder(Component.literal("\u21BA"), button -> {
                                wanderingTraderCycleLimitSlider.setCycleValue(1);
                                config.wanderingTraderCycleLimit = 1;
                        }).bounds(resetX, y, RESET_BTN_WIDTH, 20).build();
                        addScrollableWidget(resetWanderingLimitBtn, y);

                        y += ROW_HEIGHT;

                        // === Villager Cycle Limit Slider (Operator Only) ===
                        villagerCycleLimitSlider = new CycleLimitSlider(widgetX, y, WIDGET_WIDTH, 20,
                                getCycleLimitText("Villager Cycle Limit", config.villagerCycleLimit),
                                config.villagerCycleLimit, 100) {
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

                        List<Component> villagerLimitTooltip = new ArrayList<>();
                        villagerLimitTooltip.add(Component.literal("Maximum times a villager can be cycled."));
                        villagerLimitTooltip.add(Component.literal("-1 = Unlimited (default), 0 = Disabled, 1+ = Limited cycles").withStyle(ChatFormatting.GRAY));
                        if (!isSingleplayer) {
                                villagerLimitTooltip.add(Component.literal("Server Admin Option - Operator level 4 required.").withStyle(ChatFormatting.GOLD));
                        }
                        villagerLimitTooltip.add(Component.literal("Default: -1 (Unlimited)").withStyle(ChatFormatting.GRAY));
                        addTooltip(widgetX, y, WIDGET_WIDTH, 20, villagerLimitTooltip.toArray(new Component[0]));

                        Button resetVillagerLimitBtn = Button.builder(Component.literal("\u21BA"), button -> {
                                villagerCycleLimitSlider.setCycleValue(-1);
                                config.villagerCycleLimit = -1;
                        }).bounds(resetX, y, RESET_BTN_WIDTH, 20).build();
                        addScrollableWidget(resetVillagerLimitBtn, y);

                        y += ROW_HEIGHT;

                } else {
                        // Non-operator: show read-only info
                        // Show disabled toggle
                        allowWanderingTradersToggle = Button.builder(
                                Component.literal("Allow Wandering Traders: " + (config.allowWanderingTraders ? "ON" : "OFF")),
                                button -> {
                                        // Revert and show error
                                        Minecraft client = Minecraft.getInstance();
                                        if (client.player != null) {
                                                //? if >=26.1 {
                                                client.player.sendSystemMessage(
                                                        Component.literal("\u274C You need operator level 4 permission to change this setting.")
                                                                .withStyle(ChatFormatting.RED)
                                                );
                                                //? } else {
                                                /*client.player.displayClientMessage(
                                                        Component.literal("\u274C You need operator level 4 permission to change this setting.")
                                                                .withStyle(ChatFormatting.RED),
                                                        false
                                                );*/
                                                //? }
                                        }
                                }
                        ).bounds(widgetX, y, WIDGET_WIDTH, 20).build();
                        addScrollableWidget(allowWanderingTradersToggle, y);
                        addTooltip(widgetX, y, WIDGET_WIDTH, 20,
                                Component.literal("Allow cycling trades for wandering traders."),
                                Component.literal("\u26A0 You do not have permission to change this setting.").withStyle(ChatFormatting.RED),
                                Component.literal("Operator level 4 is required on multiplayer servers.").withStyle(ChatFormatting.GOLD));
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
                        Component.literal("Button Width: " + config.buttonWidth),
                        config.buttonWidth, 20, 200) {
                        @Override
                        protected void updateMessage() {
                                setMessage(Component.literal("Button Width: " + getValue()));
                        }
                        @Override
                        protected void applyValue() {
                                config.buttonWidth = getValue();
                        }
                };
                addScrollableWidget(buttonWidthSlider, y);
                addTooltip(widgetX, y, WIDGET_WIDTH, 20,
                        Component.literal("Width of the button in pixels."),
                        Component.literal("Range: 20-200").withStyle(ChatFormatting.GRAY),
                        Component.literal("Default: 100").withStyle(ChatFormatting.GRAY));

                Button resetWidthBtn = Button.builder(Component.literal("\u21BA"), button -> {
                        buttonWidthSlider.setValue(100, 20, 200);
                        config.buttonWidth = 100;
                }).bounds(resetX, y, RESET_BTN_WIDTH, 20).build();
                addScrollableWidget(resetWidthBtn, y);

                y += ROW_HEIGHT;

                // === Button Height Slider ===
                buttonHeightSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20,
                        Component.literal("Button Height: " + config.buttonHeight),
                        config.buttonHeight, 10, 100) {
                        @Override
                        protected void updateMessage() {
                                setMessage(Component.literal("Button Height: " + getValue()));
                        }
                        @Override
                        protected void applyValue() {
                                config.buttonHeight = getValue();
                        }
                };
                addScrollableWidget(buttonHeightSlider, y);
                addTooltip(widgetX, y, WIDGET_WIDTH, 20,
                        Component.literal("Height of the button in pixels."),
                        Component.literal("Range: 10-100").withStyle(ChatFormatting.GRAY),
                        Component.literal("Default: 20").withStyle(ChatFormatting.GRAY));

                Button resetHeightBtn = Button.builder(Component.literal("\u21BA"), button -> {
                        buttonHeightSlider.setValue(20, 10, 100);
                        config.buttonHeight = 20;
                }).bounds(resetX, y, RESET_BTN_WIDTH, 20).build();
                addScrollableWidget(resetHeightBtn, y);

                y += ROW_HEIGHT;

                // === Button Offset X Slider ===
                buttonOffsetXSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20,
                        Component.literal("Button Offset X: " + config.buttonOffsetX),
                        config.buttonOffsetX, -200, 200) {
                        @Override
                        protected void updateMessage() {
                                setMessage(Component.literal("Button Offset X: " + getValue()));
                        }
                        @Override
                        protected void applyValue() {
                                config.buttonOffsetX = getValue();
                        }
                };
                addScrollableWidget(buttonOffsetXSlider, y);
                addTooltip(widgetX, y, WIDGET_WIDTH, 20,
                        Component.literal("Horizontal position offset from the left edge of the trading GUI."),
                        Component.literal("Adjust if the button overlaps with other UI elements."),
                        Component.literal("Default: 6").withStyle(ChatFormatting.GRAY));

                Button resetOffsetXBtn = Button.builder(Component.literal("\u21BA"), button -> {
                        buttonOffsetXSlider.setValue(6, -200, 200);
                        config.buttonOffsetX = 6;
                }).bounds(resetX, y, RESET_BTN_WIDTH, 20).build();
                addScrollableWidget(resetOffsetXBtn, y);

                y += ROW_HEIGHT;

                // === Button Offset Y Slider ===
                buttonOffsetYSlider = new IntSlider(widgetX, y, WIDGET_WIDTH, 20,
                        Component.literal("Button Offset Y: " + config.buttonOffsetY),
                        config.buttonOffsetY, -200, 200) {
                        @Override
                        protected void updateMessage() {
                                setMessage(Component.literal("Button Offset Y: " + getValue()));
                        }
                        @Override
                        protected void applyValue() {
                                config.buttonOffsetY = getValue();
                        }
                };
                addScrollableWidget(buttonOffsetYSlider, y);
                addTooltip(widgetX, y, WIDGET_WIDTH, 20,
                        Component.literal("Vertical position offset from the top edge of the trading GUI."),
                        Component.literal("Negative values place the button above the GUI."),
                        Component.literal("Default: -25").withStyle(ChatFormatting.GRAY));

                Button resetOffsetYBtn = Button.builder(Component.literal("\u21BA"), button -> {
                        buttonOffsetYSlider.setValue(-25, -200, 200);
                        config.buttonOffsetY = -25;
                }).bounds(resetX, y, RESET_BTN_WIDTH, 20).build();
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
                Button saveBtn = Button.builder(Component.literal("Save & Close"), button -> {
                        saveConfig();
                        onClose();
                }).bounds(startX, footerY, buttonWidth, 20).build();
                this.addRenderableWidget(saveBtn);
                footerButtons.add(saveBtn);

                // Key Binds button
                Button keyBindsBtn = Button.builder(Component.literal("Key Binds"), button -> {
                        if (this.minecraft != null) {
                                ScreenCompat.open(this.minecraft, new KeyBindsScreen(this, this.minecraft.options));
                        }
                }).bounds(startX + buttonWidth + buttonSpacing, footerY, buttonWidth, 20).build();
                this.addRenderableWidget(keyBindsBtn);
                footerButtons.add(keyBindsBtn);

                // Cancel button
                Button cancelBtn = Button.builder(Component.literal("Cancel"), button -> {
                        // Revert non-operator changes
                        if (!isSingleplayer && !isOperator) {
                                config.allowWanderingTraders = originalWanderingTraderValue;
                        }
                        onClose();
                }).bounds(startX + (buttonWidth + buttonSpacing) * 2, footerY, buttonWidth, 20).build();
                this.addRenderableWidget(cancelBtn);
                footerButtons.add(cancelBtn);

                updateWidgetPositions();
        }

        private void addScrollableWidget(AbstractWidget widget, int originalY) {
                this.addRenderableWidget(widget);
                scrollableWidgets.add(new WidgetEntry(widget, originalY));
        }

        private void addTooltip(int x, int y, int width, int height, Component... lines) {
                List<Component> tooltipLines = new ArrayList<>();
                for (Component line : lines) {
                        tooltipLines.add(line);
                }
                tooltips.add(new TooltipEntry(x, y, width, height, tooltipLines));
        }

        private Component getCycleLimitText(String prefix, int value) {
                if (value < 0) return Component.literal(prefix + ": Unlimited");
                if (value == 0) return Component.literal(prefix + ": Disabled");
                return Component.literal(prefix + ": " + value);
        }

        private void saveConfig() {
                config.save();
                VillagerCycleConfig.reload();

                LOGGER.info("Config saved - allowWanderingTraders: {}, wanderingCycleLimit: {}, villagerCycleLimit: {}",
                        config.allowWanderingTraders, config.wanderingTraderCycleLimit, config.villagerCycleLimit);

                // Send packet to server with the config values
                if (ClientPlayNetworking.canSend(ReloadConfigPayload.TYPE)) {
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
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
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
        public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
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
        public boolean mouseReleased(MouseButtonEvent click) {
                int button = click.button();
                if (button == 0) {
                        isDraggingScrollbar = false;
                }
                return super.mouseReleased(click);
        }

        // ========================================
        // Rendering
        // ========================================
        //? if <26.1 {
        /*@Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
                // Draw dark background
                context.fill(0, 0, this.width, this.height, 0xC0101010);

                // Draw title
                context.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);

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
                        context.drawString(this.font,
                                Component.literal("Note: ").withStyle(ChatFormatting.GOLD)
                                        .append(Component.literal("Set a keybind to use the Drag Button feature.").withStyle(ChatFormatting.WHITE)),
                                this.width / 2 - 140, adjustedNoteY, 0xFFFFFF);
                }

                // Disable scissoring
                context.disableScissor();

                // Draw footer separator
                context.fill(10, this.height - FOOTER_HEIGHT, this.width - 10, this.height - FOOTER_HEIGHT + 1, 0xFF555555);

                // Render footer buttons (OUTSIDE scissor region!)
                for (AbstractWidget button : footerButtons) {
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
                        context.drawCenteredString(this.font, Component.literal("\u25B2").withStyle(ChatFormatting.GRAY),
                                this.width / 2, HEADER_HEIGHT + 2, 0xFFFFFF);
                }
                if (scrollOffset < maxScrollOffset && maxScrollOffset > 0) {
                        context.drawCenteredString(this.font, Component.literal("\u25BC").withStyle(ChatFormatting.GRAY),
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
                        context.setComponentTooltipForNextFrame(this.font, currentTooltip, mouseX, mouseY);
                }
        }*/
        //?} else {
        @Override
        public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
                // Draw dark background
                context.fill(0, 0, this.width, this.height, 0xC0101010);

                // Draw title
                context.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFF);

                // Draw header separator
                context.fill(10, HEADER_HEIGHT - 2, this.width - 10, HEADER_HEIGHT - 1, 0xFF555555);

                // Enable scissoring for scrollable content area
                context.enableScissor(0, HEADER_HEIGHT, this.width, this.height - FOOTER_HEIGHT);

                // Render scrollable widgets only
                for (WidgetEntry entry : scrollableWidgets) {
                        entry.widget.extractRenderState(context, mouseX, mouseY, delta);
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
                        context.text(this.font,
                                Component.literal("Note: ").withStyle(ChatFormatting.GOLD)
                                        .append(Component.literal("Set a keybind to use the Drag Button feature.").withStyle(ChatFormatting.WHITE)),
                                this.width / 2 - 140, adjustedNoteY, 0xFFFFFF);
                }

                // Disable scissoring
                context.disableScissor();

                // Draw footer separator
                context.fill(10, this.height - FOOTER_HEIGHT, this.width - 10, this.height - FOOTER_HEIGHT + 1, 0xFF555555);

                // Render footer buttons (OUTSIDE scissor region!)
                for (AbstractWidget button : footerButtons) {
                        button.extractRenderState(context, mouseX, mouseY, delta);
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
                        context.centeredText(this.font, Component.literal("\u25B2").withStyle(ChatFormatting.GRAY),
                                this.width / 2, HEADER_HEIGHT + 2, 0xFFFFFF);
                }
                if (scrollOffset < maxScrollOffset && maxScrollOffset > 0) {
                        context.centeredText(this.font, Component.literal("\u25BC").withStyle(ChatFormatting.GRAY),
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
                        context.setComponentTooltipForNextFrame(this.font, currentTooltip, mouseX, mouseY);
                }
        }
        //?}

        @Override
        public void onClose() {
                if (this.minecraft != null) {
                        ScreenCompat.open(this.minecraft, parent);
                }
        }

        @Override
        public boolean keyPressed(KeyEvent input) {
                if (input.key() == 256) { // GLFW_KEY_ESCAPE
                        onClose();
                        return true;
                }
                return super.keyPressed(input);
        }

        // Custom integer slider widget
        private abstract static class IntSlider extends AbstractSliderButton {
                private final int min;
                private final int max;

                public IntSlider(int x, int y, int width, int height, Component text, int value, int min, int max) {
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

        // Slider for cycle limits with every option reliably selectable.
        // Order left -> right: Disabled (0), 1, 2, ..., maxLimit, Unlimited (-1).
        private abstract static class CycleLimitSlider extends AbstractSliderButton {
                private final int maxLimit;

                public CycleLimitSlider(int x, int y, int width, int height, Component text, int value, int maxLimit) {
                        super(x, y, width, height, text, positionFor(value, maxLimit));
                        this.maxLimit = maxLimit;
                }

                private static double positionFor(int value, int maxLimit) {
                        int steps = maxLimit + 1;            // index range 0..steps
                        int index = (value < 0) ? steps : Math.min(value, maxLimit);
                        return (double) index / steps;
                }

                // Far-left = 0 (Disabled); far-right = Unlimited (-1); in between = 1..maxLimit.
                public int getValue() {
                        int steps = maxLimit + 1;
                        int index = (int) Math.round(this.value * steps);
                        return index >= steps ? -1 : index;
                }

                public void setCycleValue(int value) {
                        this.value = positionFor(value, maxLimit);
                        updateMessage();
                }
        }
}