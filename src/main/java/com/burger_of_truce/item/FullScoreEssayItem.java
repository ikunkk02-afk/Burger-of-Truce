package com.burger_of_truce.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FullScoreEssayItem extends Item {
	public FullScoreEssayItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			serverPlayer.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10 * 20, 0));
			serverPlayer.sendSystemMessage(Component.literal("你终于写出了一篇世界看得懂的作文。").withStyle(ChatFormatting.GOLD));
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
