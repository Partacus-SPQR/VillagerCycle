package com.villagercycle.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class VillagerCycleConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("VillagerCycleConfig");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "villagercycle.json");
	
	private static VillagerCycleConfig INSTANCE;
	
	// Config options
	public boolean enableCycleButton = true;
	public boolean allowWanderingTraders = false;
	public int buttonOffsetX = 6;
	public int buttonOffsetY = -25;
	public int buttonWidth = 100;
	public int buttonHeight = 20;
	
	public static VillagerCycleConfig load() {
		if (INSTANCE == null) {
			if (CONFIG_FILE.exists()) {
				try (FileReader reader = new FileReader(CONFIG_FILE)) {
					INSTANCE = GSON.fromJson(reader, VillagerCycleConfig.class);
					LOGGER.info("Loaded config from file");
				} catch (IOException e) {
					LOGGER.error("Failed to load config, using defaults", e);
					INSTANCE = new VillagerCycleConfig();
				}
			} else {
				INSTANCE = new VillagerCycleConfig();
				INSTANCE.save();
				LOGGER.info("Created new config file with defaults");
			}
		}
		return INSTANCE;
	}
	
	public void save() {
		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			GSON.toJson(this, writer);
			LOGGER.info("Saved config to file");
		} catch (IOException e) {
			LOGGER.error("Failed to save config", e);
		}
	}
	
	public static VillagerCycleConfig getInstance() {
		if (INSTANCE == null) {
			return load();
		}
		return INSTANCE;
	}
}
