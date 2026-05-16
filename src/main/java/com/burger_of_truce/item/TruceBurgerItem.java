package com.burger_of_truce.item;

import com.burger_of_truce.BurgerAdvancements;
import com.burger_of_truce.world.EssayScoreManager;
import com.burger_of_truce.world.RedRockKingBossManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TruceBurgerItem extends Item {
	public TruceBurgerItem(Properties properties) {
		super(properties);
	}

	@Override
	public void onCraftedBy(ItemStack stack, Level level, Player player) {
		super.onCraftedBy(stack, level, player);
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			BurgerAdvancements.award(serverPlayer, BurgerAdvancements.CRAFT_TRUCE_BURGER);
			EssayScoreManager.awardOnce(serverPlayer, EssayScoreManager.FIRST_TRUCE_BURGER, 10);
		}
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
		ItemStack result = super.finishUsingItem(stack, level, livingEntity);
		if (!level.isClientSide() && livingEntity instanceof ServerPlayer player) {
			RedRockKingBossManager.enrageNearestBossAfterBurgerEaten(player);
		}
		return result;
	}
}
