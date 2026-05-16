package com.burger_of_truce.world;

import com.burger_of_truce.BurgerOfTruceMod;
import net.minecraft.server.MinecraftServer;

public final class RedRockKingArrivalHooks {
	private RedRockKingArrivalHooks() {
	}

	public static void onArrivalDue(MinecraftServer server) {
		BurgerOfTruceMod.LOGGER.info("Red Rock King arrival hook fired. Spawning the Warden-based avatar.");
		RedRockKingBossManager.spawnBoss(server);
	}
}
