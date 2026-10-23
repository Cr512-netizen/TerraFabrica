package com.terrafabrica.content.shared.power;

/**
 * Потребитель электросети: указывает максимальное допустимое напряжение, В.
 * Потребитель подключается к кабелю, только если класс напряжения кабеля
 * не превышает {@link #maxVoltage()}.
 */
public interface IVoltageConsumer {

    /** Максимальное напряжение питания, В. */
    double maxVoltage();
}
