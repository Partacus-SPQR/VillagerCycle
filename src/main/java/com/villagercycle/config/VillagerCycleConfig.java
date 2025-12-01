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
			LOGGER.info("Saving config - allowWanderingTraders: {}, enableCycleButton: {}", 
				allowWanderingTraders, enableCycleButton);
			GSON.toJson(this, writer);
			LOGGER.info("Config successfully written to file");
		} catch (IOException e) {
			LOGGER.error("Failed to save config", e);
		}
	}
	
	/**
	 * Force reload config from disk. Use this after saving to ensure
	 * the singleton instance is up-to-date with file changes.
	 */
	public static void reload() {
		if (CONFIG_FILE.exists()) {
			try (FileReader reader = new FileReader(CONFIG_FILE)) {
				INSTANCE = GSON.fromJson(reader, VillagerCycleConfig.class);
				LOGGER.info("Reloaded config from file - allowWanderingTraders: {}", INSTANCE.allowWanderingTraders);
			} catch (IOException e) {
				LOGGER.error("Failed to reload config", e);
			}
		}
	}
	
	public static VillagerCycleConfig getInstance() {
		if (INSTANCE == null) {
			return load();
		}
		return INSTANCE;
	}
}
