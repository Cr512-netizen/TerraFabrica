package com.terrafabrica.content;

import com.terrafabrica.TerraFabrica;
import com.terrafabrica.content.era1.PorodeBlock;
import com.terrafabrica.content.era1.PorodeRichness;
import com.terrafabrica.content.shared.machine.CasingBlock;
import com.terrafabrica.content.shared.machine.SimpleMachineBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Реестр блоков Terra Fabrica.
 *
 * <p>Породы (минерал × богатство × основа): {@code <mineral>_porode_<level>} и
 * {@code deepslate_<mineral>_porode_<level>}. Машины — одноблочные
 * {@link SimpleMachineBlock} (осознанное решение для станков — см.
 * docs/DESIGN_DECISIONS.md) либо контроллеры мультиблоков из обшивок
 * ({@code *_casing}) с проверкой структуры в BE.
 */
public final class TFBlocks {

    private TFBlocks() {}

    // ------------------------------------------------------------------
    // Породы
    // ------------------------------------------------------------------

    /** Минералы Эпохи I (id, они же — основа названий предметов). */
    public static final List<String> ERA1_MINERALS = List.of(
            "copper",       // халькопирит
            "tin",          // касситерит
            "hematite",     // гематит (железная богатая)
            "magnetite",    // магнетит (железная бедная)
            "galena",       // свинцовая
            "pyrite",       // пиритовая
            "limestone",    // известняковая
            "claystone",    // глинистая
            "coal",         // угольная
            "sylvite");     // сильвиновая

    /** Минералы Эпохи II. */
    public static final List<String> ERA2_MINERALS = List.of(
            "bauxite", "chromite", "pentlandite", "sphalerite", "molybdenite",
            "wolframite", "uraninite", "pyrolusite", "fluorite", "barite",
            "apatite", "gypsum", "magnesite", "sulfur", "graphite",
            "halite", "carnallite");

    /** Минералы Эпохи III. */
    public static final List<String> ERA3_MINERALS = List.of(
            "silica", "zircon", "lithium", "gold", "silicon");

    /** Все блоки пород: id → блок. */
    public static final Map<String, DeferredHolder<Block, PorodeBlock>> PORODES = new LinkedHashMap<>();

