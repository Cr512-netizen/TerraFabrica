package com.terrafabrica.core.api.energy;

/**
 * Кастомный API энергии Terra Fabrica.
 *
 * <p>Внутренняя единица энергии — <b>джоуль (Дж)</b>. Пределы приёма/отдачи
 * (getMaxInput/getMaxOutput) задаются в <b>Дж за тик</b>; эквивалентная мощность
 * в ваттах вычисляется как {@code value * 20}, поскольку 1 тик = 1/20 с.
 *
 * <p>Для совместимости с NeoForge {@code IEnergyStorage} используется мост
 * {@link ForgeEnergyAdapter}: 1 FE = 2.5 Дж.
 *
 * <p>Возможность подключения к конкретной стороне блока определяется контекстом
 * block-capability ({@code BlockCapability<IEnergyHandler, Direction>}), а не
 * методами этого интерфейса.
 */
public interface IEnergyHandler {

    /** Текущий запас энергии, Дж. */
    long getStoredEnergy();

    /** Максимальный запас энергии, Дж. */
    long getCapacity();

    /**
     * Принимает энергию. Возвращает реально принятое количество, Дж.
     *
     * @param amount   запрошенное количество, Дж (не может быть отрицательным)
     * @param simulate если {@code true} — только проверка, состояние не меняется
     */
    long receiveEnergy(long amount, boolean simulate);

    /**
     * Отдаёт энергию. Возвращает реально отданное количество, Дж.
     *
     * @param amount   запрошенное количество, Дж
     * @param simulate если {@code true} — только проверка, состояние не меняется
     */
    long extractEnergy(long amount, boolean simulate);

    /** Максимальная скорость приёма энергии, Дж/тик. */
    long getMaxInput();

    /** Максимальная скорость отдачи энергии, Дж/тик. */
    long getMaxOutput();
}
