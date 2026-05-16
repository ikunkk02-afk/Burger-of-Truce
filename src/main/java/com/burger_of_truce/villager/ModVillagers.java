package com.burger_of_truce.villager;

import com.burger_of_truce.BurgerOfTruceMod;
import com.burger_of_truce.item.ModItems;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public final class ModVillagers {
	private static final Predicate<Holder<PoiType>> LECTERN_JOB_SITE = poi -> poi.is(PoiTypes.LIBRARIAN);
	private static final String[] TEACHER_TIPS = {
			"立意深刻，但建议正着写。",
			"你这篇作文，很有想法，但我看不懂。",
			"倒而观之，或许另有深意。",
			"汉堡不大，面包很干，扣两分。"
	};

	public static final VillagerProfession CHINESE_TEACHER = Registry.register(
			BuiltInRegistries.VILLAGER_PROFESSION,
			ResourceLocation.fromNamespaceAndPath(BurgerOfTruceMod.MOD_ID, "chinese_teacher"),
			new VillagerProfession(
					"chinese_teacher",
					LECTERN_JOB_SITE,
					LECTERN_JOB_SITE,
					ImmutableSet.of(),
					ImmutableSet.of(Blocks.LECTERN),
					SoundEvents.VILLAGER_WORK_LIBRARIAN));

	private ModVillagers() {
	}

	public static void register() {
		registerTrades();
		registerTeacherTips();
		BurgerOfTruceMod.LOGGER.info("Registered Burger of Truce villagers.");
	}

	private static void registerTrades() {
		TradeOfferHelper.registerVillagerOffers(CHINESE_TEACHER, 1, factories -> {
			factories.add((entity, random) -> sellItem(ModItems.DRY_BREAD, 2, 4, 16, 2));
			factories.add((entity, random) -> sellItem(ModItems.SCHOOL_BURGER, 4, 1, 12, 4));
		});
		TradeOfferHelper.registerVillagerOffers(CHINESE_TEACHER, 2, factories -> {
			factories.add((entity, random) -> sellItem(ModItems.REVERSED_ESSAY_PAGE, 6, 1, 12, 8));
			factories.add((entity, random) -> buyItem(ModItems.REVERSED_ESSAY_PAGE, 1, 1, 12, 8));
		});
		TradeOfferHelper.registerVillagerOffers(CHINESE_TEACHER, 3, factories -> {
			factories.add((entity, random) -> twoInputTrade(ModItems.REVERSED_ESSAY, 1, Items.EMERALD, 8, ModItems.RED_PEN, 1, 8, 12));
			factories.add((entity, random) -> twoInputTrade(ModItems.TRUCE_BURGER, 1, Items.EMERALD, 12, ModItems.CORRECTED_ESSAY, 1, 6, 14));
		});
		TradeOfferHelper.registerVillagerOffers(CHINESE_TEACHER, 5, factories -> {
			factories.add((entity, random) -> twoInputTrade(ModItems.CORRECTED_ESSAY, 1, ModItems.RED_PEN, 1, ModItems.FULL_SCORE_ESSAY, 1, 3, 30));
		});
	}

	private static void registerTeacherTips() {
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClientSide() || !(entity instanceof Villager villager) || !CHINESE_TEACHER.equals(villager.getVillagerData().getProfession())) {
				return InteractionResult.PASS;
			}

			RandomSource random = world.getRandom();
			player.sendSystemMessage(Component.literal(TEACHER_TIPS[random.nextInt(TEACHER_TIPS.length)]).withStyle(ChatFormatting.GRAY));
			return InteractionResult.PASS;
		});
	}

	private static MerchantOffer sellItem(Item item, int emeraldCost, int itemCount, int maxUses, int xp) {
		return new MerchantOffer(new ItemCost(Items.EMERALD, emeraldCost), new ItemStack(item, itemCount), maxUses, xp, 0.05F);
	}

	private static MerchantOffer buyItem(ItemLike item, int itemCount, int emeraldCount, int maxUses, int xp) {
		return new MerchantOffer(new ItemCost(item, itemCount), new ItemStack(Items.EMERALD, emeraldCount), maxUses, xp, 0.05F);
	}

	private static MerchantOffer twoInputTrade(ItemLike firstItem, int firstCount, ItemLike secondItem, int secondCount, Item result, int resultCount, int maxUses, int xp) {
		return new MerchantOffer(
				new ItemCost(firstItem, firstCount),
				Optional.of(new ItemCost(secondItem, secondCount)),
				new ItemStack(result, resultCount),
				maxUses,
				xp,
				0.05F);
	}
}
