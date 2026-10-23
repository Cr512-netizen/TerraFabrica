package com.terrafabrica.content.shared.power;

/**
 * Классы кабелей электросети.
 *
 * <p>Потери зависят от тока и длины: на каждом блоке кабеля теряется
 * {@code I² · R} ватт. Передача на большие расстояния требует повышения
 * напряжения (трансформаторы): при той же мощности выше напряжение → меньше
 * ток → меньше потери.
 */
public enum CableTier {

    /** Медный кабель: до 1 кВ. */
    COPPER(1_000.0, 0.2, 100.0),
    /** Алюминиевый кабель: до 10 кВ. */
    ALUMINUM(10_000.0, 0.4, 200.0),
    /** Стальной кабель: до 110 кВ. */
    STEEL(110_000.0, 1.2, 50.0);

    /** Номинальное напряжение, В. */
    public final double voltage;
    /** Сопротивление одного блока кабеля, Ом. */
    public final double resistance;
    /** Максимальный ток, А. */
    public final double maxCurrent;

    CableTier(double voltage, double resistance, double maxCurrent) {
        this.voltage = voltage;
        this.resistance = resistance;
        this.maxCurrent = maxCurrent;
    }

    /** Предельная передаваемая мощность, Вт (U·I). */
    public double maxPower() {
        return voltage * maxCurrent;
    }

    /** Потери в ваттах при передаче {@code power} Вт через один блок. */
    public double lossFor(double powerWatts) {
        double current = powerWatts / voltage;
        return current * current * resistance;
    }
}
