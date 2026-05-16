package com.burger_of_truce.item;

import com.burger_of_truce.world.RedRockKingBossManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class RedPenItem extends SwordItem {
	private static final float BOSS_EXTRA_DAMAGE = 8.0F;

	public RedPenItem(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		boolean result = super.hurtEnemy(stack, target, attacker);
		if (!target.level().isClientSide() && target.getTags().contains(RedRockKingBossManager.BOSS_TAG)) {
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 5 * 20, 0));
			target.invulnerableTime = 0;
			target.hurt(
					attacker instanceof Player player ? attacker.damageSources().playerAttack(player) : attacker.damageSources().mobAttack(attacker),
					BOSS_EXTRA_DAMAGE);
			if (attacker instanceof ServerPlayer player) {
				player.sendSystemMessage(Component.literal("扣分！").withStyle(ChatFormatting.RED));
			}
			if (target.getRandom().nextFloat() < 0.12F) {
				target.spawnAtLocation(ModItems.REVERSED_ESSAY_PAGE);
			}
		}
		return result;
	}
}
