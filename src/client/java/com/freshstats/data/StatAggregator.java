package com.freshstats.data;

import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

import java.util.*;

public class StatAggregator {

    public static Map<StatCategory, CategoryData> aggregateCurrent(StatHandler statHandler) {
        Map<StatCategory, CategoryData> map = new EnumMap<>(StatCategory.class);

        map.put(StatCategory.TRAVEL, aggregateTravel(statHandler));
        map.put(StatCategory.COMBAT, aggregateCombat(statHandler));
        map.put(StatCategory.TRADING, aggregateTrading(statHandler));
        map.put(StatCategory.AGRICULTURE, aggregateAgriculture(statHandler));
        map.put(StatCategory.BUILDING, aggregateBuilding(statHandler));
        map.put(StatCategory.MINING, aggregateMining(statHandler));

        return map;
    }

    private static CategoryData aggregateTravel(StatHandler statHandler) {
        CategoryData data = new CategoryData(StatCategory.TRAVEL, Text.translatable("freshstats.category.travel.unit"));
        long totalMeters = 0;

        Map<String, Long> travelStats = new LinkedHashMap<>();
        travelStats.put("freshstats.detail.walk", (long) statHandler.getStat(Stats.CUSTOM, Stats.WALK_ONE_CM));
        travelStats.put("freshstats.detail.sprint", (long) statHandler.getStat(Stats.CUSTOM, Stats.SPRINT_ONE_CM));
        travelStats.put("freshstats.detail.crouch", (long) statHandler.getStat(Stats.CUSTOM, Stats.CROUCH_ONE_CM));
        travelStats.put("freshstats.detail.swim", (long) statHandler.getStat(Stats.CUSTOM, Stats.SWIM_ONE_CM));
        travelStats.put("freshstats.detail.underwater", (long) statHandler.getStat(Stats.CUSTOM, Stats.WALK_UNDER_WATER_ONE_CM));
        travelStats.put("freshstats.detail.elytra", (long) statHandler.getStat(Stats.CUSTOM, Stats.AVIATE_ONE_CM));
        travelStats.put("freshstats.detail.creative_fly", (long) statHandler.getStat(Stats.CUSTOM, Stats.FLY_ONE_CM));
        travelStats.put("freshstats.detail.boat", (long) statHandler.getStat(Stats.CUSTOM, Stats.BOAT_ONE_CM));
        travelStats.put("freshstats.detail.minecart", (long) statHandler.getStat(Stats.CUSTOM, Stats.MINECART_ONE_CM));
        travelStats.put("freshstats.detail.horse", (long) statHandler.getStat(Stats.CUSTOM, Stats.HORSE_ONE_CM));
        travelStats.put("freshstats.detail.pig_ride", (long) statHandler.getStat(Stats.CUSTOM, Stats.PIG_ONE_CM));
        travelStats.put("freshstats.detail.strider", (long) statHandler.getStat(Stats.CUSTOM, Stats.STRIDER_ONE_CM));
        travelStats.put("freshstats.detail.climb", (long) statHandler.getStat(Stats.CUSTOM, Stats.CLIMB_ONE_CM));

        for (Map.Entry<String, Long> entry : travelStats.entrySet()) {
            long cm = entry.getValue();
            if (cm > 0) {
                long meters = cm / 100;
                totalMeters += meters;
                data.addDetail(Text.translatable(entry.getKey()), meters, ItemStack.EMPTY);
            }
        }

        data.getDetails().sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        data.setTotalValue(totalMeters);
        return data;
    }

    private static CategoryData aggregateCombat(StatHandler statHandler) {
        CategoryData data = new CategoryData(StatCategory.COMBAT, Text.translatable("freshstats.category.combat.unit"));

        long damageDealt = statHandler.getStat(Stats.CUSTOM, Stats.DAMAGE_DEALT) / 10;
        long damageTaken = statHandler.getStat(Stats.CUSTOM, Stats.DAMAGE_TAKEN) / 10;
        long mobKills = statHandler.getStat(Stats.CUSTOM, Stats.MOB_KILLS);
        long playerKills = statHandler.getStat(Stats.CUSTOM, Stats.PLAYER_KILLS);
        long deaths = statHandler.getStat(Stats.CUSTOM, Stats.DEATHS);

        long totalEntityKills = 0;
        for (EntityType<?> entityType : Registries.ENTITY_TYPE) {
            int kills = statHandler.getStat(Stats.KILLED, entityType);
            if (kills > 0) {
                totalEntityKills += kills;
                data.addDetail(Text.translatable(entityType.getTranslationKey()), kills, ItemStack.EMPTY);
            }
        }

        if (mobKills == 0 && totalEntityKills > 0) {
            mobKills = totalEntityKills;
        }

        long score = damageDealt + (mobKills * 10) + (playerKills * 50) + damageTaken;
        data.setTotalValue(score);

        if (damageDealt > 0) data.addDetail(Text.translatable("freshstats.detail.damage_dealt"), damageDealt, ItemStack.EMPTY);
        if (mobKills > 0) data.addDetail(Text.translatable("freshstats.detail.mob_kills"), mobKills, ItemStack.EMPTY);
        if (damageTaken > 0) data.addDetail(Text.translatable("freshstats.detail.damage_taken"), damageTaken, ItemStack.EMPTY);
        if (playerKills > 0) data.addDetail(Text.translatable("freshstats.detail.player_kills"), playerKills, ItemStack.EMPTY);
        if (deaths > 0) data.addDetail(Text.translatable("freshstats.detail.deaths"), deaths, ItemStack.EMPTY);

        data.getDetails().sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return data;
    }

