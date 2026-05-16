package com.burger_of_truce.item;

import com.burger_of_truce.effect.ModMobEffects;
import com.burger_of_truce.world.EssayScoreManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ReverseViewScrollItem extends Item {
	private static final int REVERSE_VIEW_DURATION_TICKS = 20 * 20;

	public ReverseViewScrollItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			serverPlayer.addEffect(new MobEffectInstance(ModMobEffects.REVERSE_VIEW, REVERSE_VIEW_DURATION_TICKS, 0));
			EssayScoreManager.awardOnce(serverPlayer, EssayScoreManager.FIRST_REVERSE_VIEW_SCROLL, 8);
			if (!serverPlayer.isCreative()) {
				stack.shrink(1);
			}
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
