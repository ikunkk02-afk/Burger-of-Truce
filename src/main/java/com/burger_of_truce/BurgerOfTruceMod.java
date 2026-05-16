package com.burger_of_truce;

import com.burger_of_truce.item.ModItems;
import com.burger_of_truce.effect.ModMobEffects;
import com.burger_of_truce.villager.ModVillagers;
import com.burger_of_truce.world.BurgerWorldEvents;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BurgerOfTruceMod implements ModInitializer {
	public static final String MOD_ID = "burger-of-truce-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModMobEffects.register();
		ModItems.register();
		ModVillagers.register();
		BurgerWorldEvents.register();

		LOGGER.info("Burger of Truce first-phase systems loaded.");
	}
}
