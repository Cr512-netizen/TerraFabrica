package com.terrafabrica.content;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.content.shared.recipe.MachineRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Типы машинных рецептов: по одному на машину. Регистрация в {@code RECIPE_TYPES},
 * сериализаторы — в {@link TFRecipeSerializers} (id совпадает с id типа).
 */
public final class TFRecipeTypes {

    private TFRecipeTypes() {}

    public static final Map<String, RecipeType<MachineRecipe>> BY_ID = new LinkedHashMap<>();

    // Эпоха I — обогащение
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> CRUSHER = type("crusher");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> SEPARATOR = type("separator");

    // Эпоха I — печи и металлургия
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> PRIMITIVE_FURNACE = type("primitive_furnace");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> BLOOMERY = type("bloomery");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> SMALL_BLAST_FURNACE = type("small_blast_furnace");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> PUDDLING_FURNACE = type("puddling_furnace");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> CRUCIBLE_FURNACE = type("crucible_furnace");

    // Эпоха I — станки
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> LATHE = type("lathe");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> DRILL_PRESS = type("drill_press");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> MILLING_MACHINE = type("milling_machine");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> GRINDING_MACHINE = type("grinding_machine");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> ROLLING_MILL = type("rolling_mill");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> POWER_HAMMER = type("power_hammer");

    // Эпоха I — химия
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> PYRITE_ROASTER = type("pyrite_roaster");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> LEAD_CHAMBER = type("lead_chamber");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> ABSORBER = type("absorber");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> SOLVAY_REACTOR = type("solvay_reactor");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> CALCINATION_FURNACE = type("calcination_furnace");

    // Эпоха II — металлургия
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> COKE_OVEN = type("coke_oven");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> BLAST_FURNACE = type("blast_furnace");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> BESSEMER_CONVERTER = type("bessemer_converter");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> OPEN_HEARTH_FURNACE = type("open_hearth_furnace");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> ELECTRIC_ARC_FURNACE = type("electric_arc_furnace");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> HEAVY_ROLLING_MILL = type("heavy_rolling_mill");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> WIRE_DRAWING_MACHINE = type("wire_drawing_machine");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> WEIGHT_DISPENSER = type("weight_dispenser");

    // Эпоха II — химия и энергетика
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> AIR_SEPARATION_UNIT = type("air_separation_unit");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> WATER_ELECTROLYZER = type("water_electrolyzer");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> AMMONIA_SYNTHESIZER = type("ammonia_synthesizer");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> OSTWALD_REACTOR = type("ostwald_reactor");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> CONTACT_ACID_REACTOR = type("contact_acid_reactor");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> CHLORALKALI_CELL = type("chloralkali_cell");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> ALUMINUM_ELECTROLYZER = type("aluminum_electrolyzer");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> FERTILIZER_MIXER = type("fertilizer_mixer");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> OIL_DISTILLATION_COLUMN = type("oil_distillation_column");

    // Эпоха III — электроника, углерод, ядерная, автоматизация
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> SEMICONDUCTOR_FURNACE = type("semiconductor_furnace");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> TRANSISTOR_ASSEMBLER = type("transistor_assembler");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> MICROCHIP_FAB = type("microchip_fab");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> CARBON_FIBER_PLANT = type("carbon_fiber_plant");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> REACTOR_CORE = type("reactor_core");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> ENRICHMENT_PLANT = type("enrichment_plant");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> SATELLITE_LAUNCHPAD = type("satellite_launchpad");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> FUSION_REACTOR = type("fusion_reactor");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> EV_CHARGER = type("ev_charger");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> TELESCOPE = type("telescope");

    private static DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> type(String id) {
        RecipeType<MachineRecipe> type = RecipeType.simple(TerraFabrica.id(id));
        BY_ID.put(id, type);
        return TerraFabrica.RECIPE_TYPES.register(id, () -> type);
    }

    public static void init() {
        // Типы регистрируются в static-инициализаторе.
    }
}
