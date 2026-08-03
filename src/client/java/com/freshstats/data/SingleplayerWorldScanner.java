package com.freshstats.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class SingleplayerWorldScanner {

    public static void scanAllSaves() {
        try {
            File savesDir = new File(MinecraftClient.getInstance().runDirectory, "saves");
            if (!savesDir.exists() || !savesDir.isDirectory()) return;

            File[] worldFolders = savesDir.listFiles();
            if (worldFolders == null) return;

            for (File folder : worldFolders) {
                if (!folder.isDirectory()) continue;
                File statsDir = new File(folder, "stats");
                if (!statsDir.exists() || !statsDir.isDirectory()) continue;

                File[] statFiles = statsDir.listFiles((dir, name) -> name.endsWith(".json"));
                if (statFiles == null || statFiles.length == 0) continue;

                // Pick the most recently modified stat file in the world folder
                File latestStatFile = statFiles[0];
                for (File f : statFiles) {
                    if (f.lastModified() > latestStatFile.lastModified()) {
                        latestStatFile = f;
                    }
                }

                parseAndSaveWorldStats(folder.getName(), latestStatFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseAndSaveWorldStats(String folderName, File statFile) {
        try (FileReader reader = new FileReader(statFile)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (!root.has("stats")) return;
            JsonObject statsObj = root.getAsJsonObject("stats");

            Map<String, Long> categoryTotals = new HashMap<>();
            Map<String, Map<String, Long>> categoryDetails = new HashMap<>();

            for (StatCategory cat : StatCategory.values()) {
                categoryDetails.put(cat.name(), new HashMap<>());
            }

            // 1. TRAVEL
            long travelMeters = 0;
            if (statsObj.has("minecraft:custom")) {
                JsonObject custom = statsObj.getAsJsonObject("minecraft:custom");

                Map<String, String> travelMap = new HashMap<>();
                travelMap.put("minecraft:walk_one_cm", "freshstats.detail.walk");
                travelMap.put("minecraft:sprint_one_cm", "freshstats.detail.sprint");
                travelMap.put("minecraft:crouch_one_cm", "freshstats.detail.crouch");
                travelMap.put("minecraft:swim_one_cm", "freshstats.detail.swim");
                travelMap.put("minecraft:walk_under_water_one_cm", "freshstats.detail.underwater");
                travelMap.put("minecraft:aviate_one_cm", "freshstats.detail.elytra");
                travelMap.put("minecraft:fly_one_cm", "freshstats.detail.creative_fly");
                travelMap.put("minecraft:boat_one_cm", "freshstats.detail.boat");
                travelMap.put("minecraft:minecart_one_cm", "freshstats.detail.minecart");
                travelMap.put("minecraft:horse_one_cm", "freshstats.detail.horse");
                travelMap.put("minecraft:pig_one_cm", "freshstats.detail.pig_ride");
                travelMap.put("minecraft:strider_one_cm", "freshstats.detail.strider");
                travelMap.put("minecraft:climb_one_cm", "freshstats.detail.climb");

                for (Map.Entry<String, String> entry : travelMap.entrySet()) {
                    if (custom.has(entry.getKey())) {
                        long cm = custom.get(entry.getKey()).getAsLong();
                        if (cm > 0) {
                            long meters = cm / 100;
                            travelMeters += meters;
                            categoryDetails.get(StatCategory.TRAVEL.name()).put(entry.getValue(), meters);
                        }
                    }
                }
            }
            categoryTotals.put(StatCategory.TRAVEL.name(), travelMeters);

            // 2. COMBAT
            long combatScore = 0;
            long damageDealt = 0;
            long damageTaken = 0;
            long mobKills = 0;
            long playerKills = 0;
            long deaths = 0;

            if (statsObj.has("minecraft:custom")) {
                JsonObject custom = statsObj.getAsJsonObject("minecraft:custom");
                if (custom.has("minecraft:damage_dealt")) {
                    damageDealt = custom.get("minecraft:damage_dealt").getAsLong() / 10;
                    if (damageDealt > 0) categoryDetails.get(StatCategory.COMBAT.name()).put("freshstats.detail.damage_dealt", damageDealt);
                }
                if (custom.has("minecraft:damage_taken")) {
                    damageTaken = custom.get("minecraft:damage_taken").getAsLong() / 10;
                    if (damageTaken > 0) categoryDetails.get(StatCategory.COMBAT.name()).put("freshstats.detail.damage_taken", damageTaken);
                }
                if (custom.has("minecraft:mob_kills")) {
                    mobKills = custom.get("minecraft:mob_kills").getAsLong();
                    if (mobKills > 0) categoryDetails.get(StatCategory.COMBAT.name()).put("freshstats.detail.mob_kills", mobKills);
                }
                if (custom.has("minecraft:player_kills")) {
                    playerKills = custom.get("minecraft:player_kills").getAsLong();
                    if (playerKills > 0) categoryDetails.get(StatCategory.COMBAT.name()).put("freshstats.detail.player_kills", playerKills);
                }
                if (custom.has("minecraft:deaths")) {
                    deaths = custom.get("minecraft:deaths").getAsLong();
                    if (deaths > 0) categoryDetails.get(StatCategory.COMBAT.name()).put("freshstats.detail.deaths", deaths);
                }
            }

            long totalEntityKills = 0;
            if (statsObj.has("minecraft:killed")) {
                JsonObject killed = statsObj.getAsJsonObject("minecraft:killed");
                for (Map.Entry<String, JsonElement> entry : killed.entrySet()) {
                    long k = entry.getValue().getAsLong();
                    if (k > 0) {
                        totalEntityKills += k;
                        String entityKey = "entity." + entry.getKey().replace(':', '.');
                        categoryDetails.get(StatCategory.COMBAT.name()).put(entityKey, k);
                    }
                }
            }

            if (mobKills == 0 && totalEntityKills > 0) mobKills = totalEntityKills;
            combatScore = damageDealt + (mobKills * 10) + (playerKills * 50) + damageTaken;
            categoryTotals.put(StatCategory.COMBAT.name(), combatScore);

            // 3. TRADING
            long trades = 0;
            if (statsObj.has("minecraft:custom")) {
                JsonObject custom = statsObj.getAsJsonObject("minecraft:custom");
                if (custom.has("minecraft:traded_with_villager")) {
                    trades = custom.get("minecraft:traded_with_villager").getAsLong();
                    if (trades > 0) categoryDetails.get(StatCategory.TRADING.name()).put("freshstats.detail.trades", trades);
                }
            }
            categoryTotals.put(StatCategory.TRADING.name(), trades);

            // 4. AGRICULTURE
            long agriActions = 0;
            if (statsObj.has("minecraft:custom")) {
                JsonObject custom = statsObj.getAsJsonObject("minecraft:custom");
                if (custom.has("minecraft:animals_bred")) {
                    long bred = custom.get("minecraft:animals_bred").getAsLong();
                    agriActions += bred;
                    if (bred > 0) categoryDetails.get(StatCategory.AGRICULTURE.name()).put("freshstats.detail.animals_bred", bred);
                }
                if (custom.has("minecraft:fish_caught")) {
                    long fish = custom.get("minecraft:fish_caught").getAsLong();
                    agriActions += fish;
                    if (fish > 0) categoryDetails.get(StatCategory.AGRICULTURE.name()).put("freshstats.detail.fish_caught", fish);
                }
            }
            categoryTotals.put(StatCategory.AGRICULTURE.name(), agriActions);

            // 5. BUILDING (Only count placed blocks / BlockItems!)
            long blocksPlaced = 0;
            if (statsObj.has("minecraft:used")) {
                JsonObject used = statsObj.getAsJsonObject("minecraft:used");
                for (Map.Entry<String, JsonElement> entry : used.entrySet()) {
                    long count = entry.getValue().getAsLong();
                    if (count > 0) {
                        String idStr = entry.getKey();
                        Identifier id = Identifier.tryParse(idStr);
                        if (id != null && Registries.ITEM.containsId(id)) {
                            Item item = Registries.ITEM.get(id);
                            // Strictly filter to BlockItem only (prevents wind_charge, mace, etc.)
                            if (item instanceof BlockItem) {
                                blocksPlaced += count;
                                categoryDetails.get(StatCategory.BUILDING.name()).put(item.getTranslationKey(), count);
                            }
                        }
                    }
                }
            }
            categoryTotals.put(StatCategory.BUILDING.name(), blocksPlaced);

            // 6. MINING
            long blocksMined = 0;
            if (statsObj.has("minecraft:mined")) {
                JsonObject mined = statsObj.getAsJsonObject("minecraft:mined");
                for (Map.Entry<String, JsonElement> entry : mined.entrySet()) {
                    long count = entry.getValue().getAsLong();
                    if (count > 0) {
                        blocksMined += count;
                        String idStr = entry.getKey();
                        Identifier id = Identifier.tryParse(idStr);
                        if (id != null && Registries.BLOCK.containsId(id)) {
                            categoryDetails.get(StatCategory.MINING.name()).put(Registries.BLOCK.get(id).getTranslationKey(), count);
                        } else {
                            categoryDetails.get(StatCategory.MINING.name()).put("block." + idStr.replace(':', '.'), count);
                        }
                    }
                }
            }
            categoryTotals.put(StatCategory.MINING.name(), blocksMined);

            // Save to GlobalStatsStorage under unique folder key
            GlobalStatsStorage.updateWorldSnapshot("singleplayer:" + folderName, categoryTotals, categoryDetails);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
