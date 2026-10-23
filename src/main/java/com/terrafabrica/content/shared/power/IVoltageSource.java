package com.terrafabrica.content.shared.power;

/**
 * Источник напряжения в электросети: задаёт номинальное напряжение, В.
 * Реализуется генератором (10 кВ), аккумулятором (12 В) и трансформатором
 * (вторичная сторона).
 */
public interface IVoltageSource {

    /** Номинальное напряжение источника, В. */
    double nominalVoltage();
}
