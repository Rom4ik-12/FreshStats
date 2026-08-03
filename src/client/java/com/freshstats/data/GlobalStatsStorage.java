package com.freshstats.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

public class GlobalStatsStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("freshstats_global.json");

    public static class WorldStatSnapshot {
        public String serverOrWorldName;
        public Map<String, Long> categoryTotals = new HashMap<>();
        // Stores category -> detail name -> count
        public Map<String, Map<String, Long>> categoryDetails = new HashMap<>();
    }

    private static Map<String, WorldStatSnapshot> globalStorage = new HashMap<>();
    private static boolean isScanning = false;

    public static void load() {
        File file = CONFIG_FILE.toFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, WorldStatSnapshot>>() {}.getType();
                Map<String, WorldStatSnapshot> loaded = GSON.fromJson(reader, type);
                if (loaded != null) {
                    // Remove legacy keys (e.g. "World: New World") that caused duplicate multiplying
                    loaded.keySet().removeIf(key -> !key.startsWith("singleplayer:") && !key.startsWith("server:"));
                    globalStorage = loaded;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Auto-scan all singleplayer world save folders on disk
        if (!isScanning) {
            isScanning = true;
            try {
                SingleplayerWorldScanner.scanAllSaves();
            } finally {
                isScanning = false;
            }
        }
    }

    public static void save() {
        File file = CONFIG_FILE.toFile();
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(globalStorage, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateWorldSnapshot(String worldId, Map<String, Long> totals, Map<String, Map<String, Long>> details) {
        WorldStatSnapshot snapshot = globalStorage.get(worldId);
        if (snapshot == null) {
            snapshot = new WorldStatSnapshot();
            snapshot.serverOrWorldName = worldId;
            globalStorage.put(worldId, snapshot);
        }
        snapshot.categoryTotals = totals;
        snapshot.categoryDetails = details;
        save();
    }

    public static int getSavedWorldsCount() {
        return globalStorage.size();
    }

    public static String getSavedWorldNamesSummary() {
        if (globalStorage.isEmpty()) return "";
        List<String> names = new ArrayList<>();
        for (String key : globalStorage.keySet()) {
            if (key.startsWith("singleplayer:")) {
                names.add(key.substring("singleplayer:".length()));
            } else if (key.startsWith("server:")) {
                names.add(key.substring("server:".length()));
            } else {
                names.add(key);
            }
        }
        return String.join(", ", names);
    }

    public static void saveCurrentWorldStats(Map<StatCategory, CategoryData> currentData) {
        String worldId = getCurrentWorldOrServerId();
        WorldStatSnapshot snapshot = globalStorage.get(worldId);
        if (snapshot == null) {
            snapshot = new WorldStatSnapshot();
            snapshot.serverOrWorldName = worldId;
            globalStorage.put(worldId, snapshot);
        }

        for (Map.Entry<StatCategory, CategoryData> entry : currentData.entrySet()) {
            StatCategory cat = entry.getKey();
            CategoryData data = entry.getValue();

            snapshot.categoryTotals.put(cat.name(), data.getTotalValue());

            Map<String, Long> detailsMap = new HashMap<>();
            for (CategoryData.DetailEntry detail : data.getDetails()) {
                Text nameText = detail.getName();
                String key;
                if (nameText.getContent() instanceof net.minecraft.text.TranslatableTextContent trans) {
                    key = trans.getKey();
                } else {
                    key = nameText.getString();
                }
                detailsMap.put(key, detail.getCount());
            }
            snapshot.categoryDetails.put(cat.name(), detailsMap);
        }

        save();
    }

    public static Map<StatCategory, CategoryData> getGlobalAggregatedStats(Map<StatCategory, CategoryData> currentData) {
        // Load clean data from disk and scan all saves
        load();
        // Save current world stats cleanly under unique key
        saveCurrentWorldStats(currentData);

        Map<StatCategory, CategoryData> aggregated = new EnumMap<>(StatCategory.class);

        for (StatCategory cat : StatCategory.values()) {
            CategoryData template = currentData.get(cat);
            Text unitText = template != null ? template.getMainUnitText() : cat.getMainUnitText();
            CategoryData catData = new CategoryData(cat, unitText);

            long grandTotal = 0;
            Map<String, Long> mergedDetails = new LinkedHashMap<>();

            for (WorldStatSnapshot snapshot : globalStorage.values()) {
                Long total = snapshot.categoryTotals.get(cat.name());
                if (total != null) {
                    grandTotal += total;
                }

                Map<String, Long> details = snapshot.categoryDetails.get(cat.name());
                if (details != null) {
                    for (Map.Entry<String, Long> dEntry : details.entrySet()) {
                        String key = dEntry.getKey();
                        // Ignore non-building items in BUILDING category (e.g. wind_charge, mace, etc.)
                        if (cat == StatCategory.BUILDING && (key.contains("wind_charge") || key.contains("mace") || key.contains("sword") || key.contains("bow") || key.contains("food"))) {
                            continue;
                        }
                        mergedDetails.merge(key, dEntry.getValue(), Long::sum);
                    }
                }
            }

            catData.setTotalValue(grandTotal);

            // Reconstruct DetailEntries with full translation keys & fallback support
            for (Map.Entry<String, Long> entry : mergedDetails.entrySet()) {
                catData.addDetail(getLocalizedFallbackText(entry.getKey()), entry.getValue(), ItemStack.EMPTY);
            }

            catData.getDetails().sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
            aggregated.put(cat, catData);
        }

        return aggregated;
    }

    public static Text getLocalizedFallbackText(String key) {
        switch (key) {
            case "Damage Dealt": case "Нанесено урона": return Text.translatable("freshstats.detail.damage_dealt");
            case "Mob Kills": case "Убито мобов": return Text.translatable("freshstats.detail.mob_kills");
            case "Damage Taken": case "Получено урона": return Text.translatable("freshstats.detail.damage_taken");
            case "Player Kills": case "Убито игроков": return Text.translatable("freshstats.detail.player_kills");
            case "Deaths": case "Смертей": return Text.translatable("freshstats.detail.deaths");
            case "Villager Trades": case "Сделок с жителями": return Text.translatable("freshstats.detail.trades");
            case "Animals Bred": case "Выращено животных": return Text.translatable("freshstats.detail.animals_bred");
            case "Fish Caught": case "Поймано рыбы": return Text.translatable("freshstats.detail.fish_caught");

            case "Walking": case "Пешком": return Text.translatable("freshstats.detail.walk");
            case "Sprinting": case "Бег": return Text.translatable("freshstats.detail.sprint");
            case "Sneaking": case "Крадучись": return Text.translatable("freshstats.detail.crouch");
            case "Swimming": case "Плавание": return Text.translatable("freshstats.detail.swim");
            case "Underwater": case "Под водой": return Text.translatable("freshstats.detail.underwater");
            case "Elytra Flying": case "Полет (Элитры)": return Text.translatable("freshstats.detail.elytra");
            case "Creative Flying": case "Творческий полет": return Text.translatable("freshstats.detail.creative_fly");
            case "Boat": case "Лодка": return Text.translatable("freshstats.detail.boat");
            case "Minecart": case "Вагонетка": return Text.translatable("freshstats.detail.minecart");
            case "Horse / Donkey": case "Лошадь / Осел": return Text.translatable("freshstats.detail.horse");
            case "Pig Riding": case "Свинья": return Text.translatable("freshstats.detail.pig_ride");
            case "Strider": case "Страйдер": return Text.translatable("freshstats.detail.strider");
            case "Climbing": case "Карабканье": return Text.translatable("freshstats.detail.climb");

            case "Cow": return Text.translatable("entity.minecraft.cow");
            case "Skeleton": return Text.translatable("entity.minecraft.skeleton");
            case "Zombie": return Text.translatable("entity.minecraft.zombie");
            case "Pig": return Text.translatable("entity.minecraft.pig");
            case "Chicken": return Text.translatable("entity.minecraft.chicken");
            case "Sheep": return Text.translatable("entity.minecraft.sheep");
            case "Spider": return Text.translatable("entity.minecraft.spider");
            case "Creeper": return Text.translatable("entity.minecraft.creeper");
            case "Enderman": return Text.translatable("entity.minecraft.enderman");
            case "Drowned": return Text.translatable("entity.minecraft.drowned");
            case "Husk": return Text.translatable("entity.minecraft.husk");
            case "Slime": return Text.translatable("entity.minecraft.slime");
            case "Witch": return Text.translatable("entity.minecraft.witch");
            default:
                if (key.startsWith("item.minecraft.")) {
                    String blockKey = key.replace("item.", "block.");
                    return Text.translatable(blockKey);
                }
                if (key.startsWith("entity.") || key.startsWith("block.") || key.startsWith("item.") || key.startsWith("freshstats.")) {
                    return Text.translatable(key);
                }
                if (key.contains(":")) {
                    String[] parts = key.split(":");
                    return Text.translatable("entity." + parts[0] + "." + parts[1]);
                }
                return Text.translatable(key);
        }
    }

    public static String getCurrentWorldOrServerId() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getCurrentServerEntry() != null) {
            return "server:" + client.getCurrentServerEntry().address.toLowerCase();
        } else if (client.isIntegratedServerRunning() && client.getServer() != null) {
            try {
                File worldDir = client.getServer().getSavePath(WorldSavePath.ROOT).toFile().getCanonicalFile();
                return "singleplayer:" + worldDir.getName();
            } catch (Exception e) {
                return "singleplayer:" + client.getServer().getSaveProperties().getLevelName();
            }
        }
        return "singleplayer:default";
    }
}
