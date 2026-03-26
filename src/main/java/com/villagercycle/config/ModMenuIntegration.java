package com.villagercycle.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModMenuIntegration implements ModMenuApi {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerCycle");

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> getConfigScreen(parent);
    }

    // Gets the appropriate config screen based on available dependencies.
    // Uses Cloth Config if available, otherwise falls back to vanilla screen.
    public static Screen getConfigScreen(Screen parent) {
        //? if <26.1 {
        /*// Try Cloth Config first
        if (isClothConfigAvailable()) {
            LOGGER.info("Cloth Config detected, using Cloth Config screen");
            try {
                return ModConfigScreen.createConfigScreen(parent);
            } catch (Throwable e) {
                LOGGER.warn("Failed to create Cloth Config screen, falling back", e);
            }
        }*/
        //?}

        LOGGER.info("Using fallback vanilla config screen");
        return new SimpleFallbackConfigScreen(parent);
    }

    //? if <26.1 {
    /*// Checks if Cloth Config is available at runtime.
    private static boolean isClothConfigAvailable() {
        try {
            Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }*/
    //?}
}