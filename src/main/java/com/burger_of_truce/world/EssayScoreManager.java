package com.burger_of_truce.world;

import com.burger_of_truce.BurgerAdvancements;
import com.burger_of_truce.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class EssayScoreManager {
	public static final String INITIAL_SCORE_PROMPT = "initial_score_prompt";
	public static final String FIRST_SCHOOL_BURGER = "first_school_burger";
	public static final String FIRST_REVERSED_ESSAY_PAGE = "first_reversed_essay_page";
	public static final String FIRST_REVERSED_ESSAY = "first_reversed_essay";
	public static final String FIRST_REVERSE_VIEW_SCROLL = "first_reverse_view_scroll";
	public static final String FIRST_TRUCE_BURGER = "first_truce_burger";
	public static final String SUCCESSFUL_TRUCE = "successful_truce";
	private static final String FULL_SCORE_MESSAGE = "full_score_message";
	private static final int PASSING_SCORE = 36;

	private EssayScoreManager() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(EssayScoreManager::tickServer);
	}

	public static void onPlayerJoin(ServerPlayer player) {
		BurgerWorldState state = BurgerWorldState.get(player.server);
		state.ensurePlayer(player.getUUID());
		if (state.markEssayMilestone(player.getUUID(), INITIAL_SCORE_PROMPT)) {
			player.sendSystemMessage(Component.literal("你的作文初始得分：2 / 60").withStyle(ChatFormatting.AQUA));
		}
		checkAdvancementThresholds(player, state.getEssayScore(player.getUUID()));
	}

	public static void awardOnce(ServerPlayer player, String milestone, int amount) {
		BurgerWorldState state = BurgerWorldState.get(player.server);
		state.ensurePlayer(player.getUUID());
		if (!state.markEssayMilestone(player.getUUID(), milestone)) {
			return;
		}

		int score = state.addEssayScore(player.getUUID(), amount);
		player.sendSystemMessage(Component.literal("作文分数 +" + amount + "，当前分数：" + score + " / 60").withStyle(ChatFormatting.GOLD));
		checkAdvancementThresholds(player, score);
		if (score >= 60 && state.markEssayMilestone(player.getUUID(), FULL_SCORE_MESSAGE)) {
			player.sendSystemMessage(Component.literal("满分作文诞生了。但世界已经看不懂你了。").withStyle(ChatFormatting.LIGHT_PURPLE));
		}
	}

	private static void tickServer(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			BurgerWorldState.get(server).ensurePlayer(player.getUUID());
			if (hasItem(player, ModItems.REVERSED_ESSAY_PAGE)) {
				awardOnce(player, FIRST_REVERSED_ESSAY_PAGE, 5);
			}
		}
	}

	private static void checkAdvancementThresholds(ServerPlayer player, int score) {
		if (score >= 2) {
			BurgerAdvancements.award(player, BurgerAdvancements.ESSAY_TWO_POINTS);
		}
		if (score >= PASSING_SCORE) {
			BurgerAdvancements.award(player, BurgerAdvancements.ESSAY_PASSED);
		}
		if (score >= 60) {
			BurgerAdvancements.award(player, BurgerAdvancements.FULL_SCORE_ESSAY);
		}
	}

	private static boolean hasItem(ServerPlayer player, net.minecraft.world.item.Item item) {
		for (ItemStack stack : player.getInventory().items) {
			if (stack.is(item)) {
				return true;
			}
		}
		return false;
	}
}
