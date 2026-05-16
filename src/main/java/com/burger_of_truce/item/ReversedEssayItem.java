package com.burger_of_truce.item;

import com.burger_of_truce.world.EssayScoreManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ReversedEssayItem extends Item {
	public ReversedEssayItem(Properties properties) {
		super(properties);
	}

	@Override
	public void onCraftedBy(ItemStack stack, Level level, Player player) {
		super.onCraftedBy(stack, level, player);
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			EssayScoreManager.awardOnce(serverPlayer, EssayScoreManager.FIRST_REVERSED_ESSAY, 5);
		}
	}
}