    static {
        List<String> all = new ArrayList<>(ERA1_MINERALS);
        all.addAll(ERA2_MINERALS);
        all.addAll(ERA3_MINERALS);
        for (String mineral : all) {
            for (PorodeRichness richness : PorodeRichness.values()) {
                for (boolean deepslate : new boolean[]{false, true}) {
                    // Блок создаётся лениво: конструктор Block трогает реестр блоков,
                    // а он к моменту инициализации мода уже заморожен. Id считается
                    // статически, поэтому объект нужен только внутри supplier'а.
                    String id = PorodeBlock.idFor(mineral, richness, deepslate);
                    PORODES.put(id, TerraFabrica.BLOCKS.register(id, () -> new PorodeBlock(mineral, richness, deepslate)));
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Обшивки мультиблоков
    // ------------------------------------------------------------------

    public static final DeferredBlock<Block> IRON_CASING =
            registerCasing("iron_casing", SoundType.METAL, 4.0F, 6.0F);
    public static final DeferredBlock<Block> STEAM_CASING =
            registerCasing("steam_casing", SoundType.METAL, 5.0F, 8.0F);
    public static final DeferredBlock<Block> REFRACTORY_BRICK =
            registerCasing("refractory_brick", SoundType.STONE, 6.0F, 10.0F);
    public static final DeferredBlock<Block> REACTOR_CASING =
            registerCasing("reactor_casing", SoundType.METAL, 5.0F, 8.0F);
    public static final DeferredBlock<Block> LEAD_CASING =
            registerCasing("lead_casing", SoundType.METAL, 4.0F, 6.0F);

    /** Специальные стёкла (тигельная печь). */
    public static final DeferredBlock<Block> HEAT_RESISTANT_GLASS =
            TerraFabrica.BLOCKS.register("heat_resistant_glass", () -> new net.minecraft.world.level.block.TransparentBlock(
                    BlockBehaviour.Properties.of().sound(SoundType.GLASS).strength(1.5F).noOcclusion()));
    public static final DeferredBlock<Block> OPTICAL_GLASS =
            TerraFabrica.BLOCKS.register("optical_glass", () -> new net.minecraft.world.level.block.TransparentBlock(
                    BlockBehaviour.Properties.of().sound(SoundType.GLASS).strength(1.5F).noOcclusion()));

    private static DeferredBlock<Block> registerCasing(String id, SoundType sound, float hardness, float resistance) {
        return TerraFabrica.BLOCKS.register(id, () -> new CasingBlock(sound, hardness, resistance));
    }

    // ------------------------------------------------------------------
    // Машины Эпохи I
    // ------------------------------------------------------------------

    public static final DeferredBlock<Block> CRUSHER = machine("crusher");
    public static final DeferredBlock<Block> SEPARATOR = machine("separator");

    public static final DeferredBlock<Block> PRIMITIVE_FURNACE = machine("primitive_furnace");
    public static final DeferredBlock<Block> BLOOMERY = machine("bloomery");
    public static final DeferredBlock<Block> SMALL_BLAST_FURNACE = machine("small_blast_furnace");
    public static final DeferredBlock<Block> PUDDLING_FURNACE = machine("puddling_furnace");
    public static final DeferredBlock<Block> CRUCIBLE_FURNACE = machine("crucible_furnace");

    public static final DeferredBlock<Block> WATER_WHEEL = machine("water_wheel");
    public static final DeferredBlock<Block> WINDMILL = machine("windmill");
    public static final DeferredBlock<Block> STEAM_ENGINE_NEWCOMEN = machine("steam_engine_newcomen");
    public static final DeferredBlock<Block> STEAM_ENGINE_WATT = machine("steam_engine_watt");

    public static final DeferredBlock<Block> SHAFT = machine("shaft");
    public static final DeferredBlock<Block> GEAR = machine("gear");
    public static final DeferredBlock<Block> BELT = machine("belt");
    public static final DeferredBlock<Block> PULLEY = machine("pulley");
    public static final DeferredBlock<Block> GEARBOX = machine("gearbox");

    public static final DeferredBlock<Block> LATHE = machine("lathe");
    public static final DeferredBlock<Block> DRILL_PRESS = machine("drill_press");
    public static final DeferredBlock<Block> MILLING_MACHINE = machine("milling_machine");
    public static final DeferredBlock<Block> GRINDING_MACHINE = machine("grinding_machine");
    public static final DeferredBlock<Block> ROLLING_MILL = machine("rolling_mill");
    public static final DeferredBlock<Block> POWER_HAMMER = machine("power_hammer");

    public static final DeferredBlock<Block> PYRITE_ROASTER = machine("pyrite_roaster");
    public static final DeferredBlock<Block> LEAD_CHAMBER = machine("lead_chamber");
    public static final DeferredBlock<Block> ABSORBER = machine("absorber");
    public static final DeferredBlock<Block> SOLVAY_REACTOR = machine("solvay_reactor");
    public static final DeferredBlock<Block> CALCINATION_FURNACE = machine("calcination_furnace");

    /**
     * Рельс: настоящий {@link RailBlock}, а не декоративный блок машины.
     *
     * <p>Без этого локомотивы и вагонетки не могли встать на собственные рельсы
     * мода: {@code RailVehicleItem} требует {@code BlockTags.RAILS}, а
     * {@code AbstractMinecart} проверяет именно {@link RailBlock}. Блок также
     * добавлен в тег {@code minecraft:rails} (data/minecraft/tags/block/rails.json).
     */
    public static final DeferredBlock<Block> RAIL = TerraFabrica.BLOCKS.register("rail",
            () -> new RailBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .strength(0.7F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> SLEEPER = machine("sleeper");
    public static final DeferredBlock<Block> CONVEYOR = machine("conveyor");

    // ------------------------------------------------------------------
    // Машины Эпохи II — металлургия
    // ------------------------------------------------------------------

    public static final DeferredBlock<Block> COKE_OVEN = machine("coke_oven");
    public static final DeferredBlock<Block> BLAST_FURNACE = machine("blast_furnace");
    public static final DeferredBlock<Block> BESSEMER_CONVERTER = machine("bessemer_converter");
    public static final DeferredBlock<Block> OPEN_HEARTH_FURNACE = machine("open_hearth_furnace");
    public static final DeferredBlock<Block> ELECTRIC_ARC_FURNACE = machine("electric_arc_furnace");
    public static final DeferredBlock<Block> HEAVY_ROLLING_MILL = machine("heavy_rolling_mill");
    public static final DeferredBlock<Block> WIRE_DRAWING_MACHINE = machine("wire_drawing_machine");
    public static final DeferredBlock<Block> WEIGHT_DISPENSER = machine("weight_dispenser");

    // ------------------------------------------------------------------
    // Машины Эпохи II — энергетика
    // ------------------------------------------------------------------

    public static final DeferredBlock<Block> STEAM_TURBINE = machine("steam_turbine");
    public static final DeferredBlock<Block> GAS_TURBINE = machine("gas_turbine");
    public static final DeferredBlock<Block> CCGT_PLANT = machine("ccgt_plant");
    public static final DeferredBlock<Block> ELECTRIC_GENERATOR = machine("electric_generator");
    public static final DeferredBlock<Block> TRANSFORMER = machine("transformer");
    public static final DeferredBlock<Block> ELECTRIC_MOTOR = machine("electric_motor");
    public static final DeferredBlock<Block> LEAD_ACID_BATTERY = machine("lead_acid_battery");

    // ------------------------------------------------------------------
    // Машины Эпохи II — электросети
    // ------------------------------------------------------------------

    public static final DeferredBlock<Block> CABLE_COPPER = machine("cable_copper");
    public static final DeferredBlock<Block> CABLE_ALUMINUM = machine("cable_aluminum");
    public static final DeferredBlock<Block> CABLE_STEEL = machine("cable_steel");
    public static final DeferredBlock<Block> POWER_POLE_WOOD = machine("power_pole_wood");
    public static final DeferredBlock<Block> POWER_POLE_CONCRETE = machine("power_pole_concrete");
    public static final DeferredBlock<Block> POWER_POLE_STEEL = machine("power_pole_steel");
    public static final DeferredBlock<Block> INSULATOR_PORCELAIN = machine("insulator_porcelain");
    public static final DeferredBlock<Block> INSULATOR_GLASS = machine("insulator_glass");
    public static final DeferredBlock<Block> SUBSTATION = machine("substation");
    public static final DeferredBlock<Block> CIRCUIT_BREAKER = machine("circuit_breaker");

    // ------------------------------------------------------------------
    // Машины Эпохи II — химия
    // ------------------------------------------------------------------

    public static final DeferredBlock<Block> AIR_SEPARATION_UNIT = machine("air_separation_unit");
    public static final DeferredBlock<Block> WATER_ELECTROLYZER = machine("water_electrolyzer");
    public static final DeferredBlock<Block> AMMONIA_SYNTHESIZER = machine("ammonia_synthesizer");
    public static final DeferredBlock<Block> OSTWALD_REACTOR = machine("ostwald_reactor");
    public static final DeferredBlock<Block> CONTACT_ACID_REACTOR = machine("contact_acid_reactor");
    public static final DeferredBlock<Block> CHLORALKALI_CELL = machine("chloralkali_cell");
    public static final DeferredBlock<Block> ALUMINUM_ELECTROLYZER = machine("aluminum_electrolyzer");
    public static final DeferredBlock<Block> FERTILIZER_MIXER = machine("fertilizer_mixer");
    public static final DeferredBlock<Block> OIL_DISTILLATION_COLUMN = machine("oil_distillation_column");

    // ------------------------------------------------------------------
    // Машины Эпохи II — связь, электроника, автоматизация
    // ------------------------------------------------------------------

    public static final DeferredBlock<Block> TELEGRAPH = machine("telegraph");
    public static final DeferredBlock<Block> TELEPHONE = machine("telephone");
    public static final DeferredBlock<Block> RADIO = machine("radio");
    public static final DeferredBlock<Block> TUBE_COMPUTER = machine("tube_computer");
    public static final DeferredBlock<Block> RELAY = machine("relay");
    public static final DeferredBlock<Block> PLC = machine("plc");
    public static final DeferredBlock<Block> SENSOR_TEMPERATURE = machine("sensor_temperature");
    public static final DeferredBlock<Block> SENSOR_PRESSURE = machine("sensor_pressure");
    public static final DeferredBlock<Block> SENSOR_LEVEL = machine("sensor_level");
    public static final DeferredBlock<Block> SENSOR_FLOW = machine("sensor_flow");
    public static final DeferredBlock<Block> VALVE = machine("valve");

    // ------------------------------------------------------------------
    // Машины Эпохи III — электроника и ядерная
    // ------------------------------------------------------------------

    public static final DeferredBlock<Block> SEMICONDUCTOR_FURNACE = machine("semiconductor_furnace");
    public static final DeferredBlock<Block> TRANSISTOR_ASSEMBLER = machine("transistor_assembler");
    public static final DeferredBlock<Block> MICROCHIP_FAB = machine("microchip_fab");
    public static final DeferredBlock<Block> CARBON_FIBER_PLANT = machine("carbon_fiber_plant");
    public static final DeferredBlock<Block> REACTOR_CORE = machine("reactor_core");
    public static final DeferredBlock<Block> ENRICHMENT_PLANT = machine("enrichment_plant");
    public static final DeferredBlock<Block> SATELLITE_LAUNCHPAD = machine("satellite_launchpad");
    public static final DeferredBlock<Block> FUSION_REACTOR = machine("fusion_reactor");
    public static final DeferredBlock<Block> EV_CHARGER = machine("ev_charger");
    public static final DeferredBlock<Block> TELESCOPE = machine("telescope");

    /** Регистрация одноблочной машины (BE регистрируется в TFBlockEntities с тем же id). */
    private static DeferredBlock<Block> machine(String id) {
        return TerraFabrica.BLOCKS.register(id, () -> SimpleMachineBlock.machine(id));
    }

    /** Инициализация (вызывается из TerraFabrica). */
    public static void init() {
        // Регистрация выполняется в static-блоках; метод — точка вызова.
    }
}
