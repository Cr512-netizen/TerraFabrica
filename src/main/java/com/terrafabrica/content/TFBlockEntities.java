package com.terrafabrica.content;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.content.era1.BloomeryBlockEntity;
import com.terrafabrica.content.era1.CalcinationFurnaceBlockEntity;
import com.terrafabrica.content.era1.ConveyorBlockEntity;
import com.terrafabrica.content.era1.CrucibleFurnaceBlockEntity;
import com.terrafabrica.content.era1.CrusherBlockEntity;
import com.terrafabrica.content.era1.DrillPressBlockEntity;
import com.terrafabrica.content.era1.GearboxBlockEntity;
import com.terrafabrica.content.era1.GrindingMachineBlockEntity;
import com.terrafabrica.content.era1.LatheBlockEntity;
import com.terrafabrica.content.era1.MillingMachineBlockEntity;
import com.terrafabrica.content.era1.PowerHammerBlockEntity;
import com.terrafabrica.content.era1.PrimitiveFurnaceBlockEntity;
import com.terrafabrica.content.era1.PuddlingFurnaceBlockEntity;
import com.terrafabrica.content.era1.PyriteRoasterBlockEntity;
import com.terrafabrica.content.era1.RollingMillBlockEntity;
import com.terrafabrica.content.era1.SeparatorBlockEntity;
import com.terrafabrica.content.era1.ShaftBlockEntity;
import com.terrafabrica.content.era1.SmallBlastFurnaceBlockEntity;
import com.terrafabrica.content.era1.SteamEngineBlockEntity;
import com.terrafabrica.content.era1.SteamEngineType;
import com.terrafabrica.content.era1.WaterWheelBlockEntity;
import com.terrafabrica.content.era1.WindmillBlockEntity;
import com.terrafabrica.content.era2.BatteryBlockEntity;
import com.terrafabrica.content.era2.BessemerConverterBlockEntity;
import com.terrafabrica.content.era2.BlastFurnaceBlockEntity;
import com.terrafabrica.content.era2.CcgtPlantBlockEntity;
import com.terrafabrica.content.era2.ChemicalReactorBlockEntity;
import com.terrafabrica.content.era2.CokeOvenBlockEntity;
import com.terrafabrica.content.era2.ElectricArcFurnaceBlockEntity;
import com.terrafabrica.content.era2.ElectricGeneratorBlockEntity;
import com.terrafabrica.content.era2.ElectricMotorBlockEntity;
import com.terrafabrica.content.era2.GasTurbineBlockEntity;
import com.terrafabrica.content.era2.HeavyRollingMillBlockEntity;
import com.terrafabrica.content.era2.OpenHearthFurnaceBlockEntity;
import com.terrafabrica.content.era2.PlcBlockEntity;
import com.terrafabrica.content.era2.RadioBlockEntity;
import com.terrafabrica.content.era2.RelayBlockEntity;
import com.terrafabrica.content.era2.SensorBlockEntity;
import com.terrafabrica.content.era2.SteamTurbineBlockEntity;
import com.terrafabrica.content.shared.power.SubstationBlockEntity;
import com.terrafabrica.content.era2.TelegraphBlockEntity;
import com.terrafabrica.content.era2.TelephoneBlockEntity;
import com.terrafabrica.content.era2.TransformerBlockEntity;
import com.terrafabrica.content.era2.TubeComputerBlockEntity;
import com.terrafabrica.content.era2.ValveBlockEntity;
import com.terrafabrica.content.era2.WeightDispenserBlockEntity;
import com.terrafabrica.content.era2.WireDrawingMachineBlockEntity;
import com.terrafabrica.content.shared.power.CableBlockEntity;
import com.terrafabrica.content.shared.power.CableTier;
import com.terrafabrica.content.shared.power.CircuitBreakerBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Реестр block entity. Конвенция: id BE = id блока (см. SimpleMachineBlock).
 */
public final class TFBlockEntities {

    private TFBlockEntities() {}

