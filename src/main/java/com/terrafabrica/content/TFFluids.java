package com.terrafabrica.content;

import com.terrafabrica.TerraFabrica;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Реестр флюидов Terra Fabrica (NeoForge {@code Fluid} + {@code FluidType}).
 *
 * <p>Жидкости: серная/азотная/соляная кислоты, нефть и фракции, расплавы;
 * газы: пар, аммиак, хлор, водород, кислород, азот, CO₂, природный газ.
 * Флюиды живут в баках машин (IFluidHandler); ведро не предусмотрено —
 * перенос только трубами/баками (см. docs/DESIGN_DECISIONS.md).
 */
public final class TFFluids {

    private TFFluids() {}

    private record Spec(String id, int density, int viscosity, int temperatureK) {}

    private static final List<Spec> SPECS = List.of(
            new Spec("steam_low", -100, 100, 373),
            new Spec("steam_high", -50, 50, 573),
            new Spec("sulfuric_acid", 1830, 1_000, 293),
            new Spec("sulfuric_acid_dilute", 1_200, 600, 293),
            new Spec("nitric_acid", 1_500, 800, 293),
            new Spec("hydrochloric_acid", 1_180, 600, 293),
            new Spec("ammonia", -33, 80, 240),
            new Spec("chlorine", 3, 40, 240),
            new Spec("hydrogen", -90, 20, 20),
            new Spec("oxygen", -20, 30, 90),
            new Spec("nitrogen", -80, 30, 77),
            new Spec("carbon_dioxide", 2, 40, 220),
            new Spec("natural_gas", -200, 20, 110),
            new Spec("crude_oil", 870, 5_000, 293),
            new Spec("fuel_oil", 990, 100_000, 293),
            new Spec("gasoline", 750, 400, 293),
            new Spec("kerosene", 800, 1_500, 293),
            new Spec("diesel", 830, 3_000, 293),
            new Spec("molten_glass", 2_500, 100_000, 1_773),
            new Spec("molten_metal", 7_000, 100_000, 1_873),
            new Spec("sulfur_dioxide", 3, 40, 260),
            new Spec("coal_gas", -20, 30, 300));

    public static final Map<String, DeferredHolder<FluidType, FluidType>> TYPES = new LinkedHashMap<>();
    public static final Map<String, DeferredHolder<Fluid, Fluid>> SOURCES = new LinkedHashMap<>();
    public static final Map<String, DeferredHolder<Fluid, Fluid>> FLOWINGS = new LinkedHashMap<>();
    public static final Map<String, DeferredHolder<Block, Block>> LIQUID_BLOCKS = new LinkedHashMap<>();

    // Часто используемые флюиды (для кода).
    public static DeferredHolder<Fluid, Fluid> HYDROGEN;
    public static DeferredHolder<Fluid, Fluid> OXYGEN;
    public static DeferredHolder<Fluid, Fluid> NITROGEN;
    public static DeferredHolder<Fluid, Fluid> AMMONIA;
    public static DeferredHolder<Fluid, Fluid> CHLORINE;
    public static DeferredHolder<Fluid, Fluid> NITRIC_ACID;
    public static DeferredHolder<Fluid, Fluid> SULFURIC_ACID;
    public static DeferredHolder<Fluid, Fluid> GASOLINE;
    public static DeferredHolder<Fluid, Fluid> KEROSENE;
    public static DeferredHolder<Fluid, Fluid> DIESEL;
    public static DeferredHolder<Fluid, Fluid> FUEL_OIL;

    static {
        for (Spec spec : SPECS) {
            String id = spec.id();
            DeferredHolder<FluidType, FluidType> type = TerraFabrica.FLUID_TYPES.register(id,
                    () -> new FluidType(FluidType.Properties.create()
                            .density(spec.density())
                            .viscosity(spec.viscosity())
                            .temperature(spec.temperatureK())
                            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));
            TYPES.put(id, type);

            DeferredHolder<Fluid, Fluid> source = TerraFabrica.FLUIDS.register(id, () -> new TFFluid.Source(props(id)));
            DeferredHolder<Fluid, Fluid> flowing = TerraFabrica.FLUIDS.register(id + "_flowing",
                    () -> new TFFluid.Flowing(props(id)));
            SOURCES.put(id, source);
            FLOWINGS.put(id, flowing);

            DeferredHolder<Block, Block> block = TerraFabrica.BLOCKS.register(id,
                    () -> new LiquidBlock((FlowingFluid) source.get(), BlockBehaviour.Properties.of()
                            .liquid().replaceable().noCollission().strength(100.0F).noLootTable()));
            LIQUID_BLOCKS.put(id, block);
        }

        HYDROGEN = SOURCES.get("hydrogen");
        OXYGEN = SOURCES.get("oxygen");
        NITROGEN = SOURCES.get("nitrogen");
        AMMONIA = SOURCES.get("ammonia");
        CHLORINE = SOURCES.get("chlorine");
        NITRIC_ACID = SOURCES.get("nitric_acid");
        SULFURIC_ACID = SOURCES.get("sulfuric_acid");
        GASOLINE = SOURCES.get("gasoline");
        KEROSENE = SOURCES.get("kerosene");
        DIESEL = SOURCES.get("diesel");
        FUEL_OIL = SOURCES.get("fuel_oil");
    }

    private static BaseFlowingFluid.Properties props(String id) {
        return new BaseFlowingFluid.Properties(
                () -> TYPES.get(id).get(),
                () -> SOURCES.get(id).get(),
                () -> FLOWINGS.get(id).get())
                .block(() -> (LiquidBlock) LIQUID_BLOCKS.get(id).get())
                .tickRate(5)
                .slopeFindDistance(3)
                .explosionResistance(100.0F);
    }

    /** Инициализация (регистрация — в static-блоке). */
    public static void init() {
    }
}
