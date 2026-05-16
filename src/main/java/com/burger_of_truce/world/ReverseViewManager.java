package com.burger_of_truce.world;

import com.burger_of_truce.BurgerOfTruceMod;
import com.burger_of_truce.effect.ModMobEffects;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ReverseViewManager {
	private static final int REVERSED_HINT_INTERVAL_TICKS = 4 * 20;
	private static final ResourceLocation TRUE_TRUCE_RECIPE = ResourceLocation.fromNamespaceAndPath(BurgerOfTruceMod.MOD_ID, "truce_burger");
	private static final String[] REVERSED_HINTS = {
			"干很包汉，大小不包汉",
			"望希的后最了掉吃你",
			"解理始开我，王岩红",
			"看着倒要法方的战止",
			"前面在案答，后身在路"
	};

	private static final Set<UUID> activePlayers = new HashSet<>();
	private static final Map<UUID, Integer> hintTimers = new HashMap<>();

	private ReverseViewManager() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(ReverseViewManager::tick);
	}

	public static boolean hasReverseView(ServerPlayer player) {
		return player.hasEffect(ModMobEffects.REVERSE_VIEW);
	}

	private static void tick(MinecraftServer server) {
		Set<UUID> seenThisTick = new HashSet<>();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			UUID playerId = player.getUUID();
			if (hasReverseView(player)) {
				seenThisTick.add(playerId);
				if (activePlayers.add(playerId)) {
					onReverseViewStarted(player);
				}
				tickHint(player);
			}
		}

		activePlayers.removeIf(playerId -> {
			if (seenThisTick.contains(playerId)) {
				return false;
			}

			hintTimers.remove(playerId);
			ServerPlayer player = server.getPlayerList().getPlayer(playerId);
			if (player != null) {
				player.sendSystemMessage(Component.literal("你重新正着看世界。").withStyle(ChatFormatting.GRAY));
			}
			return true;
		});
	}

	private static void onReverseViewStarted(ServerPlayer player) {
		player.sendSystemMessage(Component.literal("你开始倒而观之。").withStyle(ChatFormatting.LIGHT_PURPLE));
		player.awardRecipesByKey(java.util.List.of(TRUE_TRUCE_RECIPE));
		player.sendSystemMessage(Component.literal("倒而观之：真正的止战配方显现了。学校汉堡、金苹果、下界之星、回响碎片、哭泣黑曜石和红石块必须被反着理解。").withStyle(ChatFormatting.GOLD));
	}

	private static void tickHint(ServerPlayer player) {
		UUID playerId = player.getUUID();
		int timer = hintTimers.getOrDefault(playerId, 0) + 1;
		if (timer >= REVERSED_HINT_INTERVAL_TICKS) {
			timer = 0;
			String message = REVERSED_HINTS[player.getRandom().nextInt(REVERSED_HINTS.length)];
			player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.DARK_PURPLE));
		}
		hintTimers.put(playerId, timer);
	}
}
