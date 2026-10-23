package com.terrafabrica.core.util;

/**
 * Физические константы и единицы СИ, используемые модом.
 *
 * <p>Все внутренние вычисления — в СИ: джоуль (Дж), ватт (Вт), кельвин (K),
 * паскаль (Па), ньютон (Н), ньютон-метр (Н·м), вольт (В), ампер (А), килограмм (кг).
 *
 * <p>Исключение: внутриигровые передачи энергии происходят за 1 тик (1/20 с),
 * поэтому пределы передачи задаются в Дж/тик; перевод в ватты: {@code W = J_per_tick * 20}.
 */
public final class Units {

    private Units() {}

    // ------------------------------------------------------------------
    // Базовые соотношения
    // ------------------------------------------------------------------

    /** Тиков в игровой секунде. */
    public static final int TICKS_PER_SECOND = 20;

    /** Джоулей в одном киловатт-часе. */
    public static final long JOULES_PER_KWH = 3_600_000L;

    /** Джоулей в одном мегаджоуле. */
    public static final long JOULES_PER_MJ = 1_000_000L;

    /**
     * Совместимость с NeoForge {@code IEnergyStorage}: 1 FE = 2.5 Дж.
     * Направление: Дж → FE — {@code joulesToFe(x) = x / FE_TO_JOULES}.
     */
    public static final double FE_TO_JOULES = 2.5;

    /**
     * Дж → FE. Округление вниз: энергия не может «появиться» при конвертации,
     * а дробный остаток лучше потерять, чем создать из воздуха.
     */
    public static long joulesToFe(long joules) {
        return joules <= 0 ? 0 : (long) Math.floor(joules / FE_TO_JOULES);
    }

    /** FE → Дж. Тоже вниз — по той же причине; 1 FE = 2.5 Дж, 2 FE = 5 Дж без потерь. */
    public static long feToJoules(long fe) {
        return fe <= 0 ? 0 : (long) Math.floor(fe * FE_TO_JOULES);
    }

    // ------------------------------------------------------------------
    // Вращение
    // ------------------------------------------------------------------

    /** Мощность (Вт) вращающегося вала: P = (2π · RPM / 60) · Torque. */
    public static double rotationPower(double rpm, double torqueNewtonMeters) {
        return (2.0 * Math.PI * rpm / 60.0) * torqueNewtonMeters;
    }

    /** Крутящий момент (Н·м) при заданной мощности и оборотах. */
    public static double rotationTorque(double powerWatts, double rpm) {
        return rpm == 0 ? 0 : powerWatts * 60.0 / (2.0 * Math.PI * rpm);
    }

    /** Обороты при заданной мощности и моменте. */
    public static double rotationRpm(double powerWatts, double torqueNewtonMeters) {
        return torqueNewtonMeters == 0 ? 0 : powerWatts * 60.0 / (2.0 * Math.PI * torqueNewtonMeters);
    }

    /** RPM → радианы в секунду. */
    public static double rpmToRadS(double rpm) {
        return rpm * 2.0 * Math.PI / 60.0;
    }

    /** Радианы в секунду → RPM. */
    public static double radSToRpm(double radS) {
        return radS * 60.0 / (2.0 * Math.PI);
    }

    // ------------------------------------------------------------------
    // Температура
    // ------------------------------------------------------------------

    /** °C → K. */
    public static double celsiusToKelvin(double celsius) {
        return celsius + 273.15;
    }

    /** K → °C. */
    public static double kelvinToCelsius(double kelvin) {
        return kelvin - 273.15;
    }

    // ------------------------------------------------------------------
    // Разное
    // ------------------------------------------------------------------

    /** Перевод кг/кВт·ч в Дж угля на Вт·с (для расчёта расхода топлива двигателей). */
    public static double kgPerKwhToJoulesPerJoule(double kgPerKwh) {
        return kgPerKwh / JOULES_PER_KWH;
    }

    /**
     * Линейная интерполяция с ограничением.
     * Удобна для кривых «мощность от оборотов» и «КПД от температуры».
     */
    public static double clampLerp(double x, double xMin, double xMax, double yMin, double yMax) {
        if (x <= xMin) return yMin;
        if (x >= xMax) return yMax;
        double t = (x - xMin) / (xMax - xMin);
        return yMin + t * (yMax - yMin);
    }

    /** Ограничение значения в диапазоне [min, max]. */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Ограничение значения в диапазоне [min, max] (long). */
    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }
}