    private static CategoryData aggregateTrading(StatHandler statHandler) {
        CategoryData data = new CategoryData(StatCategory.TRADING, Text.translatable("freshstats.category.trading.unit"));

        long trades = statHandler.getStat(Stats.CUSTOM, Stats.TRADED_WITH_VILLAGER);
        data.setTotalValue(trades);

        if (trades > 0) {
            data.addDetail(Text.translatable("freshstats.detail.trades"), trades, ItemStack.EMPTY);
        }

        return data;
    }

    private static CategoryData aggregateAgriculture(StatHandler statHandler) {
        CategoryData data = new CategoryData(StatCategory.AGRICULTURE, Text.translatable("freshstats.category.agriculture.unit"));

        long animalsBred = statHandler.getStat(Stats.CUSTOM, Stats.ANIMALS_BRED);
        long fishCaught = statHandler.getStat(Stats.CUSTOM, Stats.FISH_CAUGHT);
        long totalCropsHarvested = 0;

        if (animalsBred > 0) data.addDetail(Text.translatable("freshstats.detail.animals_bred"), animalsBred, ItemStack.EMPTY);
        if (fishCaught > 0) data.addDetail(Text.translatable("freshstats.detail.fish_caught"), fishCaught, ItemStack.EMPTY);

        for (Block block : Registries.BLOCK) {
            if (block instanceof CropBlock || block instanceof StemBlock || isCropBlock(block)) {
                int mined = statHandler.getStat(Stats.MINED, block);
                if (mined > 0) {
                    totalCropsHarvested += mined;
                    data.addDetail(Text.translatable(block.getTranslationKey()), mined, new ItemStack(block.asItem()));
                }
            }
        }

        long totalAgri = animalsBred + fishCaught + totalCropsHarvested;
        data.setTotalValue(totalAgri);

        data.getDetails().sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return data;
    }

    private static boolean isCropBlock(Block block) {
        String path = Registries.BLOCK.getId(block).getPath();
        return path.contains("wheat") || path.contains("carrot") || path.contains("potato") ||
                path.contains("beetroot") || path.contains("melon") || path.contains("pumpkin") ||
                path.contains("sugar_cane") || path.contains("cocoa") || path.contains("cactus") ||
                path.contains("sweet_berry") || path.contains("glow_berry") || path.contains("bamboo") ||
                path.contains("nether_wart") || path.contains("mushroom");
    }

    private static CategoryData aggregateBuilding(StatHandler statHandler) {
        CategoryData data = new CategoryData(StatCategory.BUILDING, Text.translatable("freshstats.category.building.unit"));
        long totalPlaced = 0;

        for (Item item : Registries.ITEM) {
            if (item instanceof BlockItem) {
                int count = statHandler.getStat(Stats.USED, item);
                if (count > 0) {
                    totalPlaced += count;
                    data.addDetail(Text.translatable(item.getTranslationKey()), count, new ItemStack(item));
                }
            }
        }

        data.setTotalValue(totalPlaced);
        data.getDetails().sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return data;
    }

    private static CategoryData aggregateMining(StatHandler statHandler) {
        CategoryData data = new CategoryData(StatCategory.MINING, Text.translatable("freshstats.category.mining.unit"));
        long totalMined = 0;

        for (Block block : Registries.BLOCK) {
            int mined = statHandler.getStat(Stats.MINED, block);
            if (mined > 0) {
                totalMined += mined;
                data.addDetail(Text.translatable(block.getTranslationKey()), mined, new ItemStack(block.asItem()));
            }
        }

        data.setTotalValue(totalMined);
        data.getDetails().sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return data;
    }
}
