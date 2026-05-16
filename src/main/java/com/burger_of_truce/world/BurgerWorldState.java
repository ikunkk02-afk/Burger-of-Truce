package com.burger_of_truce.world;

import com.burger_of_truce.BurgerOfTruceMod;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class BurgerWorldState extends SavedData {
	private static final String DATA_NAME = BurgerOfTruceMod.MOD_ID + "_world_state";
	private static final SavedData.Factory<BurgerWorldState> FACTORY = new SavedData.Factory<>(
			BurgerWorldState::new,
			BurgerWorldState::load,
			DataFixTypes.LEVEL);

	private final Map<UUID, Integer> humanValues = new HashMap<>();
	private final Map<UUID, Integer> essayScores = new HashMap<>();
	private final Map<UUID, Set<String>> essayMilestones = new HashMap<>();
	private boolean countdownStarted;
	private long countdownStartDay;
	private int lastAnnouncedCountdownDay = -1;
	private boolean arrivalTriggered;

	public static BurgerWorldState get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public int getHumanValue(UUID playerId) {
		return humanValues.getOrDefault(playerId, 100);
	}

	public int adjustHumanValue(UUID playerId, int delta) {
		int value = Math.clamp(getHumanValue(playerId) + delta, 0, 100);
		humanValues.put(playerId, value);
		setDirty();
		return value;
	}

	public void ensurePlayer(UUID playerId) {
		if (!humanValues.containsKey(playerId)) {
			humanValues.put(playerId, 100);
			setDirty();
		}
		if (!essayScores.containsKey(playerId)) {
			essayScores.put(playerId, 2);
			setDirty();
		}
	}

	public int getEssayScore(UUID playerId) {
		return essayScores.getOrDefault(playerId, 2);
	}

	public int addEssayScore(UUID playerId, int amount) {
		int score = Math.clamp(getEssayScore(playerId) + amount, 2, 60);
		essayScores.put(playerId, score);
		setDirty();
		return score;
	}

	public boolean hasEssayMilestone(UUID playerId, String milestone) {
		return essayMilestones.getOrDefault(playerId, Set.of()).contains(milestone);
	}

	public boolean markEssayMilestone(UUID playerId, String milestone) {
		Set<String> milestones = essayMilestones.computeIfAbsent(playerId, ignored -> new HashSet<>());
		if (!milestones.add(milestone)) {
			return false;
		}

		setDirty();
		return true;
	}

	public boolean isCountdownStarted() {
		return countdownStarted;
	}

	public void startCountdown(long currentDay) {
		if (countdownStarted) {
			return;
		}

		countdownStarted = true;
		countdownStartDay = currentDay;
		lastAnnouncedCountdownDay = -1;
		arrivalTriggered = false;
		setDirty();
	}

	public int getElapsedCountdownDays(long currentDay) {
		if (!countdownStarted) {
			return 0;
		}
		return (int) Math.max(0, currentDay - countdownStartDay);
	}

	public boolean shouldAnnounceCountdownDay(int elapsedDays) {
		return elapsedDays >= 0 && elapsedDays <= 3 && elapsedDays > lastAnnouncedCountdownDay;
	}

	public void markCountdownDayAnnounced(int elapsedDays) {
		lastAnnouncedCountdownDay = elapsedDays;
		setDirty();
	}

	public boolean isArrivalTriggered() {
		return arrivalTriggered;
	}

	public void markArrivalTriggered() {
		arrivalTriggered = true;
		setDirty();
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		tag.putBoolean("CountdownStarted", countdownStarted);
		tag.putLong("CountdownStartDay", countdownStartDay);
		tag.putInt("LastAnnouncedCountdownDay", lastAnnouncedCountdownDay);
		tag.putBoolean("ArrivalTriggered", arrivalTriggered);

		CompoundTag humanityTag = new CompoundTag();
		for (Map.Entry<UUID, Integer> entry : humanValues.entrySet()) {
			humanityTag.putInt(entry.getKey().toString(), entry.getValue());
		}
		tag.put("HumanValues", humanityTag);

		CompoundTag essayScoreTag = new CompoundTag();
		for (Map.Entry<UUID, Integer> entry : essayScores.entrySet()) {
			essayScoreTag.putInt(entry.getKey().toString(), entry.getValue());
		}
		tag.put("EssayScores", essayScoreTag);

		CompoundTag essayMilestonesTag = new CompoundTag();
		for (Map.Entry<UUID, Set<String>> entry : essayMilestones.entrySet()) {
			CompoundTag playerMilestones = new CompoundTag();
			for (String milestone : entry.getValue()) {
				playerMilestones.putBoolean(milestone, true);
			}
			essayMilestonesTag.put(entry.getKey().toString(), playerMilestones);
		}
		tag.put("EssayMilestones", essayMilestonesTag);
		return tag;
	}

	private static BurgerWorldState load(CompoundTag tag, HolderLookup.Provider registries) {
		BurgerWorldState state = new BurgerWorldState();
		state.countdownStarted = tag.getBoolean("CountdownStarted");
		state.countdownStartDay = tag.getLong("CountdownStartDay");
		state.lastAnnouncedCountdownDay = tag.getInt("LastAnnouncedCountdownDay");
		state.arrivalTriggered = tag.getBoolean("ArrivalTriggered");

		CompoundTag humanityTag = tag.getCompound("HumanValues");
		for (String key : humanityTag.getAllKeys()) {
			try {
				state.humanValues.put(UUID.fromString(key), humanityTag.getInt(key));
			} catch (IllegalArgumentException ignored) {
				BurgerOfTruceMod.LOGGER.warn("Ignored invalid human value owner id '{}'.", key);
			}
		}

		CompoundTag essayScoreTag = tag.getCompound("EssayScores");
		for (String key : essayScoreTag.getAllKeys()) {
			try {
				state.essayScores.put(UUID.fromString(key), Math.clamp(essayScoreTag.getInt(key), 2, 60));
			} catch (IllegalArgumentException ignored) {
				BurgerOfTruceMod.LOGGER.warn("Ignored invalid essay score owner id '{}'.", key);
			}
		}

		CompoundTag essayMilestonesTag = tag.getCompound("EssayMilestones");
		for (String key : essayMilestonesTag.getAllKeys()) {
			try {
				UUID playerId = UUID.fromString(key);
				CompoundTag playerMilestones = essayMilestonesTag.getCompound(key);
				Set<String> milestones = new HashSet<>();
				for (String milestone : playerMilestones.getAllKeys()) {
					if (playerMilestones.getBoolean(milestone)) {
						milestones.add(milestone);
					}
				}
				state.essayMilestones.put(playerId, milestones);
			} catch (IllegalArgumentException ignored) {
				BurgerOfTruceMod.LOGGER.warn("Ignored invalid essay milestone owner id '{}'.", key);
			}
		}
		return state;
	}
}
