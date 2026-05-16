package com.burger_of_truce.item;

import com.burger_of_truce.BurgerOfTruceMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public final class ModItems {
	public static final Item DRY_BREAD = register("dry_bread", new Item(new Item.Properties().food(new FoodProperties.Builder()
			.nutrition(2)
			.saturationModifier(0.2F)
			.build())));

	public static final Item MYSTERIOUS_PATTY = register("mysterious_patty", new Item(new Item.Properties().food(new FoodProperties.Builder()
			.nutrition(3)
			.saturationModifier(0.25F)
			.build())));

	public static final Item SCHOOL_BURGER = register("school_burger", new SchoolBurgerItem(new Item.Properties().food(new FoodProperties.Builder()
			.nutrition(7)
			.saturationModifier(0.65F)
			.effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 0), 1.0F)
			.build())));

	public static final Item TRUCE_BURGER = register("truce_burger", new TruceBurgerItem(new Item.Properties().food(new FoodProperties.Builder()
			.nutrition(12)
			.saturationModifier(1.0F)
			.alwaysEdible()
			.effect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1), 1.0F)
			.effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 0), 1.0F)
			.build())));

	public static final Item ELEGY_OF_TRUCE_RECORD = register("elegy_of_truce_record", new Item(new Item.Properties().stacksTo(1)));
	public static final Item RED_ROCK_SHARD = register("red_rock_shard", new Item(new Item.Properties()));
	public static final Item DRY_BREAD_RESIDUE = register("dry_bread_residue", new Item(new Item.Properties()));
	public static final Item REVERSED_ESSAY_PAGE = register("reversed_essay_page", new Item(new Item.Properties()));
	public static final Item REVERSED_ESSAY = register("reversed_essay", new ReversedEssayItem(new Item.Properties()));
	public static final Item REVERSE_VIEW_SCROLL = register("reverse_view_scroll", new ReverseViewScrollItem(new Item.Properties().stacksTo(16)));
	public static final Item UPSIDE_DOWN_BURGER = register("upside_down_burger", new Item(new Item.Properties().food(new FoodProperties.Builder()
			.nutrition(6)
			.saturationModifier(0.5F)
			.build())));
	public static final Item RED_PEN = register("red_pen", new RedPenItem(Tiers.WOOD, new Item.Properties()
			.attributes(SwordItem.createAttributes(Tiers.WOOD, 4, -2.4F))));
	public static final Item CORRECTED_ESSAY = register("corrected_essay", new Item(new Item.Properties()));
	public static final Item FULL_SCORE_ESSAY = register("full_score_essay", new FullScoreEssayItem(new Item.Properties().stacksTo(1)));

	private ModItems() {
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
			entries.accept(DRY_BREAD);
			entries.accept(MYSTERIOUS_PATTY);
			entries.accept(SCHOOL_BURGER);
			entries.accept(TRUCE_BURGER);
			entries.accept(UPSIDE_DOWN_BURGER);
			entries.accept(DRY_BREAD_RESIDUE);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
			entries.accept(ELEGY_OF_TRUCE_RECORD);
			entries.accept(RED_ROCK_SHARD);
			entries.accept(REVERSED_ESSAY_PAGE);
			entries.accept(REVERSED_ESSAY);
			entries.accept(REVERSE_VIEW_SCROLL);
			entries.accept(RED_PEN);
			entries.accept(CORRECTED_ESSAY);
			entries.accept(FULL_SCORE_ESSAY);
		});

		BurgerOfTruceMod.LOGGER.info("Registered Burger of Truce items.");
	}

	private static Item register(String path, Item item) {
		return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(BurgerOfTruceMod.MOD_ID, path), item);
	}
}
