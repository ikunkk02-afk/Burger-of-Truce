package com.burger_of_truce.world;

import com.burger_of_truce.BurgerAdvancements;
import com.burger_of_truce.item.ModItems;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public final class RedRockKingBossManager {
	public static final String BOSS_TAG = "zhizhan_boss";
	private static final String PACIFIED_TAG = "zhizhan_boss_pacified";
	private static final String ENRAGED_TAG = "zhizhan_boss_enraged";
	private static final int PACIFIED_DESPAWN_TICKS = 30 * 20;
	private static final double BURGER_EATEN_RADIUS = 24.0D;
	private static final Map<UUID, Integer> pacifiedDespawnTimers = new HashMap<>();

	private RedRockKingBossManager() {
	}

	public static void register() {
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer) || !(entity instanceof Warden warden) || !isBoss(warden)) {
				return InteractionResult.PASS;
			}

			ItemStack heldStack = serverPlayer.getItemInHand(hand);
			if (!heldStack.is(ModItems.TRUCE_BURGER) || isPacified(warden)) {
				return InteractionResult.PASS;
			}

			if (ReverseViewManager.hasReverseView(serverPlayer)) {
				pacifyBoss(serverPlayer, warden, heldStack);
			} else {
				weakenBossBriefly(serverPlayer, warden);
			}
			return InteractionResult.SUCCESS;
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (entity instanceof Warden warden && isBoss(warden)) {
				dropBossRewards(warden);
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(RedRockKingBossManager::tickPacifiedBosses);
	}

	public static void spawnBoss(MinecraftServer server) {
		List<ServerPlayer> players = server.getPlayerList().getPlayers().stream()
				.filter(player -> !player.isSpectator())
				.toList();
		if (players.isEmpty()) {
			return;
		}

		ServerPlayer targetPlayer = players.get(server.overworld().getRandom().nextInt(players.size()));
		ServerLevel level = targetPlayer.serverLevel();
		BlockPos spawnPos = findSpawnPosition(level, targetPlayer);
		Warden warden = EntityType.WARDEN.create(level);
		if (warden == null) {
			return;
		}

		warden.moveTo(spawnPos, level.getRandom().nextFloat() * 360.0F, 0.0F);
		warden.setCustomName(Component.literal("红岩王的化身").withStyle(ChatFormatting.DARK_RED));
		warden.setCustomNameVisible(true);
		warden.addTag(BOSS_TAG);
		warden.setPersistenceRequired();
		warden.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null);
		applyBossStrength(warden);

		if (level.addFreshEntity(warden)) {
			server.getPlayerList().broadcastSystemMessage(Component.literal("红岩王的化身降临了。汉堡不大，面包很干。").withStyle(ChatFormatting.DARK_RED), false);
		}
	}

	public static void enrageNearestBossAfterBurgerEaten(ServerPlayer player) {
		Warden boss = findNearestActiveBoss(player.serverLevel(), player, BURGER_EATEN_RADIUS);
		if (boss == null) {
			return;
		}

		boss.addTag(ENRAGED_TAG);
		boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 10 * 60 * 20, 2));
		player.sendSystemMessage(Component.literal("你吃掉了人类最后的希望。").withStyle(ChatFormatting.RED));
	}

	private static void applyBossStrength(Warden warden) {
		setBaseAttribute(warden, Attributes.MAX_HEALTH, 800.0D);
		setBaseAttribute(warden, Attributes.ATTACK_DAMAGE, 45.0D);
		setBaseAttribute(warden, Attributes.MOVEMENT_SPEED, 0.35D);
		warden.setHealth(800.0F);
		warden.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 999999, 0, false, true));
	}

	private static void pacifyBoss(ServerPlayer player, Warden warden, ItemStack heldStack) {
		if (!player.isCreative()) {
			heldStack.shrink(1);
		}

		warden.addTag(PACIFIED_TAG);
		warden.setTarget(null);
		warden.setNoAi(true);
		warden.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, PACIFIED_DESPAWN_TICKS, 3));
		warden.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, PACIFIED_DESPAWN_TICKS, 4));
		warden.addEffect(new MobEffectInstance(MobEffects.GLOWING, PACIFIED_DESPAWN_TICKS, 0));
		pacifiedDespawnTimers.put(warden.getUUID(), PACIFIED_DESPAWN_TICKS);

		player.sendSystemMessage(Component.literal("你倒而观之，终于看懂了止战的方法。").withStyle(ChatFormatting.LIGHT_PURPLE));
		player.server.getPlayerList().broadcastSystemMessage(Component.literal("它停下了。也许汉堡真的能止战。").withStyle(ChatFormatting.GREEN), false);
		BurgerAdvancements.award(player, BurgerAdvancements.STOP_WAR_SUCCESS);
		EssayScoreManager.awardOnce(player, EssayScoreManager.SUCCESSFUL_TRUCE, 29);
	}

	private static void weakenBossBriefly(ServerPlayer player, Warden warden) {
		warden.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 6 * 20, 1));
		player.sendSystemMessage(Component.literal("你没有倒而观之，只让它迟疑了一瞬。").withStyle(ChatFormatting.GRAY));
	}

	private static void tickPacifiedBosses(MinecraftServer server) {
		pacifiedDespawnTimers.entrySet().removeIf(entry -> {
			int ticksRemaining = entry.getValue() - 1;
			Entity entity = findEntity(server, entry.getKey());
			if (entity == null || !entity.isAlive()) {
				return true;
			}

			if (ticksRemaining <= 0) {
				entity.discard();
				return true;
			}

			entry.setValue(ticksRemaining);
			return false;
		});
	}

	private static Entity findEntity(MinecraftServer server, UUID entityId) {
		for (ServerLevel level : server.getAllLevels()) {
			Entity entity = level.getEntity(entityId);
			if (entity != null) {
				return entity;
			}
		}
		return null;
	}

	private static Warden findNearestActiveBoss(ServerLevel level, ServerPlayer player, double radius) {
		AABB area = player.getBoundingBox().inflate(radius);
		Warden nearest = null;
		double nearestDistance = radius * radius;
		for (Warden warden : level.getEntitiesOfClass(Warden.class, area, RedRockKingBossManager::isActiveBoss)) {
			double distance = warden.distanceToSqr(player);
			if (distance <= nearestDistance) {
				nearest = warden;
				nearestDistance = distance;
			}
		}
		return nearest;
	}

	private static boolean isBoss(Warden warden) {
		return warden.getTags().contains(BOSS_TAG);
	}

	private static boolean isActiveBoss(Warden warden) {
		return isBoss(warden) && !isPacified(warden) && warden.isAlive();
	}

	private static boolean isPacified(Warden warden) {
		return warden.getTags().contains(PACIFIED_TAG);
	}

	private static void dropBossRewards(Warden warden) {
		warden.spawnAtLocation(ModItems.ELEGY_OF_TRUCE_RECORD);
		warden.spawnAtLocation(ModItems.RED_ROCK_SHARD);
		warden.spawnAtLocation(ModItems.DRY_BREAD_RESIDUE);
	}

	private static void setBaseAttribute(Warden warden, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double value) {
		AttributeInstance instance = warden.getAttribute(attribute);
		if (instance != null) {
			instance.setBaseValue(value);
		}
	}

	private static BlockPos findSpawnPosition(ServerLevel level, ServerPlayer player) {
		RandomSource random = level.getRandom();
		BlockPos playerPos = player.blockPosition();
		for (int attempt = 0; attempt < 32; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			int distance = 10 + random.nextInt(11);
			int xOffset = (int) Math.round(Math.cos(angle) * distance);
			int zOffset = (int) Math.round(Math.sin(angle) * distance);
			BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, playerPos.offset(xOffset, 0, zOffset));
			if (isSafeSpawnPosition(level, surface)) {
				return surface;
			}
		}

		return playerPos.offset(8, 0, 8);
	}

	private static boolean isSafeSpawnPosition(ServerLevel level, BlockPos pos) {
		if (!level.isLoaded(pos) || level.isOutsideBuildHeight(pos) || level.isOutsideBuildHeight(pos.above())) {
			return false;
		}

		BlockState support = level.getBlockState(pos.below());
		return support.isFaceSturdy(level, pos.below(), Direction.UP)
				&& level.isEmptyBlock(pos)
				&& level.isEmptyBlock(pos.above());
	}
}
