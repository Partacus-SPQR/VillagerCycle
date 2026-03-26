package com.villagercycle.client.screen;

//? if <26.1 {
/*import net.minecraft.client.gui.GuiGraphics;*/
//?} else {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?}
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class VillagerCycleScreen extends AbstractContainerScreen<AbstractContainerMenu> {

    public VillagerCycleScreen(AbstractContainerMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    //? if <26.1 {
    /*@Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        // Background rendering if needed
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.renderTooltip(context, mouseX, mouseY);
    }*/
    //?}
}