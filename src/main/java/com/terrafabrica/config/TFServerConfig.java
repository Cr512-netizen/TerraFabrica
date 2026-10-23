package com.terrafabrica.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Серверная конфигурация Terra Fabrica (ModConfigSpec, тип SERVER).
 *
 * <p>Содержит балансные параметры машин и глобальные множители. Детальная
 * настройка генерации пород (размер жилы, количество на чанк, Y-диапазон)
 * выполняется через worldgen-данные датапака ({@code data/terrafabrica/worldgen/});
 * здесь — только глобальный множитель плотности.
 *
 * <p>Все величины в СИ: джоули, ватты, кельвины, секунды (тики переводятся
 * константой {@code TICKS_PER_SECOND}).
 */
public final class TFServerConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // ------------------------------------------------------------------
    // Общее
    // ------------------------------------------------------------------
    public static final ModConfigSpec.BooleanValue ENABLE_ERA2;
    public static final ModConfigSpec.BooleanValue BOILER_EXPLOSIONS_ENABLED;
    public static final ModConfigSpec.BooleanValue RADIATION_ENABLED;
    public static final ModConfigSpec.DoubleValue GLOBAL_VEIN_MULTIPLIER;

    // ------------------------------------------------------------------
    // Обогащение (дробилка/сепаратор)
    // ------------------------------------------------------------------
    public static final ModConfigSpec.LongValue CRUSHER_ENERGY_PER_CYCLE;   // Дж
    public static final ModConfigSpec.IntValue CRUSHER_CYCLE_TICKS;         // тик
    public static final ModConfigSpec.LongValue SEPARATOR_ENERGY_PER_CYCLE; // Дж
    public static final ModConfigSpec.IntValue SEPARATOR_CYCLE_TICKS;       // тик
    public static final ModConfigSpec.DoubleValue CONCENTRATE_RICH_BONUS;   // 0.25
    public static final ModConfigSpec.DoubleValue CONCENTRATE_POOR_LOSS;    // 0.25

    // ------------------------------------------------------------------
    // Металлургия
    // ------------------------------------------------------------------
    public static final ModConfigSpec.IntValue BLAST_FURNACE_PIG_IRON_TICKS; // 30 с
    public static final ModConfigSpec.DoubleValue PUDDLING_IRON_YIELD;        // 0.8

    // ------------------------------------------------------------------
    // Энергия Эпохи I
    // ------------------------------------------------------------------
    public static final ModConfigSpec.LongValue WATER_WHEEL_MAX_POWER;    // Вт
    public static final ModConfigSpec.DoubleValue WATER_WHEEL_EFFICIENCY; // 0.6
    public static final ModConfigSpec.LongValue WINDMILL_MAX_POWER;       // Вт
    public static final ModConfigSpec.DoubleValue WINDMILL_EFFICIENCY;    // 0.5

    public static final ModConfigSpec.LongValue NEWCOMEN_POWER_MIN;          // Вт
    public static final ModConfigSpec.LongValue NEWCOMEN_POWER_MAX;          // Вт
    public static final ModConfigSpec.DoubleValue NEWCOMEN_EFFICIENCY;       // 0.005–0.01
    public static final ModConfigSpec.DoubleValue NEWCOMEN_COAL_KG_PER_KWH;  // 25
    public static final ModConfigSpec.IntValue NEWCOMEN_STARTUP_TICKS;       // 20 мин

    public static final ModConfigSpec.LongValue WATT_POWER_MIN;              // Вт
    public static final ModConfigSpec.LongValue WATT_POWER_MAX;              // Вт
    public static final ModConfigSpec.DoubleValue WATT_EFFICIENCY;           // 0.05–0.08
    public static final ModConfigSpec.DoubleValue WATT_COAL_KG_PER_KWH_MIN;  // 6
    public static final ModConfigSpec.DoubleValue WATT_COAL_KG_PER_KWH_MAX;  // 10

    // ------------------------------------------------------------------
    // Исследования
    // ------------------------------------------------------------------
    public static final ModConfigSpec.DoubleValue RESEARCH_SPEED_MULTIPLIER;

    static {
        BUILDER.push("general");
        ENABLE_ERA2 = BUILDER.comment("Включить контент Эпохи II (рецепты и породы).")
                .define("enableEra2", true);
        BOILER_EXPLOSIONS_ENABLED = BUILDER.comment("Разрешить редкие аварии: взрыв котла при перегреве.")
                .define("boilerExplosionsEnabled", true);
        RADIATION_ENABLED = BUILDER.comment("Радиационный фон от урановых материалов.")
                .define("radiationEnabled", true);
        GLOBAL_VEIN_MULTIPLIER = BUILDER.comment("Глобальный множитель количества жил пород (1.0 = стандарт).")
                .defineInRange("globalVeinMultiplier", 1.0, 0.0, 8.0);
        BUILDER.pop();

        BUILDER.push("enrichment");
        CRUSHER_ENERGY_PER_CYCLE = BUILDER.comment("Энергия на цикл дробилки, Дж (50 кДж).")
                .defineInRange("crusherEnergyPerCycle", 50_000L, 1_000L, 100_000_000L);
        CRUSHER_CYCLE_TICKS = BUILDER.comment("Длительность цикла дробилки, тик (5 с = 100).")
                .defineInRange("crusherCycleTicks", 100, 10, 1200);
        SEPARATOR_ENERGY_PER_CYCLE = BUILDER.comment("Энергия на цикл сепаратора, Дж (50 кДж).")
                .defineInRange("separatorEnergyPerCycle", 50_000L, 1_000L, 100_000_000L);
        SEPARATOR_CYCLE_TICKS = BUILDER.comment("Длительность цикла сепаратора, тик (5 с = 100).")
                .defineInRange("separatorCycleTicks", 100, 10, 1200);
        CONCENTRATE_RICH_BONUS = BUILDER.comment("Шанс доп. концентрата для богатой породы (25%).")
                .defineInRange("concentrateRichBonus", 0.25, 0.0, 1.0);
        CONCENTRATE_POOR_LOSS = BUILDER.comment("Шанс потери концентрата для бедной породы (25%).")
                .defineInRange("concentratePoorLoss", 0.25, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("metallurgy");
        BLAST_FURNACE_PIG_IRON_TICKS = BUILDER.comment("Выплавка чугуна в малой домне: 30 с = 600 тик.")
                .defineInRange("blastFurnacePigIronTicks", 600, 100, 6000);
        PUDDLING_IRON_YIELD = BUILDER.comment("Выход ковкого железа в пудлинговой печи: 1 чугун → 0.8 железа.")
                .defineInRange("puddlingIronYield", 0.8, 0.1, 1.0);
        BUILDER.pop();

        BUILDER.push("energy.era1");
        WATER_WHEEL_MAX_POWER = BUILDER.comment("Максимальная мощность водяного колеса, Вт (30 кВт).")
                .defineInRange("waterWheelMaxPower", 30_000L, 1_000L, 1_000_000L);
        WATER_WHEEL_EFFICIENCY = BUILDER.comment("КПД водяного колеса (0.6).")
                .defineInRange("waterWheelEfficiency", 0.6, 0.05, 1.0);
        WINDMILL_MAX_POWER = BUILDER.comment("Максимальная мощность ветряка, Вт (20 кВт).")
                .defineInRange("windmillMaxPower", 20_000L, 1_000L, 1_000_000L);
        WINDMILL_EFFICIENCY = BUILDER.comment("КПД ветряка (0.5).")
                .defineInRange("windmillEfficiency", 0.5, 0.05, 1.0);

        NEWCOMEN_POWER_MIN = BUILDER.comment("Мин. мощность двигателя Ньюкомена, Вт.")
                .defineInRange("newcomenPowerMin", 5_000L, 100L, 100_000L);
        NEWCOMEN_POWER_MAX = BUILDER.comment("Макс. мощность двигателя Ньюкомена, Вт.")
                .defineInRange("newcomenPowerMax", 10_000L, 100L, 1_000_000L);
        NEWCOMEN_EFFICIENCY = BUILDER.comment("КПД двигателя Ньюкомена (0.5–1%).")
                .defineInRange("newcomenEfficiency", 0.0075, 0.001, 0.2);
        NEWCOMEN_COAL_KG_PER_KWH = BUILDER.comment("Расход угля двигателя Ньюкомена, кг/кВт·ч (25).")
                .defineInRange("newcomenCoalKgPerKwh", 25.0, 0.1, 200.0);
        NEWCOMEN_STARTUP_TICKS = BUILDER.comment("Время запуска двигателя Ньюкомена: 20 мин = 24000 тик.")
                .defineInRange("newcomenStartupTicks", 24_000, 100, 120_000);

        WATT_POWER_MIN = BUILDER.comment("Мин. мощность двигателя Уатта, Вт.")
                .defineInRange("wattPowerMin", 20_000L, 100L, 1_000_000L);
        WATT_POWER_MAX = BUILDER.comment("Макс. мощность двигателя Уатта, Вт.")
                .defineInRange("wattPowerMax", 100_000L, 100L, 10_000_000L);
        WATT_EFFICIENCY = BUILDER.comment("КПД двигателя Уатта (5–8%).")
                .defineInRange("wattEfficiency", 0.065, 0.01, 0.3);
        WATT_COAL_KG_PER_KWH_MIN = BUILDER.comment("Мин. расход угля двигателя Уатта, кг/кВт·ч.")
                .defineInRange("wattCoalKgPerKwhMin", 6.0, 0.1, 100.0);
        WATT_COAL_KG_PER_KWH_MAX = BUILDER.comment("Макс. расход угля двигателя Уатта, кг/кВт·ч.")
                .defineInRange("wattCoalKgPerKwhMax", 10.0, 0.1, 100.0);
        BUILDER.pop();

        BUILDER.push("research");
        RESEARCH_SPEED_MULTIPLIER = BUILDER.comment("Глобальный множитель скорости исследований.")
                .defineInRange("researchSpeedMultiplier", 1.0, 0.1, 10.0);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private TFServerConfig() {}
}
