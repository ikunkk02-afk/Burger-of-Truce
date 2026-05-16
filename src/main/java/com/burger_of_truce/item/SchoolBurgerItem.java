package com.burger_of_truce.item;

import com.burger_of_truce.BurgerAdvancements;
import com.burger_of_truce.world.EssayScoreManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SchoolBurgerItem extends Item {
	public SchoolBurgerItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
		ItemStack result = super.finishUsingItem(stack, level, livingEntity);
		if (!level.isClientSide() && livingEntity instanceof ServerPlayer player) {
			BurgerAdvancements.award(player, BurgerAdvancements.EAT_SCHOOL_BURGER);
			EssayScoreManager.awardOnce(player, EssayScoreManager.FIRST_SCHOOL_BURGER, 1);
		}
		return result;
	}
}
