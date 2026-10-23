package com.terrafabrica.core.api.energy;

import com.terrafabrica.core.util.Units;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Мост между внутренним джоулевым API {@link IEnergyHandler} и NeoForge
 * {@link IEnergyStorage} (Forge Energy, FE).
 *
 * <p>Конвертация: <b>1 FE = 2.5 Дж</b>. При переводе округление всегда «в пользу
 * сохранения энергии»: FE → Дж округляется вверх при приёме и вниз при отдаче,
 * чтобы суммарная энергия не росла и не исчезала.
 *
 * <p>Использование: машина, получающая энергию извне, оборачивает свой
 * {@link IEnergyHandler} в этот адаптер и отдаёт его как {@code IEnergyStorage};
 * машина, отдающая энергию наружу, делает то же самое с противоположной стороны.
 */
public final class ForgeEnergyAdapter implements IEnergyStorage {

    private final IEnergyHandler handler;

    public ForgeEnergyAdapter(IEnergyHandler handler) {
        this.handler = handler;
    }

    public IEnergyHandler handler() {
        return handler;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        long requestedJoules = Units.feToJoules(maxReceive);
        long accepted = handler.receiveEnergy(requestedJoules, simulate);
        // Возвращаем целые FE: округляем вниз, чтобы не «продавать» неполученное.
        return (int) Math.min(Integer.MAX_VALUE, accepted / Units.FE_TO_JOULES);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        long requestedJoules = Units.feToJoules(maxExtract);
        long extracted = handler.extractEnergy(requestedJoules, simulate);
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(extracted / Units.FE_TO_JOULES));
    }

    @Override
    public int getEnergyStored() {
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(handler.getStoredEnergy() / Units.FE_TO_JOULES));
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(handler.getCapacity() / Units.FE_TO_JOULES));
    }

    @Override
    public boolean canExtract() {
        return handler.getMaxOutput() > 0 && handler.getStoredEnergy() > 0;
    }

    @Override
    public boolean canReceive() {
        return handler.getMaxInput() > 0 && handler.getStoredEnergy() < handler.getCapacity();
    }
}