    // Эпоха I: обогащение, металлургия, приводы, передача момента
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrusherBlockEntity>> CRUSHER =
            register("crusher", (pos, state) -> new CrusherBlockEntity(TFBlockEntities.CRUSHER.get(), pos, state), TFBlocks.CRUSHER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SeparatorBlockEntity>> SEPARATOR =
            register("separator", (pos, state) -> new SeparatorBlockEntity(TFBlockEntities.SEPARATOR.get(), pos, state), TFBlocks.SEPARATOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PrimitiveFurnaceBlockEntity>> PRIMITIVE_FURNACE =
            register("primitive_furnace", (pos, state) -> new PrimitiveFurnaceBlockEntity(TFBlockEntities.PRIMITIVE_FURNACE.get(), pos, state), TFBlocks.PRIMITIVE_FURNACE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BloomeryBlockEntity>> BLOOMERY =
            register("bloomery", (pos, state) -> new BloomeryBlockEntity(TFBlockEntities.BLOOMERY.get(), pos, state), TFBlocks.BLOOMERY);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SmallBlastFurnaceBlockEntity>> SMALL_BLAST_FURNACE =
            register("small_blast_furnace", (pos, state) -> new SmallBlastFurnaceBlockEntity(TFBlockEntities.SMALL_BLAST_FURNACE.get(), pos, state), TFBlocks.SMALL_BLAST_FURNACE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PuddlingFurnaceBlockEntity>> PUDDLING_FURNACE =
            register("puddling_furnace", (pos, state) -> new PuddlingFurnaceBlockEntity(TFBlockEntities.PUDDLING_FURNACE.get(), pos, state), TFBlocks.PUDDLING_FURNACE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleFurnaceBlockEntity>> CRUCIBLE_FURNACE =
            register("crucible_furnace", (pos, state) -> new CrucibleFurnaceBlockEntity(TFBlockEntities.CRUCIBLE_FURNACE.get(), pos, state), TFBlocks.CRUCIBLE_FURNACE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterWheelBlockEntity>> WATER_WHEEL =
            register("water_wheel", (pos, state) -> new WaterWheelBlockEntity(TFBlockEntities.WATER_WHEEL.get(), pos, state), TFBlocks.WATER_WHEEL);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WindmillBlockEntity>> WINDMILL =
            register("windmill", (pos, state) -> new WindmillBlockEntity(TFBlockEntities.WINDMILL.get(), pos, state), TFBlocks.WINDMILL);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamEngineBlockEntity>> STEAM_ENGINE_NEWCOMEN =
            register("steam_engine_newcomen",
                    (pos, state) -> new SteamEngineBlockEntity(TFBlockEntities.STEAM_ENGINE_NEWCOMEN.get(), pos, state, SteamEngineType.NEWCOMEN),
                    TFBlocks.STEAM_ENGINE_NEWCOMEN);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamEngineBlockEntity>> STEAM_ENGINE_WATT =
            register("steam_engine_watt",
                    (pos, state) -> new SteamEngineBlockEntity(TFBlockEntities.STEAM_ENGINE_WATT.get(), pos, state, SteamEngineType.WATT),
                    TFBlocks.STEAM_ENGINE_WATT);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShaftBlockEntity>> SHAFT =
            register("shaft", (pos, state) -> new ShaftBlockEntity(TFBlockEntities.SHAFT.get(), pos, state), TFBlocks.SHAFT);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShaftBlockEntity>> GEAR =
            register("gear", (pos, state) -> new ShaftBlockEntity(TFBlockEntities.GEAR.get(), pos, state), TFBlocks.GEAR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShaftBlockEntity>> BELT =
            register("belt", (pos, state) -> new ShaftBlockEntity(TFBlockEntities.BELT.get(), pos, state), TFBlocks.BELT);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShaftBlockEntity>> PULLEY =
            register("pulley", (pos, state) -> new ShaftBlockEntity(TFBlockEntities.PULLEY.get(), pos, state), TFBlocks.PULLEY);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GearboxBlockEntity>> GEARBOX =
            register("gearbox", (pos, state) -> new GearboxBlockEntity(TFBlockEntities.GEARBOX.get(), pos, state), TFBlocks.GEARBOX);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LatheBlockEntity>> LATHE =
            register("lathe", (pos, state) -> new LatheBlockEntity(TFBlockEntities.LATHE.get(), pos, state), TFBlocks.LATHE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DrillPressBlockEntity>> DRILL_PRESS =
            register("drill_press", (pos, state) -> new DrillPressBlockEntity(TFBlockEntities.DRILL_PRESS.get(), pos, state), TFBlocks.DRILL_PRESS);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MillingMachineBlockEntity>> MILLING_MACHINE =
            register("milling_machine", (pos, state) -> new MillingMachineBlockEntity(TFBlockEntities.MILLING_MACHINE.get(), pos, state), TFBlocks.MILLING_MACHINE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrindingMachineBlockEntity>> GRINDING_MACHINE =
            register("grinding_machine", (pos, state) -> new GrindingMachineBlockEntity(TFBlockEntities.GRINDING_MACHINE.get(), pos, state), TFBlocks.GRINDING_MACHINE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RollingMillBlockEntity>> ROLLING_MILL =
            register("rolling_mill", (pos, state) -> new RollingMillBlockEntity(TFBlockEntities.ROLLING_MILL.get(), pos, state), TFBlocks.ROLLING_MILL);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PowerHammerBlockEntity>> POWER_HAMMER =
            register("power_hammer", (pos, state) -> new PowerHammerBlockEntity(TFBlockEntities.POWER_HAMMER.get(), pos, state), TFBlocks.POWER_HAMMER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PyriteRoasterBlockEntity>> PYRITE_ROASTER =
            register("pyrite_roaster", (pos, state) -> new PyriteRoasterBlockEntity(TFBlockEntities.PYRITE_ROASTER.get(), pos, state), TFBlocks.PYRITE_ROASTER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorBlockEntity>> LEAD_CHAMBER =
            register("lead_chamber", (pos, state) -> new ChemicalReactorBlockEntity(TFBlockEntities.LEAD_CHAMBER.get(), pos, state), TFBlocks.LEAD_CHAMBER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorBlockEntity>> ABSORBER =
            register("absorber", (pos, state) -> new ChemicalReactorBlockEntity(TFBlockEntities.ABSORBER.get(), pos, state), TFBlocks.ABSORBER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorBlockEntity>> SOLVAY_REACTOR =
            register("solvay_reactor", (pos, state) -> new ChemicalReactorBlockEntity(TFBlockEntities.SOLVAY_REACTOR.get(), pos, state), TFBlocks.SOLVAY_REACTOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CalcinationFurnaceBlockEntity>> CALCINATION_FURNACE =
            register("calcination_furnace", (pos, state) -> new CalcinationFurnaceBlockEntity(TFBlockEntities.CALCINATION_FURNACE.get(), pos, state), TFBlocks.CALCINATION_FURNACE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConveyorBlockEntity>> CONVEYOR =
            register("conveyor", (pos, state) -> new ConveyorBlockEntity(TFBlockEntities.CONVEYOR.get(), pos, state), TFBlocks.CONVEYOR);

    // Эпоха II — металлургия
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CokeOvenBlockEntity>> COKE_OVEN =
            register("coke_oven", (pos, state) -> new CokeOvenBlockEntity(TFBlockEntities.COKE_OVEN.get(), pos, state), TFBlocks.COKE_OVEN);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlastFurnaceBlockEntity>> BLAST_FURNACE =
            register("blast_furnace", (pos, state) -> new BlastFurnaceBlockEntity(TFBlockEntities.BLAST_FURNACE.get(), pos, state), TFBlocks.BLAST_FURNACE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BessemerConverterBlockEntity>> BESSEMER_CONVERTER =
            register("bessemer_converter", (pos, state) -> new BessemerConverterBlockEntity(TFBlockEntities.BESSEMER_CONVERTER.get(), pos, state), TFBlocks.BESSEMER_CONVERTER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OpenHearthFurnaceBlockEntity>> OPEN_HEARTH_FURNACE =
            register("open_hearth_furnace", (pos, state) -> new OpenHearthFurnaceBlockEntity(TFBlockEntities.OPEN_HEARTH_FURNACE.get(), pos, state), TFBlocks.OPEN_HEARTH_FURNACE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricArcFurnaceBlockEntity>> ELECTRIC_ARC_FURNACE =
            register("electric_arc_furnace", (pos, state) -> new ElectricArcFurnaceBlockEntity(TFBlockEntities.ELECTRIC_ARC_FURNACE.get(), pos, state), TFBlocks.ELECTRIC_ARC_FURNACE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeavyRollingMillBlockEntity>> HEAVY_ROLLING_MILL =
            register("heavy_rolling_mill", (pos, state) -> new HeavyRollingMillBlockEntity(TFBlockEntities.HEAVY_ROLLING_MILL.get(), pos, state), TFBlocks.HEAVY_ROLLING_MILL);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WireDrawingMachineBlockEntity>> WIRE_DRAWING_MACHINE =
            register("wire_drawing_machine", (pos, state) -> new WireDrawingMachineBlockEntity(TFBlockEntities.WIRE_DRAWING_MACHINE.get(), pos, state), TFBlocks.WIRE_DRAWING_MACHINE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WeightDispenserBlockEntity>> WEIGHT_DISPENSER =
            register("weight_dispenser", (pos, state) -> new WeightDispenserBlockEntity(TFBlockEntities.WEIGHT_DISPENSER.get(), pos, state), TFBlocks.WEIGHT_DISPENSER);

    // Эпоха II — энергетика
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamTurbineBlockEntity>> STEAM_TURBINE =
            register("steam_turbine", (pos, state) -> new SteamTurbineBlockEntity(TFBlockEntities.STEAM_TURBINE.get(), pos, state), TFBlocks.STEAM_TURBINE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GasTurbineBlockEntity>> GAS_TURBINE =
            register("gas_turbine", (pos, state) -> new GasTurbineBlockEntity(TFBlockEntities.GAS_TURBINE.get(), pos, state), TFBlocks.GAS_TURBINE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CcgtPlantBlockEntity>> CCGT_PLANT =
            register("ccgt_plant", (pos, state) -> new CcgtPlantBlockEntity(TFBlockEntities.CCGT_PLANT.get(), pos, state), TFBlocks.CCGT_PLANT);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricGeneratorBlockEntity>> ELECTRIC_GENERATOR =
            register("electric_generator", (pos, state) -> new ElectricGeneratorBlockEntity(TFBlockEntities.ELECTRIC_GENERATOR.get(), pos, state), TFBlocks.ELECTRIC_GENERATOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TransformerBlockEntity>> TRANSFORMER =
            register("transformer", (pos, state) -> new TransformerBlockEntity(TFBlockEntities.TRANSFORMER.get(), pos, state), TFBlocks.TRANSFORMER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricMotorBlockEntity>> ELECTRIC_MOTOR =
            register("electric_motor", (pos, state) -> new ElectricMotorBlockEntity(TFBlockEntities.ELECTRIC_MOTOR.get(), pos, state), TFBlocks.ELECTRIC_MOTOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatteryBlockEntity>> LEAD_ACID_BATTERY =
            register("lead_acid_battery", (pos, state) -> new BatteryBlockEntity(TFBlockEntities.LEAD_ACID_BATTERY.get(), pos, state), TFBlocks.LEAD_ACID_BATTERY);

    // Эпоха II — сети
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CableBlockEntity>> CABLE_COPPER =
            register("cable_copper", (pos, state) -> new CableBlockEntity(TFBlockEntities.CABLE_COPPER.get(), pos, state, CableTier.COPPER), TFBlocks.CABLE_COPPER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CableBlockEntity>> CABLE_ALUMINUM =
            register("cable_aluminum", (pos, state) -> new CableBlockEntity(TFBlockEntities.CABLE_ALUMINUM.get(), pos, state, CableTier.ALUMINUM), TFBlocks.CABLE_ALUMINUM);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CableBlockEntity>> CABLE_STEEL =
            register("cable_steel", (pos, state) -> new CableBlockEntity(TFBlockEntities.CABLE_STEEL.get(), pos, state, CableTier.STEEL), TFBlocks.CABLE_STEEL);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SubstationBlockEntity>> SUBSTATION =
            register("substation", (pos, state) -> new SubstationBlockEntity(TFBlockEntities.SUBSTATION.get(), pos, state), TFBlocks.SUBSTATION);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CircuitBreakerBlockEntity>> CIRCUIT_BREAKER =
            register("circuit_breaker", (pos, state) -> new CircuitBreakerBlockEntity(TFBlockEntities.CIRCUIT_BREAKER.get(), pos, state), TFBlocks.CIRCUIT_BREAKER);

    // Эпоха II — химия
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorBlockEntity>> AIR_SEPARATION_UNIT =
            register("air_separation_unit", (pos, state) -> new ChemicalReactorBlockEntity(TFBlockEntities.AIR_SEPARATION_UNIT.get(), pos, state), TFBlocks.AIR_SEPARATION_UNIT);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorBlockEntity>> WATER_ELECTROLYZER =
            register("water_electrolyzer", (pos, state) -> new ChemicalReactorBlockEntity(TFBlockEntities.WATER_ELECTROLYZER.get(), pos, state), TFBlocks.WATER_ELECTROLYZER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorBlockEntity>> AMMONIA_SYNTHESIZER =
            register("ammonia_synthesizer", (pos, state) -> new ChemicalReactorBlockEntity(TFBlockEntities.AMMONIA_SYNTHESIZER.get(), pos, state), TFBlocks.AMMONIA_SYNTHESIZER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorBlockEntity>> OSTWALD_REACTOR =
            register("ostwald_reactor", (pos, state) -> new ChemicalReactorBlockEntity(TFBlockEntities.OSTWALD_REACTOR.get(), pos, state), TFBlocks.OSTWALD_REACTOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorBlockEntity>> CONTACT_ACID_REACTOR =
            register("contact_acid_reactor", (pos, state) -> new ChemicalReactorBlockEntity(TFBlockEntities.CONTACT_ACID_REACTOR.get(), pos, state), TFBlocks.CONTACT_ACID_REACTOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorBlockEntity>> CHLORALKALI_CELL =
            register("chloralkali_cell", (pos, state) -> new ChemicalReactorBlockEntity(TFBlockEntities.CHLORALKALI_CELL.get(), pos, state), TFBlocks.CHLORALKALI_CELL);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorBlockEntity>> ALUMINUM_ELECTROLYZER =
            register("aluminum_electrolyzer", (pos, state) -> new ChemicalReactorBlockEntity(TFBlockEntities.ALUMINUM_ELECTROLYZER.get(), pos, state), TFBlocks.ALUMINUM_ELECTROLYZER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorBlockEntity>> FERTILIZER_MIXER =
            register("fertilizer_mixer", (pos, state) -> new ChemicalReactorBlockEntity(TFBlockEntities.FERTILIZER_MIXER.get(), pos, state), TFBlocks.FERTILIZER_MIXER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorBlockEntity>> OIL_DISTILLATION_COLUMN =
            register("oil_distillation_column", (pos, state) -> new ChemicalReactorBlockEntity(TFBlockEntities.OIL_DISTILLATION_COLUMN.get(), pos, state), TFBlocks.OIL_DISTILLATION_COLUMN);

    // Эпоха II — связь и автоматизация
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TelegraphBlockEntity>> TELEGRAPH =
            register("telegraph", (pos, state) -> new TelegraphBlockEntity(TFBlockEntities.TELEGRAPH.get(), pos, state), TFBlocks.TELEGRAPH);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TelephoneBlockEntity>> TELEPHONE =
            register("telephone", (pos, state) -> new TelephoneBlockEntity(TFBlockEntities.TELEPHONE.get(), pos, state), TFBlocks.TELEPHONE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RadioBlockEntity>> RADIO =
            register("radio", (pos, state) -> new RadioBlockEntity(TFBlockEntities.RADIO.get(), pos, state), TFBlocks.RADIO);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TubeComputerBlockEntity>> TUBE_COMPUTER =
            register("tube_computer", (pos, state) -> new TubeComputerBlockEntity(TFBlockEntities.TUBE_COMPUTER.get(), pos, state), TFBlocks.TUBE_COMPUTER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RelayBlockEntity>> RELAY =
            register("relay", (pos, state) -> new RelayBlockEntity(TFBlockEntities.RELAY.get(), pos, state), TFBlocks.RELAY);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlcBlockEntity>> PLC =
            register("plc", (pos, state) -> new PlcBlockEntity(TFBlockEntities.PLC.get(), pos, state), TFBlocks.PLC);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SensorBlockEntity>> SENSOR_TEMPERATURE =
            register("sensor_temperature", (pos, state) -> new SensorBlockEntity(TFBlockEntities.SENSOR_TEMPERATURE.get(), pos, state), TFBlocks.SENSOR_TEMPERATURE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SensorBlockEntity>> SENSOR_PRESSURE =
            register("sensor_pressure", (pos, state) -> new SensorBlockEntity(TFBlockEntities.SENSOR_PRESSURE.get(), pos, state), TFBlocks.SENSOR_PRESSURE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SensorBlockEntity>> SENSOR_LEVEL =
            register("sensor_level", (pos, state) -> new SensorBlockEntity(TFBlockEntities.SENSOR_LEVEL.get(), pos, state), TFBlocks.SENSOR_LEVEL);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SensorBlockEntity>> SENSOR_FLOW =
            register("sensor_flow", (pos, state) -> new SensorBlockEntity(TFBlockEntities.SENSOR_FLOW.get(), pos, state), TFBlocks.SENSOR_FLOW);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ValveBlockEntity>> VALVE =
            register("valve", (pos, state) -> new ValveBlockEntity(TFBlockEntities.VALVE.get(), pos, state), TFBlocks.VALVE);

    /**
     * Блоки передаются как {@code Supplier}, а не как готовые объекты: на этапе
     * статической инициализации реестр блоков ещё не связан, и ранний
     * {@code DeferredBlock#get()} падал бы с «Trying to access unbound value».
     * Разрешение происходит внутри supplier'а, когда типы BE регистрируются.
     */
    @SafeVarargs
    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
            String id, BlockEntityType.BlockEntitySupplier<T> factory, java.util.function.Supplier<? extends Block>... blocks) {
        return TerraFabrica.BLOCK_ENTITIES.register(id, () -> {
            Block[] resolved = new Block[blocks.length];
            for (int i = 0; i < blocks.length; i++) {
                resolved[i] = blocks[i].get();
            }
            return BlockEntityType.Builder.of(factory, resolved).build(null);
        });
    }

    /** Инициализация (регистрация выполняется в статических полях выше). */
    public static void init() {
    }
}
