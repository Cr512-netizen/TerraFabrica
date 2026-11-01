package com.terrafabrica.core.research;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.content.TFItems;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Древо исследований Terra Fabrica: Эпоха I и Эпоха II.
 *
 * <p>Граф связный и без тупиков: корни (без предшественников) — «Обработка
 * дерева» и «Камень»; каждый узел либо является предшественником другого, либо
 * финальный («Переход к Эпохе II/III»). Проверка — {@link #validateGraph()}
 * при загрузке (логирует ошибки, но не крашит игру — защита от опечаток в dev).
 *
 * <p>Стоимость: предметы изымаются при старте, время — в тиках.
 */
public final class ResearchTree {

    private ResearchTree() {}

    private static final Map<String, ResearchNode> NODES = new LinkedHashMap<>();

    static {
        buildEra1();
        buildEra2();
        buildEra3();
    }

    public static Collection<ResearchNode> all() {
        return NODES.values();
    }

    public static ResearchNode get(String id) {
        return NODES.get(id);
    }

    private static void node(String id, int era, List<String> prereqs, long timeTicks,
                             List<ResearchNode.ItemCost> costs, float x, float y) {
        NODES.put(id, new ResearchNode(id, era, prereqs, timeTicks, costs, x, y));
    }

    private static ResearchNode.ItemCost cost(ItemLike item, int count) {
        return new ResearchNode.ItemCost(item, count);
    }

    // ------------------------------------------------------------------
    // Эпоха I — Механическое пробуждение
    // ------------------------------------------------------------------

    private static void buildEra1() {
        List.of(); // начало — корни
        node("woodworking", 1, List.of(), 300,
                List.of(cost(Items.OAK_PLANKS, 4)), 0, 0);
        node("stone_working", 1, List.of(), 300,
                List.of(cost(Items.COBBLESTONE, 6)), 0, 1);
        node("copper_metallurgy", 1, List.of("woodworking", "stone_working"), 600,
                List.of(cost(TFItems.rock("copper"), 8)), 1, 0);
        node("ore_enrichment", 1, List.of("woodworking", "stone_working"), 600,
                List.of(cost(TFItems.rock("copper"), 6)), 1, 1);
        node("water_wheel", 1, List.of("woodworking"), 600,
                List.of(cost(Items.OAK_PLANKS, 8), cost(TFItems.rock("copper"), 2)), 1, 2);
        node("bronze_metallurgy", 1, List.of("ore_enrichment", "copper_metallurgy"), 900,
                List.of(cost(TFItems.rock("tin"), 8), cost(TFItems.rock("copper"), 8)), 2, 0);
        node("transmission", 1, List.of("water_wheel"), 600,
                List.of(cost(Items.OAK_PLANKS, 6), cost(TFItems.rock("copper"), 4)), 2, 2);
        node("iron_metallurgy", 1, List.of("bronze_metallurgy"), 1_200,
                List.of(cost(TFItems.rock("hematite"), 6), cost(TFItems.rock("magnetite"), 6)), 3, 0);
        node("steam_newcomen", 1, List.of("iron_metallurgy", "water_wheel"), 1_800,
                List.of(cost(TFItems.PIG_IRON_INGOT, 8), cost(TFItems.concentrate("copper"), 6)), 3, 1);
        node("steam_watt", 1, List.of("steam_newcomen", "transmission"), 2_400,
                List.of(cost(TFItems.PIG_IRON_INGOT, 10), cost(TFItems.BRONZE_INGOT, 6)), 4, 1);
        node("machine_tools_lathe", 1, List.of("transmission", "iron_metallurgy"), 1_200,
                List.of(cost(Items.IRON_INGOT, 4), cost(TFItems.BRONZE_INGOT, 2)), 4, 0);
        node("machine_tools_drill", 1, List.of("machine_tools_lathe"), 1_200,
                List.of(cost(Items.IRON_INGOT, 4)), 5, 0);
        node("machine_tools_rolling", 1, List.of("machine_tools_lathe", "iron_metallurgy"), 1_800,
                List.of(cost(Items.IRON_INGOT, 6)), 5, 1);
        node("machine_tools_milling", 1, List.of("machine_tools_drill"), 1_200,
                List.of(cost(Items.IRON_INGOT, 4), cost(TFItems.BRONZE_INGOT, 2)), 6, 0);
        node("machine_tools_hammer", 1, List.of("machine_tools_rolling"), 1_800,
                List.of(cost(Items.IRON_INGOT, 6), cost(TFItems.BRONZE_INGOT, 2)), 6, 1);
        node("machine_tools_grinding", 1, List.of("machine_tools_milling"), 1_200,
                List.of(cost(Items.IRON_INGOT, 4), cost(TFItems.LEAD_INGOT, 2)), 7, 0);
        node("chemistry_sulfuric", 1, List.of("machine_tools_grinding", "steam_watt"), 2_400,
                List.of(cost(TFItems.concentrate("pyrite"), 8), cost(TFItems.LEAD_INGOT, 2)), 7, 2);
        node("transport_rails", 1, List.of("machine_tools_rolling"), 1_200,
                List.of(cost(Items.IRON_INGOT, 8)), 8, 0);
        node("chemistry_soda", 1, List.of("chemistry_sulfuric"), 2_400,
                List.of(cost(TFItems.concentrate("sylvite"), 8)), 8, 2);
        node("transport_minecart", 1, List.of("transport_rails", "machine_tools_lathe"), 1_200,
                List.of(cost(Items.IRON_INGOT, 6), cost(Items.OAK_PLANKS, 4)), 9, 0);
        node("chemistry_special_glass", 1, List.of("chemistry_soda"), 2_400,
                List.of(cost(TFItems.SODA_ASH, 4), cost(Items.GLASS, 6)), 9, 2);
        node("transport_conveyor", 1, List.of("transport_rails", "transmission"), 1_200,
                List.of(cost(Items.OAK_PLANKS, 10), cost(Items.LEATHER, 4)), 10, 0);
        node("transport_locomotive", 1, List.of("steam_watt", "transport_minecart"), 3_600,
                List.of(cost(TFItems.PIG_IRON_INGOT, 12), cost(TFItems.BRONZE_INGOT, 6), cost(Items.GLASS, 4)), 10, 1);
        node("era2_transition", 1, List.of("steam_watt", "machine_tools_hammer",
                        "chemistry_special_glass", "transport_locomotive"), 4_800,
                List.of(cost(Items.IRON_INGOT, 20), cost(TFItems.BRONZE_INGOT, 10),
                        cost(TFItems.concentrate("copper"), 10)), 11, 0);
    }

    // ------------------------------------------------------------------
    // Эпоха II — Промышленная революция
    // ------------------------------------------------------------------

    private static void buildEra3() {
        // Эпоха III — электроника, углеродные материалы, ядерная, автоматизация
        node("semiconductor", 3, List.of("tube_computer", "ccgt", "electric_arc_furnace"), 12_000,
                List.of(cost(TFItems.STEEL_INGOT, 24), cost(TFItems.ALUMINUM_INGOT, 16),
                        cost(TFItems.concentrate("graphite"), 12)), 26, 2);
        node("transistor", 3, List.of("semiconductor"), 9_600,
                List.of(cost(TFItems.STEEL_INGOT, 8), cost(TFItems.concentrate("silica"), 8),
                        cost(TFItems.concentrate("copper"), 6)), 27, 2);
        node("microchip", 3, List.of("transistor"), 12_000,
                List.of(cost(TFItems.concentrate("silica"), 12), cost(TFItems.concentrate("copper"), 8),
                        cost(TFItems.concentrate("gold"), 4)), 28, 2);
        node("carbon_fiber", 3, List.of("electric_arc_furnace", "ammonia"), 9_600,
                List.of(cost(TFItems.STEEL_INGOT, 16), cost(TFItems.concentrate("graphite"), 8),
                        cost(TFItems.concentrate("coal"), 4)), 26, 4);
        node("aerospace_alloy", 3, List.of("carbon_fiber", "superalloy"), 14_400,
                List.of(cost(TFItems.SUPERALLOY_INGOT, 16), cost(TFItems.CARBON_FIBER, 8),
                        cost(TFItems.TITANIUM_ALLOY_INGOT, 8)), 27, 4);
        node("nuclear_reactor", 3, List.of("ccgt", "heavy_rolling", "lead"), 18_000,
                List.of(cost(TFItems.STEEL_INGOT, 32), cost(TFItems.concentrate("uraninite"), 16),
                        cost(TFItems.concentrate("zircon"), 8)), 28, 4);
        node("enrichment", 3, List.of("nuclear_reactor", "air_separation"), 12_000,
                List.of(cost(TFItems.concentrate("uraninite"), 24), cost(TFItems.LEAD_INGOT, 8),
                        cost(TFItems.concentrate("fluorite"), 8)), 29, 2);
        node("yellowcake_processing", 3, List.of("enrichment"), 9_600,
                List.of(cost(TFItems.concentrate("uraninite"), 16), cost(TFItems.concentrate("graphite"), 8),
                        cost(TFItems.concentrate("sulfur"), 4)), 30, 2);
        node("satellite", 3, List.of("aerospace_alloy", "microchip", "radio"), 14_400,
                List.of(cost(TFItems.TITANIUM_ALLOY_INGOT, 24), cost(TFItems.CARBON_FIBER, 12),
                        cost(TFItems.concentrate("silicon"), 8)), 31, 2);
        node("ev", 3, List.of("car", "battery", "electric_motor"), 12_000,
                List.of(cost(TFItems.STEEL_INGOT, 20), cost(TFItems.ALUMINUM_INGOT, 16),
                        cost(TFItems.CARBON_FIBER, 8)), 32, 0);
        node("fusion_reactor", 3, List.of("satellite", "nuclear_reactor", "enrichment"), 24_000,
                List.of(cost(TFItems.SUPERALLOY_INGOT, 32), cost(TFItems.TITANIUM_ALLOY_INGOT, 24),
                        cost(TFItems.concentrate("lithium"), 16)), 33, 2);
    }

    private static void buildEra2() {
        node("coke_making", 2, List.of("era2_transition"), 1_200,
                List.of(cost(TFItems.concentrate("coal"), 12)), 12, 0);
        node("steam_turbine", 2, List.of("era2_transition", "steam_watt"), 4_800,
                List.of(cost(TFItems.STEEL_INGOT, 16), cost(TFItems.BRONZE_INGOT, 8)), 12, 2);
        node("blast_furnace_coke", 2, List.of("coke_making"), 1_800,
                List.of(cost(Items.IRON_INGOT, 16), cost(TFItems.concentrate("limestone"), 8)), 13, 0);
        node("electric_generator", 2, List.of("steam_turbine"), 4_800,
                List.of(cost(TFItems.STEEL_INGOT, 12), cost(TFItems.concentrate("copper"), 8),
                        cost(TFItems.concentrate("magnetite"), 4)), 13, 2);
        node("bessemer", 2, List.of("blast_furnace_coke"), 2_400,
                List.of(cost(TFItems.PIG_IRON_INGOT, 12)), 14, 0);
        node("transformer", 2, List.of("electric_generator"), 3_600,
                List.of(cost(TFItems.STEEL_INGOT, 8), cost(TFItems.concentrate("copper"), 10)), 14, 2);
        node("power_network", 2, List.of("transformer", "electric_generator"), 2_400,
                List.of(cost(TFItems.concentrate("copper"), 8), cost(TFItems.STEEL_INGOT, 6)), 14, 3);
        node("open_hearth", 2, List.of("bessemer"), 2_400,
                List.of(cost(TFItems.PIG_IRON_INGOT, 10), cost(TFItems.FIREBRICK, 6)), 15, 0);
        node("electric_motor", 2, List.of("electric_generator"), 3_600,
                List.of(cost(TFItems.STEEL_INGOT, 8), cost(TFItems.concentrate("copper"), 8)), 15, 2);
        node("battery", 2, List.of("electric_motor"), 3_600,
                List.of(cost(TFItems.LEAD_INGOT, 8), cost(TFItems.SULFURIC_ACID_BOTTLE, 4)), 15, 3);
        node("electric_arc_furnace", 2, List.of("open_hearth", "electric_generator"), 3_600,
                List.of(cost(TFItems.STEEL_INGOT, 8), cost(TFItems.concentrate("graphite"), 4)), 16, 0);
        node("heavy_rolling", 2, List.of("bessemer"), 2_400,
                List.of(cost(TFItems.STEEL_INGOT, 10)), 16, 1);
        node("air_separation", 2, List.of("steam_turbine", "chemistry_special_glass"), 4_800,
                List.of(cost(TFItems.STEEL_INGOT, 12), cost(TFItems.concentrate("copper"), 8)), 16, 2);
        node("wire_drawing", 2, List.of("heavy_rolling"), 1_800,
                List.of(cost(TFItems.STEEL_INGOT, 6), cost(TFItems.concentrate("copper"), 4)), 17, 1);
        node("water_electrolysis", 2, List.of("air_separation", "power_network"), 3_600,
                List.of(cost(TFItems.STEEL_INGOT, 10), cost(TFItems.NICKEL_INGOT, 4)), 17, 2);
        node("oil_refining", 2, List.of("blast_furnace_coke", "power_network"), 3_600,
                List.of(cost(TFItems.STEEL_INGOT, 12), cost(TFItems.concentrate("copper"), 4)), 17, 3);
        node("gas_turbine", 2, List.of("steam_turbine", "air_separation"), 7_200,
                List.of(cost(TFItems.STEEL_INGOT, 20), cost(TFItems.ALUMINUM_INGOT, 10)), 18, 1);
        node("ammonia", 2, List.of("air_separation", "chemistry_sulfuric"), 6_000,
                List.of(cost(TFItems.STEEL_INGOT, 12), cost(Items.IRON_INGOT, 6)), 18, 2);
        node("ccgt", 2, List.of("gas_turbine", "steam_turbine"), 9_600,
                List.of(cost(TFItems.STEEL_INGOT, 24), cost(TFItems.ALUMINUM_INGOT, 10),
                        cost(TFItems.CHROMIUM_INGOT, 6)), 19, 1);
        node("nitric_acid", 2, List.of("ammonia"), 6_000,
                List.of(cost(TFItems.STEEL_INGOT, 6), cost(TFItems.PLATINUM_INGOT, 2)), 19, 2);
        node("contact_sulfuric", 2, List.of("chemistry_sulfuric", "air_separation"), 4_800,
                List.of(cost(TFItems.LEAD_INGOT, 10), cost(TFItems.VANADIUM_INGOT, 4),
                        cost(TFItems.concentrate("pyrite"), 8)), 19, 3);
        node("chloralkali", 2, List.of("water_electrolysis", "power_network"), 3_600,
                List.of(cost(TFItems.STEEL_INGOT, 8), cost(TFItems.concentrate("graphite"), 6)), 20, 2);
        node("telegraph", 2, List.of("wire_drawing", "battery"), 2_400,
                List.of(cost(TFItems.concentrate("copper"), 6), cost(TFItems.ZINC_INGOT, 2)), 20, 4);
        node("aluminum", 2, List.of("chloralkali", "contact_sulfuric"), 6_000,
                List.of(cost(TFItems.STEEL_INGOT, 10), cost(TFItems.concentrate("fluorite"), 6),
                        cost(TFItems.SODA_ASH, 6)), 21, 2);
        node("fertilizers", 2, List.of("ammonia", "contact_sulfuric"), 3_600,
                List.of(cost(TFItems.concentrate("apatite"), 8), cost(TFItems.concentrate("sylvite"), 4)), 21, 3);
        node("telephone", 2, List.of("telegraph", "electric_motor"), 3_600,
                List.of(cost(TFItems.concentrate("copper"), 8), cost(TFItems.ALUMINUM_INGOT, 2)), 21, 4);
        node("diesel_locomotive", 2, List.of("oil_refining", "electric_motor"), 7_200,
                List.of(cost(TFItems.STEEL_INGOT, 16), cost(TFItems.ALUMINUM_INGOT, 8)), 22, 0);
        node("radio", 2, List.of("telephone", "power_network"), 4_800,
                List.of(cost(TFItems.ALUMINUM_INGOT, 10), cost(TFItems.TUNGSTEN_INGOT, 4), cost(Items.GLASS, 4)), 22, 4);
        node("ships", 2, List.of("steam_turbine", "heavy_rolling"), 7_200,
                List.of(cost(TFItems.STEEL_INGOT, 20), cost(TFItems.BRONZE_INGOT, 10)), 23, 0);
        node("tube_computer", 2, List.of("radio"), 9_600,
                List.of(cost(TFItems.ALUMINUM_INGOT, 20), cost(TFItems.TUNGSTEN_INGOT, 10), cost(Items.GLASS, 8)), 23, 4);
        node("submarine", 2, List.of("ships", "battery"), 8_400,
                List.of(cost(TFItems.STEEL_INGOT, 16), cost(TFItems.LEAD_INGOT, 8), cost(TFItems.ALUMINUM_INGOT, 4)), 24, 0);
        node("automation", 2, List.of("tube_computer", "electric_motor"), 4_800,
                List.of(cost(TFItems.STEEL_INGOT, 10), cost(TFItems.concentrate("copper"), 4),
                        cost(TFItems.ALUMINUM_INGOT, 4)), 24, 4);
        node("car", 2, List.of("oil_refining", "electric_motor"), 6_000,
                List.of(cost(TFItems.STEEL_INGOT, 12), cost(TFItems.ALUMINUM_INGOT, 6),
                        cost(TFItems.RUBBER, 4)), 25, 0);
        node("era3_transition", 2, List.of("electric_arc_furnace", "ccgt", "tube_computer",
                        "diesel_locomotive", "aluminum", "fertilizers"), 12_000,
                List.of(cost(TFItems.STEEL_INGOT, 32), cost(TFItems.ALUMINUM_INGOT, 16),
                        cost(TFItems.TUNGSTEN_INGOT, 8), cost(TFItems.CHROMIUM_INGOT, 8),
                        cost(TFItems.PLATINUM_INGOT, 4)), 25, 2);
    }

    // ------------------------------------------------------------------
    // Валидация графа
    // ------------------------------------------------------------------

    /**
     * Проверяет граф: существование предшественников, связность (все узлы
     * достижимы от корней), отсутствие тупиков (у каждого нефинального узла
     * есть потомок). Проблемы логируются — граф остаётся работоспособным.
     */
    public static void validateGraph() {
        boolean ok = true;
        // Предшественники существуют.
        for (ResearchNode node : NODES.values()) {
            for (String prereq : node.prerequisites()) {
                if (!NODES.containsKey(prereq)) {
                    TerraFabrica.LOGGER.error("[research] Узел {} ссылается на несуществующий {}", node.id(), prereq);
                    ok = false;
                }
            }
        }
        // Связность от корней (BFS по обратным рёбрам).
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        for (ResearchNode node : NODES.values()) {
            if (node.prerequisites().isEmpty()) {
                reachable.add(node.id());
                queue.add(node.id());
            }
        }
        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (ResearchNode node : NODES.values()) {
                if (!reachable.contains(node.id()) && node.prerequisites().contains(current)) {
                    reachable.add(node.id());
                    queue.add(node.id());
                }
            }
        }
        for (String id : NODES.keySet()) {
            if (!reachable.contains(id)) {
                TerraFabrica.LOGGER.error("[research] Узел {} недостижим от корней", id);
                ok = false;
            }
        }
        // Листья (узлы без потомков) — норма: это вершины веток (например car,
        // automation, era2_transition). Поэтому просто перечисляем их в debug,
        // а не считаем ошибкой графа.
        List<String> leaves = new ArrayList<>();
        for (ResearchNode node : NODES.values()) {
            boolean hasChild = NODES.values().stream()
                    .anyMatch(other -> other.prerequisites().contains(node.id()));
            if (!hasChild) {
                leaves.add(node.id());
            }
        }
        TerraFabrica.LOGGER.debug("[research] Листовых узлов (вершины веток): {}", leaves);
        TerraFabrica.LOGGER.info("[research] Древо исследований: {} узлов, валидность={}",
                NODES.size(), ok);
    }

    /** Инициализация (дерево строится в static-блоке). */
    public static void init() {
        validateGraph();
    }
}
