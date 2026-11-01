package com.terrafabrica.content;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.content.shared.recipe.MachineRecipeSerializer;
import com.terrafabrica.core.research.ResearchShapedRecipe;
import com.terrafabrica.core.research.ResearchShapelessRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Сериализаторы рецептов: по одному {@link MachineRecipeSerializer} на тип
 * машинного рецепта + сериализаторы гейтящихся рецептов крафта.
 */
public final class TFRecipeSerializers {

    private TFRecipeSerializers() {}

    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_CRUSHER = machine("crusher");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_SEPARATOR = machine("separator");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_PRIMITIVE_FURNACE = machine("primitive_furnace");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_BLOOMERY = machine("bloomery");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_SMALL_BLAST_FURNACE = machine("small_blast_furnace");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_PUDDLING_FURNACE = machine("puddling_furnace");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_CRUCIBLE_FURNACE = machine("crucible_furnace");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_LATHE = machine("lathe");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_DRILL_PRESS = machine("drill_press");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_MILLING_MACHINE = machine("milling_machine");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_GRINDING_MACHINE = machine("grinding_machine");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_ROLLING_MILL = machine("rolling_mill");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_POWER_HAMMER = machine("power_hammer");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_PYRITE_ROASTER = machine("pyrite_roaster");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_LEAD_CHAMBER = machine("lead_chamber");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_ABSORBER = machine("absorber");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_SOLVAY_REACTOR = machine("solvay_reactor");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_CALCINATION_FURNACE = machine("calcination_furnace");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_COKE_OVEN = machine("coke_oven");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_BLAST_FURNACE = machine("blast_furnace");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_BESSEMER_CONVERTER = machine("bessemer_converter");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_OPEN_HEARTH_FURNACE = machine("open_hearth_furnace");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_ELECTRIC_ARC_FURNACE = machine("electric_arc_furnace");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_HEAVY_ROLLING_MILL = machine("heavy_rolling_mill");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_WIRE_DRAWING_MACHINE = machine("wire_drawing_machine");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_WEIGHT_DISPENSER = machine("weight_dispenser");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_AIR_SEPARATION_UNIT = machine("air_separation_unit");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_WATER_ELECTROLYZER = machine("water_electrolyzer");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_AMMONIA_SYNTHESIZER = machine("ammonia_synthesizer");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_OSTWALD_REACTOR = machine("ostwald_reactor");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_CONTACT_ACID_REACTOR = machine("contact_acid_reactor");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_CHLORALKALI_CELL = machine("chloralkali_cell");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_ALUMINUM_ELECTROLYZER = machine("aluminum_electrolyzer");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_FERTILIZER_MIXER = machine("fertilizer_mixer");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_OIL_DISTILLATION_COLUMN = machine("oil_distillation_column");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_SEMICONDUCTOR_FURNACE = machine("semiconductor_furnace");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_TRANSISTOR_ASSEMBLER = machine("transistor_assembler");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_MICROCHIP_FAB = machine("microchip_fab");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_CARBON_FIBER_PLANT = machine("carbon_fiber_plant");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_REACTOR_CORE = machine("reactor_core");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_ENRICHMENT_PLANT = machine("enrichment_plant");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_SATELLITE_LAUNCHPAD = machine("satellite_launchpad");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_FUSION_REACTOR = machine("fusion_reactor");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_EV_CHARGER = machine("ev_charger");
    public static final DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> MACHINE_TELESCOPE = machine("telescope");

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ResearchShapedRecipe>> RESEARCH_SHAPED =
            TerraFabrica.RECIPE_SERIALIZERS.register("research_shaped", ResearchShapedRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ResearchShapelessRecipe>> RESEARCH_SHAPELESS =
            TerraFabrica.RECIPE_SERIALIZERS.register("research_shapeless", ResearchShapelessRecipe.Serializer::new);

    private static DeferredHolder<RecipeSerializer<?>, MachineRecipeSerializer> machine(String id) {
        return TerraFabrica.RECIPE_SERIALIZERS.register(id,
                () -> new MachineRecipeSerializer(() -> TFRecipeTypes.BY_ID.get(id)));
    }

    public static void init() {
        // Регистрация выполняется в static-инициализаторе.
    }
}
