package com.terrafabrica.core.api.heat;

import com.terrafabrica.core.util.Units;

/**
 * Реализация {@link IHeatHandler}: теплосодержание Q = C·T, пределы температуры.
 *
 * <p>Охлаждение до температуры среды (≈293.15 K) выполняется владельцем хранилища
 * в его тике с использованием {@link #getHeatTransferCoefficient()} (Вт/К):
 * {@code Q_lost = h · (T - T_ambient) · dt}.
 */
public class HeatStorage implements IHeatHandler {

    /** Температура окружающей среды (20 °C). */
    public static final double AMBIENT_TEMPERATURE_K = 293.15;

    private final double heatCapacity;       // Дж/К
    private final double maxTemperature;     // K
    private final double transferCoefficient; // Вт/К
    private double temperature;              // K

    /**
     * @param heatCapacity        теплоёмкость, Дж/К
     * @param maxTemperature      предельная температура, K
     * @param transferCoefficient коэффициент теплообмена с окружением, Вт/К
     */
    public HeatStorage(double heatCapacity, double maxTemperature, double transferCoefficient) {
        this.heatCapacity = Math.max(1e-6, heatCapacity);
        this.maxTemperature = maxTemperature;
        this.transferCoefficient = Math.max(0, transferCoefficient);
        this.temperature = AMBIENT_TEMPERATURE_K;
    }

    @Override
    public double getTemperature() {
        return temperature;
    }

    @Override
    public double getHeatCapacity() {
        return heatCapacity;
    }

    @Override
    public double getHeatEnergy() {
        return temperature * heatCapacity;
    }

    @Override
    public double addHeat(double amount, boolean simulate) {
        if (amount <= 0) return 0;
        double maxQ = maxTemperature * heatCapacity;
        double accepted = Math.min(amount, maxQ - getHeatEnergy());
        if (!simulate) temperature += accepted / heatCapacity;
        return accepted;
    }

    @Override
    public double removeHeat(double amount, boolean simulate) {
        if (amount <= 0) return 0;
        double minQ = AMBIENT_TEMPERATURE_K * heatCapacity;
        double removed = Math.min(amount, getHeatEnergy() - minQ);
        if (!simulate) temperature -= removed / heatCapacity;
        return removed;
    }

    @Override
    public double getMaxTemperature() {
        return maxTemperature;
    }

    @Override
    public double getHeatTransferCoefficient() {
        return transferCoefficient;
    }

    /**
     * Пассивное остывание к температуре среды за один тик (dt = 1/20 с).
     * Возвращает отведённое тепло, Дж.
     */
    public double coolToAmbient(boolean simulate) {
        double dt = 1.0 / Units.TICKS_PER_SECOND;
        double lost = transferCoefficient * (temperature - AMBIENT_TEMPERATURE_K) * dt;
        return removeHeat(lost, simulate);
    }

    /** Устанавливает температуру напрямую (с ограничением). */
    public void setTemperature(double kelvin) {
        this.temperature = Units.clamp(kelvin, AMBIENT_TEMPERATURE_K, maxTemperature);
    }
}
