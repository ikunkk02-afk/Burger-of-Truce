package com.burger_of_truce.effect;

import com.burger_of_truce.BurgerOfTruceMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public final class ModMobEffects {
	public static final Holder<MobEffect> REVERSE_VIEW = register("reverse_view", new ReverseViewMobEffect());

	private ModMobEffects() {
	}

	public static void register() {
		BurgerOfTruceMod.LOGGER.info("Registered Burger of Truce mob effects.");
	}

	private static Holder<MobEffect> register(String path, MobEffect effect) {
		return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BurgerOfTruceMod.MOD_ID, path), effect);
	}
}
