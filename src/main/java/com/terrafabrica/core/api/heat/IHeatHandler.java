package com.terrafabrica.core.api.heat;

/**
 * Собственная capability тепла Terra Fabrica.
 *
 * <p>Теплосодержание измеряется в джоулях, температура — в кельвинах (K).
 * Связь: {@code Q = C · T}, где C — теплоёмкость (Дж/К). Теплоёмкость задаётся
 * машиной при создании хранилища.
 *
 * <p>Теплопередача между блоками и остывание/нагрев окружающей среды
 * вычисляются серверным тиком машин; здесь — только хранилище.
 */
public interface IHeatHandler {

    /** Текущая температура, K. */
    double getTemperature();

    /** Теплоёмкость, Дж/К. */
    double getHeatCapacity();

    /** Текущее теплосодержание, Дж (= C·T). */
    double getHeatEnergy();

    /**
     * Подводит тепло. Возвращает реально принятое количество, Дж.
     *
     * @param amount   количество теплоты, Дж
     * @param simulate если {@code true} — только проверка
     */
    double addHeat(double amount, boolean simulate);

    /**
     * Отводит тепло. Возвращает реально отведённое количество, Дж.
     *
     * @param amount   количество теплоты, Дж
     * @param simulate если {@code true} — только проверка
     */
    double removeHeat(double amount, boolean simulate);

    /** Максимальная рабочая температура, K (выше — аварийный режим). */
    double getMaxTemperature();

    /** Запас теплопроводности/интенсивности обмена, Вт/К (для обмена с соседями). */
    double getHeatTransferCoefficient();
}
