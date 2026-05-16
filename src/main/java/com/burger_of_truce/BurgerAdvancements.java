package com.burger_of_truce;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class BurgerAdvancements {
	public static final ResourceLocation EAT_SCHOOL_BURGER = id("eat_school_burger");
	public static final ResourceLocation LOW_HUMANITY = id("low_humanity");
	public static final ResourceLocation CRAFT_TRUCE_BURGER = id("craft_truce_burger");
	public static final ResourceLocation STOP_WAR_SUCCESS = id("stop_war_success");
	public static final ResourceLocation ESSAY_TWO_POINTS = id("essay_two_points");
	public static final ResourceLocation ESSAY_PASSED = id("essay_passed");
	public static final ResourceLocation FULL_SCORE_ESSAY = id("full_score_essay");

	private BurgerAdvancements() {
	}

	public static void award(ServerPlayer player, ResourceLocation advancementId) {
		AdvancementHolder advancement = player.server.getAdvancements().get(advancementId);
		if (advancement == null) {
			return;
		}

		AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
		if (progress.isDone()) {
			return;
		}

		for (String criterion : progress.getRemainingCriteria()) {
			player.getAdvancements().award(advancement, criterion);
		}
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(BurgerOfTruceMod.MOD_ID, path);
	}
}
