package fzzyhmstrs.emi_loot.server;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzzyhmstrs.emi_loot.EMILoot;
import fzzyhmstrs.emi_loot.EMILootAgnos;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ServerResourceData {

    public static final Multimap<Identifier, LootTable> DIRECT_DROPS = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);
    public static final List<Identifier> SHEEP_TABLES;
    public static final List<Identifier> TABLE_EXCLUSIONS = new LinkedList<>();
    private static final int DIRECT_DROPS_PATH_LENGTH = "direct_drops/".length();
    private static final int FILE_SUFFIX_LENGTH = ".json".length();

    public static void loadDirectTables(ResourceManager resourceManager, RegistryOps<JsonElement> ops) {
        //if (LootTableParser.registryManager != null) {
            DIRECT_DROPS.clear();
            resourceManager.findResources("direct_drops", path -> path.getPath().endsWith(".json")).forEach((id, resource) -> loadDirectTable(id, resource, ops));
            resourceManager.findResources("emi_loot_data", path -> path.getPath().endsWith(".json")).forEach(ServerResourceData::loadTableExclusion);
        //}
    }

    private static void loadDirectTable(Identifier id, Resource resource, RegistryOps<JsonElement> ops) {
        if (EMILoot.DEBUG) EMILoot.LOGGER.info("Reading direct drop table from file: " + id.toString());
        String path = id.getPath();
        Identifier id2 = Identifier.of(id.getNamespace(), path.substring(DIRECT_DROPS_PATH_LENGTH, path.length() - FILE_SUFFIX_LENGTH));
        String path2 = id2.getPath();
        if (!(path2.startsWith("blocks/") || path2.startsWith("entities/"))) {
            EMILoot.LOGGER.error("File path for [" + id + "] not correct; needs a 'blocks' or 'entities' subfolder. Skipping.");
            EMILoot.LOGGER.error("Example: [./data/mod_id/direct_drops/blocks/cobblestone.json] is a valid block direct drop table path for a block added by [mod_id].");
            return;
        }
        try {
            BufferedReader reader = resource.getReader();
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            LootTable lootTable = EMILootAgnos.loadLootTable(id, LootTable.CODEC.parse(ops, json).getOrThrow());
            if (lootTable != null) {
                DIRECT_DROPS.put(id2, lootTable);
            } else {
                EMILoot.LOGGER.error("Loot table in file [" + id + "] is empty!");
            }

        } catch(Exception e) {
            EMILoot.LOGGER.error("Failed to open or read direct drops loot table file: " + id);
            e.printStackTrace();
        }
    }

    private static void loadTableExclusion(Identifier id, Resource resource) {
        if (EMILoot.DEBUG) EMILoot.LOGGER.info("Reading exclusion table from file: " + id.toString());
        try {
            BufferedReader reader = resource.getReader();
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonElement list = json.get("exclusions");
            if (list != null && list.isJsonArray()) {
                list.getAsJsonArray().forEach(element -> {
                    if (element.isJsonPrimitive()) {
                        Identifier identifier = Identifier.of(element.getAsString());
                        if (EMILoot.DEBUG) EMILoot.LOGGER.info("Adding exclusion: " + identifier);
                        TABLE_EXCLUSIONS.add(identifier);
                    } else {
                        EMILoot.LOGGER.error("Exclusion element not properly formatted: " + element);
                    }
                });
            } else {
                EMILoot.LOGGER.error("Exclusions in file: " + id + " not readable.");
            }
        } catch(Exception e) {
            EMILoot.LOGGER.error("Failed to open or read table exclusions file: " + id);
        }
    }

    public static boolean skipTable(Identifier id) {
        return TABLE_EXCLUSIONS.contains(id);
    }

    public static Multimap<Identifier, LootTable> getMissedDirectDrops(List<Identifier> parsedList) {
        Multimap<Identifier, LootTable> missedDrops = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);
        for (Map.Entry<Identifier, LootTable> entry : DIRECT_DROPS.entries()) {
            if (!parsedList.contains(entry.getKey())) {
                missedDrops.put(entry.getKey(), entry.getValue());
            }
        }
        return missedDrops;
    }

    static {
        Identifier[] ids = {
                LootTables.WHITE_SHEEP_ENTITY.getValue(),
                LootTables.ORANGE_SHEEP_ENTITY.getValue(),
                LootTables.MAGENTA_SHEEP_ENTITY.getValue(),
                LootTables.LIGHT_BLUE_SHEEP_ENTITY.getValue(),
                LootTables.YELLOW_SHEEP_ENTITY.getValue(),
                LootTables.LIME_SHEEP_ENTITY.getValue(),
                LootTables.PINK_SHEEP_ENTITY.getValue(),
                LootTables.GRAY_SHEEP_ENTITY.getValue(),
                LootTables.LIGHT_GRAY_SHEEP_ENTITY.getValue(),
                LootTables.CYAN_SHEEP_ENTITY.getValue(),
                LootTables.PURPLE_SHEEP_ENTITY.getValue(),
                LootTables.BLUE_SHEEP_ENTITY.getValue(),
                LootTables.BROWN_SHEEP_ENTITY.getValue(),
                LootTables.GREEN_SHEEP_ENTITY.getValue(),
                LootTables.RED_SHEEP_ENTITY.getValue(),
                LootTables.BLACK_SHEEP_ENTITY.getValue()
        };
        SHEEP_TABLES = Arrays.stream(ids).toList();
    }
}