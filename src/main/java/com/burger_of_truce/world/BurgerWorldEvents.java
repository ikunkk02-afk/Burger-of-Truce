package com.burger_of_truce.world;

import com.burger_of_truce.BurgerAdvancements;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class BurgerWorldEvents {
	private static final int LOW_FOOD_THRESHOLD = 6;
	private static final int HUMANITY_LOSS_INTERVAL_TICKS = 15 * 20;
	private static final int HUMANITY_LOSS_AMOUNT = 2;
	private static final int LOW_HUMANITY_THRESHOLD = 30;
	private static final int LOW_HUMANITY_WARNING_COOLDOWN_TICKS = 35 * 20;
	private static final String[] LOW_HUMANITY_MESSAGES = {
			"你听见操场下方传来空餐盘的回声。",
			"风把面包屑吹成一条很直的线。",
			"有人在远处数数，可数字从三开始倒着走。",
			"你的影子闻起来像一份没有交上去的午餐。",
			"红色的岩层短暂地学会了呼吸。"
	};

	private static final Map<UUID, Integer> hungerTimers = new HashMap<>();
	private static final Map<UUID, Integer> lowHumanityWarningTimers = new HashMap<>();

	private BurgerWorldEvents() {
	}

	public static void register() {
		RedRockKingBossManager.register();
		ReverseViewManager.register();
		EssayScoreManager.register();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.player;
			BurgerWorldState state = BurgerWorldState.get(server);
			state.ensurePlayer(player.getUUID());
			EssayScoreManager.onPlayerJoin(player);
			startCountdownIfNeeded(server, state);
		});

		ServerTickEvents.END_SERVER_TICK.register(BurgerWorldEvents::tickServer);
	}

	private static void tickServer(MinecraftServer server) {
		BurgerWorldState state = BurgerWorldState.get(server);
		if (!server.getPlayerList().getPlayers().isEmpty()) {
			startCountdownIfNeeded(server, state);
		}

		tickCountdown(server, state);
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			state.ensurePlayer(player.getUUID());
			tickHumanity(player, state);
		}
	}

	private static void startCountdownIfNeeded(MinecraftServer server, BurgerWorldState state) {
		if (state.isCountdownStarted()) {
			return;
		}

		long currentDay = server.overworld().getDayTime() / 24000L;
		state.startCountdown(currentDay);
		server.getPlayerList().broadcastSystemMessage(Component.literal("红岩王的影子被第一位访客惊醒。三日之后，世界将要求一份答案。").withStyle(ChatFormatting.RED), false);
	}

	private static void tickCountdown(MinecraftServer server, BurgerWorldState state) {
		if (!state.isCountdownStarted()) {
			return;
		}

		long currentDay = server.overworld().getDayTime() / 24000L;
		int elapsedDays = state.getElapsedCountdownDays(currentDay);
		if (state.shouldAnnounceCountdownDay(elapsedDays)) {
			server.getPlayerList().broadcastSystemMessage(countdownMessage(elapsedDays), false);
			state.markCountdownDayAnnounced(elapsedDays);
		}

		if (elapsedDays >= 3 && !state.isArrivalTriggered()) {
			RedRockKingArrivalHooks.onArrivalDue(server);
			state.markArrivalTriggered();
		}
	}

	private static Component countdownMessage(int elapsedDays) {
		return switch (elapsedDays) {
			case 0 -> Component.literal("第一日：红岩王仍在梦中翻身，止战汉堡的传闻开始发酵。").withStyle(ChatFormatting.GOLD);
			case 1 -> Component.literal("第二日：岩层的红光靠近地表，仍剩两万四千刻。").withStyle(ChatFormatting.YELLOW);
			case 2 -> Component.literal("第三日前夜：风里有焦面包的味道，最后一天已经开始。").withStyle(ChatFormatting.LIGHT_PURPLE);
			default -> Component.literal("红岩王即将降临").withStyle(ChatFormatting.DARK_RED);
		};
	}

	private static void tickHumanity(ServerPlayer player, BurgerWorldState state) {
		UUID playerId = player.getUUID();
		if (player.getFoodData().getFoodLevel() < LOW_FOOD_THRESHOLD) {
			int timer = hungerTimers.getOrDefault(playerId, 0) + 1;
			if (timer >= HUMANITY_LOSS_INTERVAL_TICKS) {
				timer = 0;
				int humanValue = state.adjustHumanValue(playerId, -HUMANITY_LOSS_AMOUNT);
				if (humanValue < LOW_HUMANITY_THRESHOLD) {
					BurgerAdvancements.award(player, BurgerAdvancements.LOW_HUMANITY);
				}
			}
			hungerTimers.put(playerId, timer);
		} else {
			hungerTimers.remove(playerId);
		}

		int humanValue = state.getHumanValue(playerId);
		if (humanValue < LOW_HUMANITY_THRESHOLD) {
			tickLowHumanityWarning(player);
		} else {
			lowHumanityWarningTimers.remove(playerId);
		}
	}

	private static void tickLowHumanityWarning(ServerPlayer player) {
		UUID playerId = player.getUUID();
		int timer = lowHumanityWarningTimers.getOrDefault(playerId, 0) + 1;
		if (timer >= LOW_HUMANITY_WARNING_COOLDOWN_TICKS) {
			timer = 0;
			if (player.getRandom().nextFloat() < 0.45F) {
				String message = LOW_HUMANITY_MESSAGES[player.getRandom().nextInt(LOW_HUMANITY_MESSAGES.length)];
				player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.GRAY));
			}
		}
		lowHumanityWarningTimers.put(playerId, timer);
	}
}
