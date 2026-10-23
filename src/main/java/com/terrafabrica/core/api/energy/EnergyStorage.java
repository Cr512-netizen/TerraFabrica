package com.terrafabrica.core.api.energy;

import com.terrafabrica.core.util.Units;

/**
 * Простая реализация {@link IEnergyHandler}: буфер энергии в джоулях с
 * раздельными пределами приёма/отдачи (Дж/тик).
 *
 * <p>Потокобезопасна для независимых вызовов receive/extract (расчёты не
 * выполняются в параллельных потоках в Minecraft, но класс не имеет
 * изменяемого промежуточного состояния).
 */
public class EnergyStorage implements IEnergyHandler {

    private final long capacity;
    private long energy;
    private long maxInput;
    private long maxOutput;

    /**
     * @param capacity  ёмкость, Дж
     * @param maxInput  максимум приёма, Дж/тик
     * @param maxOutput максимум отдачи, Дж/тик
     */
    public EnergyStorage(long capacity, long maxInput, long maxOutput) {
        this.capacity = Math.max(0, capacity);
        this.maxInput = Math.max(0, maxInput);
        this.maxOutput = Math.max(0, maxOutput);
    }

    @Override
    public long getStoredEnergy() {
        return energy;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    /** Устанавливает запас (с ограничением по ёмкости). */
    public void setEnergy(long energy) {
        this.energy = Units.clamp(energy, 0, capacity);
    }

    @Override
    public long receiveEnergy(long amount, boolean simulate) {
        if (amount <= 0) return 0;
        long accepted = Math.min(Math.min(amount, maxInput), capacity - energy);
        if (!simulate) energy += accepted;
        return accepted;
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        if (amount <= 0) return 0;
        long extracted = Math.min(Math.min(amount, maxOutput), energy);
        if (!simulate) energy -= extracted;
        return extracted;
    }

    @Override
    public long getMaxInput() {
        return maxInput;
    }

    @Override
    public long getMaxOutput() {
        return maxOutput;
    }

    /** Меняет предел приёма (Дж/тик) — например, при апгрейде машины. */
    public void setMaxInput(long maxInput) {
        this.maxInput = Math.max(0, maxInput);
    }

    /** Меняет предел отдачи (Дж/тик). */
    public void setMaxOutput(long maxOutput) {
        this.maxOutput = Math.max(0, maxOutput);
    }

    /**
     * Мгновенная мощность отдачи в ваттах при текущем запасе
     * (min(maxOutput, energy) * 20).
     */
    public long getOutputPowerWatts() {
        return Math.min(maxOutput, energy) * Units.TICKS_PER_SECOND;
    }
}
